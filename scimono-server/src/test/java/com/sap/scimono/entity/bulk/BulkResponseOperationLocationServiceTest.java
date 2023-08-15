package com.sap.scimono.entity.bulk;

import com.sap.scimono.entity.ErrorResponse;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.User;
import com.sap.scimono.helper.ResourceLocationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class BulkResponseOperationLocationServiceTest {

  @Mock
  private ResourceLocationService usersLocationService;
  @Mock
  private ResourceLocationService groupsLocationService;

  @DisplayName("Test Bulk request with unsuccessful POST methods returns response without location")
  @Test
  void validateLocationForUnsuccessfulPostMethod() {
    ResponseOperation responseOperation = buildResponseOperationWithoutBulkId();
    BulkBody<ResponseOperation> bulkResponse = BulkBody.forResponse(Collections.singletonList(responseOperation));
    BulkResponseOperationLocationService responseService = new BulkResponseOperationLocationService(usersLocationService, groupsLocationService);
    BulkBody<ResponseOperation> response = responseService.rebuildWithLocations(bulkResponse);
    assertEquals(1, response.getOperations().size(), "The number of operations is not as same as expected.");
    assertNull(response.getOperations().get(0).getLocation(), "The operation location is not null as expected.");
  }

  @DisplayName("Test Bulk request with successful POST methods returns response with location")
  @ParameterizedTest
  @ValueSource(strings = { User.RESOURCE_TYPE_USER, Group.RESOURCE_TYPE_GROUP })
  void validateLocationForSuccessfulPostMethod(String resourceType) {
    ResponseOperation responseOperation = buildResponseOperationWithResourceType(resourceType);
    Mockito.lenient().doReturn(URI.create("/Users/example")).when(usersLocationService).getLocation(any());
    Mockito.lenient().doReturn(URI.create("/Groups/example")).when(groupsLocationService).getLocation(any());
    BulkBody<ResponseOperation> bulkResponse = BulkBody.forResponse(Collections.singletonList(responseOperation));
    BulkResponseOperationLocationService responseService = new BulkResponseOperationLocationService(usersLocationService, groupsLocationService);
    BulkBody<ResponseOperation> response = responseService.rebuildWithLocations(bulkResponse);
    assertEquals(1, response.getOperations().size(), "The number of operations is not as same as expected.");
    assertNotNull(response.getOperations().get(0).getLocation(), "The operation location is missing.");
  }

  private ResponseOperation buildResponseOperationWithResourceType(String resourceType) {
    return new ResponseOperation.Builder().setResourceType(resourceType).setBulkId("example").setMethod(RequestMethod.POST).build();
  }

  private ResponseOperation buildResponseOperationWithoutBulkId() {
    ErrorResponse response = new ErrorResponse(Response.Status.BAD_REQUEST.getStatusCode(), null, "Error");
    return new ResponseOperation.Builder().withError(response).setMethod(RequestMethod.POST).build();
  }

}
