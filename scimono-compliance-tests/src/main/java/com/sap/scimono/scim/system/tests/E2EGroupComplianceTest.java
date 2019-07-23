
package com.sap.scimono.scim.system.tests;

import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.MemberRef;
import com.sap.scimono.entity.Meta;
import com.sap.scimono.entity.Resource;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.definition.CoreGroupAttributes;
import com.sap.scimono.entity.definition.CoreUserAttributes;
import com.sap.scimono.entity.paging.PagedByIdentitySearchResult;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.patch.PatchBody;
import com.sap.scimono.entity.patch.PatchOperation;
import com.sap.scimono.scim.system.tests.extensions.GroupFailSafeClient;
import com.sap.scimono.scim.system.tests.extensions.ResourceClientExtension;
import com.sap.scimono.scim.system.tests.extensions.UserFailsSafeClient;
import com.sap.scimono.scim.system.tests.util.CustomTargetSystemRestClient;
import com.sap.scimono.scim.system.tests.util.TestData;
import com.sap.scimono.scim.system.tests.util.TestReporter;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.function.Executable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.sap.scimono.api.API.COUNT_PARAM;
import static com.sap.scimono.api.API.GROUPS;
import static com.sap.scimono.client.query.ResourcePageQuery.identityPageQuery;
import static com.sap.scimono.client.query.ResourcePageQuery.indexPageQuery;
import static com.sap.scimono.entity.definition.CoreGroupAttributes.MEMBERS;
import static com.sap.scimono.entity.paging.PagedByIdentitySearchResult.PAGINATION_BY_ID_END_PARAM;
import static com.sap.scimono.entity.paging.PagedByIdentitySearchResult.PAGINATION_BY_ID_START_PARAM;
import static com.sap.scimono.entity.paging.PagedByIndexSearchResult.DEFAULT_COUNT;
import static com.sap.scimono.scim.system.tests.util.CustomTargetSystemRestClient.GROUP_LIST_RESPONSE_TYPE_INDEX_PAGING;
import static com.sap.scimono.scim.system.tests.util.TestData.buildGroup;
import static com.sap.scimono.scim.system.tests.util.TestData.buildGroupMemberResourceWithId;
import static com.sap.scimono.scim.system.tests.util.TestData.buildTestUser;
import static com.sap.scimono.scim.system.tests.util.TestData.extractGroupIds;
import static com.sap.scimono.scim.system.tests.util.TestUtil.constructResourceLocation;
import static java.util.Collections.singletonMap;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(TestReporter.class)
public class E2EGroupComplianceTest extends SCIMComplianceTest {
  private static final Logger logger = LoggerFactory.getLogger(E2EGroupComplianceTest.class);
  private static final int RESOURCES_PER_PAGE = 1000;
  private static final int ASSIGNMENT_UPDATE_DELAY = 30;
  private static final int SECONDS = 1000;

  @RegisterExtension
  private ResourceClientExtension<Group> groupClientExtension = new ResourceClientExtension<>(new GroupFailSafeClient(groupRequest));

  @RegisterExtension
  private ResourceClientExtension<User> userClientExtension = new ResourceClientExtension<>(new UserFailsSafeClient(userRequest));

  @Test
  void testGetGroupEmptyList() {
    int startIndex = 1;
    PagedByIndexSearchResult<Group> groupsPage = groupClientExtension.getPagedByIndex(startIndex, 100);

    // @formatter:off
    assertAll("Verify empty list response is received",
        () -> assertEquals(0, groupsPage.getTotalResults(), "Verify 'totalResults' is 0"),
        () -> assertEquals(0, groupsPage.getItemsPerPage(), "Verify 'itemsPerPage' is 0"),
        () -> assertEquals(startIndex, groupsPage.getStartIndex(), "Verify startIndex is equal to the one provided in request starIndex param"),
        () -> assertTrue(groupsPage.getResources().isEmpty(), "Verify 'Resources' is empty list")
    );
    // @formatter:on
  }

  @TestFactory
  public Collection<DynamicTest> testGetMultipleGroups() {
    int createdGroupsCount = 3;
    String commonGroupDisplayName = "testGetMultipleGroups";

    return Arrays.asList(
        getMultipleGroupsDynamicTest("Test get multiple groups without members", () -> {
          return createMultipleGroups(commonGroupDisplayName + "-Without-Members", createdGroupsCount);
        }),
        getMultipleGroupsDynamicTest("Test get multiple groups with members", () -> {
          String userNameOfMember = commonGroupDisplayName + "-UserMember";
          return createMultipleGroups(commonGroupDisplayName + "-With-Members", userNameOfMember, createdGroupsCount);
        })
    );
  }

  private Executable getMemberRefAssertions(Collection<MemberRef> expectedMembers, Collection<MemberRef> actualMembers) {
    return () -> assertAll("Verify memberRefs are same ",
        expectedMembers.stream()
            .map(MemberRef::getValue)
            .map(expMemberVal ->
              () -> assertTrue(actualMembers.stream().map(MemberRef::getValue).anyMatch(expMemberVal::equals), "Verify member existence"))
        );
  }

  @Test
  public void testGetGroupsRequiredAttributeAreFetched() {
    String testGroupName = "testGetGroupsRequiredAttributeAreFetched";
    logger.info("Creating Group -{}-", testGroupName);
    Group group = groupClientExtension.create(buildGroup(testGroupName));

    logger.info("Getting Group -{}-", testGroupName);
    Group fetchedGroup = groupClientExtension.getSingle(group.getId());

    // @formatter:off
    assertAll("Verify required attributes are present it GET response",
        () -> assertEquals(testGroupName, fetchedGroup.getDisplayName(), "Verify 'displayName' exist and is equal to the one from CREATE response"),
        () -> assertEquals(group.getId(), fetchedGroup.getId(), "Verify 'Id' exist and is equal to the one from CREATE response"),
        getMetaAssertions(fetchedGroup, Group.RESOURCE_TYPE_GROUP)
    );
    // @formatter:on
  }

  @TestFactory
  public Collection<DynamicTest> testGetSingleGroup() {
    int createdGroupsCount = 1;
    String commonGroupDisplayName = "testGetMultipleGroups";

    return Arrays.asList(
        getSingleGroupDynamicTest("Test get multiple groups without members", () -> {
          return createMultipleGroups(commonGroupDisplayName + "-Without-Members", createdGroupsCount).get(0);
        }),
        getSingleGroupDynamicTest("Test get multiple groups with members", () -> {
          String userNameOfMember = commonGroupDisplayName + "-UserMember";
          return createMultipleGroups(commonGroupDisplayName + "-With-Members", userNameOfMember, createdGroupsCount).get(0);
        })
    );
  }

  @Test
  public void testCreateEmptyGroup() {
    String displayName = "testCreateEmptyGroup";
    Group group = groupClientExtension.create(buildGroup(displayName));

    // @formatter:off
    assertAll("Verifying attributes in response...",
        () -> assertEquals(displayName, group.getDisplayName(), "verify displayName"),
        () -> assertTrue(group.getMembers().isEmpty(), "verify list of members is empty"),
        () -> assertNotNull(group.getMeta(), "Verify 'meta' exist"),
        getMetaAssertions(group, Group.RESOURCE_TYPE_GROUP));
    // @formatter:on
  }

