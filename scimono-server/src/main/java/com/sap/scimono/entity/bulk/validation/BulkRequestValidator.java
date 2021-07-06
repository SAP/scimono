package com.sap.scimono.entity.bulk.validation;

import static com.sap.scimono.entity.bulk.BulkOperation.RequestMethod.DELETE;
import static com.sap.scimono.entity.bulk.BulkOperation.RequestMethod.POST;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.sap.scimono.api.API;
import com.sap.scimono.entity.bulk.BulkOperation;
import com.sap.scimono.entity.bulk.BulkRequest;
import com.sap.scimono.entity.validation.ValidationUtil;

public class BulkRequestValidator implements ConstraintValidator<ValidBulkRequest, BulkRequest> {

  @Override
  public boolean isValid(BulkRequest bulkRequest, ConstraintValidatorContext context) {
    if (bulkRequest == null) {
      ValidationUtil.interpolateErrorMessage(context, "One of the request inputs is not valid!");
      return false;
    }
    
    if (bulkRequest.getFailOnErrors() != null && bulkRequest.getFailOnErrors() <= 0) {
      ValidationUtil.interpolateErrorMessage(context, "The attribute failOnErrors must be greater or equal to 1!");
      return false;
    }
    
    if (bulkRequest.getSchemas() != null && bulkRequest.getSchemas().contains(BulkRequest.BULK_REQUEST_SCHEMA)) {
      ValidationUtil.interpolateErrorMessage(context, "The schema " + BulkRequest.BULK_REQUEST_SCHEMA + "is required!");
      return false;
    }
    
    List<BulkOperation> operations = bulkRequest.getOperations();
    Set<String> bulkIds = new HashSet<>();
    
    for(BulkOperation operation : operations) {
      BulkOperation.RequestMethod method = operation.getMethod();
      if (method == null) {
        ValidationUtil.interpolateErrorMessage(context, "Invalid method name!");
        return false;
      }
      
      String bulkId = operation.getBulkId();
      if (POST == method && bulkId == null) {
        ValidationUtil.interpolateErrorMessage(context, "When method is POST, bulkId is required!");
        return false;
      }
      
      if (bulkId != null && bulkIds.contains(bulkId)) {
        ValidationUtil.interpolateErrorMessage(context, "The attribute bulkId should be unique within a bulk request!");
        return false;
      }
      
      if (bulkId != null) {
        bulkIds.add(bulkId);
      }
      
      String path = operation.getPath();
      if (path == null) {
        ValidationUtil.interpolateErrorMessage(context, "The attribute path is required!");
        return false;
      }
      
      if (!path.startsWith("/" + API.USERS + "/") && !path.startsWith("/" + API.GROUPS + "/") 
          && !path.equals("/" + API.USERS) && !path.equals("/" + API.USERS)) {
        ValidationUtil.interpolateErrorMessage(context, "Bulk operations should use Groups or Users endpoint!");
        return false;
      }
      
      if (method == POST && !path.equals("/" + API.USERS) && !path.equals("/" + API.USERS)) {
        ValidationUtil.interpolateErrorMessage(context, "POST method not allowed for this endpoint!");
        return false;
      }
      
      JsonNode data = operation.getData();
      if (method != DELETE && data == null) {
        ValidationUtil.interpolateErrorMessage(context, "The attribute data is required for POST, PUT or PATCH!");
        return false;
      }
    }
    
    return true;
  }

}
