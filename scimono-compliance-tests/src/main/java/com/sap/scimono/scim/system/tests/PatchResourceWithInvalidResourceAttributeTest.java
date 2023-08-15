
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

import static com.sap.scimono.scim.system.tests.util.TestData.JACKSON_NODE_FACTORY;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PatchResourceWithInvalidResourceAttributeTest extends SCIMHttpResponseCodeTest {
  private static final Logger logger = LoggerFactory.getLogger(PatchResourceWithInvalidResourceAttributeTest.class);
  private static final String ERROR_MESSAGE = "Test fails when making patch request with shema: ";

  @RegisterExtension 
  UserClientScimResponseExtension resourceAwareUserRequest = UserClientScimResponseExtension.forClearingAfterEachExecutions(userRequest);

  @RegisterExtension 
  GroupClientScimResponseExtension resourceAwareGroupRequest = GroupClientScimResponseExtension.forClearingAfterEachExecutions(groupRequest);

  @ParameterizedTest(name = "Test Patch user with Add operation and invalid Schema attribute in path: {0}")
  @MethodSource("getResourceAttributesTestParameters")
  public void testPatchUserRequestChangingInvalidSchemaAttributeAddOp(String patchAttributePath) {
    // @formatter:off
    PatchOperation operation = new PatchOperation.Builder()
        .setOp(PatchOperation.Type.ADD)
        .setPath(patchAttributePath)
        .setValue(JACKSON_NODE_FACTORY.numberNode(10))
        .build();

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes()
        .addOperation(operation)
        .build();
    // @formatter:on

    logger.info("Patching User with invalid Patch Body");
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, VALID_UUID);
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode(), ERROR_MESSAGE + patchAttributePath);
  }

  @ParameterizedTest(name = "Test Patch group with Add operation and invalid Schema attribute in path: {0}")
  @MethodSource("getResourceAttributesTestParameters")
  public void testPatchGroupRequestChangingInvalidSchemaAttributeAddOp(String patchAttributePath) {
    // @formatter:off
    PatchOperation operation = new PatchOperation.Builder()
        .setOp(PatchOperation.Type.ADD)
        .setPath(patchAttributePath)
        .setValue(JACKSON_NODE_FACTORY.numberNode(10))
        .build();

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes()
        .addOperation(operation)
        .build();
    // @formatter:on
    logger.info("Patching User with invalid Patch Body");
    SCIMResponse<?> response = resourceAwareGroupRequest.patchGroup(patchBody, VALID_UUID);
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode(), ERROR_MESSAGE + patchAttributePath);
  }

  @ParameterizedTest(name = "Test Patch user with Replace operation and invalid Schema attribute in path: {0}")
  @MethodSource("getResourceAttributesTestParameters")
  public void testPatchUserRequestChangingInvalidSchemaAttributeReplaceOp(String patchAttributePath) {
    // @formatter:off
    PatchOperation operation = new PatchOperation.Builder()
        .setOp(PatchOperation.Type.REPLACE)
        .setPath(patchAttributePath)
        .setValue(JACKSON_NODE_FACTORY.numberNode(10))
        .build();

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes()
        .addOperation(operation)
        .build();
    // @formatter:on
    logger.info("Patching User with invalid Patch Body");
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, VALID_UUID);
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode(), ERROR_MESSAGE + patchAttributePath);
  }

  @ParameterizedTest(name = "Test Patch group with Replace operation and invalid Schema attribute in path: {0}")
  @MethodSource("getResourceAttributesTestParameters")
  public void testPatchGroupRequestChangingInvalidSchemaAttributeReplaceOp(String patchAttributePath) {
    // @formatter:off
    PatchOperation operation = new PatchOperation.Builder()
        .setOp(PatchOperation.Type.REPLACE)
        .setPath(patchAttributePath)
        .setValue(JACKSON_NODE_FACTORY.numberNode(10))
        .build();

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes()
        .addOperation(operation)
        .build();
    // @formatter:on
    logger.info("Patching User with invalid Patch Body");
    SCIMResponse<?> response = resourceAwareGroupRequest.patchGroup(patchBody, VALID_UUID);
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode(), ERROR_MESSAGE + patchAttributePath);
  }

  @ParameterizedTest(name = "Test Patch user with Remove operation and invalid Schema attribute in path: {0}")
  @MethodSource("getResourceAttributesTestParameters")
  public void testPatchUserRequestChangingInvalidSchemaAttributeRemoveOp(String patchAttributePath) {
    // @formatter:off
    PatchOperation operation = new PatchOperation.Builder()
        .setOp(PatchOperation.Type.REMOVE)
        .setPath(patchAttributePath)
        .setValue(JACKSON_NODE_FACTORY.numberNode(10))
        .build();

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes()
        .addOperation(operation)
        .build();
    // @formatter:on
    logger.info("Patching User with invalid Patch Body");
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, VALID_UUID);
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode(), ERROR_MESSAGE + patchAttributePath);
  }

  @ParameterizedTest(name = "Test Patch group with Remove operation and invalid Schema attribute in path: {0}")
  @MethodSource("getResourceAttributesTestParameters")
  public void testPatchGroupRequestChangingInvalidSchemaAttributeRemoveOp(String patchAttributePath) {
    // @formatter:off
    PatchOperation operation = new PatchOperation.Builder()
        .setOp(PatchOperation.Type.REMOVE)
        .setPath(patchAttributePath)
        .setValue(JACKSON_NODE_FACTORY.numberNode(10))
        .build();

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes()
        .addOperation(operation)
        .build();
    // @formatter:on
    logger.info("Patching User with invalid Patch Body");
    SCIMResponse<?> response = resourceAwareGroupRequest.patchGroup(patchBody, VALID_UUID);
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode(), ERROR_MESSAGE + patchAttributePath);
  }

  private static Stream<String> getResourceAttributesTestParameters() {
    // @formatter:off
    return Stream.of(
        "urn:ietf:params:scim:schemas:extension:enterprise:2.0:Group:employeeNumber",
        "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:employeNumber",
        "urn:ietf:params:scim:schemas:extension:enterprise:2.0:DJEKICHAN:employeNumber",
        "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:userName:userName",
        "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:userName.userName",
        "urn:ietf:params:scim:schemas:extension:enterprise:2.0:Group:null",
        "urn:ietf:params:scim:schemas:core:2.0:User:userName.userName",
        "urn:ietf:params:scim:schemas:core:2.0:Group:displayName.displayName",
        "IllegalPatchSchema",
        "",
        null
    );
    // @formatter:on
  }
}