  @Test
  public void testCreateGroupWithSameDisplayName() {
    String displayName = "testCreateGroupWithSameDisplayName";

    logger.info("Creating first Group with dispayName -{}-", displayName);
    Group firstGroup = groupClientExtension.create(buildGroup(displayName));
    logger.info("Creating second Group with dispayName -{}-", displayName);
    Group secondGroup = groupClientExtension.create(buildGroup(displayName));

    assertNotEquals(firstGroup.getId(), secondGroup.getId(), "Verify Identifiers from both POST responses are different");

    logger.info("Fetching first Group with dispayName -{}-", displayName);
    Group firstGroupFetched = groupClientExtension.getSingle(firstGroup.getId());
    logger.info("Fetching second Group with dispayName -{}-", displayName);
    Group secondGroupFetched = groupClientExtension.getSingle(secondGroup.getId());

    assertNotEquals(firstGroupFetched.getId(), secondGroupFetched.getId(), "Verify Identifiers from both GET responses are different");
  }

  @Test
  public void testCreateGroupWithUserMember() {
    logger.info("Creating User -testCreateGroupWithUserMember-User- who will be used as a member");
    User userMember = userClientExtension.create(buildTestUser("testCreateGroupWithUserMember-User"));

    String testGroupName = "testCreateGroupWithUserMember";
    logger.info("Creating Group -{}- with members", testGroupName);
    Group createdTestGroupWithMembers = groupClientExtension.create(buildGroup(testGroupName, userMember.getId()));

    // @formatter:off
    assertAll("Verify group attributes from response",
        () -> assertNotNull(createdTestGroupWithMembers),
        getMetaAssertions(createdTestGroupWithMembers, Group.RESOURCE_TYPE_GROUP),
        () -> assertEquals(testGroupName, createdTestGroupWithMembers.getDisplayName(), "verify group displayName"),
        () -> assertEquals(1, createdTestGroupWithMembers.getMembers().size(), "verify members size"),
        getMembersAssertions(Collections.singletonList(userMember), createdTestGroupWithMembers.getMembers()));
    // @formatter:on
  }

  @Test
  public void testCreateGroupWithGroupMember() {
    logger.info("Creating Group -testCreateGroupWithGroupMember-Group- who will be used as a member");
    Group groupMember = groupClientExtension.create(buildGroup("testCreateGroupWithGroupMember-Group"));

    String testGroupName = "testCreateGroupWithGroupMember-Group";
    logger.info("Creating Group -{}- with members", testGroupName);
    Group createdTestGroupWithMembers = groupClientExtension.create(buildGroup(testGroupName, groupMember.getId()));

    // @formatter:off
    assertAll("Verify group attributes from response",
        () -> assertNotNull(createdTestGroupWithMembers),
        getMetaAssertions(createdTestGroupWithMembers, Group.RESOURCE_TYPE_GROUP),
        () -> assertEquals(testGroupName, createdTestGroupWithMembers.getDisplayName(), "verify group displayName"),
        () -> assertEquals(2, createdTestGroupWithMembers.getMembers().size(), "verify members size"),
        getMembersAssertions(Collections.singletonList(groupMember), createdTestGroupWithMembers.getMembers()));
    // @formatter:on
  }

  @Test
  public void testCreateGroupWithUserAndGroupMembers() {
    logger.info("Creating User -testCreateGroupWithUserAndGroupMembers-User- who will be used as a member");
    User userMember = userClientExtension.create(buildTestUser("testCreateGroupWithUserAndGroupMembers-User"));

    logger.info("Creating Group -testCreateGroupWithUserAndGroupMembers-Group- who will be used as a member");
    Group groupMember = groupClientExtension.create(buildGroup("testCreateGroupWithUserAndGroupMembers-Group"));

    String testGroupName = "testCreateGroupWithUserAndGroupMembers";
    logger.info("Creating Group -{}- with members", testGroupName);
    Group createdTestGroupWithMembers = groupClientExtension.create(buildGroup(testGroupName, userMember.getId(), groupMember.getId()));

    // @formatter:off
    assertAll("Verify group attributes from response",
        () -> assertNotNull(createdTestGroupWithMembers),
        getMetaAssertions(createdTestGroupWithMembers, Group.RESOURCE_TYPE_GROUP),
        () -> assertEquals(testGroupName, createdTestGroupWithMembers.getDisplayName(), "verify group displayName"),
        () -> assertEquals(2, createdTestGroupWithMembers.getMembers().size(), "verify members size"),
        getMembersAssertions(Arrays.asList(userMember, groupMember), createdTestGroupWithMembers.getMembers()));
    // @formatter:on
  }

  @Test
  public void testUpdateGroupDisplayName() {
    String displayName = "testUpdateGroupDisplayName";

    logger.info("Creating Group -{}- with members", displayName);
    Group group = groupClientExtension.create(buildGroup(displayName));

    String newDisplayName = displayName + "-new";
    logger.info("Updating Group -{}- with new displayName -{}-", displayName, newDisplayName);
    Group updatedGroup = groupClientExtension.update(group.getId(), new Group.Builder().setDisplayName(newDisplayName).build());

    // @formatter:off
    assertAll("Verify Group response attributes",
        () -> assertEquals(newDisplayName, updatedGroup.getDisplayName(), "Verify displayName is changed"),
        () -> assertTrue(updatedGroup.getMembers().isEmpty(), "Verify update response does not contain any members"),
        getMetaAssertions(group, Group.RESOURCE_TYPE_GROUP),
        () -> assertNotEquals(group.getMeta().getLastModified(), updatedGroup.getMeta().getLastModified(), "Verify meta 'lastModified' is updated")
    );
    // @formatter:on
  }


  @Test
  public void testUpdateGroupRemovingMembers() {
    logger.info("Creating User -testUpdateGroupRemovingMembers-User- who will be used as a member");
    User userMember = userClientExtension.create(buildTestUser("testUpdateGroupRemovingMembers-User"));

    String groupDisplayName = "testUpdateGroupRemovingMembers-GroupWithMembers";
    logger.info("Creating Group -{}- with members", groupDisplayName);
    Group createdTestGroupWithMembers = groupClientExtension.create(buildGroup(groupDisplayName, userMember.getId()));

    Group groupForUpdate = new Group.Builder(createdTestGroupWithMembers).removeMembers().build();
    logger.info("Updating Group -testUpdateGroupRemovingMembers-GroupWithMembers- and remove members");
    Group updatedGroupWithMembers = groupClientExtension.update(groupForUpdate.getId(), groupForUpdate);

    // @formatter:off
    assertAll("Verify Group response attributes",
        () -> assertEquals(createdTestGroupWithMembers.getDisplayName(), updatedGroupWithMembers.getDisplayName(), "Verify displayName was not changed"),
        () -> assertTrue(updatedGroupWithMembers.getMembers().isEmpty(), "Verify members does not exist"));
    // @formatter:on
  }

