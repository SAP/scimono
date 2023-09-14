
package com.sap.scimono.scim.system.tests;

import com.sap.scimono.scim.system.tests.util.CustomTargetSystemRestClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.core.Response;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SCIMHttpResponseCodeTest extends SCIMComplianceTest {

  public static final String ILLEGAL_UUID = "e87ca7b1-35f4d-493d-96f2-5ba3cf43deb";
  public static final String VALID_UUID = "aff7a7d5-42ed-4854-9e68-d51780cea401";

  @Test
  @DisplayName("Test Get non existing resource type")
  public void testGetMissingResource404() {
   Response httpResponse = CustomTargetSystemRestClient.INSTANCE.getEntitiesHttpResponse("wrongEndpoint");

   assertEquals(NOT_FOUND.getStatusCode(), httpResponse.getStatus(), "Verify response status");
  }

}
