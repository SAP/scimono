package com.sap.scimono.entity.bulk;

import com.sap.scimono.helper.ResourceLocationService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

@RunWith(MockitoJUnitRunner.class)
public class BulkResponseOperationLocationServiceTest {

  private ResourceLocationService usersLocationService;
  private ResourceLocationService groupsLocationService;

  @DisplayName("Test Bulk request with multiple POST methods with null bulkId returns invalid operation response")
  @Test
  public void validateBulkIdIsRequiredForPostMethodTest() {
    ResponseOperation responseOperation = buildResponseOperation(null);
    BulkBody<ResponseOperation> bulkResponse = BulkBody.forResponse(Arrays.asList(responseOperation, responseOperation));
    BulkResponseOperationLocationService responseService = new BulkResponseOperationLocationService(usersLocationService, groupsLocationService);
    BulkBody<ResponseOperation> response = responseService.rebuildWithLocations(bulkResponse);
    Assert.assertEquals("The number of operations is not as same as expected.", 2, response.getOperations().size());
  }

  private ResponseOperation buildResponseOperation(String bulkId) {
    return new ResponseOperation.Builder().setBulkId(bulkId).setMethod(RequestMethod.POST).build();
  }

}
