
package com.sap.scimono.scim.system.tests;

import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.MemberRef;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.patch.PatchBody;
import com.sap.scimono.entity.patch.PatchOperation;
import com.sap.scimono.scim.system.tests.util.TestData;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;

import static com.sap.scimono.entity.definition.CoreGroupAttributes.DISPLAY_NAME;
import static com.sap.scimono.scim.system.tests.util.TestData.JACKSON_NODE_FACTORY;
import static com.sap.scimono.scim.system.tests.util.TestData.buildGroup;
import static com.sap.scimono.scim.system.tests.util.TestData.buildTestUser;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GroupOperationsHttpResponseCodeTest extends SCIMHttpResponseCodeTest {
  private static final Logger logger = LoggerFactory.getLogger(GroupOperationsHttpResponseCodeTest.class);

  @Test
  public void testGetGroup200() {
    Group groupWithoutMembers = TestData.buildGroup("testGetGroupHTTPResponse");

    SCIMResponse<Group> scimResponse = groupRequest.createGroup(groupWithoutMembers);

    assertTrue(scimResponse.isSuccess());
    assertEquals(CREATED.getStatusCode(), scimResponse.getStatusCode());

    scimResponse = groupRequest.readSingleGroup(scimResponse.get().getId());
    assertTrue(scimResponse.isSuccess());

    assertEquals(OK.getStatusCode(), scimResponse.getStatusCode());
  }

  @Test
  public void testGetGroupWithIllegalId400() {
    SCIMResponse<Group> scimResponse = groupRequest.readSingleGroup(ILLEGAL_UUID);

    assertFalse(scimResponse.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), scimResponse.getStatusCode());
  }

  @Test
  public void testGetGroupWithNonExistingId400() {
    SCIMResponse<Group> scimResponse = groupRequest.readSingleGroup(UUID.randomUUID().toString());

    assertFalse(scimResponse.isSuccess());
    assertEquals(NOT_FOUND.getStatusCode(), scimResponse.getStatusCode());
  }

  @Test
  public void testCreateGroupWithNonExistingMember() {
    SCIMResponse<Group> response = groupRequest.createGroup(buildGroup("testCreateGroupWithNonExistingMember", UUID.randomUUID().toString()));

    // @formatter:off
    assertAll("Verify error response is received",
        () -> assertFalse(response.isSuccess()),
        () -> assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode()));
    // @formatter:on
  }

  @Test
  public void testGetAllGroups200() {
    Group groupWithoutMembers = TestData.buildGroup("testGetAllGroupsHTTPResponse");

    SCIMResponse<Group> scimResponse = groupRequest.createGroup(groupWithoutMembers);
    assertTrue(scimResponse.isSuccess());
    assertEquals(CREATED.getStatusCode(), scimResponse.getStatusCode());

    SCIMResponse<PagedByIndexSearchResult<Group>> getGroupsResponse = groupRequest.readMultipleGroups();
    assertTrue(getGroupsResponse.isSuccess());
    assertEquals(OK.getStatusCode(), getGroupsResponse.getStatusCode());
  }

  @Test
  public void testGetAllGroupsWithIllegalId400() {
    SCIMResponse<Group> scimResponse = groupRequest.readSingleGroup("#");

    assertFalse(scimResponse.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), scimResponse.getStatusCode());
  }

  @Test
  public void testCreateGroup201() {
    Group groupWithoutMembers = TestData.buildGroup("testCreateGroupHTTPResponse");
    SCIMResponse<Group> scimResponse = groupRequest.createGroup(groupWithoutMembers);

    assertTrue(scimResponse.isSuccess());
    assertEquals(CREATED.getStatusCode(), scimResponse.getStatusCode());
  }

  @Test
  public void testUpdateGroup200() {
    Group parentGroup = TestData.buildGroup("testUpdateGroupHTTPResponse");
    SCIMResponse<Group> groupScimResponse = groupRequest.createGroup(parentGroup);

    assertTrue(groupScimResponse.isSuccess());
    assertEquals(CREATED.getStatusCode(), groupScimResponse.getStatusCode());

    Group createdParentGroup = groupScimResponse.get();

    User testUser = TestData.buildTestUser("testUpdateGroupHTTPResponse");
    SCIMResponse<User> userScimResponse = userRequest.createUser(testUser);

    assertTrue(userScimResponse.isSuccess());
    assertEquals(CREATED.getStatusCode(), userScimResponse.getStatusCode());

    User createdGroupMember = userScimResponse.get();
    assertNotNull(createdGroupMember);
    assertNotNull(createdParentGroup);
    assertNotNull(testUser);
    assertEquals(36, createdGroupMember.getId().length());

    MemberRef groupMember = TestData.buildGroupMemberResourceWithId(createdGroupMember.getId());
    Group updatedGroupWithMember = new Group.Builder(createdParentGroup).setId(createdParentGroup.getId()).addMember(groupMember).build();

    groupScimResponse = groupRequest.updateGroup(updatedGroupWithMember);
    assertTrue(userScimResponse.isSuccess());
    assertEquals(OK.getStatusCode(), groupScimResponse.getStatusCode());
  }

  @Test
  public void testUpdateGroupWithoutIdInBody200() {
    Group groupToCreate = TestData.buildGroup("testUpdateGroupWithoutIdInBody200");
    SCIMResponse<Group> scimResponse = groupRequest.createGroup(groupToCreate);

    assertTrue(scimResponse.isSuccess());
    assertEquals(CREATED.getStatusCode(), scimResponse.getStatusCode());
    Group createdGroup = scimResponse.get();

    String newDisplayName = "testUpdateGroupWithoutIdInBody200-updated";
    Group updatedGroup = new Group.Builder(createdGroup).setId(null).setDisplayName(newDisplayName).setId(createdGroup.getId()).build();

    scimResponse = groupRequest.updateGroup(updatedGroup);

    assertTrue(scimResponse.isSuccess());
    assertEquals(OK.getStatusCode(), scimResponse.getStatusCode());
    assertEquals(newDisplayName, scimResponse.get().getDisplayName());
  }

  @Test
  public void testUpdateGroupWithIllegalId400() {
    Group parentGroup = TestData.buildGroup("testUpdateGroupWithIllegalId400");
    SCIMResponse<Group> groupScimResponse = groupRequest.createGroup(parentGroup);

    assertTrue(groupScimResponse.isSuccess());
    assertEquals(CREATED.getStatusCode(), groupScimResponse.getStatusCode());

    Group createdParentGroup = groupScimResponse.get();

    User testUser = TestData.buildTestUser("testUpdateGroupWithIllegalId400");
    SCIMResponse<User> userScimResponse = userRequest.createUser(testUser);
    assertTrue(userScimResponse.isSuccess());
    assertEquals(CREATED.getStatusCode(), userScimResponse.getStatusCode());

    User createdGroupMember = userScimResponse.get();

    assertNotNull(createdGroupMember);
    assertNotNull(createdParentGroup);
    assertNotNull(testUser);
    assertEquals(36, createdGroupMember.getId().length());

    MemberRef groupMember = TestData.buildGroupMemberResourceWithId(createdGroupMember.getId());
    Group updatedGroupWithMember = new Group.Builder(createdParentGroup).addMember(groupMember).setId(ILLEGAL_UUID).build();

    groupScimResponse = groupRequest.updateGroup(updatedGroupWithMember);

    assertFalse(groupScimResponse.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), groupScimResponse.getStatusCode());
  }

  @Test
  public void testUpdateGroupWithNonExistingId404() {
    String nonExistingId = UUID.randomUUID().toString();
    Group parentGroup = new Group.Builder().setDisplayName("testUpdateGroupWithNonExistingId400").setId(nonExistingId).build();

    SCIMResponse<Group> scimResponse = groupRequest.updateGroup(parentGroup);

    assertFalse(scimResponse.isSuccess());
    assertEquals(NOT_FOUND.getStatusCode(), scimResponse.getStatusCode());
  }

  @Test
  public void testDeleteGroup204() {
    Group groupWithoutMembers = TestData.buildGroup("testDeleteGroupHTTPResponse");
    SCIMResponse<Group> scimResponse = groupRequest.createGroup(groupWithoutMembers);

    assertTrue(scimResponse.isSuccess());
    assertEquals(CREATED.getStatusCode(), scimResponse.getStatusCode());

    SCIMResponse<Void> deleteScimGroupResponse = groupRequest.deleteGroup(scimResponse.get().getId());

    assertTrue(deleteScimGroupResponse.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), deleteScimGroupResponse.getStatusCode());
  }

  @Test
  public void testDeleteGroupWithIllegalId400() {
    SCIMResponse<Void> deleteScimGroupResponse = groupRequest.deleteGroup(ILLEGAL_UUID);

    assertFalse(deleteScimGroupResponse.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), deleteScimGroupResponse.getStatusCode());
  }

  @Test
  public void testDeleteGroupWithNonExistingId404() {
    SCIMResponse<Void> deleteScimGroupResponse = groupRequest.deleteGroup(UUID.randomUUID().toString());

    assertFalse(deleteScimGroupResponse.isSuccess());
    assertEquals(NOT_FOUND.getStatusCode(), deleteScimGroupResponse.getStatusCode());
  }

  @Test
  public void testDeleteGroupTwice404() {
    String displayName = "testDeleteGroupTwice-Group";
    logger.info("Creating Group -{}- that will be deleted after that", displayName);
    Group group = createGroup(buildGroup(displayName));

    SCIMResponse<Void> firstDeleteAttemptResponse = groupRequest.deleteGroup(group.getId());
    SCIMResponse<Void> secondDeleteAttemptResponse = groupRequest.deleteGroup(group.getId());

    // @formatter:off
    assertAll("Verify delete group attempts",
        () -> assertTrue(firstDeleteAttemptResponse.isSuccess(), "Verify first delete attempt is successful"),
        () -> assertEquals(NO_CONTENT.getStatusCode(), firstDeleteAttemptResponse.getStatusCode(), "Verify correct response code"),
        () -> assertFalse(secondDeleteAttemptResponse.isSuccess(), "Verify second delete attempt is failure"),
        () -> assertEquals(NOT_FOUND.getStatusCode(), secondDeleteAttemptResponse.getStatusCode(), "Verify correct response code"));
    // @formatter:on
  }

  @Test
  public void testDeleteGroupMembersStillExist() {
    logger.info("Creating User -testDeleteGroupMembersStillExist-User- who will be used as a member");
    User user = createUser(buildTestUser("testDeleteGroupMembersStillExist-User"));

    String displayName = "testDeleteGroupMembersStillExist-Group";
    logger.info("Creating Group -{}- that will be deleted after that", displayName);

    Group group = createGroup(buildGroup(displayName, user.getId()));
    SCIMResponse<Void> deleteGroupResponse = groupRequest.deleteGroup(group.getId());
    SCIMResponse<User> getUserResponse = userRequest.readSingleUser(user.getId());

    // @formatter:off
    assertAll("Verify SCIM responses",
        () -> assertTrue(deleteGroupResponse.isSuccess(), "Verify Group is successfully deleted"),
        () -> assertEquals(NO_CONTENT.getStatusCode(), deleteGroupResponse.getStatusCode(), "Verify DELETE Group response code"),
        () -> assertTrue(getUserResponse.isSuccess(), "Verify GET User response is successful"),
        () -> assertEquals(CREATED.getStatusCode(), getUserResponse.getStatusCode(), "Verify GET User response code"));
    // @formatter:on
  }

  @Test
  public void testPatchGroupAddOperationForSimpleAttributeWithCorrectAttributes204() {
    Group testGroup = TestData.buildGroup("testPatchGroupAddOperationForSimpleAttributeWithCorrectAttributes204");
    SCIMResponse<Group> createGroupResponse = groupRequest.createGroup(testGroup);

    assertTrue(createGroupResponse.isSuccess());
    assertEquals(CREATED.getStatusCode(), createGroupResponse.getStatusCode());
    Group createdGroup = createGroupResponse.get();

    // @formatter:off
    PatchOperation operation = new PatchOperation.Builder().setOp(PatchOperation.Type.ADD).setPath(DISPLAY_NAME.scimName())
        .setValue(JACKSON_NODE_FACTORY.textNode("AdoDisplayName")).build();

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes().addOperation(operation).build();
    // @formatter:on

    SCIMResponse<?> patchResponse = groupRequest.patchGroup(patchBody, createdGroup.getId());
    assertTrue(patchResponse.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), patchResponse.getStatusCode());
  }

  @Test
  public void testPatchGroupReplaceOperationForSimpleAttributeWithCorrectAttributes204() {
    Group testGroup = TestData.buildGroup("testPatchGroupReplaceOperationForSimpleAttributeWithCorrectAttributes204");
    SCIMResponse<Group> createGroupResponse = groupRequest.createGroup(testGroup);

    assertTrue(createGroupResponse.isSuccess());
    assertEquals(CREATED.getStatusCode(), createGroupResponse.getStatusCode());

    Group createdGroup = createGroupResponse.get();
    // @formatter:off
    PatchOperation operation = new PatchOperation.Builder().setOp(PatchOperation.Type.REPLACE).setPath(DISPLAY_NAME.scimName())
        .setValue(JACKSON_NODE_FACTORY.textNode("AdoDisplayName")).build();

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes().addOperation(operation).build();
    // @formatter:on

    SCIMResponse<?> patchResponse = groupRequest.patchGroup(patchBody, createdGroup.getId());

    assertTrue(patchResponse.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), patchResponse.getStatusCode());
  }

  @Test
  public void testPatchGroupRemoveOperationForSimpleAttributeWithCorrectAttributes204() {
    Group testGroup = TestData.buildGroup("testPatchGroupRemoveOperationForSimpleAttributeWithCorrectAttributes204");
    SCIMResponse<Group> createGroupResponse = groupRequest.createGroup(testGroup);

    assertTrue(createGroupResponse.isSuccess());
    assertEquals(CREATED.getStatusCode(), createGroupResponse.getStatusCode());

    Group createdGroup = createGroupResponse.get();
    // @formatter:off
    PatchOperation operation = new PatchOperation.Builder().setOp(PatchOperation.Type.REMOVE).setPath(DISPLAY_NAME.scimName()).build();

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes().addOperation(operation).build();
    // @formatter:on
    SCIMResponse<?> patchResponse = groupRequest.patchGroup(patchBody, createdGroup.getId());

    assertTrue(patchResponse.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), patchResponse.getStatusCode());
  }

  @Test
  public void testPatchGroupRequestDoesNotContainPatchSchema400() {
    // @formatter:off
    PatchOperation operation = new PatchOperation.Builder()
        .setOp(PatchOperation.Type.ADD)
        .setPath(DISPLAY_NAME.scimName())
        .setValue(JACKSON_NODE_FACTORY.textNode("AdoDisplayName")).build();
    // @formatter:on
    PatchBody patchBody = new PatchBody.Builder().addOperation(operation).setSchemas(Collections.emptySet()).build();
    SCIMResponse<?> response = groupRequest.patchGroup(patchBody, VALID_UUID);

    assertFalse(response.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());
  }

  @Test
  public void testPatchGroupRequestWithWrongPatchSchema400() {
    // @formatter:off
    PatchOperation operation = new PatchOperation.Builder().setOp(PatchOperation.Type.ADD).setPath(DISPLAY_NAME.scimName())
        .setValue(JACKSON_NODE_FACTORY.textNode("AdoDisplayName")).build();

    PatchBody patchBody = new PatchBody.Builder().setSchemas(new HashSet<>(Collections.singletonList("IllegalPatchSchema"))).addOperation(operation).build();
    // @formatter:on

    SCIMResponse<?> response = groupRequest.patchGroup(patchBody, VALID_UUID);

    assertFalse(response.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());
  }

  @Test
  public void testPatchGroupRequestWithNoOperations400() {
    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes().build();
    SCIMResponse<?> response = groupRequest.patchGroup(patchBody, VALID_UUID);

    assertFalse(response.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());
  }

  @Test
  public void testPatchGroupRequestWithInvalidOperationType400() {
    // @formatter:off
    PatchOperation operation = new PatchOperation.Builder().setOp(new PatchOperation.Type("invalid")).setPath(DISPLAY_NAME.scimName())
        .setValue(JACKSON_NODE_FACTORY.textNode("AdoDisplayName")).build();

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes().addOperation(operation).build();
    // @formatter:on
    SCIMResponse<?> response = groupRequest.patchGroup(patchBody, VALID_UUID);

    assertFalse(response.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());
  }

  @Test
  public void testPatchGroupRequestWithMissingPathWhenItIsRequired400() {
    // @formatter:off
    PatchOperation operation = new PatchOperation.Builder().setOp(PatchOperation.Type.REMOVE)
        .setValue(JACKSON_NODE_FACTORY.textNode("AdoDisplayName")).build();

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes().addOperation(operation).build();
    // @formatter:on
    SCIMResponse<?> response = groupRequest.patchGroup(patchBody, VALID_UUID);

    assertFalse(response.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());
  }

  @Test
  public void testPatchGroupRequestWithMissingValueInAddOperation400() {
    // @formatter:off
    PatchOperation operation = new PatchOperation.Builder().setOp(PatchOperation.Type.ADD).setPath(DISPLAY_NAME.scimName()).build();

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes().addOperation(operation).build();
    // @formatter:on
    SCIMResponse<?> response = groupRequest.patchGroup(patchBody, VALID_UUID);

    assertFalse(response.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());
  }

  @Test
  public void testPatchGroupRequestWithMissingValueInReplaceOperation400() {
    // @formatter:off
    PatchOperation operation = new PatchOperation.Builder().setOp(PatchOperation.Type.REPLACE).setPath(DISPLAY_NAME.scimName()).build();

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes().addOperation(operation).build();
    // @formatter:on
    SCIMResponse<?> response = groupRequest.patchGroup(patchBody, VALID_UUID);

    assertFalse(response.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());
  }

  @Test
  public void testPatchGroupWithInvalidId() {
    // @formatter:off
    PatchOperation operation = TestData.setDefaultPatchOperationAttributes(PatchOperation.Type.REMOVE);

    PatchBody patchBody = TestData.setDefaultPatchBodyAttributes().addOperation(operation).build();
    // @formatter:on
    SCIMResponse<?> response = groupRequest.patchGroup(patchBody, UUID.randomUUID().toString());

    assertFalse(response.isSuccess());
    assertEquals(NOT_FOUND.getStatusCode(), response.getStatusCode());
  }
}
