package com.sap.scimono.entity.bulk.validation;

import static com.sap.scimono.entity.bulk.RequestMethod.DELETE;
import static com.sap.scimono.entity.bulk.RequestMethod.PATCH;
import static com.sap.scimono.entity.bulk.RequestMethod.POST;
import static com.sap.scimono.entity.bulk.RequestMethod.PUT;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.sap.scimono.api.API;
import com.sap.scimono.entity.bulk.BulkBody;
import com.sap.scimono.entity.bulk.RequestMethod;
import com.sap.scimono.entity.bulk.RequestOperation;
import com.sap.scimono.entity.validation.ValidationUtil;

public class BulkRequestValidator implements ConstraintValidator<ValidBulkRequest, BulkBody<RequestOperation>> {

  @Override
  public boolean isValid(BulkBody<RequestOperation> bulkRequest, ConstraintValidatorContext context) {
    if (bulkRequest == null) {
      ValidationUtil.interpolateErrorMessage(context, "One of the request inputs is not valid!");
      return false;
    }

    if (bulkRequest.getFailOnErrors() != null && bulkRequest.getFailOnErrors() <= 0) {
      ValidationUtil.interpolateErrorMessage(context, "The attribute failOnErrors must be greater or equal to 1!");
      return false;
    }

    if (bulkRequest.getSchemas() != null && !bulkRequest.getSchemas().contains(BulkBody.BULK_REQUEST_SCHEMA)) {
      ValidationUtil.interpolateErrorMessage(context, "The schema " + BulkBody.BULK_REQUEST_SCHEMA + " is required!");
      return false;
    }

    return areOperationsValid(context, bulkRequest.getOperations());
  }

  private boolean areOperationsValid(ConstraintValidatorContext context, List<RequestOperation> operations) {
    Set<String> processedBulkIds = new HashSet<>();

    for (RequestOperation operation : operations) {
      String msgPattern = "Invalid operation with bulkId: " + operation.getBulkId() + ". Reason: %s";
      UnaryOperator<String> errorMsgBuilder = detail -> String.format(msgPattern, detail);

      RequestMethod method = operation.getMethod();
      if (method == null) {
        ValidationUtil.interpolateErrorMessage(context, errorMsgBuilder.apply("Invalid method name!, Valid methods: "
            + Arrays.toString(RequestMethod.values())));
        return false;
      }

      String bulkId = operation.getBulkId();
      if (POST == method && bulkId == null) {
        ValidationUtil.interpolateErrorMessage(context, "bulkId is required for method: " + POST);
        return false;
      }

      if (bulkId != null && processedBulkIds.contains(bulkId)) {
        ValidationUtil.interpolateErrorMessage(context, errorMsgBuilder.apply("BulkId should be unique within a bulk request!"));
        return false;
      }

      if (bulkId != null) {
        processedBulkIds.add(bulkId);
      }

      if (!validatePath(context, errorMsgBuilder, operation)) {
        return false;
      }

      if (method != DELETE && !operation.isDataAvailable()) {
        ValidationUtil.interpolateErrorMessage(context, errorMsgBuilder.apply("The attribute data is required for POST, PUT or PATCH!"));
        return false;
      }
    }

    return true;
  }

  private boolean validatePath(ConstraintValidatorContext context, UnaryOperator<String> errorMsgBuilder, RequestOperation operation) {
    String resourceEndpoint = RequestOperation.extractRootFromPath(operation.getPath());
    if (!API.USERS.equalsIgnoreCase(resourceEndpoint) && !API.GROUPS.equalsIgnoreCase(resourceEndpoint)) {
      String msg = String.format("Invalid path endpoint for operation with bulkId: %s. Path should start with either %s or %s endpoint.",
          operation.getBulkId(), API.USERS, API.GROUPS);

      ValidationUtil.interpolateErrorMessage(context, errorMsgBuilder.apply(msg));
      return false;
    }

    RequestMethod method = operation.getMethod();
    if ((PUT == method || PATCH == method) && !operation.getResourceId().isPresent()) {
      ValidationUtil.interpolateErrorMessage(context, errorMsgBuilder.apply("Path should point to resource id for PUT and POST methods"));
      return false;
    }

    return true;
  }
}
