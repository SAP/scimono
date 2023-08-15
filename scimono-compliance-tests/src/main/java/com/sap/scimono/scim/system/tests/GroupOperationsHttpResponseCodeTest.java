
package com.sap.scimono.scim.system.tests;

import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.MemberRef;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.patch.PatchBody;
import com.sap.scimono.entity.patch.PatchOperation;
import com.sap.scimono.scim.system.tests.conditions.EnableOnGroupsBackendState;
import com.sap.scimono.scim.system.tests.extensions.GroupClientScimResponseExtension;
import com.sap.scimono.scim.system.tests.extensions.GroupFailSafeClient;
import com.sap.scimono.scim.system.tests.extensions.UserClientScimResponseExtension;
import com.sap.scimono.scim.system.tests.extensions.UserFailSafeClient;
import com.sap.scimono.scim.system.tests.util.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;

import static com.sap.scimono.entity.definition.CoreGroupAttributes.DISPLAY_NAME;
import static com.sap.scimono.scim.system.tests.conditions.BackendState.WITH_INITIAL_EXISTING_RESOURCES;
import static com.sap.scimono.scim.system.tests.util.TestData.JACKSON_NODE_FACTORY;
import static com.sap.scimono.scim.system.tests.util.TestData.buildGroup;
import static com.sap.scimono.scim.system.tests.util.TestData.buildTestUser;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.CREATED;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GroupOperationsHttpResponseCodeTest extends SCIMHttpResponseCodeTest {
  private static final Logger logger = LoggerFactory.getLogger(GroupOperationsHttpResponseCodeTest.class);

  @RegisterExtension 
  UserClientScimResponseExtension resourceAwareUserRequest = UserClientScimResponseExtension.forClearingAfterEachExecutions(userRequest);

  @RegisterExtension 
  GroupClientScimResponseExtension resourceAwareGroupRequest = GroupClientScimResponseExtension.forClearingAfterEachExecutions(groupRequest);
  
  private final GroupFailSafeClient groupFailSafeClient = resourceAwareGroupRequest.getFailSafeClient();
  private final UserFailSafeClient userFailSafeClient = resourceAwareUserRequest.getFailSafeClient();

  @Test
  @DisplayName("Test Create and Get group and verify Http status code: 200")
  public void testCreateAndGetGroup200() {
    SCIMResponse<Group> scimResponse = createGroupAndVerifySuccessfulResponse("testGetGroupHTTPResponse");

    logger.info("Fetching Group: testGetGroupHTTPResponse");
    scimResponse = resourceAwareGroupRequest.readSingleGroup(scimResponse.get().getId());
    assertTrue(scimResponse.isSuccess());

    assertAll("Verify GET Response", getResponseStatusAssertions(scimResponse, true, OK));
  }

  @Test
  @DisplayName("Test Get group and verify Http status code: 200")
  @EnableOnGroupsBackendState(state = WITH_INITIAL_EXISTING_RESOURCES)
  public void testGetGroup200() {
    SCIMResponse<PagedByIndexSearchResult<Group>> readGroupsResponse = getMultipleGroupsAndVerifySuccessfulResponse();
    Group group = readGroupsResponse.get().getResources().stream().findFirst().orElseThrow(IllegalStateException::new);

    logger.info("Fetching Group: testGetGroupHTTPResponse");
    SCIMResponse<Group> scimResponse = resourceAwareGroupRequest.readSingleGroup(group.getId());
    assertTrue(scimResponse.isSuccess());

    assertAll("Verify GET Response", getResponseStatusAssertions(scimResponse, true, OK));
  }

  @Test
  @DisplayName("Test Get group with non existing id and verify Http status code: 404")
  public void testGetGroupWithNonExistingId404() {
    logger.info("Fetching Group with non existing id");
    SCIMResponse<Group> scimResponse = resourceAwareGroupRequest.readSingleGroup(UUID.randomUUID().toString());

    assertAll("Verify GET Response", getResponseStatusAssertions(scimResponse, false, NOT_FOUND));
  }

  @Test
  @DisplayName("Test Create group with non existing member and verify Http status code: 404")
  public void testCreateGroupWithNonExistingMember404() {
    logger.info("Fetching Group with non existing member");
    SCIMResponse<Group> response = resourceAwareGroupRequest.createGroup(buildGroup("testCreateGroupWithNonExistingMember", UUID.randomUUID().toString()));

    assertAll("Verify GET Response", getResponseStatusAssertions(response, false, NOT_FOUND));
  }

  @Test
  @DisplayName("Test Get multiple groups and verify Http status code: 200")
  public void testGetAllGroups200() {
    createGroupAndVerifySuccessfulResponse("testGetAllGroupsHTTPResponse");

    logger.info("Fetching multiple Groups");
    SCIMResponse<PagedByIndexSearchResult<Group>> getGroupsResponse = resourceAwareGroupRequest.readMultipleGroups();
    assertAll("Verify GET Response", getResponseStatusAssertions(getGroupsResponse, true, OK));
  }

  @Test
  @DisplayName("Test Create group and verify Http status code: 201")
  public void testCreateGroup201() {
    createGroupAndVerifySuccessfulResponse("testCreateGroupHTTPResponse");
  }

  @Test
  @DisplayName("Test Update group displayName with PUT and verify Http status code: 400")
  public void testUpdateGroupDisplayName200() {
    String displayName = "testUpdateGroupDsiplayNameHTTPResponse";
    SCIMResponse<Group> scimResponse = createGroupAndVerifySuccessfulResponse(displayName);

    logger.info("Updating Group: {}", displayName);
    scimResponse = resourceAwareGroupRequest.updateGroup(new Group.Builder(scimResponse.get()).setDisplayName(displayName + "-new").build());

    assertAll("Verify Update Group Response", getResponseStatusAssertions(scimResponse, true, OK));
  }

  @Test
  @DisplayName("Test Update group with PUT and verify Http status code: 200")
  public void testUpdateGroup200() {
    String displayName = "testUpdateGroupHTTPResponse";
    SCIMResponse<Group> groupScimResponse = createGroupAndVerifySuccessfulResponse(displayName);

    Group createdParentGroup = groupScimResponse.get();

    String testUserName = "testUpdateGroupHTTPResponse-User";
    User testUser = TestData.buildTestUser(testUserName);

    logger.info("Creating User: {}", testUserName);
    SCIMResponse<User> userScimResponse = resourceAwareUserRequest.createUser(testUser);
    assertAll("Verify Create User Response", getResponseStatusAssertions(userScimResponse, true, CREATED));

    MemberRef groupMember = TestData.buildGroupMemberResourceWithId(userScimResponse.get().getId());
    Group updatedGroupWithMember = new Group.Builder(createdParentGroup).setId(createdParentGroup.getId()).addMember(groupMember).build();

    logger.info("Updating Group: {}, adding new member", displayName);
    groupScimResponse = resourceAwareGroupRequest.updateGroup(updatedGroupWithMember);

    assertAll("Verify Update Group Response", getResponseStatusAssertions(groupScimResponse, true, OK));
  }

  @Test
  @DisplayName("Test Get group with # instead of id and verify Http status code: 400")
  public void testUpdateGroupWithoutIdInBody200() {
    Group createdGroup = createGroupAndVerifySuccessfulResponse("testUpdateGroupWithoutIdInBody200").get();

    String newDisplayName = "testUpdateGroupWithoutIdInBody200-updated";
    Group updatedGroup = new Group.Builder(createdGroup).setId(null).setDisplayName(newDisplayName).setId(createdGroup.getId()).build();

    logger.info("Updating Group: {}", createdGroup.getDisplayName());
    SCIMResponse<Group> scimResponse = resourceAwareGroupRequest.updateGroup(updatedGroup);

    assertAll("Verify Update Group Response", getResponseStatusAssertions(scimResponse, true, OK));
    assertEquals(newDisplayName, scimResponse.get().getDisplayName(), "Verify 'displayName'");
  }

  @Test
  @DisplayName("Test Update Group without id in body and verify Http status code: 400")
  public void testUpdateGroupWithIllegalId400() {
    Group createdParentGroup = createGroupAndVerifySuccessfulResponse("testUpdateGroupWithIllegalId400").get();

    String testUserName = "testUpdateGroupWithIllegalId400-User";
    User testUser = TestData.buildTestUser(testUserName);

    logger.info("Creating User: {}", testUserName);
    SCIMResponse<User> userScimResponse = resourceAwareUserRequest.createUser(testUser);
    assertAll("Verify Create User Response", getResponseStatusAssertions(userScimResponse, true, CREATED));

    MemberRef groupMember = TestData.buildGroupMemberResourceWithId(userScimResponse.get().getId());
    Group updatedGroupWithMember = new Group.Builder(createdParentGroup).addMember(groupMember).setId(ILLEGAL_UUID).build();

    logger.info("Updating Group: {}", createdParentGroup.getDisplayName());
    SCIMResponse<Group> groupScimResponse = resourceAwareGroupRequest.updateGroup(updatedGroupWithMember);

    assertAll("Verify Update Group Response", getResponseStatusAssertions(groupScimResponse, false, BAD_REQUEST));
  }

  @Test
  @DisplayName("Test Update group with non existing id and verify Http status code: 404")
  public void testUpdateGroupWithNonExistingId404() {
    String nonExistingId = UUID.randomUUID().toString();
    Group parentGroup = new Group.Builder().setDisplayName("testUpdateGroupWithNonExistingId400").setId(nonExistingId).build();

    logger.info("Updating Group with non-existing Id");
    SCIMResponse<Group> scimResponse = resourceAwareGroupRequest.updateGroup(parentGroup);
    assertAll("Verify Update Group Response", getResponseStatusAssertions(scimResponse, false, NOT_FOUND));
  }

  @Test
  @DisplayName("Test Delete group and verify Http status code: 204")
  public void testDeleteGroup204() {
    Group createdGroup = createGroupAndVerifySuccessfulResponse("testDeleteGroupHTTPResponse").get();

    logger.info("Deleting group: {}", createdGroup.getDisplayName());
    SCIMResponse<Void> deleteScimGroupResponse = resourceAwareGroupRequest.deleteGroup(createdGroup.getId());

    assertAll("Verify Delete Group Response", getResponseStatusAssertions(deleteScimGroupResponse, true, NO_CONTENT));
  }

  @Test
  @DisplayName("Test Delete group with non existing id and verify Http status code: 404")
  public void testDeleteGroupWithNonExistingId404() {
    logger.info("Deleting Group with non existing id");
    SCIMResponse<Void> deleteScimGroupResponse = resourceAwareGroupRequest.deleteGroup(UUID.randomUUID().toString());

    assertAll("Verify Delete Group Response", getResponseStatusAssertions(deleteScimGroupResponse, false, NOT_FOUND));
  }

  @Test
  @DisplayName("Test Delete deleted group and verify Http status code: 404")
  public void testDeleteGroupTwice404() {
    String displayName = "testDeleteGroupTwice-Group";
    logger.info("Creating Group -{}- that will be deleted after that", displayName);
    Group group = groupFailSafeClient.create(buildGroup(displayName));

    SCIMResponse<Void> firstDeleteAttemptResponse = resourceAwareGroupRequest.deleteGroup(group.getId());
    SCIMResponse<Void> secondDeleteAttemptResponse = resourceAwareGroupRequest.deleteGroup(group.getId());

    // @formatter:off
    assertAll("Verify delete group attempts",
        () -> assertAll("Verify Delete first Group Response", getResponseStatusAssertions(firstDeleteAttemptResponse, true, NO_CONTENT)),
        () -> assertAll("Verify Delete second Group Response", getResponseStatusAssertions(secondDeleteAttemptResponse, false, NOT_FOUND))
    );
    // @formatter:on
  }

  @Test
  @DisplayName("Test Delete group and verify members still exist and Http status code: 400")
  public void testDeleteGroupMembersStillExist() {
    logger.info("Creating User -testDeleteGroupMembersStillExist-User- who will be used as a member");
    User user = userFailSafeClient.create(buildTestUser("testDeleteGroupMembersStillExist-User"));

    String displayName = "testDeleteGroupMembersStillExist-Group";
    logger.info("Creating Group -{}- that will be deleted after that", displayName);
    Group group = groupFailSafeClient.create(buildGroup(displayName, user.getId()));

    logger.info("Deleting Group: {}", group.getDisplayName());
    SCIMResponse<Void> deleteGroupResponse = resourceAwareGroupRequest.deleteGroup(group.getId());

    logger.info("Fetching User: {}", user.getUserName());
    SCIMResponse<User> getUserResponse = resourceAwareUserRequest.readSingleUser(user.getId());

    // @formatter:off
    assertAll("Verify SCIM responses",
        () -> assertAll("Verify Delete Group Response", getResponseStatusAssertions(deleteGroupResponse, true, NO_CONTENT)),
        () -> assertAll("Verify GET User Response", getResponseStatusAssertions(getUserResponse, true, OK))
    );
    // @formatter:on
  }

  @Test
  @DisplayName("Test Patch group with Add operation for simple attribute and verify Http status code: 204")
  public void testPatchGroupAddOperationForSimpleAttributeWithCorrectAttributes204() {
    Group createdGroup = createGroupAndVerifySuccessfulResponse("testPatchGroupAddOperationForSimpleAttributeWithCorrectAttributes204").get();

    // @formatter:off
    PatchOperation operation = new PatchOperation.Builder().setOp(PatchOperation.Type.ADD).setPath(DISPLAY_NAME.scimName())
        .setValue(JACKSON_NODE_FACTORY.textNode("AdoDisplayName")).build();

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes().addOperation(operation).build();
    // @formatter:on

    logger.info("Patching Group: {}, adding new 'displayName'", createdGroup.getDisplayName());
    SCIMResponse<?> patchResponse = resourceAwareGroupRequest.patchGroup(patchBody, createdGroup.getId());
    assertAll("Verify Patch Group Response", getResponseStatusAssertions(patchResponse, true, NO_CONTENT));
  }

  @Test
  @DisplayName("Test Patch group with Replace operation for simple attribute and verify Http status code: 204")
  public void testPatchGroupReplaceOperationForSimpleAttributeWithCorrectAttributes204() {
    Group createdGroup = createGroupAndVerifySuccessfulResponse("testPatchGroupReplaceOperationForSimpleAttributeWithCorrectAttributes204").get();
    // @formatter:off
    PatchOperation operation = new PatchOperation.Builder().setOp(PatchOperation.Type.REPLACE).setPath(DISPLAY_NAME.scimName())
        .setValue(JACKSON_NODE_FACTORY.textNode("AdoDisplayName")).build();

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes().addOperation(operation).build();
    // @formatter:on

    logger.info("Patching Group: {}, replacing 'displayName'", createdGroup.getDisplayName());
    SCIMResponse<?> patchResponse = resourceAwareGroupRequest.patchGroup(patchBody, createdGroup.getId());

    assertAll("Verify Patch Group Response", getResponseStatusAssertions(patchResponse, true, NO_CONTENT));
  }

  @Test
  @DisplayName("Test Patch group with Remove operation for simple attribute and verify Http status code: 204")
  public void testPatchGroupRemoveOperationForSimpleAttributeWithCorrectAttributes204() {
    // @formatter:off
    Group createdGroup = createGroupAndVerifySuccessfulResponse("testPatchGroupRemoveOperationForSimpleAttributeWithCorrectAttributes204")
        .get();

    PatchOperation operation = new PatchOperation.Builder().setOp(PatchOperation.Type.REMOVE).setPath(DISPLAY_NAME.scimName()).build();
    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes().addOperation(operation).build();

    // @formatter:on

    logger.info("Patching Group: {}, removing 'displayName'", createdGroup.getDisplayName());
    SCIMResponse<?> patchResponse = resourceAwareGroupRequest.patchGroup(patchBody, createdGroup.getId());

    assertAll("Verify Patch Group Response", getResponseStatusAssertions(patchResponse, true, NO_CONTENT));
  }

  @Test
  @DisplayName("Test Patch group with missing Patch schema in body and verify Http status code: 400")
  public void testPatchGroupRequestDoesNotContainPatchSchema400() {
    // @formatter:off
    PatchOperation operation = new PatchOperation.Builder()
        .setOp(PatchOperation.Type.ADD)
        .setPath(DISPLAY_NAME.scimName())
        .setValue(JACKSON_NODE_FACTORY.textNode("AdoDisplayName")).build();
    // @formatter:on

    PatchBody patchBody = new PatchBody.Builder().addOperation(operation).setSchemas(Collections.emptySet()).build();

    logger.info("Patching Group without Patch Schema");
    SCIMResponse<?> response = resourceAwareGroupRequest.patchGroup(patchBody, VALID_UUID);
    assertAll("Verify Patch Group Response", getResponseStatusAssertions(response, false, BAD_REQUEST));
  }

  @Test
  @DisplayName("Test Patch group with wrong Patch schema in body and verify Http status code: 400")
  public void testPatchGroupRequestWithWrongPatchSchema400() {
    // @formatter:off
    PatchOperation operation = new PatchOperation.Builder().setOp(PatchOperation.Type.ADD).setPath(DISPLAY_NAME.scimName())
        .setValue(JACKSON_NODE_FACTORY.textNode("AdoDisplayName")).build();

    PatchBody patchBody = new PatchBody.Builder().setSchemas(new HashSet<>(Collections.singletonList("IllegalPatchSchema"))).addOperation(operation).build();
    // @formatter:on

    logger.info("Patching Group with wrong Patch Schema");
    SCIMResponse<?> response = resourceAwareGroupRequest.patchGroup(patchBody, VALID_UUID);
    assertAll("Verify Patch Group Response", getResponseStatusAssertions(response, false, BAD_REQUEST));
  }

  @Test
  @DisplayName("Test Patch group with empty Patch operation list and verify Http status code: 400")
  public void testPatchGroupRequestWithNoOperations400() {
    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes().build();

    logger.info("Patching Group with no patch operations");
    SCIMResponse<?> response = resourceAwareGroupRequest.patchGroup(patchBody, VALID_UUID);

    assertAll("Verify Patch Group Response", getResponseStatusAssertions(response, false, BAD_REQUEST));
  }

  @Test
  @DisplayName("Test Patch group with invalid Patch operation type and verify Http status code: 400")
  public void testPatchGroupRequestWithInvalidOperationType400() {
    // @formatter:off
    PatchOperation operation = new PatchOperation.Builder()
        .setOp(new PatchOperation.Type("invalid"))
        .setPath(DISPLAY_NAME.scimName())
        .setValue(JACKSON_NODE_FACTORY.textNode("AdoDisplayName"))
        .build();

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes()
        .addOperation(operation)
        .build();
    // @formatter:on

    logger.info("Patching Group with invalid operation type");
    SCIMResponse<?> response = resourceAwareGroupRequest.patchGroup(patchBody, VALID_UUID);

    assertAll("Verify Patch Group Response", getResponseStatusAssertions(response, false, BAD_REQUEST));
  }

  @Test
  @DisplayName("Test Patch group with missing path when it is required (for Remove operation) and verify Http status code: 400")
  public void testPatchGroupRequestWithMissingPathWhenItIsRequired400() {
    // @formatter:off
    PatchOperation operation = new PatchOperation.Builder()
        .setOp(PatchOperation.Type.REMOVE)
        .setValue(JACKSON_NODE_FACTORY.textNode("AdoDisplayName"))
        .build();

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes()
        .addOperation(operation)
        .build();
    // @formatter:on

    logger.info("Patching Group with missing path on remove operation");
    SCIMResponse<?> response = resourceAwareGroupRequest.patchGroup(patchBody, VALID_UUID);

    assertAll("Verify Patch Group Response", getResponseStatusAssertions(response, false, BAD_REQUEST));
  }

  @Test
  @DisplayName("Test Patch group with missing value on Add operation and verify Http status code: 400")
  public void testPatchGroupRequestWithMissingValueInAddOperation400() {
    // @formatter:off
    PatchOperation operation = new PatchOperation.Builder()
        .setOp(PatchOperation.Type.ADD)
        .setPath(DISPLAY_NAME.scimName())
        .build();

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes()
        .addOperation(operation)
        .build();
    // @formatter:on

    logger.info("Patching Group with missing value on add operation");
    SCIMResponse<?> response = resourceAwareGroupRequest.patchGroup(patchBody, VALID_UUID);

    assertAll("Verify Patch Group Response", getResponseStatusAssertions(response, false, BAD_REQUEST));
  }

  @Test
  @DisplayName("Test Patch group with missing value on Replace operation and verify Http status code: 400")
  public void testPatchGroupRequestWithMissingValueInReplaceOperation400() {
    // @formatter:off
    PatchOperation operation = new PatchOperation.Builder()
        .setOp(PatchOperation.Type.REPLACE)
        .setPath(DISPLAY_NAME.scimName())
        .build();

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes()
        .addOperation(operation)
        .build();
    // @formatter:on

    logger.info("Patching Group with missing value on replace operation");
    SCIMResponse<?> response = resourceAwareGroupRequest.patchGroup(patchBody, VALID_UUID);

    assertAll("Verify Patch Group Response", getResponseStatusAssertions(response, false, BAD_REQUEST));
  }

  @Test
  @DisplayName("Test Patch group with non existing id and verify Http status code: 404")
  public void testPatchGroupWithNonExistingId404() {
    // @formatter:off
    PatchOperation operation = TestData.setDefaultPatchOperationAttributes(PatchOperation.Type.REMOVE);

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes()
        .addOperation(operation)
        .build();
    // @formatter:on

    logger.info("Patching Group with non existing id");
    SCIMResponse<?> response = resourceAwareGroupRequest.patchGroup(patchBody, UUID.randomUUID().toString());
    assertAll("Verify Patch Group Response", getResponseStatusAssertions(response, false, NOT_FOUND));
  }

  @Test
  @DisplayName("Test Create a group without displayname and verify Http status code: 400")
  public void testCreateGroupWithoutDisplayName400() {
    Group.Builder groupToCreate = new Group.Builder();

    logger.info("Creating Group without displayname");
    SCIMResponse<Group> scimResponse = resourceAwareGroupRequest.createGroup(groupToCreate.build());

    assertAll("Verify Create Group Response", getResponseStatusAssertions(scimResponse, false, BAD_REQUEST));
  }

  private SCIMResponse<Group> createGroupAndVerifySuccessfulResponse(String displayName) {
    Group groupToCreate = TestData.buildGroup(displayName);

    logger.info("Creating Group: {}", displayName);
    SCIMResponse<Group> scimResponse = resourceAwareGroupRequest.createGroup(groupToCreate);
    assertAll("Verify Create Group Response", getResponseStatusAssertions(scimResponse, true, CREATED));

    return scimResponse;
  }

  private SCIMResponse<PagedByIndexSearchResult<Group>> getMultipleGroupsAndVerifySuccessfulResponse() {
    logger.info("Fetching multiple Groups");
    SCIMResponse<PagedByIndexSearchResult<Group>> readGroupsResponse = resourceAwareGroupRequest.readMultipleGroupsWithoutPaging();
    assertAll("Verify GET Multiple Groups Response", getResponseStatusAssertions(readGroupsResponse, true, OK));

    return readGroupsResponse;
  }
}
