
package com.sap.scimono.scim.system.tests;

import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.entity.patch.PatchBody;
import com.sap.scimono.entity.patch.PatchOperation;
import com.sap.scimono.scim.system.tests.util.TestData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PatchResourceWithWrongPatchSchemaTest extends SCIMHttpResponseCodeTest {

  @ParameterizedTest
  @MethodSource("getPatchSchemaNameParameters")
  public void testPatchUserRequestWithWrongPatchSchemaNameAddOp(String patchSchemaName) {
    PatchBody patchBody = TestData.preparePatchBodyWithCustomOperationAndSchema(PatchOperation.Type.ADD, patchSchemaName);

    SCIMResponse<?> response = userRequest.patchUser(patchBody, VALID_UUID);
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());
  }

  @ParameterizedTest
  @MethodSource("getPatchSchemaNameParameters")
  public void testPatchGroupRequestWithWrongPatchSchemaNameAddOp(String patchSchemaName) {
    PatchBody patchBody = TestData.preparePatchBodyWithCustomOperationAndSchema(PatchOperation.Type.ADD, patchSchemaName);

    SCIMResponse<?> response = groupRequest.patchGroup(patchBody, VALID_UUID);
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());
  }

  @ParameterizedTest
  @MethodSource("getPatchSchemaNameParameters")
  public void testPatchUserRequestWithWrongPatchSchemaNameReplaceOp(String patchSchemaName) {
    PatchBody patchBody = TestData.preparePatchBodyWithCustomOperationAndSchema(PatchOperation.Type.REPLACE, patchSchemaName);

    SCIMResponse<?> response = userRequest.patchUser(patchBody, VALID_UUID);
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());
  }

  @ParameterizedTest
  @MethodSource("getPatchSchemaNameParameters")
  public void testPatchGroupRequestWithWrongPatchSchemaNameReplaceOp(String patchSchemaName) {
    PatchBody patchBody = TestData.preparePatchBodyWithCustomOperationAndSchema(PatchOperation.Type.REPLACE, patchSchemaName);

    SCIMResponse<?> response = groupRequest.patchGroup(patchBody, VALID_UUID);
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());
  }

  @ParameterizedTest
  @MethodSource("getPatchSchemaNameParameters")
  public void testPatchUserRequestWithWrongPatchSchemaNameRemoveOp(String patchSchemaName) {
    PatchBody patchBody = TestData.preparePatchBodyWithCustomOperationAndSchema(PatchOperation.Type.REMOVE, patchSchemaName);

    SCIMResponse<?> response = userRequest.patchUser(patchBody, VALID_UUID);
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());
  }

  @ParameterizedTest
  @MethodSource("getPatchSchemaNameParameters")
  public void testPatchGroupRequestWithWrongPatchSchemaNameRemoveOp(String patchSchemaName) {
    PatchBody patchBody = TestData.preparePatchBodyWithCustomOperationAndSchema(PatchOperation.Type.REMOVE, patchSchemaName);

    SCIMResponse<?> response = groupRequest.patchGroup(patchBody, VALID_UUID);
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