  @Test
  public void testUpdateGroupRemovingOnlyOneMember() {
    logger.info("Creating User -testUpdateGroupRemovingOnlyOneMember-FirstUser- who will be used as a member");
    User firstUserMember = userClientExtension.create(buildTestUser("testUpdateGroupRemovingOnlyOneMember-FirstUser"));

    logger.info("Creating User -testUpdateGroupRemovingOnlyOneMember-SecondUser- who will be used as a member");
    User secondUserMember = userClientExtension.create(buildTestUser("testUpdateGroupRemovingOnlyOneMember-SecondUser"));

    String groupDisplayName = "testUpdateGroupRemovingOnlyOneMember-GroupWithMembers";
    logger.info("Creating Group -{}- with members", groupDisplayName);
    Group createdTestGroupWithMembers = groupClientExtension.create(buildGroup(groupDisplayName, firstUserMember.getId()));

    Group groupForUpdate = new Group.Builder(createdTestGroupWithMembers)
        .removeMembers()
        .addMember(buildGroupMemberResourceWithId(firstUserMember.getId()))
        .build();

    logger.info("Updating Group -testUpdateGroupRemovingOnlyOneMember-GroupWithMembers- and remove members");
    Group updatedGroupWithMembers = groupClientExtension.update(groupForUpdate.getId(), groupForUpdate);

    // @formatter:off
    assertAll("Verify Group response attributes",
        () -> assertEquals(createdTestGroupWithMembers.getDisplayName(), updatedGroupWithMembers.getDisplayName(), "Verify displayName was not changed"),
        () -> assertEquals(1, updatedGroupWithMembers.getMembers().size(), "Verify members does not exist"),
        () -> assertFalse(isResourceExistAsMemberInGroup(secondUserMember, updatedGroupWithMembers), "Verify secondUser is not present in members response"));
    // @formatter:on
  }

  @Test
  public void testUpdateGroupAddAdditionalMember() {
    logger.info("Creating User -testUpdateGroupAddAdditionalMember-User- who will be used as a member");
    User userMember = userClientExtension.create(buildTestUser("testUpdateGroupAddAdditionalMember-User"));

    String groupDisplayName = "testUpdateGroupAddAdditionalMember-GroupWithMembers";
    logger.info("Creating Group -{}- with members", groupDisplayName);
    Group createdTestGroupWithMembers = groupClientExtension.create(buildGroup(groupDisplayName, userMember.getId()));

    logger.info("Creating User -testUpdateGroupAddAdditionalMember-SecondMember- who will be used as a member");
    User secondUserMember = userClientExtension.create(buildTestUser("testUpdateGroupAddAdditionalMember-SecondMember"));
    MemberRef groupMemberUser = buildGroupMemberResourceWithId(secondUserMember.getId());
    Group groupForUpdate = new Group.Builder(createdTestGroupWithMembers).addMember(groupMemberUser).build();

    logger.info("Updating Group -{}- and add additional member", groupDisplayName);
    Group updatedGroupWithMembers = groupClientExtension.update(groupForUpdate.getId(), groupForUpdate);

    // @formatter:off
    assertAll("Verify Group response attributes",
        () -> assertEquals(createdTestGroupWithMembers.getDisplayName(), updatedGroupWithMembers.getDisplayName(), "Verify displayName was not changed"),
        () -> assertEquals(createdTestGroupWithMembers.getMembers().size() + 1, updatedGroupWithMembers.getMembers().size(), "Verify members size is incremented"),
        getMembersAssertions(Arrays.asList(userMember, secondUserMember), updatedGroupWithMembers.getMembers()));
    // @formatter:on
  }

  @Test
  public void testUpdateGroupReplaceMembers() {
    logger.info("Creating User -testUpdateGroupReplaceMembers-FirstUser- who will be used as a member");
    User firstUserMember = userClientExtension.create(buildTestUser("testUpdateGroupReplaceMembers-FirstUser"));

    String groupDisplayName = "testUpdateGroupReplaceMembers-GroupWithMembers";
    logger.info("Creating Group -{}- with members", groupDisplayName);
    Group createdTestGroupWithMembers = groupClientExtension.create(buildGroup(groupDisplayName, firstUserMember.getId()));

    logger.info("Creating User -testUpdateGroupReplaceMembers-SecondUser- who will be used as a member in update");
    User secondUserMember = userClientExtension.create(buildTestUser("testUpdateGroupReplaceMembers-SecondUser"));

    MemberRef groupMemberUser = buildGroupMemberResourceWithId(secondUserMember.getId());
    Group groupForUpdate = new Group.Builder(createdTestGroupWithMembers).removeMembers().addMember(groupMemberUser).build();

    logger.info("Updating Group -{}- and replace members", groupDisplayName);
    Group updatedGroupWithMembers = groupClientExtension.update(groupForUpdate.getId(), groupForUpdate);

    // @formatter:off
    assertAll("Verify Group response attributes",
        () -> assertEquals(createdTestGroupWithMembers.getDisplayName(), updatedGroupWithMembers.getDisplayName(), "Verify displayName was not changed"),
        () -> assertEquals(groupForUpdate.getMembers().size(), updatedGroupWithMembers.getMembers().size()),
        getMembersAssertions(Collections.singletonList(secondUserMember), updatedGroupWithMembers.getMembers()),
        () -> assertFalse(isResourceExistAsMemberInGroup(firstUserMember, updatedGroupWithMembers), "verify old members are removed"));
    // @formatter:on
  }

  @Test
  public void testUpdateMembersAndGet() {
    logger.info("Creating User -testUpdateMembersAndGet-FirstUser- who will be used as a member");
    User user = userClientExtension.create(buildTestUser("testUpdateGroupReplaceMembers-FirstUser"));

    String groupDisplayName = "testUpdateMembersAndGet-Group";
    logger.info("Creating Group -{}-", groupDisplayName);
    Group createdGroup = groupClientExtension.create(buildGroup(groupDisplayName));

    logger.info("Updating Group -{}- and addting additional members", groupDisplayName);
    groupClientExtension.update(createdGroup.getId(), new Group.Builder(createdGroup).addMember(buildGroupMemberResourceWithId(user.getId())).build());

    Group updatedGroup = groupClientExtension.getSingle(createdGroup.getId());
    // @formatter:off
    assertAll("Verify Group response attributes",
        () -> assertEquals(createdGroup.getDisplayName(), updatedGroup.getDisplayName(), "Verify displayName was not changed"),
        () -> assertEquals(1, updatedGroup.getMembers().size(), "Verify member is added"),
        getMembersAssertions(Collections.singletonList(user), updatedGroup.getMembers()));
    // @formatter:on
  }

  @Test
  public void testCreateGroupWith500Members() {
    Group groupWithManyMembers = groupClientExtension.create(buildGroup("test2CreateGroupWithMembers500"));

    List<MemberRef> members = new ArrayList<>(500);
    for (int i = 1; i <= 500; i++) {
      User tempUser = userClientExtension.create(buildTestUser(String.format("usercopy%d", i)));
      members.add(new MemberRef.Builder().setValue(tempUser.getId()).build());
    }

    Group groupWithAddedMembers = new Group.Builder(groupWithManyMembers).addMembers(members).build();
    Group updatedgroupWithManyMembers = groupClientExtension.update(groupWithAddedMembers.getId(), groupWithAddedMembers);

    assertNotNull(updatedgroupWithManyMembers);
    assertEquals(500, updatedgroupWithManyMembers.getMembers().size());
  }

