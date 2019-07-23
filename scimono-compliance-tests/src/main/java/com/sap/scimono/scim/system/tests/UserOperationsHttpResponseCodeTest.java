
package com.sap.scimono.scim.system.tests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.entity.Email;
import com.sap.scimono.entity.EnterpriseExtension;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.definition.CoreUserAttributes;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.patch.PatchBody;
import com.sap.scimono.entity.patch.PatchOperation;
import com.sap.scimono.scim.system.tests.util.CustomTargetSystemRestClient;
import com.sap.scimono.scim.system.tests.util.TestData;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.UUID;

import static com.sap.scimono.api.API.USERS;
import static com.sap.scimono.entity.definition.CoreUserAttributes.DISPLAY_NAME;
import static com.sap.scimono.scim.system.tests.util.TestData.JACKSON_NODE_FACTORY;
import static com.sap.scimono.scim.system.tests.util.TestData.buildTestUser;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserOperationsHttpResponseCodeTest extends SCIMHttpResponseCodeTest {
  private static final Logger logger = LoggerFactory.getLogger(UserOperationsHttpResponseCodeTest.class);

  @Test
  public void testGetUser200() {
    User testUser = TestData.buildTestUser("testGetUserHTTPResponse");
    SCIMResponse<User> scimResponse = userRequest.createUser(testUser);

    assertEquals(CREATED.getStatusCode(), scimResponse.getStatusCode());
    scimResponse = userRequest.readSingleUser(scimResponse.get().getId());

    assertTrue(scimResponse.isSuccess());
    assertEquals(OK.getStatusCode(), scimResponse.getStatusCode());
  }

  @Test
  public void testGetUserIllegalId1400() {
    SCIMResponse<User> scimResponse = userRequest.readSingleUser("e87ca7b1-5f4d-493d-96f2-5ba3cf43deb51");

    assertFalse(scimResponse.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), scimResponse.getStatusCode());
  }

  @Test
  public void testGetUserIllegalId2400() {
    SCIMResponse<User> scimResponse = userRequest.readSingleUser(ILLEGAL_UUID);

    assertFalse(scimResponse.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), scimResponse.getStatusCode());
  }

  @Test
  public void testGetUserIllegalId3400() {
    SCIMResponse<User> scimResponse = userRequest.readSingleUser("@!$^&*()_+=-[].,<>\'\":");

    assertFalse(scimResponse.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), scimResponse.getStatusCode());
  }

  @Test
  public void testGetUserNonExistingId404() {
    SCIMResponse<User> scimResponse = userRequest.readSingleUser(UUID.randomUUID().toString());

    assertFalse(scimResponse.isSuccess());
    assertEquals(NOT_FOUND.getStatusCode(), scimResponse.getStatusCode());
  }

  @Test
  public void testGetAllUsers200() {
    User testUser = TestData.buildTestUser("testGetAllUsersHTTPResponse");
    SCIMResponse<User> createUserResponse = userRequest.createUser(testUser);

    assertTrue(createUserResponse.isSuccess());
    assertEquals(CREATED.getStatusCode(), createUserResponse.getStatusCode());

    SCIMResponse<PagedByIndexSearchResult<User>> readUsersResponse = userRequest.readMultipleUsers();

    assertTrue(readUsersResponse.isSuccess());
    assertEquals(OK.getStatusCode(), readUsersResponse.getStatusCode());
  }

  @Test
  public void testGetAllUsersHashTag400() {
    SCIMResponse<User> scimResponse = userRequest.readSingleUser("#");

    assertFalse(scimResponse.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), scimResponse.getStatusCode());
  }

  @Test
  public void testCreateUserWithEmail201() {
    Email testMail = TestData.buildPersonalEmailWithDefaultAttrs();
    User testUser = TestData.setAttributesToATestUser("testCreateUserWithIllegalEmail400").addEmail(testMail).build();
    SCIMResponse<User> scimResponse = userRequest.createUser(testUser);

    assertTrue(scimResponse.isSuccess());
    assertEquals(CREATED.getStatusCode(), scimResponse.getStatusCode());

    User createdUser = getUser(scimResponse.get().getId());
    assertEquals(1, createdUser.getEmails().size());
  }

  @Test
  public void testCreateUsersWithSameUserNames409() {
    User testUser = TestData.setAttributesToATestUser("testCreateUsersWithSameUserNames409").build();
    SCIMResponse<User> scimResponse = userRequest.createUser(testUser);

    assertTrue(scimResponse.isSuccess());
    assertEquals(CREATED.getStatusCode(), scimResponse.getStatusCode());

    User testUserDuplicate = TestData.setAttributesToATestUser("testCreateUsersWithSameUserNames409").build();
    SCIMResponse<User> scimResponseForDuplicate = userRequest.createUser(testUserDuplicate);

    assertFalse(scimResponseForDuplicate.isSuccess());
    assertEquals(CONFLICT.getStatusCode(), scimResponseForDuplicate.getStatusCode());
  }

  @Test
  public void testUpdateUser200() {
    User testUser = TestData.buildTestUser("testUpdateUserHTTPResponse");
    SCIMResponse<User> scimResponse = userRequest.createUser(testUser);

    assertTrue(scimResponse.isSuccess());
    assertEquals(CREATED.getStatusCode(), scimResponse.getStatusCode());
    User createdUser = scimResponse.get();

    User updatedUser = new User.Builder(createdUser).setDisplayName("testUpdateUserHTTPResponseUpdatedUser").setId(createdUser.getId()).build();
    scimResponse = userRequest.updateUser(updatedUser);

    assertTrue(scimResponse.isSuccess());
    assertEquals(OK.getStatusCode(), scimResponse.getStatusCode());
    assertEquals("testUpdateUserHTTPResponseUpdatedUser", scimResponse.get().getDisplayName());
  }

  @Test
  public void testUpdateUserWithoutIdInBody200() {
    User testUser = TestData.buildTestUser("testUpdateUserWithoutId200");
    SCIMResponse<User> scimResponse = userRequest.createUser(testUser);

    assertEquals(CREATED.getStatusCode(), scimResponse.getStatusCode());
    User createdUser = scimResponse.get();

    User updatedUser = new User.Builder(createdUser).setDisplayName("testUpdateUserHTTPResponseUpdatedUser").build();
    Response httpResponse = CustomTargetSystemRestClient.INSTANCE.putEntityHttpResponse(USERS, createdUser.getId(), updatedUser);

    assertEquals(OK.getStatusCode(), httpResponse.getStatus());
    assertEquals("testUpdateUserHTTPResponseUpdatedUser", httpResponse.readEntity(User.class).getDisplayName());
  }

  @Test
  public void testUpdateUserWithIllegalId400() {
    User testUser = TestData.buildTestUser("testUpdateUserWithIllegalId400");
    SCIMResponse<User> scimResponse = userRequest.createUser(testUser);

    assertTrue(scimResponse.isSuccess());
    assertEquals(CREATED.getStatusCode(), scimResponse.getStatusCode());

    User updatedUser = new User.Builder(scimResponse.get()).setDisplayName("testUpdateUserHTTPResponseUpdatedUser").setId(ILLEGAL_UUID).build();
    scimResponse = userRequest.updateUser(updatedUser);

    assertFalse(scimResponse.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), scimResponse.getStatusCode());
  }

  @Test
  public void testUpdateUserWithNonExistingId404() {
    String nonExistingUserId = UUID.randomUUID().toString();
    User testUser = TestData.setAttributesToATestUser("testUpdateUserWithNonExistingId404").setId(nonExistingUserId).build();

    SCIMResponse<User> scimResponse = userRequest.updateUser(testUser);

    assertFalse(scimResponse.isSuccess());
    assertEquals(NOT_FOUND.getStatusCode(), scimResponse.getStatusCode());
  }

  @Test
  public void testDeleteUser204() {
    User testUser = TestData.buildTestUser("testDeleteUserHTTPResponse");
    SCIMResponse<User> createUserResponse = userRequest.createUser(testUser);

    assertTrue(createUserResponse.isSuccess());
    assertEquals(CREATED.getStatusCode(), createUserResponse.getStatusCode());

    SCIMResponse<Void> deleteUserResponse = userRequest.deleteUser(createUserResponse.get().getId());

    assertTrue(deleteUserResponse.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), deleteUserResponse.getStatusCode());
  }

  @Test
  public void testDeleteUserWithIllegalId400() {
    SCIMResponse<Void> scimResponse = userRequest.deleteUser(ILLEGAL_UUID);

    assertFalse(scimResponse.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), scimResponse.getStatusCode());
  }

  @Test
  public void testDeleteUserWithNonExistingId404() {
    SCIMResponse<Void> scimResponse = userRequest.deleteUser(UUID.randomUUID().toString());

    assertFalse(scimResponse.isSuccess());
    assertEquals(NOT_FOUND.getStatusCode(), scimResponse.getStatusCode());
  }

  @Test
  public void testDeleteUserTwice404() {
    String userName = "testDeleteUserTwice404-User";
    logger.info("Creating User -{}- that will be deleted after that", userName);
    User user = createUser(buildTestUser(userName));

    SCIMResponse<Void> firstDeleteAttemptResponse = userRequest.deleteUser(user.getId());
    SCIMResponse<Void> secondDeleteAttemptResponse = userRequest.deleteUser(user.getId());

    assertAll("Verify delete user attempts",
        () -> assertTrue(firstDeleteAttemptResponse.isSuccess(), "Verify first delete attempt is successful"),
        () -> assertEquals(NO_CONTENT.getStatusCode(), firstDeleteAttemptResponse.getStatusCode(), "Verify correct response code"),
        () -> assertFalse(secondDeleteAttemptResponse.isSuccess(), "Verify second delete attempt is failure"),
        () -> assertEquals(NOT_FOUND.getStatusCode(), secondDeleteAttemptResponse.getStatusCode(), "Verify correct response code"));
  }

  @Test
  public void testPatchUserAddOperationForSimpleAttributeWithCorrectAttributes204() {
    User testUser = TestData.buildTestUser("testPatchUserAddOperationForSimpleAttributeWithCorrectAttributes204");
    SCIMResponse<User> createUserResponse = userRequest.createUser(testUser);

    assertTrue(createUserResponse.isSuccess());
    assertEquals(CREATED.getStatusCode(), createUserResponse.getStatusCode());

 // @formatter:off
    PatchOperation operation = new PatchOperation.Builder()
        .setOp(PatchOperation.Type.ADD)
        .setPath(DISPLAY_NAME.scimName())
        .setValue(JACKSON_NODE_FACTORY.textNode("AdoDisplayName"))
        .build();

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes()
        .addOperation(operation)
        .build();
 // @formatter:on

    SCIMResponse<?> patchUserResponse = userRequest.patchUser(patchBody, createUserResponse.get().getId());

    assertTrue(patchUserResponse.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), patchUserResponse.getStatusCode());
  }

  @Test
  public void testPatchUserReplaceOperationForSimpleAttributeWithCorrectAttributes204() {
    User testUser = TestData.buildTestUser("testPatchUserReplaceOperationForSimpleAttributeWithCorrectAttributes204");
    SCIMResponse<User> createUserResponse = userRequest.createUser(testUser);

    assertTrue(createUserResponse.isSuccess());
    assertEquals(CREATED.getStatusCode(), createUserResponse.getStatusCode());

 // @formatter:off
    PatchOperation operation = new PatchOperation.Builder()
        .setOp(PatchOperation.Type.REPLACE)
        .setPath(DISPLAY_NAME.scimName())
        .setValue(JACKSON_NODE_FACTORY.textNode("AdoDisplayName"))
        .build();

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes()
        .addOperation(operation)
        .build();
 // @formatter:on

    SCIMResponse<?> patchUserResponse = userRequest.patchUser(patchBody, createUserResponse.get().getId());

    assertTrue(patchUserResponse.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), patchUserResponse.getStatusCode());
  }

  @Test
  public void testPatchUserRemoveOperationForSimpleAttributeWithCorrectAttributes204() {
    User testUser = TestData.buildTestUser("testPatchUserRemoveOperationForSimpleAttributeWithCorrectAttributes204");
    SCIMResponse<User> createUserResponse = userRequest.createUser(testUser);

    assertTrue(createUserResponse.isSuccess());
    assertEquals(CREATED.getStatusCode(), createUserResponse.getStatusCode());

    // @formatter:off
    PatchOperation operation = new PatchOperation.Builder()
        .setOp(PatchOperation.Type.REMOVE)
        .setPath(DISPLAY_NAME.scimName())
        .build();

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes()
        .addOperation(operation)
        .build();
    // @formatter:on

    SCIMResponse<?> patchUserResponse = userRequest.patchUser(patchBody, createUserResponse.get().getId());
    assertTrue(patchUserResponse.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), patchUserResponse.getStatusCode());
  }

  @Test
  public void testPatchUserRequestDoesNotContainPatchSchema400() {
 // @formatter:off
    PatchOperation operation = new PatchOperation.Builder()
        .setOp(PatchOperation.Type.ADD)
        .setPath(DISPLAY_NAME.scimName())
        .setValue(JACKSON_NODE_FACTORY.textNode("AdoDisplayName"))
        .build();
    // @formatter:on
    PatchBody patchBody = new PatchBody.Builder().addOperation(operation).setSchemas(Collections.emptySet()).build();

    SCIMResponse<?> response = userRequest.patchUser(patchBody, VALID_UUID);

    assertFalse(response.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());
  }

  @Test
  public void testPatchUserRequestWithNoOperations400() {
    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes().build();

    SCIMResponse<?> response = userRequest.patchUser(patchBody, VALID_UUID);

    assertFalse(response.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());
  }

  @Test
  public void testPatchUserRequestWithNoOperationType400() {
    PatchBody patchBody = TestData.preparePatchBodyWithCustomOperationAndSchema(null, "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User");

    SCIMResponse<?> response = userRequest.patchUser(patchBody, VALID_UUID);

    assertFalse(response.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());
  }

  @Test
  public void testPatchUserRequestWithEmptyOperationType400() {
    PatchBody patchBody = TestData.preparePatchBodyWithCustomOperationAndSchema(new PatchOperation.Type(""),
        "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User");

    SCIMResponse<?> response = userRequest.patchUser(patchBody, VALID_UUID);

    assertFalse(response.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());
  }

  @Test
  public void testPatchUserRequestWithIllegalOperationType400() {
    PatchBody patchBody = TestData.preparePatchBodyWithCustomOperationAndSchema(new PatchOperation.Type("merge"),
        "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User");

    SCIMResponse<?> response = userRequest.patchUser(patchBody, VALID_UUID);

    assertFalse(response.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());
  }

  // ignore until the patch implementation is ready
  @Test
  public void testPatchUserRequestWithMissingPathOnAddOp204() {
    User testUser = TestData.buildTestUser("testPatchUserRequestWithMissingPathOnAddOp200");
    SCIMResponse<User> createUserResponse = userRequest.createUser(testUser);

    assertTrue(createUserResponse.isSuccess());
    assertEquals(CREATED.getStatusCode(), createUserResponse.getStatusCode());

    // @formatter:off
    PatchOperation operation = new PatchOperation.Builder()
        .setOp(PatchOperation.Type.ADD)
        .setValue(JACKSON_NODE_FACTORY.objectNode().set(DISPLAY_NAME.scimName(), JACKSON_NODE_FACTORY.textNode("AdoDisplayName")))
        .build();

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes()
        .addOperation(operation)
        .build();
    // @formatter:on

    SCIMResponse<?> patchUserResponse = userRequest.patchUser(patchBody, createUserResponse.get().getId());

    assertTrue(patchUserResponse.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), patchUserResponse.getStatusCode());
  }

  // ignore until the patch implementation is ready
  @Test
  public void testPatchUserRequestWithMissingPathOnReplaceOp204() {
    User testUser = TestData.buildTestUser("testPatchUserRequestWithMissingPathOnReplaceOp204");
    SCIMResponse<User> createUserResponse = userRequest.createUser(testUser);

    assertTrue(createUserResponse.isSuccess());
    assertEquals(CREATED.getStatusCode(), createUserResponse.getStatusCode());

    // @formatter:off
    PatchOperation operation = new PatchOperation.Builder()
        .setOp(PatchOperation.Type.REPLACE)
        .setValue(JACKSON_NODE_FACTORY.objectNode().set(DISPLAY_NAME.scimName(), JACKSON_NODE_FACTORY.textNode("AdoDisplayName")))
        .build();

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes()
        .addOperation(operation)
        .build();
    // @formatter:on

    SCIMResponse<?> patchUserResponse = userRequest.patchUser(patchBody, createUserResponse.get().getId());

    assertTrue(patchUserResponse.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), patchUserResponse.getStatusCode());
  }

  @Test
  public void testPatchUserRequestWithMissingPathWhenItIsRequired400() {
    // @formatter:off
    PatchOperation operation = new PatchOperation.Builder()
        .setOp(PatchOperation.Type.REMOVE)
        .setValue(JACKSON_NODE_FACTORY.objectNode().set(DISPLAY_NAME.scimName(), JACKSON_NODE_FACTORY.textNode("AdoDisplayName")))
        .build();

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes()
        .addOperation(operation)
        .build();
    // @formatter:on
    SCIMResponse<?> response = userRequest.patchUser(patchBody, VALID_UUID);

    assertFalse(response.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());
  }

  // ignore until the patch implementation is ready
  @Test
  public void testPatchUserRequestWithAddOp204() {
    User testUser = TestData.buildTestUser("testPatchUserRequestWithAddOp204");
    SCIMResponse<User> createUserResponse = userRequest.createUser(testUser);

    assertTrue(createUserResponse.isSuccess());
    assertEquals(CREATED.getStatusCode(), createUserResponse.getStatusCode());

    // @formatter:off
    PatchOperation operation = TestData.setDefaultPatchOperationAttributes(PatchOperation.Type.ADD);

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes()
        .addOperation(operation)
        .build();
    // @formatter:on
    SCIMResponse<?> patchUserResponse = userRequest.patchUser(patchBody, createUserResponse.get().getId());

    assertTrue(patchUserResponse.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), patchUserResponse.getStatusCode());
  }

  // ignore until the patch implementation is ready
  @Test
  public void testPatchUserRequestWithReplaceOp204() {
    User testUser = TestData.buildTestUser("testPatchUserRequestWithReplaceOp204");
    SCIMResponse<User> createUserResponse = userRequest.createUser(testUser);

    assertTrue(createUserResponse.isSuccess());
    assertEquals(CREATED.getStatusCode(), createUserResponse.getStatusCode());

    // @formatter:off
    PatchOperation operation = TestData.setDefaultPatchOperationAttributes(PatchOperation.Type.REPLACE);

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes()
        .addOperation(operation)
        .build();
    // @formatter:on
    SCIMResponse<?> patchUserResponse = userRequest.patchUser(patchBody, createUserResponse.get().getId());

    assertTrue(patchUserResponse.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), patchUserResponse.getStatusCode());
  }

  // ignore until the patch implementation is ready
  @Test
  public void testPatchUserWithInvalidId404() {
    // @formatter:off
    PatchOperation operation = TestData.setDefaultPatchOperationAttributes(PatchOperation.Type.REMOVE);

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes()
        .addOperation(operation)
        .build();
    // @formatter:on
    SCIMResponse<?> response = userRequest.patchUser(patchBody, UUID.randomUUID().toString());

    assertFalse(response.isSuccess());
    assertEquals(NOT_FOUND.getStatusCode(), response.getStatusCode());
  }

  @Test
  public void testPatchUserRequestWithRemoveOp204() {
    User testUser = TestData.buildTestUser("testPatchUserRequestWithRemoveOp204");
    SCIMResponse<User> createUserResponse = userRequest.createUser(testUser);

    assertTrue(createUserResponse.isSuccess());
    assertEquals(CREATED.getStatusCode(), createUserResponse.getStatusCode());

    // @formatter:off
     PatchOperation operation = new PatchOperation.Builder()
        .setOp(PatchOperation.Type.REMOVE)
        .setPath(DISPLAY_NAME.scimName())
        .build();

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes()
        .addOperation(operation)
        .build();
    // @formatter:on
    SCIMResponse<?> patchUserResponse = userRequest.patchUser(patchBody, createUserResponse.get().getId());

    assertTrue(patchUserResponse.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), patchUserResponse.getStatusCode());
  }

  @Test
  public void testPatchUserRequestWithMissingValueInAddOperation400() {
    // @formatter:off
    PatchOperation operation = new PatchOperation.Builder()
        .setOp(PatchOperation.Type.ADD)
        .setPath(DISPLAY_NAME.scimName())
        .build();

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes()
        .addOperation(operation)
        .build();
    // @formatter:on
    SCIMResponse<?> response = userRequest.patchUser(patchBody, VALID_UUID);

    assertFalse(response.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());
  }

  @Test
  public void testPatchUserRequestWithMissingValueInReplaceOperation400() {
    // @formatter:off
    PatchOperation operation = new PatchOperation.Builder()
        .setOp(PatchOperation.Type.REPLACE)
        .setPath(DISPLAY_NAME.scimName())
        .build();

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes()
        .addOperation(operation)
        .build();
    // @formatter:on
    SCIMResponse<?> response = userRequest.patchUser(patchBody, VALID_UUID);

    assertFalse(response.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());
  }

  @Test
  public void testCreateUserWithMissingExtensionSchemaInSchemasAttribute400() {
    EnterpriseExtension extension = new EnterpriseExtension.Builder().setCostCenter("something").build();
    User user = TestData.setAttributesToATestUser("testCreateUserWithMissingExtensionSchemaInSchemasAttribute400").addExtension(extension).build();

    ObjectMapper objectMapper = new ObjectMapper();

    JsonNode userNode = objectMapper.convertValue(user, JsonNode.class);
    ArrayNode schemasNode = (ArrayNode) userNode.get(CoreUserAttributes.SCHEMAS.scimName());

    for (int i = 0; i < schemasNode.size(); i++) {
      if (schemasNode.get(i).asText().equals(EnterpriseExtension.ENTERPRISE_URN)) {
        schemasNode.remove(i);
      }
    }
    Response response = CustomTargetSystemRestClient.INSTANCE.postEntityHttpResponse(USERS, userNode);

    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
  }
}
