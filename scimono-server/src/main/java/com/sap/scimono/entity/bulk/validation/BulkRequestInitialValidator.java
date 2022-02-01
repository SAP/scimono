package com.sap.scimono.entity.bulk.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.sap.scimono.entity.bulk.BulkBody;
import com.sap.scimono.entity.bulk.RequestOperation;
import com.sap.scimono.entity.validation.ValidationUtil;

public class BulkRequestInitialValidator implements ConstraintValidator<ValidBulkRequest, BulkBody<RequestOperation>> {

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

    return hasDuplicateBulkId(context, bulkRequest.getOperations());
  }
  
  private boolean hasDuplicateBulkId(RequestOperation operation, List<String> allProcessedIDs) {
      List<Integer> allIndexes = IntStream.range(0, allProcessedIDs.size()).boxed().filter(i -> allProcessedIDs.get(i).equals(operation.getBulkId()))
          .collect(Collectors.toList());

      if (allIndexes.size() > 1) {
        return true;
      }
    return false;
  }
  
  private List<String> getAllProcessedBulkIDs(List<RequestOperation> operations) {
    List<String> allProcessedIDs = new ArrayList<String>();
    for (RequestOperation operation : operations) {
      String bulkId = operation.getBulkId();
      if (bulkId != null) {
        allProcessedIDs.add(bulkId);
      }
    }
    return allProcessedIDs;
  }

  private boolean hasDuplicateBulkId(ConstraintValidatorContext context, List<RequestOperation> operations) {
    List<String> allProcessedIDs = getAllProcessedBulkIDs(operations);

    for (RequestOperation operation : operations) {
      String msgPattern = "Invalid operation with bulkId: " + operation.getBulkId() + ". Reason: %s";
      UnaryOperator<String> errorMsgBuilder = detail -> String.format(msgPattern, detail);

      String bulkId = operation.getBulkId();
      if (bulkId != null && hasDuplicateBulkId(operation, allProcessedIDs)) {
        ValidationUtil.interpolateErrorMessage(context, errorMsgBuilder.apply("BulkId should be unique within a bulk request!"));
        return false;
      }
    }

    return true;
  }
}