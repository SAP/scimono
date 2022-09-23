package com.sap.scimono.entity.validation;

import com.sap.scimono.callback.config.SCIMConfigurationCallback;
import com.sap.scimono.entity.ErrorResponse;
import com.sap.scimono.entity.bulk.RequestMethod;
import com.sap.scimono.entity.bulk.BulkBody;
import com.sap.scimono.entity.bulk.RequestOperation;
import com.sap.scimono.entity.bulk.ResponseOperation;
import com.sap.scimono.entity.bulk.validation.BulkOperationsValidator;
import com.sap.scimono.helper.ResourceLocationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

@RunWith(MockitoJUnitRunner.class)
public class BulkOperationsValidatorTest {

  @Mock
  private SCIMConfigurationCallback scimConfigurationCallback;
  @Mock
  private ResourceLocationService usersLocationService;
  @Mock
  private ResourceLocationService groupsLocationService;

  @Test
  public void getValidResponseDataRequestOperationNullsTest() {
    RequestOperation requestOperation = buildRequestOperation(null);
    ResponseOperation responseOperation = buildResponseOperation(null, buildErrorResponse());
    BulkBody<RequestOperation> bulkRequest = BulkBody.forRequest(null, Arrays.asList(requestOperation, requestOperation));
    BulkBody<ResponseOperation> bulkResponse = BulkBody.forResponse(Arrays.asList(responseOperation, responseOperation));
    BulkOperationsValidator operationsValidator = new BulkOperationsValidator(scimConfigurationCallback, usersLocationService, groupsLocationService);
    operationsValidator.getValidResponseData(bulkRequest, bulkResponse);
  }

  private RequestOperation buildRequestOperation(String bulkId) {
    return new RequestOperation.Builder().setBulkId(bulkId).setMethod(RequestMethod.POST).setPath("/Users").build();
  }

  private ResponseOperation buildResponseOperation(String bulkId, ErrorResponse errorResponse) {
    return new ResponseOperation("POST", bulkId, "/Users", null, errorResponse, "400").builder().build();
  }

  private ErrorResponse buildErrorResponse() {
    return new ErrorResponse(400, "invalidValue", "Invalid operation for method: POST");
  }

}
