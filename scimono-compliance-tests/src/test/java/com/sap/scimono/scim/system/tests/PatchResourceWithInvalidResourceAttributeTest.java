
package com.sap.scimono.scim.system.tests;

import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.entity.patch.PatchBody;
import com.sap.scimono.entity.patch.PatchOperation;
import com.sap.scimono.scim.system.tests.util.TestData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.sap.scimono.scim.system.tests.util.TestData.JACKSON_NODE_FACTORY;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PatchResourceWithInvalidResourceAttributeTest extends SCIMHttpResponseCodeTest {
  private static final String ERROR_MESSAGE = "Test fails when making patch request with shema: ";

  @ParameterizedTest
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
    SCIMResponse<?> response = userRequest.patchUser(patchBody, VALID_UUID);
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode(), ERROR_MESSAGE + patchAttributePath);
  }

  @ParameterizedTest
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
    SCIMResponse<?> response = groupRequest.patchGroup(patchBody, VALID_UUID);
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode(), ERROR_MESSAGE + patchAttributePath);
  }

  @ParameterizedTest
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
    SCIMResponse<?> response = userRequest.patchUser(patchBody, VALID_UUID);
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode(), ERROR_MESSAGE + patchAttributePath);
  }

  @ParameterizedTest
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
    SCIMResponse<?> response = groupRequest.patchGroup(patchBody, VALID_UUID);
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode(), ERROR_MESSAGE + patchAttributePath);
  }

  @ParameterizedTest
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
    SCIMResponse<?> response = userRequest.patchUser(patchBody, VALID_UUID);
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode(), ERROR_MESSAGE + patchAttributePath);
  }

  @ParameterizedTest
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
    SCIMResponse<?> response = groupRequest.patchGroup(patchBody, VALID_UUID);
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
