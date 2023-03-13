
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
import com.sap.scimono.scim.system.tests.conditions.EnableOnUsersBackendState;
import com.sap.scimono.scim.system.tests.extensions.UserClientScimResponseExtension;
import com.sap.scimono.scim.system.tests.extensions.UserFailSafeClient;
import com.sap.scimono.scim.system.tests.util.CustomTargetSystemRestClient;
import com.sap.scimono.scim.system.tests.util.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.UUID;

import static com.sap.scimono.api.API.USERS;
import static com.sap.scimono.entity.definition.CoreUserAttributes.DISPLAY_NAME;
import static com.sap.scimono.scim.system.tests.conditions.BackendState.EMPTY;
import static com.sap.scimono.scim.system.tests.conditions.BackendState.WITH_INITIAL_EXISTING_RESOURCES;
import static com.sap.scimono.scim.system.tests.util.TestData.JACKSON_NODE_FACTORY;
import static com.sap.scimono.scim.system.tests.util.TestData.buildTestUser;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.CONFLICT;
import static jakarta.ws.rs.core.Response.Status.CREATED;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserOperationsHttpResponseCodeTest extends SCIMHttpResponseCodeTest {
  private static final Logger logger = LoggerFactory.getLogger(UserOperationsHttpResponseCodeTest.class);

  @RegisterExtension 
  UserClientScimResponseExtension resourceAwareUserRequest = UserClientScimResponseExtension.forClearingAfterEachExecutions(userRequest);
  
  private final UserFailSafeClient userFailSafeClient = resourceAwareUserRequest.getFailSafeClient();

  @Test
  @DisplayName("Test Create and Get user and verify Http status code: 200")
  @EnableOnUsersBackendState(state = EMPTY)
  public void testCreateAndGetUser200() {
    String testUserName = "testCreateAndGetUserHTTPResponse";
    SCIMResponse<User> createUserResponse = createUserAndVerifySuccessfulResponse(testUserName);

    logger.info("Fetching user User: {}", testUserName);
    SCIMResponse<User> readUserResponse = resourceAwareUserRequest.readSingleUser(createUserResponse.get().getId());

    assertAll("Verify GET Response", getResponseStatusAssertions(readUserResponse, true, OK));
  }
  
  @Test
  @DisplayName("Test Create user without userName and verify Http status code: 400")
  @EnableOnUsersBackendState(state = EMPTY)
  public void testCreateUserWithoutUserName400() {
    final User userToCreate = userFailSafeClient.create(new User.Builder().setDisplayName("testCreatUserWithoutUserName400").build());

    logger.info("Creating User without userName");
    final SCIMResponse<User> scimResponse = resourceAwareUserRequest.createUser(userToCreate);
    assertAll("Verify Create User Response", getResponseStatusAssertions(scimResponse, false, BAD_REQUEST));
  }

  @Test
  @DisplayName("Test Get user and verify Http status code: 200")
  @EnableOnUsersBackendState(state = WITH_INITIAL_EXISTING_RESOURCES)
  public void testGetUser200() {
    SCIMResponse<PagedByIndexSearchResult<User>> readUsersResponse = getMultipleUsersAndVerifySuccessfulResponse();
    User user = readUsersResponse.get().getResources().stream().findFirst().orElseThrow(IllegalStateException::new);

    logger.info("Fetching user User: {}", user.getUserName());
    SCIMResponse<User> readUserResponse = resourceAwareUserRequest.readSingleUser(user.getId());

    assertAll("Verify GET Response", getResponseStatusAssertions(readUserResponse, true, OK));
  }

  @Test
  @DisplayName("Test Get users with non existing id and verify Http status code: 404")
  public void testGetUserNonExistingId404() {
    logger.info("Fetching User with non existing id");
    SCIMResponse<User> scimResponse = resourceAwareUserRequest.readSingleUser(UUID.randomUUID().toString());

    assertAll("Verify GET Response", getResponseStatusAssertions(scimResponse, false, NOT_FOUND));
  }

  @Test
  @DisplayName("Test Get all users and verify Http status code: 200")
  public void testGetAllUsers200() {
    logger.info("Fetching multiple Users");
    SCIMResponse<PagedByIndexSearchResult<User>> readUsersResponse = resourceAwareUserRequest.readMultipleUsersWithoutPaging();

    assertAll("Verify GET Response", getResponseStatusAssertions(readUsersResponse, true, OK));
  }

  @Test
  @DisplayName("Test Get all users using index paging and verify Http status code: 200")
  public void testGetMultipleUsersWithIndexPaging200() {
    logger.info("Fetching multiple Users");
    SCIMResponse<PagedByIndexSearchResult<User>> readUsersResponse = resourceAwareUserRequest.readMultipleUsers();

    assertAll("Verify GET Response", getResponseStatusAssertions(readUsersResponse, true, OK));
  }

  @Test
  @DisplayName("Test Get all users with # instead of id and verify Http status code: 404")
  public void testGetAllUsersHashTag404() {
    logger.info("Fetching User with #");
    SCIMResponse<User> scimResponse = resourceAwareUserRequest.readSingleUser("#");

    assertAll("Verify GET Response", getResponseStatusAssertions(scimResponse, false, NOT_FOUND));
  }

  @Test
  @DisplayName("Test Get user with email and verify Http status code: 201")
  public void testCreateUserWithEmail201() {
    Email testMail = TestData.buildPersonalEmailWithDefaultAttrs();
    String testUserName = "testCreateUserWithIllegalEmail400";
    User testUser = TestData.setAttributesToATestUser(testUserName).addEmail(testMail).build();

    logger.info("Creating User: {}, with emails", testUserName);
    SCIMResponse<User> scimResponse = resourceAwareUserRequest.createUser(testUser);

    assertAll("Verify Create User Response", getResponseStatusAssertions(scimResponse, true, CREATED));

    logger.info("Fetching User: {}", testUserName);
    User createdUser = userFailSafeClient.getSingle(scimResponse.get().getId());

    assertEquals(1, createdUser.getEmails().size(), "Verify emails size");
  }

  @Test
  @DisplayName("Test create a user with only required attributes and verify Http status code: 201")
  public void testCreateUserWithOnlyRequiredAttributes201(){
    String testUserName = "testCreateUserWithOnlyRequiredAttributes201";
    User testUser = TestData.setAttributesToATestUser(testUserName).build();

    logger.info("Creating User: {}, with username", testUserName);
    SCIMResponse<User> scimResponse = resourceAwareUserRequest.createUser(testUser);

    assertAll("Verify Create User Response", getResponseStatusAssertions(scimResponse, true, CREATED));
  }

  @Test
  @DisplayName("Test Create user with duplicate userName and verify Http status code: 409")
  public void testCreateUsersWithSameUserNames409() {
    String testUserName = "testCreateUsersWithSameUserNames409";
    User testUser = TestData.setAttributesToATestUser(testUserName).build();

    logger.info("Creating User: {}", testUserName);
    SCIMResponse<User> scimResponse = resourceAwareUserRequest.createUser(testUser);
    assertAll("Verify Create User Response", getResponseStatusAssertions(scimResponse, true, CREATED));

    User testUserDuplicate = TestData.setAttributesToATestUser(testUserName).build();
    logger.info("Creating User: {}, again", testUserName);
    SCIMResponse<User> scimResponseForDuplicate = resourceAwareUserRequest.createUser(testUserDuplicate);

    assertAll("Verify Create User Response", getResponseStatusAssertions(scimResponseForDuplicate, false, CONFLICT));
  }

  @Test
  @DisplayName("Test Update user with PUT and verify Http status code: 200")
  public void testUpdateUser200() {
    User createdUser = createUserAndVerifySuccessfulResponse("testUpdateUserHTTPResponse").get();

    User updatedUser = new User.Builder(createdUser).setDisplayName("testUpdateUserHTTPResponseUpdatedUser").setId(createdUser.getId()).build();
    logger.info("Creating User: {}, replacing displayName", createdUser.getUserName());
    SCIMResponse<User> scimResponse = resourceAwareUserRequest.updateUser(updatedUser);

    assertAll("Verify Update User Response", getResponseStatusAssertions(scimResponse, true, OK));
    assertEquals("testUpdateUserHTTPResponseUpdatedUser", scimResponse.get().getDisplayName(), "Verify 'displayName' is updated");
  }

  @Test
  @DisplayName("Test Update user with PUT without id in body and verify Http status code: 200")
  public void testUpdateUserWithoutIdInBody200() {
    User createdUser = createUserAndVerifySuccessfulResponse("testUpdateUserWithoutId200").get();

    User updatedUser = new User.Builder(createdUser).setDisplayName("testUpdateUserHTTPResponseUpdatedUser").build();
    logger.info("Updating User: {}, replacing 'displayName'", createdUser.getUserName());
    Response httpResponse = CustomTargetSystemRestClient.INSTANCE.putEntityHttpResponse(USERS, createdUser.getId(), updatedUser);

    assertEquals(httpResponse.getStatus(), OK.getStatusCode(), "Verify Update User Response");
    assertEquals("testUpdateUserHTTPResponseUpdatedUser", httpResponse.readEntity(User.class).getDisplayName(), "Verify 'displayName' is updated");
  }

  @Test
  @DisplayName("Test Update user with non existing Id with PUT and verify Http status code: 404")
  public void testUpdateUserWithNonExistingId404() {
    String nonExistingUserId = UUID.randomUUID().toString();
    User testUser = TestData.setAttributesToATestUser("testUpdateUserWithNonExistingId404").setId(nonExistingUserId).build();

    logger.info("Updating User with non existing id");
    SCIMResponse<User> scimResponse = resourceAwareUserRequest.updateUser(testUser);

    assertAll("Verify Update User Response", getResponseStatusAssertions(scimResponse, false, NOT_FOUND));
  }

  @Test
  @DisplayName("Test Delete user and verify Http status code: 204")
  public void testDeleteUser204() {
    SCIMResponse<User> createUserResponse = createUserAndVerifySuccessfulResponse("testDeleteUserHTTPResponse");

    logger.info("Deleting User: testDeleteUserHTTPResponse");
    SCIMResponse<Void> deleteUserResponse = resourceAwareUserRequest.deleteUser(createUserResponse.get().getId());

    assertAll("Verify Delete User Response", getResponseStatusAssertions(deleteUserResponse, true, NO_CONTENT));
  }

  @Test
  @DisplayName("Test Delete user with non existing id and verify Http status code: 404")
  public void testDeleteUserWithNonExistingId404() {
    logger.info("Deleting User with non existing Id");
    SCIMResponse<Void> scimResponse = resourceAwareUserRequest.deleteUser(UUID.randomUUID().toString());

    assertAll("Verify Delete User Response", getResponseStatusAssertions(scimResponse, false, NOT_FOUND));
  }

  @Test
  @DisplayName("Test Delete user twice and verify Http status code: 404 on second attempt")
  public void testDeleteUserTwice404() {
    String userName = "testDeleteUserTwice404-User";
    logger.info("Creating User -{}- that will be deleted after that", userName);
    User user = userFailSafeClient.create(buildTestUser(userName));

    logger.info("Deleting User: {}", userName);
    SCIMResponse<Void> firstDeleteAttemptResponse = resourceAwareUserRequest.deleteUser(user.getId());

    logger.info("Deleting User: {}, again", userName);
    SCIMResponse<Void> secondDeleteAttemptResponse = resourceAwareUserRequest.deleteUser(user.getId());

    // @formatter:off
    assertAll("Verify delete group attempts",
        () -> assertAll("Verify Delete first User Response", getResponseStatusAssertions(firstDeleteAttemptResponse, true, NO_CONTENT)),
        () -> assertAll("Verify Delete second User Response", getResponseStatusAssertions(secondDeleteAttemptResponse, false, NOT_FOUND))
    );
    // @formatter:on
  }

  @Test
  @DisplayName("Test Patch User with Add operation for simple attribute and verify Http status code: 204")
  public void testPatchUserAddOperationForSimpleAttributeWithCorrectAttributes204() {
    User createdUser = createUserAndVerifySuccessfulResponse("testPatchUserAddOperationForSimpleAttributeWithCorrectAttributes204").get();

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

    logger.info("Patching User: {}, adding 'displayName'", createdUser.getUserName());
    SCIMResponse<?> patchUserResponse = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());

    assertAll("Verify Patch User Response", getResponseStatusAssertions(patchUserResponse, true, NO_CONTENT));
  }

  @Test
  @DisplayName("Test Patch User with Replace operation for simple attribute and verify Http status code: 204")
  public void testPatchUserReplaceOperationForSimpleAttributeWithCorrectAttributes204() {
    User createdUser = createUserAndVerifySuccessfulResponse("testPatchUserReplaceOperationForSimpleAttributeWithCorrectAttributes204").get();

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

    logger.info("Patching User: {}, replacing 'displayName'", createdUser.getUserName());
    SCIMResponse<?> patchUserResponse = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());

    assertAll("Verify Patch User Response", getResponseStatusAssertions(patchUserResponse, true, NO_CONTENT));
  }

  @Test
  @DisplayName("Test Patch User with Remove operation for simple attribute and verify Http status code: 204")
  public void testPatchUserRemoveOperationForSimpleAttributeWithCorrectAttributes204() {
    User createdUser = createUserAndVerifySuccessfulResponse("testPatchUserRemoveOperationForSimpleAttributeWithCorrectAttributes204").get();

    // @formatter:off
    PatchOperation operation = new PatchOperation.Builder()
        .setOp(PatchOperation.Type.REMOVE)
        .setPath(DISPLAY_NAME.scimName())
        .build();

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes()
        .addOperation(operation)
        .build();
    // @formatter:on

    logger.info("Patching User: {}, removing 'displayName'", createdUser.getUserName());
    SCIMResponse<?> patchUserResponse = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());

    assertAll("Verify Patch User Response", getResponseStatusAssertions(patchUserResponse, true, NO_CONTENT));
  }

  @Test
  @DisplayName("Test Patch User with missing Patch schema in body and verify Http status code: 400")
  public void testPatchUserRequestDoesNotContainPatchSchema400() {
 // @formatter:off
    PatchOperation operation = new PatchOperation.Builder()
        .setOp(PatchOperation.Type.ADD)
        .setPath(DISPLAY_NAME.scimName())
        .setValue(JACKSON_NODE_FACTORY.textNode("AdoDisplayName"))
        .build();
    PatchBody patchBody = new PatchBody.Builder()
        .addOperation(operation)
        .setSchemas(Collections.emptySet())
        .build();
// @formatter:on

    logger.info("Patching User without patch schema");
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, VALID_UUID);

    assertAll("Verify Patch User Response", getResponseStatusAssertions(response, false, BAD_REQUEST));
  }

  @Test
  @DisplayName("Test Patch User with empty Patch operation list and verify Http status code: 400")
  public void testPatchUserRequestWithNoOperations400() {
    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes().build();

    logger.info("Patching User with no operations");
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, VALID_UUID);

    assertAll("Verify Patch User Response", getResponseStatusAssertions(response, false, BAD_REQUEST));
  }

  @Test
  @DisplayName("Test Patch User with no operation type and verify Http status code: 400")
  public void testPatchUserRequestWithNoOperationType400() {
    PatchBody patchBody = TestData.preparePatchBodyWithCustomOperationAndSchema(null, "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User");

    logger.info("Patching User with no operation type");
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, VALID_UUID);

    assertAll("Verify Patch User Response", getResponseStatusAssertions(response, false, BAD_REQUEST));
  }

  @Test
  @DisplayName("Test Patch User with empty operation type and verify Http status code: 400")
  public void testPatchUserRequestWithEmptyOperationType400() {
    PatchBody patchBody = TestData.preparePatchBodyWithCustomOperationAndSchema(new PatchOperation.Type(""),
        "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User");

    logger.info("Patching User with empty operation type");
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, VALID_UUID);

    assertAll("Verify Patch User Response", getResponseStatusAssertions(response, false, BAD_REQUEST));
  }

  @Test
  @DisplayName("Test Patch User with illegal operation type and verify Http status code: 400")
  public void testPatchUserRequestWithIllegalOperationType400() {
    PatchBody patchBody = TestData.preparePatchBodyWithCustomOperationAndSchema(new PatchOperation.Type("merge"),
        "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User");

    logger.info("Patching User with illegal operation type");
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, VALID_UUID);

    assertAll("Verify Patch User Response", getResponseStatusAssertions(response, false, BAD_REQUEST));
  }

  @Test
  @DisplayName("Test Patch User with missing path on Add operation and verify Http status code: 204")
  public void testPatchUserRequestWithMissingPathOnAddOp204() {
    User createdUser = createUserAndVerifySuccessfulResponse("testPatchUserRequestWithMissingPathOnAddOp200").get();

    // @formatter:off
    PatchOperation operation = new PatchOperation.Builder()
        .setOp(PatchOperation.Type.ADD)
        .setValue(JACKSON_NODE_FACTORY.objectNode().set(DISPLAY_NAME.scimName(), JACKSON_NODE_FACTORY.textNode("AdoDisplayName")))
        .build();

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes()
        .addOperation(operation)
        .build();
    // @formatter:on

    logger.info("Patching User: {}, adding new 'displayName'", createdUser.getDisplayName());
    SCIMResponse<?> patchUserResponse = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());

    assertAll("Verify Patch User Response", getResponseStatusAssertions(patchUserResponse, true, NO_CONTENT));
  }

  @Test
  @DisplayName("Test Patch User with missing path on Replace operation and verify Http status code: 204")
  public void testPatchUserRequestWithMissingPathOnReplaceOp204() {
    User createdUser = createUserAndVerifySuccessfulResponse("testPatchUserRequestWithMissingPathOnReplaceOp204").get();

    // @formatter:off
    PatchOperation operation = new PatchOperation.Builder()
        .setOp(PatchOperation.Type.REPLACE)
        .setValue(JACKSON_NODE_FACTORY.objectNode().set(DISPLAY_NAME.scimName(), JACKSON_NODE_FACTORY.textNode("AdoDisplayName")))
        .build();

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes()
        .addOperation(operation)
        .build();
    // @formatter:on

    logger.info("Patching User: {}, replacing 'displayName'", createdUser.getUserName());
    SCIMResponse<?> patchUserResponse = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());

    assertAll("Verify Patch User Response", getResponseStatusAssertions(patchUserResponse, true, NO_CONTENT));
  }

  @Test
  @DisplayName("Test Patch User with missing path when it is required (on Remove operation) operation and verify Http status code: 400")
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

    logger.info("Patching User without operation path on REMOVE operation");
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, VALID_UUID);

    assertAll("Verify Patch User Response", getResponseStatusAssertions(response, false, BAD_REQUEST));
  }

  @Test
  @DisplayName("Test Patch User with Add Operation and verify Http status code: 204")
  public void testPatchUserRequestWithAddOp204() {
    User createdUser = createUserAndVerifySuccessfulResponse("testPatchUserRequestWithAddOp204").get();

    PatchOperation operation = TestData.setDefaultPatchOperationAttributes(PatchOperation.Type.ADD);
    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes().addOperation(operation).build();

    logger.info("Patching User: {}, adding new displayName", createdUser.getUserName());
    SCIMResponse<?> patchUserResponse = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());

    assertAll("Verify Patch User Response", getResponseStatusAssertions(patchUserResponse, true, NO_CONTENT));
  }

  @Test
  @DisplayName("Test Patch User with Replace Operation and verify Http status code: 204")
  public void testPatchUserRequestWithReplaceOp204() {
    User createdUser = createUserAndVerifySuccessfulResponse("testPatchUserRequestWithReplaceOp204").get();

    PatchOperation operation = TestData.setDefaultPatchOperationAttributes(PatchOperation.Type.REPLACE);
    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes().addOperation(operation).build();

    logger.info("Patching User: {}, replacing displayName", createdUser.getUserName());
    SCIMResponse<?> patchUserResponse = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());

    assertAll("Verify Patch User Response", getResponseStatusAssertions(patchUserResponse, true, NO_CONTENT));
  }

  @Test
  @DisplayName("Test Patch User with non existing Id and verify Http status code: 404")
  public void testPatchUserWithNonExistingId404() {
    PatchOperation operation = TestData.setDefaultPatchOperationAttributes(PatchOperation.Type.REMOVE);
    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes().addOperation(operation).build();

    logger.info("Patching User with non existing id");
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, UUID.randomUUID().toString());

    assertAll("Verify Patch User Response", getResponseStatusAssertions(response, false, NOT_FOUND));
  }

  @Test
  @DisplayName("Test Patch User with Remove Operation and verify Http status code: 204")
  public void testPatchUserRequestWithRemoveOp204() {
    User createdUser = createUserAndVerifySuccessfulResponse("testPatchUserRequestWithRemoveOp204").get();

    // @formatter:off
     PatchOperation operation = new PatchOperation.Builder()
        .setOp(PatchOperation.Type.REMOVE)
        .setPath(DISPLAY_NAME.scimName())
        .build();

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes()
        .addOperation(operation)
        .build();
    // @formatter:on

    logger.info("Patching User: {}, removing displayName", createdUser.getUserName());
    SCIMResponse<?> patchUserResponse = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());

    assertAll("Verify Patch User Response", getResponseStatusAssertions(patchUserResponse, true, NO_CONTENT));
  }

  @Test
  @DisplayName("Test Patch User with missing value on Add operation and verify Http status code: 400")
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

    logger.info("Patching User without operation value");
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, VALID_UUID);

    assertAll("Verify Patch User Response", getResponseStatusAssertions(response, false, BAD_REQUEST));
  }

  @Test
  @DisplayName("Test Patch User with missing value on Replace operation and verify Http status code: 400")
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

    logger.info("Patching User without operation value");
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, VALID_UUID);

    assertAll("Verify Patch User Response", getResponseStatusAssertions(response, false, BAD_REQUEST));
  }

  @Test
  @DisplayName("Test Create user with missing extension schema in schemas attribute and verify Http status code: 400")
  public void testCreateUserWithMissingExtensionSchemaInSchemasAttribute400() {
    EnterpriseExtension extension = new EnterpriseExtension.Builder().setCostCenter("something").build();
    String testUserName = "testCreateUserWithMissingExtensionSchemaInSchemasAttribute400";
    User user = TestData.setAttributesToATestUser(testUserName).addExtension(extension).build();

    ObjectMapper objectMapper = new ObjectMapper();

    JsonNode userNode = objectMapper.convertValue(user, JsonNode.class);
    ArrayNode schemasNode = (ArrayNode) userNode.get(CoreUserAttributes.SCHEMAS.scimName());

    for (int i = 0; i < schemasNode.size(); i++) {
      if (schemasNode.get(i).asText().equals(EnterpriseExtension.ENTERPRISE_URN)) {
        schemasNode.remove(i);
      }
    }

    logger.info("Creating User: {}, with missing extension schema in 'schemas' attribute", testUserName);
    Response response = CustomTargetSystemRestClient.INSTANCE.postEntityHttpResponse(USERS, userNode);

    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus(), "Verify status code");
  }

  private SCIMResponse<User> createUserAndVerifySuccessfulResponse(String userName) {
    User userToCreate = TestData.buildTestUser(userName);

    logger.info("Creating User: {}", userName);
    SCIMResponse<User> scimResponse = resourceAwareUserRequest.createUser(userToCreate);
    assertAll("Verify Create User Response", getResponseStatusAssertions(scimResponse, true, CREATED));

    return scimResponse;
  }

  private SCIMResponse<PagedByIndexSearchResult<User>> getMultipleUsersAndVerifySuccessfulResponse() {
    logger.info("Fetching multiple Users");
    SCIMResponse<PagedByIndexSearchResult<User>> readUsersResponse = resourceAwareUserRequest.readMultipleUsersWithoutPaging();
    assertAll("Verify GET Multiple Users Response", getResponseStatusAssertions(readUsersResponse, true, OK));

    return readUsersResponse;
  }
}
