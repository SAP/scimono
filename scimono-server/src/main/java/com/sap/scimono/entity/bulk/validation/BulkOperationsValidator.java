
package com.sap.scimono.entity.bulk.validation;

import static com.sap.scimono.entity.bulk.RequestMethod.DELETE;
import static com.sap.scimono.entity.bulk.RequestMethod.PATCH;
import static com.sap.scimono.entity.bulk.RequestMethod.POST;
import static com.sap.scimono.entity.bulk.RequestMethod.PUT;
import static com.sap.scimono.exception.SCIMException.Type.TOO_MANY;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.sap.scimono.api.API;
import com.sap.scimono.callback.config.SCIMConfigurationCallback;
import com.sap.scimono.entity.ErrorResponse;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.bulk.BulkBody;
import com.sap.scimono.entity.bulk.RequestMethod;
import com.sap.scimono.entity.bulk.RequestOperation;
import com.sap.scimono.entity.bulk.RequestOperation.Builder;
import com.sap.scimono.entity.bulk.ResponseOperation;
import com.sap.scimono.entity.config.BulkSetting;
import com.sap.scimono.exception.InternalScimonoException;
import com.sap.scimono.exception.SCIMException;
import com.sap.scimono.helper.ResourceLocationService;

public class BulkOperationsValidator {

  public static List<RequestOperation> getValidBulkOperations(BulkBody<RequestOperation> bulkRequest,
      SCIMConfigurationCallback scimConfigurationCallback) {
    List<RequestOperation> requestOperations = bulkRequest.getOperations();
    List<RequestOperation> result = requestOperations.stream().map(operation -> {
      Builder operationBuilder = operation.builder();
      RequestMethod method = operation.getMethod();
      if (method == null) {
        operationBuilder.setData(
            buildValidationErrorResponse(operation.getBulkId(), "Invalid method name!, Valid methods: " + Arrays.toString(RequestMethod.values())));
      }

      String bulkId = operation.getBulkId();
      if (POST == method && bulkId == null) {
        operationBuilder.setData(buildValidationErrorResponse(operation.getBulkId(), "bulkId is required for method: " + POST));
      }

      String resourceEndpoint = null;
      String msg = String.format("Invalid path endpoint for operation with bulkId: %s. Path should start with either %s or %s endpoint.",
          operation.getBulkId(), API.USERS, API.GROUPS);
      try {
        resourceEndpoint = RequestOperation.extractRootFromPath(operation.getPath());
      } catch (InternalScimonoException e) {
        operationBuilder.setData(buildValidationErrorResponse(operation.getBulkId(), msg));
      }

      if (!API.USERS.equalsIgnoreCase(resourceEndpoint) && !API.GROUPS.equalsIgnoreCase(resourceEndpoint)) {
        operationBuilder.setData(buildValidationErrorResponse(operation.getBulkId(), msg));
      }

      if ((PUT == method || PATCH == method) && !operation.getResourceId().isPresent()) {
        operationBuilder.setData(buildValidationErrorResponse(operation.getBulkId(), "Path should point to resource id for PUT and POST methods"));
      }

      if (method != DELETE && !operation.isDataAvailable()) {
        operationBuilder.setData(buildValidationErrorResponse(operation.getBulkId(), "The attribute data is required for POST, PUT or PATCH!"));
      }
      return operationBuilder.build();
    }).collect(Collectors.toList());

    validateByBulkSettings(bulkRequest, scimConfigurationCallback);

    return result;
  }

  public static BulkBody<ResponseOperation> getValidResponseData(BulkBody<RequestOperation> bulkRequest, BulkBody<ResponseOperation> bulkResponse,
      ResourceLocationService usersLocationService, ResourceLocationService groupsLocationService) {
    Map<String, RequestOperation> requestOperations = bulkRequest.getOperations().stream()
        .collect(Collectors.toMap(RequestOperation::getBulkId, Function.identity()));

    List<ResponseOperation> responseOperations = bulkResponse.getOperations().stream().map(respOperation -> {
      ResponseOperation.Builder builder = respOperation.builder();
      RequestOperation reqOperation = requestOperations.get(respOperation.getBulkId());

      builder.withLocation(getValidResponseLocation(reqOperation, respOperation, usersLocationService, groupsLocationService));
      return builder.build();
    }).collect(Collectors.toList());

    return BulkBody.forResponse(responseOperations);
  }

  private static String getValidResponseLocation(RequestOperation reqOperation, ResponseOperation respOperation,
      ResourceLocationService usersLocationService, ResourceLocationService groupsLocationService) {
    if (reqOperation.getMethod() == RequestMethod.POST && !respOperation.isSuccessful()) {
      return null;
    }

    String location = respOperation.getLocation();
    String resourceType = respOperation.getResourceType();

    if (location == null && User.RESOURCE_TYPE_USER.equalsIgnoreCase(resourceType)) {
      location = usersLocationService.getLocation(respOperation.getResourceId()).toString();
    }

    if (location == null && Group.RESOURCE_TYPE_GROUP.equalsIgnoreCase(resourceType)) {
      location = groupsLocationService.getLocation(respOperation.getResourceId()).toString();
    }

    return location;
  }

  private static void validateByBulkSettings(BulkBody<RequestOperation> bulkRequest, SCIMConfigurationCallback scimConfigurationCallback) {
    BulkSetting bulkSetting = scimConfigurationCallback.getBulkSetting();
    if (bulkSetting == null || !bulkSetting.isSupported()) {
      String msg = "Service provider does not support bulk operations. Please check the bulk settings.";
      throw new WebApplicationException(msg, Response.Status.NOT_IMPLEMENTED);
    }

    if (bulkRequest.getOperations().size() > bulkSetting.getMaxOperations()) {
      String msg = "Bulk operations count exceeded the maximum value supported.";
      throw new SCIMException(TOO_MANY, msg, Response.Status.REQUEST_ENTITY_TOO_LARGE);
    }
  }

  private static ErrorResponse buildValidationErrorResponse(String bulkId, String errMsg) {
    return new ErrorResponse(Response.Status.BAD_REQUEST.getStatusCode(), SCIMException.Type.INVALID_VALUE.toJson(),
        buildErrorMessage(bulkId, errMsg));
  }

  private static String buildErrorMessage(String bulkId, String errMsg) {
    String msgPattern = "Invalid operation with bulkId: " + bulkId + ". Reason: %s";
    UnaryOperator<String> errorMsgBuilder = detail -> String.format(msgPattern, detail);
    return errorMsgBuilder.apply(errMsg);
  }
}
