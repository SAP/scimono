
package com.sap.scimono.entity.bulk.validation;

import static com.sap.scimono.entity.bulk.RequestMethod.DELETE;
import static com.sap.scimono.entity.bulk.RequestMethod.PATCH;
import static com.sap.scimono.entity.bulk.RequestMethod.POST;
import static com.sap.scimono.entity.bulk.RequestMethod.PUT;
import static com.sap.scimono.exception.SCIMException.Type.TOO_MANY;

import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import com.sap.scimono.api.API;
import com.sap.scimono.callback.config.SCIMConfigurationCallback;
import com.sap.scimono.entity.ErrorResponse;
import com.sap.scimono.entity.bulk.BulkBody;
import com.sap.scimono.entity.bulk.RequestMethod;
import com.sap.scimono.entity.bulk.RequestOperation;
import com.sap.scimono.entity.bulk.RequestOperation.Builder;
import com.sap.scimono.entity.config.BulkSetting;
import com.sap.scimono.exception.InternalScimonoException;
import com.sap.scimono.exception.SCIMException;

public class BulkOperationsValidator {
  
  private static final String INVALID_PATH_ENDPOINT_ERROR_PLACEHOLDER = "Invalid path endpoint for operation with bulkId: %s. Path should start with either %s or %s endpoint.";
  
  private final SCIMConfigurationCallback scimConfigurationCallback;
  
  public BulkOperationsValidator(SCIMConfigurationCallback scimConfigurationCallback) {
    this.scimConfigurationCallback = scimConfigurationCallback;
  }

  public List<RequestOperation> getValidBulkOperations(BulkBody<RequestOperation> bulkRequest) {
    validateByBulkSettings(bulkRequest, scimConfigurationCallback);
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
      try {
        resourceEndpoint = RequestOperation.extractRootFromPath(operation.getPath());
      } catch (InternalScimonoException e) {
        operationBuilder.setData(buildValidationErrorResponse(operation.getBulkId(),
            String.format(INVALID_PATH_ENDPOINT_ERROR_PLACEHOLDER, operation.getBulkId(), API.USERS, API.GROUPS)));
      }

      if (!API.USERS.equalsIgnoreCase(resourceEndpoint) && !API.GROUPS.equalsIgnoreCase(resourceEndpoint)) {
        operationBuilder.setData(buildValidationErrorResponse(operation.getBulkId(),
            String.format(INVALID_PATH_ENDPOINT_ERROR_PLACEHOLDER, operation.getBulkId(), API.USERS, API.GROUPS)));
      }

      if ((PUT == method || PATCH == method) && !operation.getResourceId().isPresent()) {
        operationBuilder.setData(buildValidationErrorResponse(operation.getBulkId(), "Path should point to resource id for PUT and POST methods"));
      }

      if (method != DELETE && !operation.isDataAvailable()) {
        operationBuilder.setData(buildValidationErrorResponse(operation.getBulkId(), "The attribute data is required for POST, PUT or PATCH!"));
      }
      return operationBuilder.build();
    }).collect(Collectors.toList());

    return result;
  }

  private void validateByBulkSettings(BulkBody<RequestOperation> bulkRequest, SCIMConfigurationCallback scimConfigurationCallback) {
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

  private ErrorResponse buildValidationErrorResponse(String bulkId, String errMsg) {
    return new ErrorResponse(Response.Status.BAD_REQUEST.getStatusCode(), SCIMException.Type.INVALID_VALUE.toJson(),
        buildErrorMessage(bulkId, errMsg));
  }

  private String buildErrorMessage(String bulkId, String errMsg) {
    String msgPattern = "Invalid operation with bulkId: " + bulkId + ". Reason: %s";
    UnaryOperator<String> errorMsgBuilder = detail -> String.format(msgPattern, detail);
    return errorMsgBuilder.apply(errMsg);
  }
}