  @Test
  public void testDeleteGroupWithoutMembers() {
    String groupDisplayName = "testDeleteGroupWithoutMembers-Group";
    logger.info("Creating Group -{}-", groupDisplayName);
    Group createdGroup = groupClientExtension.create(buildGroup(groupDisplayName));

    logger.info("Deleting Group -{}-", groupDisplayName);
    SCIMResponse<Void> deleteGroupResponse = groupRequest.deleteGroup(createdGroup.getId());
    assertTrue(deleteGroupResponse.isSuccess(), "SCIM remove request failed - Group id: " + createdGroup.getId());
    groupClientExtension.removeManagedResource(createdGroup.getId());

    logger.info("Reading Group -{}-", groupDisplayName);
    SCIMResponse<?> readGroupResponse = groupRequest.readSingleGroup(createdGroup.getId());
    assertEquals(NOT_FOUND.getStatusCode(), readGroupResponse.getStatusCode(), "Verify response code");
  }

  @Test
  public void testDeleteGroupWithUserMembers() {
    logger.info("Creating User -testDeleteGroupWithUserMembers-User- who will be used as a member");
    User user = userClientExtension.create(buildTestUser("testDeleteGroupWithUserMembers-User"));

    String groupDisplayName = "TestGroupWithMembersForDeletion-Group";
    Group testGroup = buildGroup(groupDisplayName, user.getId());

    logger.info("Creating Group -{}- with members", groupDisplayName);
    Group groupForDeletion = groupClientExtension.create(testGroup);
    assertNotNull(groupForDeletion);

    logger.info("Deleting Group -{}-", groupDisplayName);
    SCIMResponse<Void> deleteGroupResponse = groupRequest.deleteGroup(groupForDeletion.getId());
    assertTrue(deleteGroupResponse.isSuccess(), "SCIM remove request failed - Group id: " + groupForDeletion.getId());
    groupClientExtension.removeManagedResource(groupForDeletion.getId());

    logger.info("Reading Group -{}-", groupDisplayName);
    SCIMResponse<?> readGroupResponse = groupRequest.readSingleGroup(groupForDeletion.getId());
    assertEquals(NOT_FOUND.getStatusCode(), readGroupResponse.getStatusCode(), "Verify response code");
  }

  @Test
  public void testDeleteGroupWithGroupMembers() {
    logger.info("Creating User -testDeleteGroupWithGroupMembers-User- which will be used as a member");
    Group groupMember = groupClientExtension.create(buildGroup("testDeleteGroupWithGroupMembers-User"));

    String groupDisplayName = "testDeleteGroupWithGroupMembers-Group";
    Group testGroup = buildGroup(groupDisplayName, groupMember.getId());

    logger.info("Creating Group -{}- with members", groupDisplayName);
    Group groupForDeletion = groupClientExtension.create(testGroup);
    assertNotNull(groupForDeletion);

    logger.info("Deleting Group -{}-", groupDisplayName);
    SCIMResponse<Void> deleteGroupResponse = groupRequest.deleteGroup(groupForDeletion.getId());
    assertTrue(deleteGroupResponse.isSuccess(), "SCIM remove request failed - Group id: " + groupForDeletion.getId());
    groupClientExtension.removeManagedResource(groupForDeletion.getId());

    logger.info("Reading Group -{}-", groupDisplayName);
    SCIMResponse<?> readGroupResponse = groupRequest.readSingleGroup(groupForDeletion.getId());
    assertEquals(NOT_FOUND.getStatusCode(), readGroupResponse.getStatusCode(), "Verify response code");
  }

  @Test
  public void testCreateGroupWithMemberAndVisitMemberReference() {
    logger.info("Creating User -testCreateGroupWithMemberAndVisitMemberReference-User- who will be used as a member");
    User user = userClientExtension.create(buildTestUser("testCreateGroupWithMemberAndVisitMemberReference-User"));

    String testGroupName = "testCreateGroupWithMemberAndVisitMemberReference-Group";
    logger.info("Creating Group -{}- with members", testGroupName);
    Group createdTestGroupWithMembers = groupClientExtension.create(buildGroup(testGroupName, user.getId()));

    assertNotNull(createdTestGroupWithMembers);

    logger.info("Reading Group -{}-", testGroupName);
    Group createdGroupWithMemberLocation = groupClientExtension.getSingle(createdTestGroupWithMembers.getId());

    String memberRef = createdGroupWithMemberLocation.getMembers().iterator().next().getReference();
    assertTrue(memberRef.endsWith("Users/" + user.getId()), "Verify member '$ref'");

    logger.info("Reading User -{}-", user.getUserName());
    SCIMResponse<User> scimResponse = configureScimClientService(memberRef.substring(0, memberRef.lastIndexOf("Users/" + user.getId())))
        .buildUserRequest()
        .readSingleUser(user.getId());

    assertTrue(scimResponse.isSuccess());
    assertNotNull(scimResponse.get());
  }

  @Test
  public void testDeleteGroupAndGet() {
    String displayName = "testDeleteGroupAndGet-Group";
    logger.info("Creating Group -{}- that will be deleted after that", displayName);
    Group group = groupClientExtension.create(buildGroup(displayName));

    groupRequest.deleteGroup(group.getId());
    SCIMResponse<Group> readResponse = groupRequest.readSingleGroup(group.getId());
    assertFalse(readResponse.isSuccess(), "Verify that group is deleted and cannot be read");
  }

  @Test
  public void testGetGroupsAfterGroupMemberDeletion() {
    User createdTestUser = userClientExtension.create(buildTestUser("testUserForGroupMemDel"));

    String testGroupName = "testGroupForGroupMemDel";
    Group createdTestGroupWithMember = groupClientExtension.create(buildGroup(testGroupName, createdTestUser.getId()));

    assertTrue(isGroupIdPresentInGroupResponse(createdTestGroupWithMember));

    userClientExtension.delete(createdTestUser.getId());

    assertTrue(isGroupIdPresentInGroupResponse(createdTestGroupWithMember));
  }

  @Test
  public void testDeleteGroupMembersUpdate() throws InterruptedException {
    User createdTestUser2 = userClientExtension.create(buildTestUser("testDeleteGroupMembersUpdateUsr1"));
    Group createdMemberTestGroup = groupClientExtension.create(buildGroup("testDeleteGroupMembersUpdateGrp1"));
    User createdTestUser3 = userClientExtension.create(buildTestUser("testDeleteGroupMembersUpdateUsr2"));

    String testGroupName = "testDeleteGroupMembersUpdateGrp2";
    Group createdTestGroupWithMembers = groupClientExtension.create(
        buildGroup(testGroupName, createdTestUser2.getId(), createdMemberTestGroup.getId(), createdTestUser3.getId()));

    assertEquals(3, groupClientExtension.getSingle(createdTestGroupWithMembers.getId()).getMembers().size());
    assertTrue(isGroupIdPresentInGroupResponse(createdTestGroupWithMembers));

    userClientExtension.delete(createdTestUser2.getId());

    SCIMResponse<?> deleteGroupResponse = groupRequest.deleteGroup(createdMemberTestGroup.getId());
    assertEquals(SC_NO_CONTENT, deleteGroupResponse.getStatusCode());
    groupClientExtension.removeManagedResource(createdMemberTestGroup.getId());

    userClientExtension.delete(createdTestUser3.getId());

    int i = 0;
    int groupSize = 100;
    do {
      groupSize = groupClientExtension.getSingle(createdTestGroupWithMembers.getId()).getMembers().size();
      if (groupSize == 0) {
        break;
      }
      Thread.sleep(ASSIGNMENT_UPDATE_DELAY * SECONDS);
      i++;
    } while (i < 20);

    assertEquals(0, groupClientExtension.getSingle(createdTestGroupWithMembers.getId()).getMembers().size());
  }

