package com.sap.scimono.entity.bulk.validation;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

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

    return !hasDuplicateBulkId(context, bulkRequest.getOperations());
  }

  private boolean hasDuplicateBulkId(ConstraintValidatorContext context, List<RequestOperation> operations) {
    return operations.stream()
    .map(RequestOperation::getBulkId)
    .filter(Objects::nonNull)
    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
    .entrySet().stream()
    .filter(e -> e.getValue() > 1)
    .map(Map.Entry::getKey)
    .anyMatch(bulkId -> {
      String message = "Invalid operation with bulkId: " + bulkId + ". Reason: BulkId should be unique within a bulk request!";
      ValidationUtil.interpolateErrorMessage(context, message);
      return true;
    });
  }
}