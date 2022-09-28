package com.sap.scimono.entity.bulk;

import com.sap.scimono.helper.ResourceLocationService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
public class BulkResponseOperationLocationServiceTest {

  private ResourceLocationService usersLocationService;
  private ResourceLocationService groupsLocationService;

  @DisplayName("Test Bulk request with unsuccessful POST methods returns response without location")
  @Test
  public void validateLocationForUnsuccessfulPostMethod() {
    ResponseOperation responseOperation = buildResponseOperation(null);
    BulkBody<ResponseOperation> bulkResponse = BulkBody.forResponse(Collections.singletonList(responseOperation));
    BulkResponseOperationLocationService responseService = new BulkResponseOperationLocationService(usersLocationService, groupsLocationService);
    BulkBody<ResponseOperation> response = responseService.rebuildWithLocations(bulkResponse);
    Assert.assertEquals("The number of operations is not as same as expected.", 1, response.getOperations().size());
    Assert.assertNull("The operation location is not null as expected.", responseOperation.getLocation());
  }

  @DisplayName("Test Bulk request with successful POST methods returns response with location")
  @Test
  public void validateLocationForSuccessfulPostMethod() {
    ResponseOperation responseOperation = buildResponseOperation("example");
    BulkBody<ResponseOperation> bulkResponse = BulkBody.forResponse(Collections.singletonList(responseOperation));
    BulkResponseOperationLocationService responseService = new BulkResponseOperationLocationService(usersLocationService, groupsLocationService);
    BulkBody<ResponseOperation> response = responseService.rebuildWithLocations(bulkResponse);
    Assert.assertEquals("The number of operations is not as same as expected.", 1, response.getOperations().size());
    Assert.assertNull("The operation location is not null as expected.", responseOperation.getLocation());
  }

  private ResponseOperation buildResponseOperation(String bulkId) {
    return new ResponseOperation.Builder().setBulkId(bulkId).setMethod(RequestMethod.POST).build();
  }

}