  @Test
  public void testAddGroupMemberWithPATCH() {
    User originalUser = userClientExtension.create(buildTestUser("testAddGroupMemberWithPATCH-originalUser"));
    Group createdMemberTestGroup = groupClientExtension.create(buildGroup("testAddGroupMemberWithPATCH-GR", originalUser.getId()));

    User patchedUser = userClientExtension.create(buildTestUser("testAddGroupMemberWithPATCH-patchedUser"));
    MemberRef userMemberRef = new MemberRef.Builder().setValue(patchedUser.getId()).setType(MemberRef.Type.USER).build();
    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.ADD, MEMBERS.scimName(), TestData.buildMultivaluedJSONNode(userMemberRef));

    groupClientExtension.patch(createdMemberTestGroup.getId(), patchBody);

    Group patchedGroup = groupClientExtension.getSingle(createdMemberTestGroup.getId());
    assertEquals(2, patchedGroup.getMembers().size());
    assertTrue(patchedGroup.getMembers().stream().map(MemberRef::getValue).anyMatch(patchedUser.getId()::equals),
        "Group does not contain member with id: " + patchedUser.getId());
  }

  @Test
  public void testReplaceGroupMemberWithPATCH() {
    User originalUser = userClientExtension.create(buildTestUser("testReplaceGroupMemberWithPATCH-originalUser"));
    Group createdMemberTestGroup = groupClientExtension.create(buildGroup("testReplaceGroupMemberWithPATCH-GR", originalUser.getId()));

    User patchedUser = userClientExtension.create(buildTestUser("testReplaceGroupMemberWithPATCH-patchedUser"));
    MemberRef userMemberRef = new MemberRef.Builder().setValue(patchedUser.getId()).setType(MemberRef.Type.USER).build();
    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, MEMBERS.scimName(), TestData.buildMultivaluedJSONNode(userMemberRef));

    groupClientExtension.patch(createdMemberTestGroup.getId(), patchBody);

    Group patchedGroup = groupClientExtension.getSingle(createdMemberTestGroup.getId());
    assertEquals(1, patchedGroup.getMembers().size());
    assertTrue(patchedGroup.getMembers().stream().map(MemberRef::getValue).anyMatch(patchedUser.getId()::equals),
        "Group does not contain member with id: " + patchedUser.getId());
  }

  @Test
  public void testRemoveGroupMemberWithPATCH() {
    User originalUser = userClientExtension.create(buildTestUser("testRemoveGroupMemberWithPATCH-originalUser"));
    Group createdMemberTestGroup = groupClientExtension.create(buildGroup("testRemoveGroupMemberWithPATCH-GR", originalUser.getId()));

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REMOVE, MEMBERS.scimName(), null);

    groupClientExtension.patch(createdMemberTestGroup.getId(), patchBody);

    Group patchedGroup = groupClientExtension.getSingle(createdMemberTestGroup.getId());
    assertEquals(0, patchedGroup.getMembers().size());
  }

  @Test
  public void testGroupMemberDisplayNameUpdateWithPUTRequest() throws InterruptedException {
    String updateMemberDisplayNameTestUser = "testUserForDisplayNameUpdateWithPUT";
    User createdTestUser = userClientExtension.create(buildTestUser(updateMemberDisplayNameTestUser));

    String updateMemberDisplayNameTestGroup = "testGroupForDisplayNameUpdateWithPUT";
    Group createdMemberTestGroup = groupClientExtension.create(buildGroup(updateMemberDisplayNameTestGroup));

    String testGroupName = "testGroupContainingUpdatedWithPUTMembers";
    Group createdTestGroupWithMembers = groupClientExtension.create(buildGroup(testGroupName, createdTestUser.getId(), createdMemberTestGroup.getId()));

    assertEquals(2, groupClientExtension.getSingle(createdTestGroupWithMembers.getId()).getMembers().size());
    assertTrue(isGroupIdPresentInGroupResponse(createdTestGroupWithMembers));

    String updateMemberDisplayNameTestUserNewName = updateMemberDisplayNameTestUser.concat("v22");
    User updatedDisplayNameMemberUser = new User.Builder(createdTestUser).setDisplayName(updateMemberDisplayNameTestUserNewName).build();
    userClientExtension.update(createdTestUser.getId(), updatedDisplayNameMemberUser);

    String updateMemberDisplayNameTestGroupNewName = updateMemberDisplayNameTestGroup.concat("v22");
    Group updatedDisplayNameMemberGroup = new Group.Builder(createdMemberTestGroup).setDisplayName(updateMemberDisplayNameTestGroupNewName).build();
    groupClientExtension.update(createdMemberTestGroup.getId(), updatedDisplayNameMemberGroup);

    Thread.sleep(ASSIGNMENT_UPDATE_DELAY * SECONDS);

    List<String> groupMembersDisplayNames = getGroupMembersDisplayNames(createdTestGroupWithMembers, updateMemberDisplayNameTestUserNewName,
        updateMemberDisplayNameTestGroupNewName);

    assertEquals(2, groupMembersDisplayNames.size());
    assertTrue(groupMembersDisplayNames.contains(updateMemberDisplayNameTestUserNewName));
    assertTrue(groupMembersDisplayNames.contains(updateMemberDisplayNameTestGroupNewName));
  }

  @Test
  public void testGroupMemberDisplayNameUpdateWithPATCHRequest() throws InterruptedException {
    User createdTestUser = userClientExtension.create(buildTestUser("testUserForDisplayNameUpdateWithPATCH"));
    Group createdMemberTestGroup = groupClientExtension.create(buildGroup("testGroupForDisplayNameUpdateWithPATCH"));

    Group createdTestGroupWithMembers = groupClientExtension.create(
        buildGroup("testGroupContainingUpdatedPATCHMembers", createdTestUser.getId(), createdMemberTestGroup.getId()));

    assertEquals(2, groupClientExtension.getSingle(createdTestGroupWithMembers.getId()).getMembers().size());
    assertTrue(isGroupIdPresentInGroupResponse(createdTestGroupWithMembers));

    String newUserDisplayName = "updatedUserDisplayName";
    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, CoreUserAttributes.DISPLAY_NAME.scimName(), newUserDisplayName);

    userClientExtension.patch(createdTestUser.getId(), patchBody);

    String newGroupDisplayName = "updatedGroupDisplayName";
    patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, CoreGroupAttributes.DISPLAY_NAME.scimName(), newGroupDisplayName);

    groupClientExtension.patch(createdMemberTestGroup.getId(), patchBody);

    Thread.sleep(ASSIGNMENT_UPDATE_DELAY * SECONDS);

    List<String> groupMembersDisplayNames = getGroupMembersDisplayNames(createdTestGroupWithMembers, newUserDisplayName, newGroupDisplayName);

    assertEquals(2, groupMembersDisplayNames.size());
    assertTrue(groupMembersDisplayNames.contains(newUserDisplayName));
    assertTrue(groupMembersDisplayNames.contains(newGroupDisplayName));
  }

  @Test
  public void testGetGroupsTotalCountWithStartIndex() {
    int startIndex = 1;
    int count = 0;

    PagedByIndexSearchResult<Group> getPagedGroupsSearchResult = groupClientExtension.getPagedByIndex(startIndex, count);

    assertEquals(Long.valueOf(startIndex), getPagedGroupsSearchResult.getStartIndex());
    assertEquals(count, getPagedGroupsSearchResult.getItemsPerPage());
    assertTrue(getPagedGroupsSearchResult.getTotalResults() > 0);

    assertTrue(getPagedGroupsSearchResult.getResources().isEmpty());
  }

  @Test
  public void testGetUsersTotalCountWithStartId() {
    String startId = PAGINATION_BY_ID_START_PARAM;
    int count = 0;

    PagedByIdentitySearchResult<Group> getPagedGroupsSearchResult = groupClientExtension.getPagedById(startId, count);

    assertEquals(startId, getPagedGroupsSearchResult.getStartId());
    assertEquals(36, getPagedGroupsSearchResult.getNextId().length());
    assertEquals(count, getPagedGroupsSearchResult.getItemsPerPage());
    assertTrue(getPagedGroupsSearchResult.getTotalResults() > 0);

    assertTrue(getPagedGroupsSearchResult.getResources().isEmpty());
  }

  @Test
  public void testGetGroupsWithStarIndexEqualTotalResults() {
    String displayName = "testGetGroupsWithStarIndexEqualTotalResults-Group";
    int groupsCount = 3;

    List<Group> createdGroups = createMultipleGroups(displayName, groupsCount);
    int readCount = 100;

    logger.info("Fetching Groups with startIndex: {} and count: {}", groupsCount, readCount);
    PagedByIndexSearchResult<Group> groupsPage = groupClientExtension.getPagedByIndex(groupsCount, readCount);

    // @formatter:off
    assertAll("Verify Correct ListResponse values",
        () -> assertEquals(groupsCount, groupsPage.getStartIndex(), "Verify 'startIndex"),
        () -> assertEquals(groupsCount, groupsPage.getTotalResults(), "Verify 'totalResults' is equal to created Groups"),
        () -> assertEquals(1, groupsPage.getItemsPerPage(), "Verify 'itemsPerPage' contains only one Resource"),
        () -> assertEquals(1, groupsPage.getResources().size(), "Verify 'Resources' list size is equal to 'ItemsPerPage'"),
        () -> {
          String firstGroupIdFromGetResponse = groupsPage.getResources().get(0).getId();
          assertTrue(createdGroups.stream().map(Group::getId).anyMatch(firstGroupIdFromGetResponse::equals), "Verify fetched group is part of previously created Groups");
        });
    // @formatter:on
  }

  @Test
  public void testGetGroupsWithStarIndexOutOfRange() {
    String displayName = "testGetGroupsWithStarIndexOutOfRange-Group";
    int groupsCount = 3;

    createMultipleGroups(displayName, groupsCount);

    int readCount = 100;
    int startIndex = groupsCount + 1;

    logger.info("Fetching Groups with startIndex: {} and count: {}", startIndex, readCount);
    PagedByIndexSearchResult<Group> groupsPage = groupClientExtension.getPagedByIndex(startIndex, readCount);

    // @formatter:off
    assertAll("Verify Correct ListResponse values",
        () -> assertEquals(startIndex, groupsPage.getStartIndex(), "Verify 'startIndex"),
        () -> assertEquals(groupsCount, groupsPage.getTotalResults(), "Verify 'totalResults' is equal to created Groups"),
        () -> assertEquals(0, groupsPage.getItemsPerPage(), "Verify 'itemsPerPage' contains only one Resource"),
        () -> assertTrue(groupsPage.getResources().isEmpty(), "Verify 'Resources' list size is empty'"));
    // @formatter:on
  }

  @Test
  public void testGetGroupsDefaultStartIndex() {
    int count = 1;

    PagedByIndexSearchResult<Group> getPagedGroupsSearchResult = CustomTargetSystemRestClient.INSTANCE.getEntitiesHttpResponse(GROUPS, singletonMap(COUNT_PARAM, count))
        .readEntity(GROUP_LIST_RESPONSE_TYPE_INDEX_PAGING);

    assertEquals(Long.valueOf(1), getPagedGroupsSearchResult.getStartIndex());
    assertEquals(count, getPagedGroupsSearchResult.getItemsPerPage());
    assertTrue(getPagedGroupsSearchResult.getTotalResults() > 0);

    assertEquals(1, getPagedGroupsSearchResult.getResources().size());
  }

  @Test
  public void testGetGroupsDefaultCountWithStartIndex() {
    int startIndex = 1;

    SCIMResponse<PagedByIndexSearchResult<Group>> pagedGroupsResponse = groupRequest.readMultipleGroups(indexPageQuery().withStartIndex(startIndex));
    assertTrue(pagedGroupsResponse.isSuccess());

    PagedByIndexSearchResult<Group> pagedGroupsResult = pagedGroupsResponse.get();

    assertEquals(Long.valueOf(1), pagedGroupsResult.getStartIndex());
    assertTrue(pagedGroupsResult.getTotalResults() > 0);

    if (pagedGroupsResult.getTotalResults() <= Long.parseLong(DEFAULT_COUNT)) {
      assertEquals(pagedGroupsResult.getTotalResults(), pagedGroupsResult.getItemsPerPage());
    } else {
      assertEquals(Long.parseLong(DEFAULT_COUNT), pagedGroupsResult.getItemsPerPage());
    }
  }

  @Test
  public void testGetGroupsDefaultCountWithStartId() {
    String startId = PAGINATION_BY_ID_START_PARAM;

    SCIMResponse<PagedByIdentitySearchResult<Group>> pagedGroupsResponse = groupRequest.readMultipleGroups(identityPageQuery().withStartId(startId));
    assertTrue(pagedGroupsResponse.isSuccess());

    PagedByIdentitySearchResult<Group> pagedGroupsResult = pagedGroupsResponse.get();

    assertEquals(startId, pagedGroupsResult.getStartId());
    assertTrue(pagedGroupsResult.getTotalResults() > 0);

    if (pagedGroupsResult.getTotalResults() <= Long.parseLong(DEFAULT_COUNT)) {
      assertEquals(pagedGroupsResult.getTotalResults(), pagedGroupsResult.getItemsPerPage());
    } else {
      assertEquals(Long.parseLong(DEFAULT_COUNT), pagedGroupsResult.getItemsPerPage());
    }
  }

  @Test
  public void testGetGroupsPagingStartIdEqEnd() {
    String testGroupName = "testGetGroupsPagingStartIdEqEnd";
    String testGroupId;
    int groupNameUniquenessCounter = 1;

    do {
      Group createdTestGroup = groupClientExtension.create(buildGroup(testGroupName + groupNameUniquenessCounter));
      testGroupId = createdTestGroup.getId();
      groupNameUniquenessCounter++;

    } while (!testGroupId.startsWith("f"));

    String startId = PAGINATION_BY_ID_END_PARAM;

    SCIMResponse<PagedByIdentitySearchResult<Group>> pagedGroupsResponse = groupRequest.readMultipleGroups(identityPageQuery().withStartId(startId));
    assertTrue(pagedGroupsResponse.isSuccess());

    PagedByIdentitySearchResult<Group> pagedGroupsResult = pagedGroupsResponse.get();

    assertTrue(pagedGroupsResult.getTotalResults() > 0);
    assertEquals(0, pagedGroupsResult.getItemsPerPage());
    assertEquals(startId, pagedGroupsResult.getStartId());
    assertEquals(PAGINATION_BY_ID_END_PARAM, pagedGroupsResult.getNextId());

  }

  @Test
  public void testGetGroupsPagingStartIdWithUpperCase() {
    Group createdTestGroup = groupClientExtension.create(buildGroup("testGetGroupsPagingStartIdWithUpperCase"));
    SCIMResponse<PagedByIdentitySearchResult<Group>> pagedGroupsResponse = groupRequest.readMultipleGroups(identityPageQuery().withStartId(createdTestGroup.getId()));
    assertTrue(pagedGroupsResponse.isSuccess());

    PagedByIdentitySearchResult<Group> pagedGroupsResult = pagedGroupsResponse.get();

    assertTrue(pagedGroupsResult.getTotalResults() > 0);
    assertTrue(pagedGroupsResult.getItemsPerPage() > 0);
    assertEquals(createdTestGroup.getId(), pagedGroupsResult.getStartId());

    String biggestValidUUID = "FFFFFFFF-FFFF-1FFF-BFFF-FFFFFFFFFFFF";
    pagedGroupsResponse = groupRequest.readMultipleGroups(identityPageQuery().withStartId(biggestValidUUID));
    assertTrue(pagedGroupsResponse.isSuccess());

    pagedGroupsResult = pagedGroupsResponse.get();
    assertTrue(pagedGroupsResult.getTotalResults() > 0);
    assertEquals(0, pagedGroupsResult.getItemsPerPage());
    assertEquals(biggestValidUUID, pagedGroupsResult.getStartId());
    assertEquals(PAGINATION_BY_ID_END_PARAM, pagedGroupsResult.getNextId());
  }

  @Test
  public void testGetGroupsSeveralPagesUsingIndexPaging() {
    int startIndex = 1;
    int count = 3;
    long totalResults = 0;
    PagedByIndexSearchResult<Group> getPagedGroupsResult;
    List<Group> groupsFromAllPages = new LinkedList<>();
    List<Group> allGroups = groupClientExtension.getAllWithIdPaging();

    do {
      getPagedGroupsResult = groupClientExtension.getPagedByIndex(startIndex, count);

      assertEquals(Long.valueOf(startIndex), getPagedGroupsResult.getStartIndex());

      totalResults = getPagedGroupsResult.getTotalResults();
      assertTrue(totalResults > 0);
      assertEquals(allGroups.size(), totalResults);

      List<Group> groupsPerPage = getPagedGroupsResult.getResources();
      groupsFromAllPages.addAll(groupsPerPage);

      startIndex = startIndex + count;
    } while (startIndex <= totalResults);

    assertEquals(groupsFromAllPages.size(), allGroups.size());

    List<String> groupIdsFromAllPages = extractGroupIds(groupsFromAllPages);
    List<String> allGroupIds = extractGroupIds(allGroups);

    groupIdsFromAllPages.removeAll(allGroupIds);
    assertEquals(0, groupIdsFromAllPages.size());
  }

  @Test
  public void testGetGroupsSeveralPagesUsingIdPaging() {
    String startId = PAGINATION_BY_ID_START_PARAM;
    int count = 3;
    long totalResults = 0;
    PagedByIdentitySearchResult<Group> pagedGroups;
    List<Group> groupsFromAllPages = new LinkedList<>();
    List<Group> allGroups = groupClientExtension.getAllWithIndexPaging();

    do {
      pagedGroups = groupClientExtension.getPagedById(startId, count);

      assertEquals(startId, pagedGroups.getStartId());

      totalResults = pagedGroups.getTotalResults();
      assertTrue(totalResults > 0);
      assertEquals(allGroups.size(), totalResults);

      List<Group> groupsPerPage = pagedGroups.getResources();
      groupsFromAllPages.addAll(groupsPerPage);

      startId = pagedGroups.getNextId();
    } while (!startId.equals(PAGINATION_BY_ID_END_PARAM));

    assertTrue(groupsFromAllPages.containsAll(allGroups));
    assertEquals(allGroups.size(), groupsFromAllPages.size());

    assertEquals(groupsFromAllPages.size(), allGroups.size());

    List<String> groupIdsFromAllPages = extractGroupIds(groupsFromAllPages);
    List<String> allGroupsIds = extractGroupIds(allGroups);

    groupIdsFromAllPages.removeAll(allGroupsIds);
    assertEquals(0, groupIdsFromAllPages.size());
  }

  @Test
  public void testGetGroupsFilteredByDisplayName() {
    String displayName = "testGetGroupsFilteredByDisplayName";
    Collection<Group> testGroups = IntStream.range(0, 5).mapToObj(operand -> groupClientExtension.create(buildGroup(displayName))).collect(Collectors.toList());

    Collection<Group> filteredGroups = groupClientExtension.getAllByFilter(String.format("%s eq \"%s\"", CoreGroupAttributes.DISPLAY_NAME.scimName(), displayName));
    assertNotNull(filteredGroups);
    assertTrue(filteredGroups.containsAll(testGroups));
  }

  @Test
  public void testGetFilteredGroupsTotalCount() {
    String testDisplayName = "testGetFilteredGroupsTotalCount";
    groupClientExtension.create(buildGroup(testDisplayName));

    String filterExpression = String.format("%s eq \"%s\"", CoreGroupAttributes.DISPLAY_NAME.scimName(), testDisplayName);

    PagedByIdentitySearchResult<Group> allGroups = groupClientExtension.getPagedById(PAGINATION_BY_ID_START_PARAM, RESOURCES_PER_PAGE);
    PagedByIdentitySearchResult<Group> filteredGroups = groupClientExtension.getByFilteredAndPagedById(PAGINATION_BY_ID_START_PARAM, RESOURCES_PER_PAGE,
        filterExpression);

    assertEquals(1, filteredGroups.getResources().size());
    assertEquals(1, filteredGroups.getTotalResults());

    assertTrue(allGroups.getTotalResults() > 1);
  }

  private DynamicTest getMultipleGroupsDynamicTest(String testName, Supplier<Collection<Group>> createdGroupsSupplier) {
    // @formatter:off
    return DynamicTest.dynamicTest(testName, () -> {
          Collection<Group> createdGroups = createdGroupsSupplier.get();

          logger.info("Fetching Groups");
          PagedByIndexSearchResult<Group> groupsPage = groupRequest.readMultipleGroupsWithoutPaging().get();

          List<Group> fetchedGroups = groupsPage.getResources();
          List<Executable> assertions = getReadGroupsAssertions(createdGroups, fetchedGroups);

          assertAll("Verify empty list response is received",
            () -> assertEquals(createdGroups.size(), groupsPage.getTotalResults(), "Verify 'totalResults'"),
            () -> assertEquals(createdGroups.size(), groupsPage.getItemsPerPage(), "Verify 'itemsPerPage'"),
            () -> assertAll("Verify 'Resources list'", assertions)
            );
          }
        );
    // @formatter:on
  }

  private DynamicTest getSingleGroupDynamicTest(String testName, Supplier<Group> createdGroupSupplier) {
    // @formatter:off
    return DynamicTest.dynamicTest(testName, () -> {
          Group createdGroup = createdGroupSupplier.get();

          logger.info("Fetching Single group: {}", createdGroup.getDisplayName());
          Group fetchedGroup = groupRequest.readSingleGroup(createdGroup.getId()).get();

          assertAll(
              () -> assertEquals(createdGroup.getDisplayName(), fetchedGroup.getDisplayName(), "Verify 'displayName' is same"),
              () -> assertEquals(createdGroup.getMembers().size(), fetchedGroup.getMembers().size(), "Verify members list size is same"),
              () -> getMetaAssertions(fetchedGroup, Group.RESOURCE_TYPE_GROUP),
              getMemberRefAssertions(createdGroup.getMembers(), fetchedGroup.getMembers()));
          }
        );
    // @formatter:on
  }

  private List<String> getGroupMembersDisplayNames(final Group parentGroup, final String newUserDisplayName, final String newGroupDisplayName)
      throws InterruptedException {
    int i = 0;
    List<String> groupMembersDisplayNames = null;
    do {
      groupMembersDisplayNames = new ArrayList<>();
      Group createdTestGroupAfterMembersUpdate = groupClientExtension.getSingle(parentGroup.getId());
      Set<MemberRef> members = createdTestGroupAfterMembersUpdate.getMembers();
      List<String> groupMembersDisplayString = new ArrayList<>();
      for (MemberRef nextMember : members) {
        groupMembersDisplayString.add(nextMember.getDisplay());
        System.out.println(String.format("current group member is %s", nextMember.getDisplay()));
      }

      Thread.sleep(ASSIGNMENT_UPDATE_DELAY * SECONDS);
      if (groupMembersDisplayString.contains(newUserDisplayName) && groupMembersDisplayString.contains(newGroupDisplayName)) {
        for (MemberRef nextMember : members) {
          groupMembersDisplayNames.add(nextMember.getDisplay());
        }
        break;
      }
      i++;
    } while (i < 20);

    return groupMembersDisplayNames;
  }

  private Executable getMetaAssertions(Resource<?> resource, String resourceType) {
    // @formatter:off
    Meta meta = resource.getMeta();

    return () ->  assertAll( "Verify 'meta' attributes",
        () -> assertNotNull(meta, "Verify meta existence"),
        () -> assertEquals(resourceType.toLowerCase(), meta.getResourceType().toLowerCase(), "verify 'resourceType'"),
        () -> assertNotNull(meta.getLocation(), "verify location 'location' is not empty"),
        () -> assertTrue(meta.getLocation().endsWith(constructResourceLocation(resource)), "verify location is correct"),
        () -> assertNotNull(meta.getCreated(), "verify location 'created' is not empty"),
        () -> assertNotNull(meta.getLastModified(), "verify location 'lastModified' is not empty"),
        () -> assertNotNull(meta.getVersion(), "verify location 'version' is not empty")
    );
  }

  private Executable getMembersAssertions(Collection<Resource<?>> expectedMembers, Collection<MemberRef> actualMembers) {
    // @formatter:off
    Collection<Executable> memberAssertions = expectedMembers.stream().map(expMember -> (Executable) () -> {
      String resourceName;
      String resourceType;

      if(expMember instanceof User) {
        resourceName = ((User) expMember).getUserName();
        resourceType = User.RESOURCE_TYPE_USER;
      } else {
        resourceName = ((Group) expMember).getDisplayName();
        resourceType = Group.RESOURCE_TYPE_GROUP;
      }

      assertAll(String.format("Verify member with Id: %s | type: %s | name: %s", expMember.getId(), resourceType, resourceName),
          () -> assertTrue(isResourceExistAsMemberInGroupMembers(expMember, actualMembers), "verify member existence"),
          () -> {
            MemberRef memberRef = actualMembers.stream()
                .filter(ref -> expMember.getId().equals(ref.getValue()))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Not existing member"));

            assertEquals(memberRef.getValue(), expMember.getId(), "Verify member value points to resource Id");
        });
    }).collect(Collectors.toList());

    return () -> assertAll("Verify Group members", Stream.concat(
        Stream.of(() -> assertFalse(actualMembers.isEmpty(), "Verify any members exist")),
        memberAssertions.stream())
    );
    // @formatter:on
  }

  private boolean isResourceExistAsMemberInGroup(Resource<?> resource, Group group) {
    return isResourceExistAsMemberInGroupMembers(resource, group.getMembers());
  }

  private boolean isResourceExistAsMemberInGroupMembers(Resource<?> resource, Collection<MemberRef> groupMembers) {
    return groupMembers.stream().map(MemberRef::getValue).anyMatch(resource.getId()::equals);
  }

  private List<Group> createMultipleGroups(String commonDisplayNamePart, int count) {
    return IntStream.rangeClosed(1, count)
        .mapToObj(number -> commonDisplayNamePart + number)
        .peek(currentDisplayName -> logger.info("Creating Group -{}-", currentDisplayName))
        .map(TestData::buildGroup)
        .map(groupClientExtension::create)
        .collect(Collectors.toList());
  }

  private List<Group> createMultipleGroups(String commonDisplayNamePart, String memberUsername, int count) {
    logger.info("Creating User with username -{}- who will be used as a member", memberUsername);
    User user = userClientExtension.create(buildTestUser(memberUsername));

    return IntStream.rangeClosed(1, count)
        .mapToObj(number -> commonDisplayNamePart + number)
        .peek(currentDisplayName -> logger.info("Creating Group -{}-", currentDisplayName))
        .map(currentDisplayName -> buildGroup(currentDisplayName, user.getId()))
        .map(groupClientExtension::create)
        .collect(Collectors.toList());
  }

  private List<Executable> getReadGroupsAssertions(Collection<Group> createdGroups, Collection<Group> fetchedGroups) {
    // @formatter:off
    return createdGroups.stream()
        .map(group -> (Executable) () -> assertAll("Verify assertions for current '" + group.getDisplayName() + "' group",
            () -> assertTrue(fetchedGroups.stream().map(Group::getId).anyMatch(group.getId()::equals), "Verify existence in GET Groups response"),
            () -> {
              Group fetchedGroup = fetchedGroups.stream()
                  .filter(currentGroup -> group.getId().equals(currentGroup.getId()))
                  .findFirst()
                  .orElseThrow(() -> new RuntimeException("Group was not found"));

              assertAll(
                  () -> assertEquals(group.getDisplayName(), fetchedGroup.getDisplayName(), "Verify 'displayName' is same"),
                  () -> assertEquals(group.getMembers().size(), fetchedGroup.getMembers().size(), "Verify members list size is same"),
                  () -> getMetaAssertions(fetchedGroup, Group.RESOURCE_TYPE_GROUP),
                  getMemberRefAssertions(group.getMembers(), fetchedGroup.getMembers()));
            })
        ).collect(Collectors.toList());
    // @formatter:on
  }

  private boolean isGroupIdPresentInGroupResponse(final Group testGroup) {
    List<Group> allGroups = groupClientExtension.getAllWithIndexPaging();
    for (Group nextGroup : allGroups) {
      if (testGroup.getId().equals(nextGroup.getId())) {

        return true;
      }
    }

    return false;
  }
}
