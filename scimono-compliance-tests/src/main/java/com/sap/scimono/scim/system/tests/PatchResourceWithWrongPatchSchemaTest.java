
package com.sap.scimono.scim.system.tests;

import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.entity.patch.PatchBody;
import com.sap.scimono.entity.patch.PatchOperation;
import com.sap.scimono.scim.system.tests.extensions.GroupClientScimResponseExtension;
import com.sap.scimono.scim.system.tests.extensions.UserClientScimResponseExtension;
import com.sap.scimono.scim.system.tests.util.TestData;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PatchResourceWithWrongPatchSchemaTest extends SCIMHttpResponseCodeTest {
  private static final Logger logger = LoggerFactory.getLogger(PatchResourceWithWrongPatchSchemaTest.class);

  @RegisterExtension
  UserClientScimResponseExtension resourceAwareUserRequest = UserClientScimResponseExtension.forClearingAfterEachExecutions(userRequest);

  @RegisterExtension
  GroupClientScimResponseExtension resourceAwareGroupRequest = GroupClientScimResponseExtension.forClearingAfterEachExecutions(groupRequest);


  @ParameterizedTest(name = "Test Patch user with Add operation and invalid Patch Schema attribute: {0}")
  @MethodSource("getPatchSchemaNameParameters")
  public void testPatchUserRequestWithWrongPatchSchemaNameAddOp(String patchSchemaName) {
    PatchBody patchBody = TestData.preparePatchBodyWithCustomOperationAndSchema(PatchOperation.Type.ADD, patchSchemaName);

    logger.info("Patching User with wrong Patch Schema");
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, VALID_UUID);
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());
  }

  @ParameterizedTest(name = "Test Patch group with Add operation and invalid Patch Schema attribute: {0}")
  @MethodSource("getPatchSchemaNameParameters")
  public void testPatchGroupRequestWithWrongPatchSchemaNameAddOp(String patchSchemaName) {
    PatchBody patchBody = TestData.preparePatchBodyWithCustomOperationAndSchema(PatchOperation.Type.ADD, patchSchemaName);

    logger.info("Patching Group with wrong Patch Schema");
    SCIMResponse<?> response = resourceAwareGroupRequest.patchGroup(patchBody, VALID_UUID);
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());
  }

  @ParameterizedTest(name = "Test Patch user with Replace operation and invalid Patch Schema attribute: {0}")
  @MethodSource("getPatchSchemaNameParameters")
  public void testPatchUserRequestWithWrongPatchSchemaNameReplaceOp(String patchSchemaName) {
    PatchBody patchBody = TestData.preparePatchBodyWithCustomOperationAndSchema(PatchOperation.Type.REPLACE, patchSchemaName);

    logger.info("Patching User with wrong Patch Schema");
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, VALID_UUID);
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());
  }

  @ParameterizedTest(name = "Test Patch group with Replace operation and invalid Patch Schema attribute: {0}")
  @MethodSource("getPatchSchemaNameParameters")
  public void testPatchGroupRequestWithWrongPatchSchemaNameReplaceOp(String patchSchemaName) {
    PatchBody patchBody = TestData.preparePatchBodyWithCustomOperationAndSchema(PatchOperation.Type.REPLACE, patchSchemaName);

    logger.info("Patching Group with wrong Patch Schema");
    SCIMResponse<?> response = resourceAwareGroupRequest.patchGroup(patchBody, VALID_UUID);
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());
  }

  @ParameterizedTest(name = "Test Patch user with Remove operation and invalid Patch Schema attribute: {0}")
  @MethodSource("getPatchSchemaNameParameters")
  public void testPatchUserRequestWithWrongPatchSchemaNameRemoveOp(String patchSchemaName) {
    PatchBody patchBody = TestData.preparePatchBodyWithCustomOperationAndSchema(PatchOperation.Type.REMOVE, patchSchemaName);

    logger.info("Patching User with wrong Patch Schema");
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, VALID_UUID);
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());
  }

  @ParameterizedTest(name = "Test Patch group with Remove operation and invalid Patch Schema attribute: {0}")
  @MethodSource("getPatchSchemaNameParameters")
  public void testPatchGroupRequestWithWrongPatchSchemaNameRemoveOp(String patchSchemaName) {
    PatchBody patchBody = TestData.preparePatchBodyWithCustomOperationAndSchema(PatchOperation.Type.REMOVE, patchSchemaName);

    logger.info("Patching Group with wrong Patch Schema");
    SCIMResponse<?> response = resourceAwareGroupRequest.patchGroup(patchBody, VALID_UUID);
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());
  }

  private static Stream<String> getPatchSchemaNameParameters() {
    // @formatter:off
    return Stream.of(
        "urn:ietf:params:scim:api:messages:1.0:PatchOp",
        "urn:ietf:params:scim:api:messages:2.0:PatchOp1",
        "urn:ietf:params:scim:schemas:core:2.0:Schema",
        "urn:ietf:params:scim:schemas:core:2.0:User",
        "urn:ietf:params:scim:schemas:core:2.0:ResourceType",
        "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User",
        "urn:ietf:params:scim:schemas:core:2.0:Group",
        "urn:ietf:params:scim:api:messages:2.0:PatchOp:PatchOp",
        "urn:ietf:params:scim:api:messages:2.0:PatchOp.PatchOp",
        "urn:ietf:params:scim:api:messages:2.0:PatchOp:DisplayName",
        "IllegalPatchSchema",
        "",
        null
    );
    // @formatter:on
  }
}
