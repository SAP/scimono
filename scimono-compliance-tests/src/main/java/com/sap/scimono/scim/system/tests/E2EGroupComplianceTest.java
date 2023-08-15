
package com.sap.scimono.scim.system.tests;

import static com.sap.scimono.api.API.COUNT_PARAM;
import static com.sap.scimono.api.API.GROUPS;
import static com.sap.scimono.client.query.ResourcePageQuery.identityPageQuery;
import static com.sap.scimono.client.query.ResourcePageQuery.indexPageQuery;
import static com.sap.scimono.entity.definition.CoreGroupAttributes.MEMBERS;
import static com.sap.scimono.entity.paging.PagedByIdentitySearchResult.PAGINATION_BY_ID_END_PARAM;
import static com.sap.scimono.entity.paging.PagedByIdentitySearchResult.PAGINATION_BY_ID_START_PARAM;
import static com.sap.scimono.entity.paging.PagedByIndexSearchResult.DEFAULT_COUNT;
import static com.sap.scimono.scim.system.tests.conditions.BackendState.EMPTY;
import static com.sap.scimono.scim.system.tests.conditions.BackendState.WITH_INITIAL_EXISTING_RESOURCES;
import static com.sap.scimono.scim.system.tests.util.CustomTargetSystemRestClient.GROUP_LIST_RESPONSE_TYPE_INDEX_PAGING;
import static com.sap.scimono.scim.system.tests.util.TestData.buildGroup;
import static com.sap.scimono.scim.system.tests.util.TestData.buildGroupMemberResourceWithId;
import static com.sap.scimono.scim.system.tests.util.TestData.buildTestUser;
import static com.sap.scimono.scim.system.tests.util.TestData.extractGroupIds;
import static java.util.Collections.singletonMap;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.sap.scimono.scim.system.tests.conditions.EnableOnGroupsBackendState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.function.Executable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.MemberRef;
import com.sap.scimono.entity.Resource;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.definition.CoreGroupAttributes;
import com.sap.scimono.entity.definition.CoreUserAttributes;
import com.sap.scimono.entity.paging.PagedByIdentitySearchResult;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.patch.PatchBody;
import com.sap.scimono.entity.patch.PatchOperation;
import com.sap.scimono.scim.system.tests.extensions.GroupClientScimResponseExtension;
import com.sap.scimono.scim.system.tests.extensions.GroupFailSafeClient;
import com.sap.scimono.scim.system.tests.extensions.UserClientScimResponseExtension;
import com.sap.scimono.scim.system.tests.extensions.UserFailSafeClient;
import com.sap.scimono.scim.system.tests.util.CustomTargetSystemRestClient;
import com.sap.scimono.scim.system.tests.util.TestData;
import com.sap.scimono.scim.system.tests.util.TestReporter;

@ExtendWith(TestReporter.class)
public class E2EGroupComplianceTest extends SCIMComplianceTest {
  private static final Logger logger = LoggerFactory.getLogger(E2EGroupComplianceTest.class);
  private static final int RESOURCES_PER_PAGE = 1000;
  private static final int ASSIGNMENT_UPDATE_DELAY = 30;
  private static final int SECONDS = 1000;

  @RegisterExtension
  UserClientScimResponseExtension resourceAwareUserRequest = UserClientScimResponseExtension.forClearingAfterEachExecutions(userRequest);

  @RegisterExtension
  GroupClientScimResponseExtension resourceAwareGroupRequest = GroupClientScimResponseExtension.forClearingAfterEachExecutions(groupRequest);

  private final GroupFailSafeClient groupFailSafeClient = resourceAwareGroupRequest.getFailSafeClient();
  private final UserFailSafeClient userFailSafeClient = resourceAwareUserRequest.getFailSafeClient();

  @Test
  @DisplayName("Test Get groups empty list response")
  @EnableOnGroupsBackendState(state = EMPTY)
  void testGetGroupsEmptyList() {
    int startIndex = 1;
    PagedByIndexSearchResult<Group> groupsPage = groupFailSafeClient.getPagedByIndex(startIndex, 100);

    // @formatter:off
    assertAll("Verify empty list response is received",
        () -> assertEquals(0, groupsPage.getTotalResults(), "Verify 'totalResults' is 0"),
        () -> assertTrue(groupsPage.getItemsPerPage() > 0, "Verify 'itemsPerPage' is 0"),
        () -> assertEquals(startIndex, groupsPage.getStartIndex(), "Verify startIndex is equal to the one provided in request starIndex param"),
        () -> assertTrue(groupsPage.getResources().isEmpty(), "Verify 'Resources' is empty list")
    );
    // @formatter:on
  }

  @TestFactory
  @EnableOnGroupsBackendState(state = EMPTY)
  public Collection<DynamicTest> testCreateAndGetMultipleGroups() {
    int createdGroupsCount = 3;
    String commonGroupDisplayName = "testCreateAndGetMultipleGroups";

    // @formatter:off
    return Arrays.asList(createAndGetMultipleGroupsDynamicTest("Test create and get multiple groups without members", () -> {
      return createMultipleGroups(commonGroupDisplayName + "-Without-Members", createdGroupsCount);
    }), createAndGetMultipleGroupsDynamicTest("Test get multiple groups with members", () -> {
      String userNameOfMember = commonGroupDisplayName + "-UserMember";
      return createMultipleGroups(commonGroupDisplayName + "-With-Members", userNameOfMember, createdGroupsCount);
    }));
    // @formatter:on
  }

  @Test
  @DisplayName("Test GET multiple groups")
  @EnableOnGroupsBackendState(state = WITH_INITIAL_EXISTING_RESOURCES)
  public void testGetMultipleGroups() {
    PagedByIndexSearchResult<Group> groups = groupFailSafeClient.getAllWithoutPaging();
    assertAll(getCommonAttributesPresenceAssertions(groups.getResources()));
  }

  private Executable getMemberRefAssertions(final Collection<MemberRef> expectedMembers, final Collection<MemberRef> actualMembers) {
    return () -> assertAll("Verify memberRefs are same ", expectedMembers.stream().map(MemberRef::getValue).map(
        expMemberVal -> () -> assertTrue(actualMembers.stream().map(MemberRef::getValue).anyMatch(expMemberVal::equals), "Verify member existence")));
  }

  @Test
  @DisplayName("Test Create and Get Groups and verify all required attributes are fetched")
  public void testCreateAndGetGroupsRequiredAttributeAreFetched() {
    String testGroupName = "testGetGroupsRequiredAttributeAreFetched";
    logger.info("Creating Group -{}-", testGroupName);
    Group group = groupFailSafeClient.create(buildGroup(testGroupName));

    logger.info("Getting Group -{}-", testGroupName);
    Group fetchedGroup = groupFailSafeClient.getSingle(group.getId());

    // @formatter:off
    assertAll("Verify required attributes are present it GET response",
        () -> assertEquals(testGroupName, fetchedGroup.getDisplayName(), "Verify 'displayName' exist and is equal to the one from CREATE response"),
        () -> assertEquals(group.getId(), fetchedGroup.getId(), "Verify 'Id' exist and is equal to the one from CREATE response"),
        getMetaAssertions(fetchedGroup, Group.RESOURCE_TYPE_GROUP)
    );
    // @formatter:on
  }

  @Test
  @DisplayName("Test Get Group and verify all required attributes are fetched")
  @EnableOnGroupsBackendState(state = WITH_INITIAL_EXISTING_RESOURCES)
  public void testGetGroupRequiredAttributeAreFetched() {
    PagedByIndexSearchResult<Group> groupsResponse = groupFailSafeClient.getAllWithoutPaging();
    Group group = groupsResponse.getResources().stream().findFirst().orElseThrow(IllegalStateException::new);

    logger.info("Getting Group with id: {}", group.getId());
    Group fetchedGroup = groupFailSafeClient.getSingle(group.getId());

    assertAll(getCommonAttributesPresenceAssertions(Collections.singletonList(fetchedGroup)));
  }


  @TestFactory
  @DisplayName("Test Create and Get single group")
  public Collection<DynamicTest> testCreateAndGetSingleGroup() {
    int createdGroupsCount = 1;
    String commonGroupDisplayName = "testCreateAndGetSingleGroup";

    // @formatter:off
    return Arrays.asList(
        getSingleGroupDynamicTest("Test get single group without members", () -> {
          return createMultipleGroups(commonGroupDisplayName + "-Without-Members", createdGroupsCount).get(0);
        }),
        getSingleGroupDynamicTest("Test get single group with members", () -> {
          String userNameOfMember = commonGroupDisplayName + "-UserMember";
          return createMultipleGroups(commonGroupDisplayName + "-With-Members", userNameOfMember, createdGroupsCount).get(0);
        })
    );
    // @formatter:on
  }

  @Test
  @DisplayName("Test Create empty (without members) group")
  public void testCreateEmptyGroup() {
    String displayName = "testCreateEmptyGroup";
    logger.info("Creating empty Group: {}", displayName);
    Group group = groupFailSafeClient.create(buildGroup(displayName));

    // @formatter:off
    assertAll("Verifying attributes in response...",
        () -> assertEquals(displayName, group.getDisplayName(), "verify displayName"),
        () -> assertTrue(group.getMembers().isEmpty(), "verify list of members is empty"),
        () -> assertNotNull(group.getMeta(), "Verify 'meta' exist"),
        getMetaAssertions(group, Group.RESOURCE_TYPE_GROUP));
    // @formatter:on
  }

  @Test
  @DisplayName("Test Create group with existing displayName")
  public void testCreateGroupWithSameDisplayName() {
    String displayName = "testCreateGroupWithSameDisplayName";

    logger.info("Creating first Group with dispayName: {}", displayName);
    Group firstGroup = groupFailSafeClient.create(buildGroup(displayName));
    logger.info("Creating second Group with dispayName: {}", displayName);
    Group secondGroup = groupFailSafeClient.create(buildGroup(displayName));

    assertNotEquals(firstGroup.getId(), secondGroup.getId(), "Verify Identifiers from both POST responses are different");

    logger.info("Fetching first Group with dispayName: {}", displayName);
    Group firstGroupFetched = groupFailSafeClient.getSingle(firstGroup.getId());
    logger.info("Fetching second Group with dispayName: {}", displayName);
    Group secondGroupFetched = groupFailSafeClient.getSingle(secondGroup.getId());

    assertNotEquals(firstGroupFetched.getId(), secondGroupFetched.getId(), "Verify Identifiers from both GET responses are different");
  }

  @Test
  @DisplayName("Test Create group with user member")
  public void testCreateGroupWithUserMember() {
    logger.info("Creating User -testCreateGroupWithUserMember-User- who will be used as a member");
    User userMember = userFailSafeClient.create(buildTestUser("testCreateGroupWithUserMember-User"));

    String testGroupName = "testCreateGroupWithUserMember";
    logger.info("Creating Group -{}- with members", testGroupName);
    Group createdTestGroupWithMembers = groupFailSafeClient.create(buildGroup(testGroupName, userMember.getId()));

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
  @DisplayName("Test Create group with group member")
  public void testCreateGroupWithGroupMember() {
    logger.info("Creating Group -testCreateGroupWithGroupMember-Group- which will be used as a member");
    Group groupMember = groupFailSafeClient.create(buildGroup("testCreateGroupWithGroupMember-GroupMember"));

    String testGroupName = "testCreateGroupWithGroupMember-Group";
    logger.info("Creating Group -{}- with members", testGroupName);
    Group createdTestGroupWithMembers = groupFailSafeClient.create(buildGroup(testGroupName, groupMember.getId()));

    // @formatter:off
    assertAll("Verify group attributes from response",
        () -> assertNotNull(createdTestGroupWithMembers),
        getMetaAssertions(createdTestGroupWithMembers, Group.RESOURCE_TYPE_GROUP),
        () -> assertEquals(testGroupName, createdTestGroupWithMembers.getDisplayName(), "verify group displayName"),
        () -> assertEquals(1, createdTestGroupWithMembers.getMembers().size(), "verify members size"),
        getMembersAssertions(Collections.singletonList(groupMember), createdTestGroupWithMembers.getMembers()));
    // @formatter:on
  }

  @Test
  @DisplayName("Test Create group with user and group member")
  public void testCreateGroupWithUserAndGroupMembers() {
    logger.info("Creating User -testCreateGroupWithUserAndGroupMembers-User- who will be used as a member");
    User userMember = userFailSafeClient.create(buildTestUser("testCreateGroupWithUserAndGroupMembers-User"));

    logger.info("Creating Group -testCreateGroupWithUserAndGroupMembers-Group- which will be used as a member");
    Group groupMember = groupFailSafeClient.create(buildGroup("testCreateGroupWithUserAndGroupMembers-Group"));

    String testGroupName = "testCreateGroupWithUserAndGroupMembers";
    logger.info("Creating Group -{}- with members", testGroupName);
    Group createdTestGroupWithMembers = groupFailSafeClient.create(buildGroup(testGroupName, userMember.getId(), groupMember.getId()));

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
  @DisplayName("Test update group's displayName")
  public void testUpdateGroupDisplayName() {
    String displayName = "testUpdateGroupDisplayName";

    logger.info("Creating Group -{}- with members", displayName);
    Group group = groupFailSafeClient.create(buildGroup(displayName));

    String newDisplayName = displayName + "-new";
    logger.info("Updating Group -{}- with new displayName -{}-", displayName, newDisplayName);
    Group updatedGroup = groupFailSafeClient.update(group.getId(), new Group.Builder().setDisplayName(newDisplayName).setId(group.getId()).build());

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
  @DisplayName("Test Update group with PUT operation and remove members")
  public void testUpdateGroupRemovingMembers() {
    logger.info("Creating User -testUpdateGroupRemovingMembers-User- who will be used as a member");
    User userMember = userFailSafeClient.create(buildTestUser("testUpdateGroupRemovingMembers-User"));

    String groupDisplayName = "testUpdateGroupRemovingMembers-GroupWithMembers";
    logger.info("Creating Group -{}- with members", groupDisplayName);
    Group createdTestGroupWithMembers = groupFailSafeClient.create(buildGroup(groupDisplayName, userMember.getId()));

    Group groupForUpdate = new Group.Builder(createdTestGroupWithMembers).removeMembers().build();
    logger.info("Updating Group -testUpdateGroupRemovingMembers-GroupWithMembers- and remove members");
    Group updatedGroupWithMembers = groupFailSafeClient.update(groupForUpdate.getId(), groupForUpdate);

    // @formatter:off
    assertAll("Verify Group response attributes",
        () -> assertEquals(createdTestGroupWithMembers.getDisplayName(), updatedGroupWithMembers.getDisplayName(), "Verify displayName was not changed"),
        () -> assertTrue(updatedGroupWithMembers.getMembers().isEmpty(), "Verify members does not exist"));
    // @formatter:on
  }

  @Test
  @DisplayName("Test Update group with PUT operation and remove members")
  public void testUpdateGroupRemovingOnlyOneMember() {
    logger.info("Creating User -testUpdateGroupRemovingOnlyOneMember-FirstUser- who will be used as a member");
    User firstUserMember = userFailSafeClient.create(buildTestUser("testUpdateGroupRemovingOnlyOneMember-FirstUser"));

    logger.info("Creating User -testUpdateGroupRemovingOnlyOneMember-SecondUser- who will be used as a member");
    User secondUserMember = userFailSafeClient.create(buildTestUser("testUpdateGroupRemovingOnlyOneMember-SecondUser"));

    String groupDisplayName = "testUpdateGroupRemovingOnlyOneMember-GroupWithMembers";
    logger.info("Creating Group -{}- with members", groupDisplayName);
    Group createdTestGroupWithMembers = groupFailSafeClient.create(buildGroup(groupDisplayName, firstUserMember.getId()));

    // @formatter:off
    Group groupForUpdate = new Group.Builder(createdTestGroupWithMembers)
        .removeMembers()
        .addMember(buildGroupMemberResourceWithId(firstUserMember.getId()))
        .build();

    logger.info("Updating Group -testUpdateGroupRemovingOnlyOneMember-GroupWithMembers- and remove members");
    Group updatedGroupWithMembers = groupFailSafeClient.update(groupForUpdate.getId(), groupForUpdate);

    assertAll("Verify Group response attributes",
        () -> assertEquals(createdTestGroupWithMembers.getDisplayName(), updatedGroupWithMembers.getDisplayName(), "Verify displayName was not changed"),
        () -> assertEquals(1, updatedGroupWithMembers.getMembers().size(), "Verify members does not exist"),
        () -> assertFalse(isResourceExistAsMemberInGroup(secondUserMember, updatedGroupWithMembers), "Verify secondUser is not present in members response"));
    // @formatter:on
  }

  @Test
  @DisplayName("Test Update group with PUT operation and add additional member")
  public void testUpdateGroupAddAdditionalMember() {
    logger.info("Creating User -testUpdateGroupAddAdditionalMember-User- who will be used as a member");
    User userMember = userFailSafeClient.create(buildTestUser("testUpdateGroupAddAdditionalMember-User"));

    String groupDisplayName = "testUpdateGroupAddAdditionalMember-GroupWithMembers";
    logger.info("Creating Group -{}- with members", groupDisplayName);
    Group createdTestGroupWithMembers = groupFailSafeClient.create(buildGroup(groupDisplayName, userMember.getId()));

    logger.info("Creating User -testUpdateGroupAddAdditionalMember-SecondMember- who will be used as a member");
    User secondUserMember = userFailSafeClient.create(buildTestUser("testUpdateGroupAddAdditionalMember-SecondMember"));
    MemberRef groupMemberUser = buildGroupMemberResourceWithId(secondUserMember.getId());
    Group groupForUpdate = new Group.Builder(createdTestGroupWithMembers).addMember(groupMemberUser).build();

    logger.info("Updating Group -{}- and add additional member", groupDisplayName);
    Group updatedGroupWithMembers = groupFailSafeClient.update(groupForUpdate.getId(), groupForUpdate);

    // @formatter:off
    assertAll("Verify Group response attributes",
        () -> assertEquals(createdTestGroupWithMembers.getDisplayName(), updatedGroupWithMembers.getDisplayName(), "Verify displayName was not changed"),
        () -> assertEquals(createdTestGroupWithMembers.getMembers().size() + 1, updatedGroupWithMembers.getMembers().size(), "Verify members size is incremented"),
        getMembersAssertions(Arrays.asList(userMember, secondUserMember), updatedGroupWithMembers.getMembers()));
    // @formatter:on
  }

  @Test
  @DisplayName("Test Update group with PUT operation and replace members")
  public void testUpdateGroupReplaceMembers() {
    logger.info("Creating User -testUpdateGroupReplaceMembers-FirstUser- who will be used as a member");
    User firstUserMember = userFailSafeClient.create(buildTestUser("testUpdateGroupReplaceMembers-FirstUser"));

    String groupDisplayName = "testUpdateGroupReplaceMembers-GroupWithMembers";
    logger.info("Creating Group -{}- with members", groupDisplayName);
    Group createdTestGroupWithMembers = groupFailSafeClient.create(buildGroup(groupDisplayName, firstUserMember.getId()));

    logger.info("Creating User -testUpdateGroupReplaceMembers-SecondUser- who will be used as a member in update");
    User secondUserMember = userFailSafeClient.create(buildTestUser("testUpdateGroupReplaceMembers-SecondUser"));

    MemberRef groupMemberUser = buildGroupMemberResourceWithId(secondUserMember.getId());
    Group groupForUpdate = new Group.Builder(createdTestGroupWithMembers).removeMembers().addMember(groupMemberUser).build();

    logger.info("Updating Group -{}- and replace members", groupDisplayName);
    Group updatedGroupWithMembers = groupFailSafeClient.update(groupForUpdate.getId(), groupForUpdate);

    // @formatter:off
    assertAll("Verify Group response attributes",
        () -> assertEquals(createdTestGroupWithMembers.getDisplayName(), updatedGroupWithMembers.getDisplayName(), "Verify displayName was not changed"),
        () -> assertEquals(groupForUpdate.getMembers().size(), updatedGroupWithMembers.getMembers().size()),
        getMembersAssertions(Collections.singletonList(secondUserMember), updatedGroupWithMembers.getMembers()),
        () -> assertFalse(isResourceExistAsMemberInGroup(firstUserMember, updatedGroupWithMembers), "verify old members are removed"));
    // @formatter:on
  }

  @Test
  @DisplayName("Test Update group members with PUT operation and verify Get response")
  public void testUpdateMembersAndGet() {
    logger.info("Creating User -testUpdateMembersAndGet-FirstUser- who will be used as a member");
    User user = userFailSafeClient.create(buildTestUser("testUpdateGroupReplaceMembers-FirstUser"));

    String groupDisplayName = "testUpdateMembersAndGet-Group";
    logger.info("Creating Group -{}-", groupDisplayName);
    Group createdGroup = groupFailSafeClient.create(buildGroup(groupDisplayName));

    logger.info("Updating Group -{}- and addting additional members", groupDisplayName);
    groupFailSafeClient.update(createdGroup.getId(), new Group.Builder(createdGroup).addMember(buildGroupMemberResourceWithId(user.getId())).build());

    Group updatedGroup = groupFailSafeClient.getSingle(createdGroup.getId());
    // @formatter:off
    assertAll("Verify Group response attributes",
        () -> assertEquals(createdGroup.getDisplayName(), updatedGroup.getDisplayName(), "Verify displayName was not changed"),
        () -> assertEquals(1, updatedGroup.getMembers().size(), "Verify member is added"),
        getMembersAssertions(Collections.singletonList(user), updatedGroup.getMembers()));
    // @formatter:on
  }

  @Test
  @DisplayName("Test Create group with 500 members")
  public void testCreateGroupWith500Members() {
    Group groupWithManyMembers = groupFailSafeClient.create(buildGroup("test2CreateGroupWithMembers500"));

    List<MemberRef> members = new ArrayList<>(500);
    for (int i = 1; i <= 500; i++) {
      User tempUser = userFailSafeClient.create(buildTestUser(String.format("usercopy%d", i)));
      members.add(new MemberRef.Builder().setValue(tempUser.getId()).build());
    }

    Group groupWithAddedMembers = new Group.Builder(groupWithManyMembers).addMembers(members).build();
    Group updatedgroupWithManyMembers = groupFailSafeClient.update(groupWithAddedMembers.getId(), groupWithAddedMembers);

    assertNotNull(updatedgroupWithManyMembers);
    assertEquals(500, updatedgroupWithManyMembers.getMembers().size());
  }

  @Test
  @DisplayName("Test Create group without members")
  public void testDeleteGroupWithoutMembers() {
    String groupDisplayName = "testDeleteGroupWithoutMembers-Group";
    logger.info("Creating Group -{}-", groupDisplayName);
    Group createdGroup = groupFailSafeClient.create(buildGroup(groupDisplayName));

    logger.info("Deleting Group: {}", groupDisplayName);
    SCIMResponse<Void> deleteGroupResponse = resourceAwareGroupRequest.deleteGroup(createdGroup.getId());
    assertTrue(deleteGroupResponse.isSuccess(), "SCIM remove request failed - Group id: " + createdGroup.getId());

    logger.info("Reading Group: {}", groupDisplayName);
    SCIMResponse<?> readGroupResponse = resourceAwareGroupRequest.readSingleGroup(createdGroup.getId());
    assertEquals(NOT_FOUND.getStatusCode(), readGroupResponse.getStatusCode(), "Verify response code");
  }

  @Test
  @DisplayName("Test Delete grooup with user members")
  public void testDeleteGroupWithUserMembers() {
    logger.info("Creating User -testDeleteGroupWithUserMembers-User- who will be used as a member");
    User user = userFailSafeClient.create(buildTestUser("testDeleteGroupWithUserMembers-User"));

    String groupDisplayName = "TestGroupWithMembersForDeletion-Group";
    Group testGroup = buildGroup(groupDisplayName, user.getId());

    logger.info("Creating Group -{}- with members", groupDisplayName);
    Group groupForDeletion = groupFailSafeClient.create(testGroup);
    assertNotNull(groupForDeletion);

    logger.info("Deleting Group -{}-", groupDisplayName);
    SCIMResponse<Void> deleteGroupResponse = resourceAwareGroupRequest.deleteGroup(groupForDeletion.getId());
    assertTrue(deleteGroupResponse.isSuccess(), "SCIM remove request failed - Group id: " + groupForDeletion.getId());

    logger.info("Reading Group -{}-", groupDisplayName);
    SCIMResponse<?> readGroupResponse = resourceAwareGroupRequest.readSingleGroup(groupForDeletion.getId());
    assertEquals(NOT_FOUND.getStatusCode(), readGroupResponse.getStatusCode(), "Verify response code");
  }

  @Test
  @DisplayName("Test Delete group with group members")
  public void testDeleteGroupWithGroupMembers() {
    logger.info("Creating User -testDeleteGroupWithGroupMembers-User- which will be used as a member");
    Group groupMember = groupFailSafeClient.create(buildGroup("testDeleteGroupWithGroupMembers-User"));

    String groupDisplayName = "testDeleteGroupWithGroupMembers-Group";
    Group testGroup = buildGroup(groupDisplayName, groupMember.getId());

    logger.info("Creating Group -{}- with members", groupDisplayName);
    Group groupForDeletion = groupFailSafeClient.create(testGroup);

    logger.info("Deleting Group -{}-", groupDisplayName);
    SCIMResponse<Void> deleteGroupResponse = resourceAwareGroupRequest.deleteGroup(groupForDeletion.getId());
    assertTrue(deleteGroupResponse.isSuccess(), "SCIM remove request failed - Group id: " + groupForDeletion.getId());

    logger.info("Reading Group -{}-", groupDisplayName);
    SCIMResponse<?> readGroupResponse = resourceAwareGroupRequest.readSingleGroup(groupForDeletion.getId());
    assertEquals(NOT_FOUND.getStatusCode(), readGroupResponse.getStatusCode(), "Verify response code");
  }

  @Test
  @DisplayName("Test Create group With member and visit member reference")
  public void testCreateGroupWithMemberAndVisitMemberReference() {
    logger.info("Creating User -testCreateGroupWithMemberAndVisitMemberReference-User- who will be used as a member");
    User user = userFailSafeClient.create(buildTestUser("testCreateGroupWithMemberAndVisitMemberReference-User"));

    String testGroupName = "testCreateGroupWithMemberAndVisitMemberReference-Group";
    logger.info("Creating Group -{}- with members", testGroupName);
    Group createdTestGroupWithMembers = groupFailSafeClient.create(buildGroup(testGroupName, user.getId()));

    logger.info("Reading Group -{}-", testGroupName);
    Group createdGroupWithMemberLocation = groupFailSafeClient.getSingle(createdTestGroupWithMembers.getId());

    String memberRef = createdGroupWithMemberLocation.getMembers().iterator().next().getReference();
    assertTrue(memberRef.endsWith("Users/" + user.getId()), "Verify member '$ref'");

    logger.info("Reading User -{}-", user.getUserName());
    SCIMResponse<User> scimResponse = configureScimClientService(memberRef.substring(0, memberRef.lastIndexOf("Users/" + user.getId())))
        .buildUserRequest().readSingleUser(user.getId());

    assertTrue(scimResponse.isSuccess());
    assertNotNull(scimResponse.get());
  }

  @Test
  @DisplayName("Test Delete group and verify Get response")
  public void testDeleteGroupAndGet() {
    String displayName = "testDeleteGroupAndGet-Group";
    logger.info("Creating Group -{}- that will be deleted after that", displayName);
    Group group = groupFailSafeClient.create(buildGroup(displayName));

    resourceAwareGroupRequest.deleteGroup(group.getId());
    SCIMResponse<Group> readResponse = resourceAwareGroupRequest.readSingleGroup(group.getId());
    assertFalse(readResponse.isSuccess(), "Verify that group is deleted and cannot be read");
  }

  @Test
  @DisplayName("Test Get groups after group member deletion")
  public void testGetGroupsAfterGroupMemberDeletion() {
    String userName = "testUserForGroupMemDel";
    logger.info("Creating User: {}", userName);
    User createdTestUser = userFailSafeClient.create(buildTestUser("testUserForGroupMemDel"));

    String testGroupName = "testGroupForGroupMemDel";
    logger.info("Creating Group: {}, with member: {}", testGroupName, userName);
    Group createdTestGroupWithMember = groupFailSafeClient.create(buildGroup(testGroupName, createdTestUser.getId()));

    assertTrue(isGroupIdPresentInGroupResponse(createdTestGroupWithMember), "Verify group is present in GET response");

    logger.info("Deleting Group: {}, with member: {}", testGroupName, userName);
    userFailSafeClient.delete(createdTestUser.getId());

    assertTrue(isGroupIdPresentInGroupResponse(createdTestGroupWithMember), "Verify group is present in GET response after it was deleted");
  }

  @Test
  @DisplayName("Test Delete group members and verify members removed from Get response")
  public void testDeleteGroupMembersUpdate() throws InterruptedException {

    logger.info("Creating User: testDeleteGroupMembersUpdateUsr1, who will be used as a member");
    User createdTestUser2 = userFailSafeClient.create(buildTestUser("testDeleteGroupMembersUpdateUsr1"));

    logger.info("Creating User: testDeleteGroupMembersUpdateUsr2, who will be used as a member");
    User createdTestUser3 = userFailSafeClient.create(buildTestUser("testDeleteGroupMembersUpdateUsr2"));

    String testGroupName = "testDeleteGroupMembersUpdateGrp2";
    logger.info("Creating Group: {} with user members", testGroupName);
    Group createdTestGroupWithMembers = groupFailSafeClient.create(buildGroup(testGroupName, createdTestUser2.getId(), createdTestUser3.getId()));

    assertEquals(2, groupFailSafeClient.getSingle(createdTestGroupWithMembers.getId()).getMembers().size(), "Verify members size is GET response");
    assertTrue(isGroupIdPresentInGroupResponse(createdTestGroupWithMembers), "Verify group is present in GET response");

    logger.info("Deleting User: {}", createdTestUser2.getUserName());
    userFailSafeClient.delete(createdTestUser2.getId());

    logger.info("Deleting User: {}", createdTestUser3.getUserName());
    userFailSafeClient.delete(createdTestUser3.getId());

    int i = 0;
    int groupSize = 100;
    do {
      groupSize = groupFailSafeClient.getSingle(createdTestGroupWithMembers.getId()).getMembers().size();
      if (groupSize == 0) {
        break;
      }
      Thread.sleep(ASSIGNMENT_UPDATE_DELAY * SECONDS);
      i++;
    } while (i < 20);

    assertEquals(0, groupFailSafeClient.getSingle(createdTestGroupWithMembers.getId()).getMembers().size(), "Verify members size in GET response");
  }

  @Test
  @DisplayName("Test Add group member with Patch")
  public void testAddGroupMemberWithPATCH() {
    logger.info("Creating User: testAddGroupMemberWithPATCH-originalUser, who will be used as a member");
    User originalUser = userFailSafeClient.create(buildTestUser("testAddGroupMemberWithPATCH-originalUser"));

    String groupDisplayName = "testAddGroupMemberWithPATCH-GR";
    logger.info("Creating Group: {} with User member: {}", groupDisplayName, originalUser.getUserName());
    Group createdMemberTestGroup = groupFailSafeClient.create(buildGroup(groupDisplayName, originalUser.getId()));

    logger.info("Creating User: testAddGroupMemberWithPATCH-patchedUser, who will be used as a member");
    User patchedUser = userFailSafeClient.create(buildTestUser("testAddGroupMemberWithPATCH-patchedUser"));

    MemberRef userMemberRef = new MemberRef.Builder().setValue(patchedUser.getId()).setType(MemberRef.Type.USER).build();
    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.ADD, MEMBERS.scimName(), TestData.buildMultivaluedJSONNode(userMemberRef));

    logger.info("Patching Group: {}, assigning User member: {}", groupDisplayName, patchedUser.getUserName());
    groupFailSafeClient.patch(createdMemberTestGroup.getId(), patchBody);

    logger.info("Fetching Group: {}", groupDisplayName);
    Group patchedGroup = groupFailSafeClient.getSingle(createdMemberTestGroup.getId());

    assertEquals(2, patchedGroup.getMembers().size(), "Verify 'members' size");
    assertTrue(patchedGroup.getMembers().stream().map(MemberRef::getValue).anyMatch(patchedUser.getId()::equals),
        "Group does not contain member with id: " + patchedUser.getId());
  }

  @Test
  @DisplayName("Test Replace group member with Patch")
  public void testReplaceGroupMemberWithPATCH() {
    logger.info("Creating User: testReplaceGroupMemberWithPATCH-originalUser, who will be used as a member");
    User originalUser = userFailSafeClient.create(buildTestUser("testReplaceGroupMemberWithPATCH-originalUser"));

    String groupDisplayName = "testReplaceGroupMemberWithPATCH-GR";
    logger.info("Creating Group: {} with User member: {}", groupDisplayName, originalUser.getUserName());
    Group createdMemberTestGroup = groupFailSafeClient.create(buildGroup(groupDisplayName, originalUser.getId()));

    logger.info("Creating User: testReplaceGroupMemberWithPATCH-patchedUser, who will be used as a member");
    User patchedUser = userFailSafeClient.create(buildTestUser("testReplaceGroupMemberWithPATCH-patchedUser"));

    MemberRef userMemberRef = new MemberRef.Builder().setValue(patchedUser.getId()).setType(MemberRef.Type.USER).build();
    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, MEMBERS.scimName(), TestData.buildMultivaluedJSONNode(userMemberRef));

    logger.info("Patching Group: {}, replacing User members with: {}", groupDisplayName, patchedUser.getUserName());
    groupFailSafeClient.patch(createdMemberTestGroup.getId(), patchBody);

    logger.info("Fetching Group: {}", groupDisplayName);
    Group patchedGroup = groupFailSafeClient.getSingle(createdMemberTestGroup.getId());

    assertEquals(1, patchedGroup.getMembers().size(), "Verify 'members' size");
    assertTrue(patchedGroup.getMembers().stream().map(MemberRef::getValue).anyMatch(patchedUser.getId()::equals),
        "Group does not contain member with id: " + patchedUser.getId());
  }

  @Test
  @DisplayName("Test Remove all group members with Patch")
  public void testRemoveGroupMemberWithPATCH() {
    logger.info("Creating User: testRemoveGroupMemberWithPATCH-originalUser, who will be used as a member");
    User originalUser = userFailSafeClient.create(buildTestUser("testRemoveGroupMemberWithPATCH-originalUser"));

    String groupDisplayName = "testRemoveGroupMemberWithPATCH-GR";
    logger.info("Creating Group: {} with User member: {}", groupDisplayName, originalUser.getUserName());
    Group createdMemberTestGroup = groupFailSafeClient.create(buildGroup("testRemoveGroupMemberWithPATCH-GR", originalUser.getId()));

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REMOVE, MEMBERS.scimName(), null);

    logger.info("Patching Group: {}, removing User members", groupDisplayName);
    groupFailSafeClient.patch(createdMemberTestGroup.getId(), patchBody);

    logger.info("Fetching Group: {}", groupDisplayName);
    Group patchedGroup = groupFailSafeClient.getSingle(createdMemberTestGroup.getId());
    assertEquals(0, patchedGroup.getMembers().size(), "Verify 'members' size");
  }

  @Test
  @DisplayName("Test Update Group member displayName with PUT")
  public void testGroupMemberDisplayNameUpdateWithPUTRequest() throws InterruptedException {
    String updateMemberDisplayNameTestUser = "testUserForDisplayNameUpdateWithPUT";
    logger.info("Creating User: {}, who will be used as a member", updateMemberDisplayNameTestUser);
    User createdTestUser = userFailSafeClient.create(buildTestUser(updateMemberDisplayNameTestUser));

    String updateMemberDisplayNameTestGroup = "testGroupForDisplayNameUpdateWithPUT";
    logger.info("Creating Group: {}, which will be used as a member", updateMemberDisplayNameTestGroup);
    Group createdMemberTestGroup = groupFailSafeClient.create(buildGroup(updateMemberDisplayNameTestGroup));

    String testGroupName = "testGroupContainingUpdatedWithPUTMembers";
    logger.info("Creating Group: {} with User member: {} and group member: {}", testGroupName, createdTestUser.getUserName(),
        updateMemberDisplayNameTestGroup);
    Group createdTestGroupWithMembers = groupFailSafeClient
        .create(buildGroup(testGroupName, createdTestUser.getId(), createdMemberTestGroup.getId()));

    assertEquals(2, groupFailSafeClient.getSingle(createdTestGroupWithMembers.getId()).getMembers().size(), "Verify members size");
    assertTrue(isGroupIdPresentInGroupResponse(createdTestGroupWithMembers), "Verify group is present in GET all response");

    String updateMemberDisplayNameTestUserNewName = updateMemberDisplayNameTestUser.concat("v22");
    User updatedDisplayNameMemberUser = new User.Builder(createdTestUser).setDisplayName(updateMemberDisplayNameTestUserNewName).build();

    logger.info("Updating User: {}, adding new displayName", updateMemberDisplayNameTestUser);
    userFailSafeClient.update(createdTestUser.getId(), updatedDisplayNameMemberUser);

    String updateMemberDisplayNameTestGroupNewName = updateMemberDisplayNameTestGroup.concat("v22");
    Group updatedDisplayNameMemberGroup = new Group.Builder(createdMemberTestGroup).setDisplayName(updateMemberDisplayNameTestGroupNewName).build();

    logger.info("Updating Group: {}, adding new displayName", updateMemberDisplayNameTestGroup);
    groupFailSafeClient.update(createdMemberTestGroup.getId(), updatedDisplayNameMemberGroup);

    Thread.sleep(ASSIGNMENT_UPDATE_DELAY * SECONDS);

    List<String> groupMembersDisplayNames = getGroupMembersDisplayNames(createdTestGroupWithMembers, updateMemberDisplayNameTestUserNewName,
        updateMemberDisplayNameTestGroupNewName);

    // @formatter:off
    assertAll("Verify member.display is updated in parent group", () -> assertEquals(2, groupMembersDisplayNames.size(), "Verify members size"),
        () -> assertTrue(groupMembersDisplayNames.contains(updateMemberDisplayNameTestUserNewName), "Verify user member display is updated"),
        () -> assertTrue(groupMembersDisplayNames.contains(updateMemberDisplayNameTestGroupNewName), "Verify group member display is updated"));
    // @formatter:on
  }

  @Test
  @DisplayName("Test Update Group member displayName with PATCH")
  public void testGroupMemberDisplayNameUpdateWithPATCHRequest() throws InterruptedException {
    String userMemberName = "testUserForDisplayNameUpdateWithPATCH";
    logger.info("Creating User: {}, who will be used as a member", userMemberName);
    User createdTestUser = userFailSafeClient.create(buildTestUser(userMemberName));

    String groupMemberName = "testGroupForDisplayNameUpdateWithPATCH";
    logger.info("Creating Group: {}, which will be used as a member", groupMemberName);
    Group createdMemberTestGroup = groupFailSafeClient.create(buildGroup("testGroupForDisplayNameUpdateWithPATCH"));

    String groupDisplayName = "testGroupContainingUpdatedPATCHMembers";
    logger.info("Creating Group: {} with User member: {} and group member: {}", groupDisplayName, userMemberName, groupMemberName);
    Group createdTestGroupWithMembers = groupFailSafeClient
        .create(buildGroup(groupDisplayName, createdTestUser.getId(), createdMemberTestGroup.getId()));

    assertEquals(2, groupFailSafeClient.getSingle(createdTestGroupWithMembers.getId()).getMembers().size(), "Verify members size");
    assertTrue(isGroupIdPresentInGroupResponse(createdTestGroupWithMembers));

    String newUserDisplayName = "updatedUserDisplayName";
    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, CoreUserAttributes.DISPLAY_NAME.scimName(), newUserDisplayName);

    logger.info("Patching User: {}, replacing 'displayName'", userMemberName);
    userFailSafeClient.patch(createdTestUser.getId(), patchBody);

    String newGroupDisplayName = "updatedGroupDisplayName";
    patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, CoreGroupAttributes.DISPLAY_NAME.scimName(), newGroupDisplayName);

    logger.info("Patching Group: {}, replacing 'displayName'", groupMemberName);
    groupFailSafeClient.patch(createdMemberTestGroup.getId(), patchBody);

    Thread.sleep(ASSIGNMENT_UPDATE_DELAY * SECONDS);

    List<String> groupMembersDisplayNames = getGroupMembersDisplayNames(createdTestGroupWithMembers, newUserDisplayName, newGroupDisplayName);

    // @formatter:on
    assertAll("Verify member.display is updated in parent group", () -> assertEquals(2, groupMembersDisplayNames.size(), "Verify members size"),
        () -> assertTrue(groupMembersDisplayNames.contains(newUserDisplayName), "Verify user member display is updated"),
        () -> assertTrue(groupMembersDisplayNames.contains(newGroupDisplayName), "Verify group member display is updated"));
    // @formatter:off
  }

  @Test
  @DisplayName("Test Create and Get groups with index paging and count=0")
  public void testCreateAndGetGroupsWithZeroCountAndStartIndex() {
    createMultipleGroups("testCreateAndGetGroupsWithZeroCountAndStartIndex", 3);
    testGetGroupsWithZeroCountAndStartIndex();
  }

  @Test
  @DisplayName("Test Get groups with index paging and count=0")
  @EnableOnGroupsBackendState(state = WITH_INITIAL_EXISTING_RESOURCES)
  public void testGetGroupsWithZeroCountAndStartIndex() {
    int startIndex = 1;
    final int count = 0;

    logger.info("Fetching multiple groups with starIndex: {} and count: {}", startIndex, count);
    PagedByIndexSearchResult<Group> getPagedGroupsSearchResult = groupFailSafeClient.getPagedByIndex(startIndex, count);

    // @formatter:off
    assertAll("Verify List Response",
        () -> assertEquals(startIndex, getPagedGroupsSearchResult.getStartIndex(), "Verify 'startIndex"),
        () -> assertTrue(getPagedGroupsSearchResult.getItemsPerPage() >= count, "Verify 'itemsPerPage' is greater than or equal to: " + count),
        () -> assertTrue(getPagedGroupsSearchResult.getTotalResults() > 0, "Verify 'totalResults' is bigger 0"),
        () -> assertTrue(getPagedGroupsSearchResult.getResources().isEmpty(), "Verify 'Resources' list size is not empty")
    );
    // @formatter:on
  }


  @Test
  @DisplayName("Test Create and Get groups with Id paging and count=0")
  public void testCreateGetGroupsWithZeroCountAndStartId() {
    createMultipleGroups("testCreateGetGroupsWithZeroCountAndStartId", 3);
    testGetGroupsWithZeroCountAndStartId();
  }

  @Test
  @DisplayName("Test Get groups with Id paging and count=0")
  @EnableOnGroupsBackendState(state = WITH_INITIAL_EXISTING_RESOURCES)
  public void testGetGroupsWithZeroCountAndStartId() {
    String startId = PAGINATION_BY_ID_START_PARAM;
    int count = 0;

    logger.info("Fetching Groups with startId: {} and count: {}", startId, count);
    PagedByIdentitySearchResult<Group> getPagedGroupsSearchResult = groupFailSafeClient.getPagedById(startId, count);

    // @formatter:off
    assertAll("Verify List Response",
        () -> assertEquals(startId, getPagedGroupsSearchResult.getStartId(), "Verify 'startId'"),
        () -> assertTrue(count <= getPagedGroupsSearchResult.getItemsPerPage(), "Verify 'count' is equal or less to 'itemsPerPage'"),
        () -> assertTrue(getPagedGroupsSearchResult.getTotalResults() > 0, "Verify 'totalResults' is greater than 0"),
        () -> assertTrue(getPagedGroupsSearchResult.getResources().isEmpty(), "Verify 'Resources' list size is empty'"),
        () -> assertTrue(getPagedGroupsSearchResult.getResources().size() <= getPagedGroupsSearchResult.getItemsPerPage(),
            "Verify 'Resources' list size is less than or equal to 'itemsPerPage''")
    );
    // @formatter:on
  }

  @Test
  @DisplayName("Test Create And Get groups with index paging and startIndex=totalResults")
  @EnableOnGroupsBackendState(state = EMPTY)
  public void testCreateAndGetGroupsWithStarIndexEqualTotalResults() {
    String displayName = "testCreateAndGetGroupsWithStarIndexEqualTotalResults-Group";
    int groupsCount = 3;

    List<Group> createdGroups = createMultipleGroups(displayName, groupsCount);
    int readCount = 100;

    logger.info("Fetching Groups with startIndex: {} and count: {}", groupsCount, readCount);
    PagedByIndexSearchResult<Group> groupsPage = groupFailSafeClient.getPagedByIndex(groupsCount, readCount);

    // @formatter:off
    assertAll(
        getResourcesWithStartIndexEqualTotalResultsAssertions(groupsCount, readCount, groupsPage),
        () -> {
          String firstGroupIdFromGetResponse = groupsPage.getResources().get(0).getId();
          assertTrue(createdGroups.stream().map(Group::getId).anyMatch(firstGroupIdFromGetResponse::equals),
              "Verify fetched group is part of previously created Groups");
        });
    // @formatter:on
  }

  @Test
  @DisplayName("Test Get groups with index paging and startIndex=totalResults")
  @EnableOnGroupsBackendState(state = WITH_INITIAL_EXISTING_RESOURCES)
  public void testGetGroupsWithStarIndexEqualTotalResults() {
    PagedByIndexSearchResult<Group> groupsWithoutPaging = groupFailSafeClient.getAllWithoutPaging();
    assertTrue(groupsWithoutPaging.getTotalResults() > 0, "Verify 'totalResult' is not 0");

    int readCount = 100;
    int startIndex = groupsWithoutPaging.getTotalResults();

    logger.info("Fetching Groups with startIndex: {} and count: {}", startIndex, readCount);
    PagedByIndexSearchResult<Group> groupsPage = groupFailSafeClient.getPagedByIndex(startIndex, readCount);

    assertAll(getResourcesWithStartIndexEqualTotalResultsAssertions(startIndex, readCount, groupsPage));
  }

  @Test
  @DisplayName("Test Get groups with index paging and startIndex out of range (more than total results)")
  public void testGetGroupsWithStarIndexOutOfRange() {
    logger.info("Fetching all groups");
    int alreadyCreatedGroups = groupFailSafeClient.getAllWithIndexPaging().size();

    int readCount = 100;
    int startIndex = alreadyCreatedGroups + 1;

    logger.info("Fetching Groups with startIndex: {} and count: {}", startIndex, readCount);
    PagedByIndexSearchResult<Group> groupsPage = groupFailSafeClient.getPagedByIndex(startIndex, readCount);

    // @formatter:off
    assertAll("Verify Correct ListResponse values",
        () -> assertEquals(startIndex, groupsPage.getStartIndex(), "Verify 'startIndex"),
        () -> assertEquals(alreadyCreatedGroups, groupsPage.getTotalResults(), "Verify 'totalResults' is equal to created Groups"),
        () -> assertTrue(groupsPage.getItemsPerPage() <= readCount, "Verify 'count' is equal or less to 'itemsPerPage'"),
        () -> assertTrue(groupsPage.getResources().isEmpty(), "Verify 'Resources' list size is empty'"));
    // @formatter:on
  }

  @Test
  @DisplayName("Test Create and Get groups with default startIndex")
  public void testCreateAndGetGroupsDefaultStartIndex() {
    createMultipleGroups("testCreateAndGetGroupsDefaultStartIndex", 3);
    testGetGroupsDefaultStartIndex();
  }

  @Test
  @DisplayName("Test Create and Get groups with Id paging and negative count")
  @EnableOnGroupsBackendState(state = EMPTY)
  public void testCreateAndGetGroupsNegativeCountAndStartId() {
    createMultipleGroups("testCreateAndGetGroupsNegativeCountAndStartId", 3);
    testGetGroupsNegativeCountAndStartId();
  }

  @Test
  @DisplayName("Test Get groups with Id paging and negative count")
  @EnableOnGroupsBackendState(state = WITH_INITIAL_EXISTING_RESOURCES)
  public void testGetGroupsNegativeCountAndStartId() {
    String startId = PAGINATION_BY_ID_START_PARAM;
    int count = -10;

    logger.info("Fetching Groups with startId: {} and count: {}", startId, count);
    PagedByIdentitySearchResult<Group> getPagedGroupsSearchResult = groupFailSafeClient.getPagedById(startId, count);

    // @formatter:off
    assertAll("Verify List Response",
        () -> assertEquals(startId, getPagedGroupsSearchResult.getStartId(), "Verify 'startId'"),
        () -> assertTrue(0 <= getPagedGroupsSearchResult.getItemsPerPage(), "Verify 'ItemsPerPage' is greater or equal to 0"),
        () -> assertTrue(getPagedGroupsSearchResult.getTotalResults() > 0, "Verify 'totalResults' is greater than 0"),
        () -> assertTrue(getPagedGroupsSearchResult.getResources().size() <= getPagedGroupsSearchResult.getItemsPerPage(),
            "Verify 'Resources' list size is less than or equal to 'itemsPerPage''")
    );
    // @formatter:on
  }

  @Test
  @DisplayName("Test Get groups with default startIndex")
  @EnableOnGroupsBackendState(state = WITH_INITIAL_EXISTING_RESOURCES)
  public void testGetGroupsDefaultStartIndex() {
    // @formatter:off
    int count = 1;
    PagedByIndexSearchResult<Group> getPagedGroupsSearchResult = CustomTargetSystemRestClient.INSTANCE
        .getEntitiesHttpResponse(GROUPS, singletonMap(COUNT_PARAM, count))
        .readEntity(GROUP_LIST_RESPONSE_TYPE_INDEX_PAGING);

    assertAll("Verify List Response",
        () -> assertEquals(1, getPagedGroupsSearchResult.getStartIndex(), "Verify 'startIndex'"),
        () -> assertTrue(count <= getPagedGroupsSearchResult.getItemsPerPage(), "Verify 'count' is equal or less to 'itemsPerPage'"),
        () -> assertTrue(getPagedGroupsSearchResult.getTotalResults() > 0, "Verify 'totalResults' is greater than 0"),
        () -> assertEquals(count, getPagedGroupsSearchResult.getResources().size(), "Verify 'Resources' list size")
    );
    // @formatter:on
  }

  @Test
  @DisplayName("Test Create and Get groups with default count")
  public void testCreateAndGetGroupsDefaultCountWithStartIndex() {
    createMultipleGroups("testGetGroupsDefaultCountWithStartIndex", 3);
    testGetGroupsDefaultCountWithStartIndex();
  }

  @Test
  @DisplayName("Test Get groups with default count")
  @EnableOnGroupsBackendState(state = WITH_INITIAL_EXISTING_RESOURCES)
  public void testGetGroupsDefaultCountWithStartIndex() {
    int startIndex = 1;

    logger.info("Fetching Multiple Groups with startIndex: {}, and default count", startIndex);
    SCIMResponse<PagedByIndexSearchResult<Group>> pagedGroupsResponse = resourceAwareGroupRequest
        .readMultipleGroups(indexPageQuery().withStartIndex(startIndex));
    assertTrue(pagedGroupsResponse.isSuccess(), "Verify GET response is success");

    PagedByIndexSearchResult<Group> pagedGroupsResult = pagedGroupsResponse.get();

    // @formatter:off
    assertAll("Verify List response",
        () -> assertEquals(startIndex, pagedGroupsResult.getStartIndex(), "Verify 'startIndex'"),
        () -> assertTrue(pagedGroupsResult.getTotalResults() > 0, "Verify 'totalResults' is greater than 0")
    );
    // @formatter:on
  }

  @Test
  @DisplayName("Test Get groups with Id paging and default startId")
  @EnableOnGroupsBackendState(state = WITH_INITIAL_EXISTING_RESOURCES)
  public void testGetGroupsDefaultCountWithStartId() {
    String startId = PAGINATION_BY_ID_START_PARAM;

    logger.info("Fetching Multiple Groups with startId: {}, and default count", startId);
    SCIMResponse<PagedByIdentitySearchResult<Group>> pagedGroupsResponse = resourceAwareGroupRequest
        .readMultipleGroups(identityPageQuery().withStartId(startId));
    assertTrue(pagedGroupsResponse.isSuccess());

    PagedByIdentitySearchResult<Group> pagedGroupsResult = pagedGroupsResponse.get();

    // @formatter:off
    assertAll("Verify List response",
        () -> assertEquals(startId, pagedGroupsResult.getStartId(), "Verify 'startId'"),
        () -> assertTrue(pagedGroupsResult.getTotalResults() > 0, "Verify 'totalResults' is greater than 0")
    );
    // @formatter:on

    if (pagedGroupsResult.getTotalResults() <= Long.parseLong(DEFAULT_COUNT)) {
      assertTrue(pagedGroupsResult.getItemsPerPage() >= Integer.parseInt(DEFAULT_COUNT),
          "Verify 'itemsPerPage' is greater than or equal to: " + DEFAULT_COUNT);
    } else {
      assertEquals(Integer.parseInt(DEFAULT_COUNT), pagedGroupsResult.getItemsPerPage(), "Verify 'itemsPerPage'");
    }
  }

  @Test
  @DisplayName("Test Create and Get groups with Id paging and default startId")
  public void testCreateAndGetGroupsDefaultCountWithStartId() {
    createMultipleGroups("testGetGroupsDefaultCountWithStartId", 3);
    testGetGroupsDefaultCountWithStartId();
  }

  @Test
  @DisplayName("Test Create and Get groups with Id paging and startId=end")
  public void testCreateAndGetGroupsPagingStartIdEqEnd() {
    createMultipleGroups("testCreateAndGetGroupsPagingStartIdEqEnd", 10);
    testGetGroupsPagingStartIdEqEnd();
  }

  @Test
  @DisplayName("Test Get groups with Id paging and startId=end")
  @EnableOnGroupsBackendState(state = WITH_INITIAL_EXISTING_RESOURCES)
  public void testGetGroupsPagingStartIdEqEnd() {
    String startId = PAGINATION_BY_ID_END_PARAM;

    logger.info("Fetching Multiple groups with startId: {}, and default count", startId);
    SCIMResponse<PagedByIdentitySearchResult<Group>> pagedGroupsResponse = resourceAwareGroupRequest
        .readMultipleGroups(identityPageQuery().withStartId(startId));
    assertTrue(pagedGroupsResponse.isSuccess(), "Verify GET response is success");

    PagedByIdentitySearchResult<Group> pagedGroupsResult = pagedGroupsResponse.get();

    // @formatter:off
    assertAll("Verify List Response",
        () -> assertTrue(pagedGroupsResult.getTotalResults() > 0, "Verify 'totalResults' is greater than 0"),
        () -> assertEquals(startId, pagedGroupsResult.getStartId(), "Verify 'startId'"),
        () -> assertEquals(PAGINATION_BY_ID_END_PARAM, pagedGroupsResult.getNextId(), "Verify 'nextId'")
    );
    // @formatter:on
  }

  @Test
  @DisplayName("Test Create and Get groups with Id paging and startId in upper case")
  public void testCreateAndGetGroupsPagingStartIdWithUpperCase() {
    createMultipleGroups("testGetGroupsPagingStartIdWithUpperCase-Multi", 6);
    testGetGroupsPagingStartIdWithUpperCase();
  }

  @Test
  @DisplayName("Test Get groups with Id paging and startId in upper case")
  @EnableOnGroupsBackendState(state = WITH_INITIAL_EXISTING_RESOURCES)
  public void testGetGroupsPagingStartIdWithUpperCase() {
    logger.info("Fetching multiple Groups");
    Group group = groupFailSafeClient.getAllWithoutPaging().getResources().stream().findFirst().orElseThrow(IllegalStateException::new);

    logger.info("Fetching Multiple Groups with startId: {}, and default count", group.getId());
    SCIMResponse<PagedByIdentitySearchResult<Group>> pagedGroupsResponse = resourceAwareGroupRequest
        .readMultipleGroups(identityPageQuery().withStartId(group.getId().toUpperCase()));

    assertTrue(pagedGroupsResponse.isSuccess(), "Verify GET response is success");

    final PagedByIdentitySearchResult<Group> firstPagedGroupsResult = pagedGroupsResponse.get();

    // @formatter:off
    assertAll("Verify first GET Groups response",
        () -> assertTrue(firstPagedGroupsResult.getTotalResults() > 0, "Verify 'totalResults' is greater than 0"),
        () -> assertTrue(firstPagedGroupsResult.getItemsPerPage() > 0, "Verify 'itemsPerPage' is greater than 0"),
        () -> assertEquals(group.getId().toLowerCase(), firstPagedGroupsResult.getStartId().toLowerCase(), "Verify 'startId'")
    );
    // @formatter:on
  }

  @Test
  @DisplayName("Test Create and Get groups all pages with index paging")
  public void testCreateAndGetGroupsSeveralPagesUsingIndexPaging() {
    createMultipleGroups("testCreateAndGetGroupsSeveralPagesUsingIndexPaging", 10);
    testGetGroupsSeveralPagesUsingIndexPaging();
  }

  @Test
  @DisplayName("Test Get groups all pages with index paging")
  @EnableOnGroupsBackendState(state = WITH_INITIAL_EXISTING_RESOURCES)
  public void testGetGroupsSeveralPagesUsingIndexPaging() {
    int startIndex = 1;
    int count = 3;
    long totalResults = 0;

    PagedByIndexSearchResult<Group> groupsWithoutPaging = groupFailSafeClient.getAllWithoutPaging();
    assumeTrue(groupsWithoutPaging.getTotalResults() > count,
        "Aborted: It is required more than " + count + " Groups to exist in the backend, in order to execute this test");

    List<Group> allGroups = groupsWithoutPaging.getResources();
    List<Group> groupsFromAllPages = new LinkedList<>();
    do {
      logger.info("Fetching Multiple Groups with startIndex: {}, and count: {}", startIndex, count);
      PagedByIndexSearchResult<Group> getPagedGroupsResult = groupFailSafeClient.getPagedByIndex(startIndex, count);

      final int startIndexCopy = startIndex;

      // @formatter:off
      assertAll("Verify List Response", () -> assertEquals(startIndexCopy, getPagedGroupsResult.getStartIndex(), "Verify 'startIndex'"),
          () -> assertTrue(getPagedGroupsResult.getTotalResults() > 0, "Verify 'totalResults' is greater that 0"),
          () -> assertEquals(allGroups.size(), getPagedGroupsResult.getTotalResults(), "Verify 'totalResult' size"));
      // @formatter:on
      totalResults = getPagedGroupsResult.getTotalResults();

      List<Group> groupsPerPage = getPagedGroupsResult.getResources();
      groupsFromAllPages.addAll(groupsPerPage);

      startIndex = startIndex + count;
    } while (startIndex <= totalResults);

    assertEquals(allGroups.size(), groupsFromAllPages.size(), "Verify all Groups count is equal to sum of those extracted from all pages");

    List<String> groupIdsFromAllPages = extractGroupIds(groupsFromAllPages);
    List<String> allGroupIds = extractGroupIds(allGroups);
    groupIdsFromAllPages.removeAll(allGroupIds);

    assertEquals(0, groupIdsFromAllPages.size(), "Verify paged Groups are same sa All groups");
  }

  @Test
  @DisplayName("Test Create and Get groups all pages with id paging")
  public void testCreateAndGetGroupsSeveralPagesUsingIdPaging() {
    createMultipleGroups("testCreateAndGetGroupsSeveralPagesUsingIdPaging", 10);
    testGetGroupsSeveralPagesUsingIdPaging();
  }

  @Test
  @DisplayName("Test Get groups all pages with id paging")
  @EnableOnGroupsBackendState(state = WITH_INITIAL_EXISTING_RESOURCES)
  public void testGetGroupsSeveralPagesUsingIdPaging() {
    String startId = PAGINATION_BY_ID_START_PARAM;
    int count = 3;

    PagedByIndexSearchResult<Group> groupsWithoutPaging = groupFailSafeClient.getAllWithoutPaging();
    assumeTrue(groupsWithoutPaging.getTotalResults() > count,
        "Aborted: It is required more than " + count + " Groups to exist in the backend, in order to execute this test");

    List<Group> allGroups = groupsWithoutPaging.getResources();
    List<Group> groupsFromAllPages = new LinkedList<>();
    do {
      logger.info("Fetching Multiple Groups with startId: {}, and count: {}", startId, count);
      PagedByIdentitySearchResult<Group> pagedGroups = groupFailSafeClient.getPagedById(startId, count);

      final String startIdCopy = startId;

      // @formatter:off
      assertAll("Verify List Response", () -> assertEquals(startIdCopy, pagedGroups.getStartId(), "Verify 'startId'"),
          () -> assertTrue(pagedGroups.getTotalResults() > 0, "Verify 'totalResults' is greater that 0"),
          () -> assertEquals(allGroups.size(), pagedGroups.getTotalResults(), "Verify 'totalResult' size"));
      // @formatter:on

      List<Group> groupsPerPage = pagedGroups.getResources();
      groupsFromAllPages.addAll(groupsPerPage);

      startId = pagedGroups.getNextId();
    } while (!startId.equals(PAGINATION_BY_ID_END_PARAM));

    assertEquals(allGroups.size(), groupsFromAllPages.size(), "Verify all Groups count is equal to sum of those extracted from all pages");

    List<String> groupIdsFromAllPages = extractGroupIds(groupsFromAllPages);
    List<String> allGroupsIds = extractGroupIds(allGroups);

    groupIdsFromAllPages.removeAll(allGroupsIds);
    assertEquals(0, groupIdsFromAllPages.size(), "Verify paged Groups are same sa All groups");
  }

  @Test
  @DisplayName("Test Create and Get groups with displayName filter")
  public void testCreateAndGetGroupsFilteredByDisplayName() {
    String displayName = "testCreateAndGetGroupsFilteredByDisplayName";
    createMultipleGroups(displayName, 5);

    String filteredGroupDisplayName = displayName + "-filtered";
    logger.info("Creating Group: {}", filteredGroupDisplayName);
    Group filteredGroup = groupFailSafeClient.create(buildGroup(filteredGroupDisplayName));

    testGetGroupsFilteredByDisplayName(filteredGroup);
  }

  @Test
  @DisplayName("Test Get groups with displayName filter")
  @EnableOnGroupsBackendState(state = WITH_INITIAL_EXISTING_RESOURCES)
  public void testGetGroupsFilteredByDisplayName() {
    Group filteredGroup = getGroupsAndExtractOne(group -> group.getDisplayName() != null,
        "Unable to Group with existing 'organization' value");

    testGetGroupsFilteredByDisplayName(filteredGroup);
  }

  @Test
  @DisplayName("Test Get groups with displayName filter and id paging")
  public void testGetFilteredGroupsTotalCount() {
    String testDisplayName = "testGetFilteredGroupsTotalCount";

    logger.info("Creating Group: {}", testDisplayName);
    groupFailSafeClient.create(buildGroup(testDisplayName));

    String otherDisplayName = testDisplayName + "random";
    logger.info("Creating Group: {}", otherDisplayName);
    groupFailSafeClient.create(buildGroup(testDisplayName + "random"));

    String filterExpression = String.format("%s eq \"%s\"", CoreGroupAttributes.DISPLAY_NAME.scimName(), testDisplayName);

    logger.info("Fetching Multiple Groups with startId: {}, and count: {}", PAGINATION_BY_ID_START_PARAM, RESOURCES_PER_PAGE);
    PagedByIdentitySearchResult<Group> allGroups = groupFailSafeClient.getPagedById(PAGINATION_BY_ID_START_PARAM, RESOURCES_PER_PAGE);

    logger.info("Fetching Multiple Groups with startId: {}, and count: {} and filter expression: {}", PAGINATION_BY_ID_START_PARAM,
        RESOURCES_PER_PAGE, filterExpression);
    PagedByIdentitySearchResult<Group> filteredGroups = groupFailSafeClient.getByFilteredAndPagedById(PAGINATION_BY_ID_START_PARAM,
        RESOURCES_PER_PAGE, filterExpression);

    // @formatter:off
    assertAll("Verify GET Responses",
        () -> assertEquals(1, filteredGroups.getResources().size(), "Verify filtered Groups list size"),
        () -> assertEquals(1, filteredGroups.getTotalResults(), "Verify 'totalResults' of Groups fetched with filter"),
        () -> assertTrue(allGroups.getTotalResults() > 1, "Verify 'totalResults' of Groups fetched without filter is bigger than 1")
    );
    // @formatter:on
  }

  @Test
  @DisplayName("Test Create and Get groups with displayName filter and id paging")
  public void testCreateGetFilteredPagedByIdGroupssTotalCount() {
    createMultipleGroups("testCreateGetFilteredPagedByIdGroupssTotalCount", 3);

    String testGroupName = "uniqueName1543258";
    logger.info("Creating Group: {}", testGroupName);
    Group group = groupFailSafeClient.create(buildGroup(testGroupName));

    testGetFilteredPagedByIdGroupsTotalCount(group);
  }


  @Test
  @DisplayName("Test Get groups with displayName filter and id paging")
  @EnableOnGroupsBackendState(state = WITH_INITIAL_EXISTING_RESOURCES)
  public void testGetFilteredPagedByIdGroupsTotalCount() {
    logger.info("Fetching multiple groups without paging");
    PagedByIndexSearchResult<Group> groupsFetchedWithoutPaging = groupFailSafeClient.getAllWithoutPaging();
    assumeTrue(groupsFetchedWithoutPaging.getTotalResults() > 1,
        "Aborted: Min 2 Groups must be present in the backend, in order to execute this test");

    Group group = groupsFetchedWithoutPaging.getResources().stream().findFirst().orElseThrow(IllegalAccessError::new);
    testGetFilteredPagedByIdGroupsTotalCount(group);
  }

  private DynamicTest createAndGetMultipleGroupsDynamicTest(final String testName, final Supplier<Collection<Group>> createdGroupsSupplier) {
    // @formatter:off
    return DynamicTest.dynamicTest(testName, () -> {
       try{
            Collection<Group> createdGroups = createdGroupsSupplier.get();

            logger.info("Fetching Groups");
            PagedByIndexSearchResult<Group> groupsPage = resourceAwareGroupRequest.readMultipleGroupsWithoutPaging().get();

            List<Group> fetchedGroups = groupsPage.getResources();
            List<Executable> assertions = getReadGroupsAssertions(createdGroups, fetchedGroups);

            assertAll("Verify empty list response is received",
              () -> assertEquals(createdGroups.size(), groupsPage.getTotalResults(), "Verify 'totalResults'"),
              () -> assertTrue(groupsPage.getItemsPerPage() >= createdGroups.size(), "Verify 'itemsPerPage' is greater than or equal to: " + createdGroups.size()),
              () -> assertAll("Verify 'Resources list'", assertions)
              );
           } finally{
              resourceAwareUserRequest.clearManagedResources();
              resourceAwareGroupRequest.clearManagedResources();
           }
          }
        );
    // @formatter:on
  }

  private DynamicTest getSingleGroupDynamicTest(final String testName, final Supplier<Group> createdGroupSupplier) {
    // @formatter:off
    return DynamicTest.dynamicTest(testName, () -> {
          Group createdGroup = createdGroupSupplier.get();

          logger.info("Fetching Single group: {}", createdGroup.getDisplayName());
          Group fetchedGroup = resourceAwareGroupRequest.readSingleGroup(createdGroup.getId()).get();

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
      Group createdTestGroupAfterMembersUpdate = groupFailSafeClient.getSingle(parentGroup.getId());
      Set<MemberRef> members = createdTestGroupAfterMembersUpdate.getMembers();
      List<String> groupMembersDisplayString = new ArrayList<>();
      for (MemberRef nextMember : members) {
        groupMembersDisplayString.add(nextMember.getDisplay());
        logger.info("current group member is {}", nextMember.getDisplay());
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

  private Executable getMembersAssertions(final Collection<Resource<?>> expectedMembers, final Collection<MemberRef> actualMembers) {
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

  private boolean isResourceExistAsMemberInGroup(final Resource<?> resource, final Group group) {
    return isResourceExistAsMemberInGroupMembers(resource, group.getMembers());
  }

  private boolean isResourceExistAsMemberInGroupMembers(final Resource<?> resource, final Collection<MemberRef> groupMembers) {
    return groupMembers.stream().map(MemberRef::getValue).anyMatch(resource.getId()::equals);
  }

  private List<Group> createMultipleGroups(final String commonDisplayNamePart, final int count) {
    return IntStream.rangeClosed(1, count).mapToObj(number -> commonDisplayNamePart + number)
        .peek(currentDisplayName -> logger.info("Creating Group -{}-", currentDisplayName)).map(TestData::buildGroup).map(groupFailSafeClient::create)
        .collect(Collectors.toList());
  }

  private List<Group> createMultipleGroups(final String commonDisplayNamePart, final String memberUsername, final int count) {
    logger.info("Creating User with username -{}- who will be used as a member", memberUsername);
    User user = userFailSafeClient.create(buildTestUser(memberUsername));

    return IntStream.rangeClosed(1, count).mapToObj(number -> commonDisplayNamePart + number)
        .peek(currentDisplayName -> logger.info("Creating Group -{}-", currentDisplayName))
        .map(currentDisplayName -> buildGroup(currentDisplayName, user.getId())).map(groupFailSafeClient::create).collect(Collectors.toList());
  }

  private List<Executable> getReadGroupsAssertions(final Collection<Group> createdGroups, final Collection<Group> fetchedGroups) {
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


  private List<Executable> getCommonAttributesPresenceAssertions(List<Group> groups) {
    // @formatter:off
    return groups.stream()
        .map(group -> (Executable) () -> assertAll("Verify assertions for current group: " + group.getId(),
            () -> assertNotNull(group.getDisplayName(), "Verify 'displayName' attribute is present"),
            () -> assertNotNull(group.getMembers(), "Verify 'members' attribute is present"),
            getMetaAssertions(group, Group.RESOURCE_TYPE_GROUP),
            () -> {
              Set<MemberRef> groupMembers = group.getMembers();
              if (groupMembers.isEmpty()) {
                return;
              }

              groupMembers.forEach(getMemberRefPresenceAssertions());
            })
        ).collect(Collectors.toList());
    // @formatter:on
  }

  private Consumer<MemberRef> getMemberRefPresenceAssertions() {
    return member -> assertAll("Verify member common attributes are present",
        () -> assertNotNull(member.getType(), "Verify 'type' attribute is present"),
        () -> assertNotNull(member.getValue(), "Verify 'value' attribute is present")
    );
  }

  private void testGetFilteredPagedByIdGroupsTotalCount(Group groupToFilter) {
    String filterExpression = String.format("displayName eq \"%s\"", groupToFilter.getDisplayName());

    logger.info("Fetching Multiple Groups with startId: {}, and count: {}", PAGINATION_BY_ID_START_PARAM, RESOURCES_PER_PAGE);
    PagedByIdentitySearchResult<Group> allGroups = groupFailSafeClient.getPagedById(PAGINATION_BY_ID_START_PARAM, RESOURCES_PER_PAGE);

    logger.info("Fetching Multiple Groups with startId: {}, and count: {} and filter expression: {}", PAGINATION_BY_ID_START_PARAM, RESOURCES_PER_PAGE,
        filterExpression);
    PagedByIdentitySearchResult<Group> filteredGroups = groupFailSafeClient.getByFilteredAndPagedById(PAGINATION_BY_ID_START_PARAM, RESOURCES_PER_PAGE,
        filterExpression);

    // @formatter:off
    assertAll("Verify GET Responses",
        () -> assertEquals(1, filteredGroups.getResources().size(), "Verify filtered Groups list size"),
        () -> assertEquals(1, filteredGroups.getTotalResults(), "Verify 'totalResults' of Groups fetched with filter"),
        () -> assertTrue(allGroups.getTotalResults() > 1, "Verify 'totalResults' of Groups fetched without filter is bigger than 1"));
    // @formatter:on
  }

  private void testGetGroupsFilteredByDisplayName(Group filteredGroup) {
    String filterExpression = String.format("%s eq \"%s\"", CoreGroupAttributes.DISPLAY_NAME.scimName(), filteredGroup.getDisplayName());
    logger.info("Fetching Groups by filter expression: {}", filterExpression);
    List<Group> filteredGroups = groupFailSafeClient.getAllByFilter(filterExpression);

    assertAll("Verify GET filtered Groups response",
        () -> assertEquals(1, filteredGroups.size(), "Verify exact number of Groups is fetched"),
        () -> assertEquals(filteredGroup.getId(), filteredGroups.get(0).getId(), "Verify Group with same Id is fetched"));
  }

  private boolean isGroupIdPresentInGroupResponse(final Group testGroup) {
    List<Group> allGroups = groupFailSafeClient.getAllWithIndexPaging();
    for (Group nextGroup : allGroups) {
      if (testGroup.getId().equals(nextGroup.getId())) {

        return true;
      }
    }

    return false;
  }

  private Group getGroupsAndExtractOne(Predicate<Group> condition, String noGroupsFoundMessage) {
    logger.info("Fetching multiple groups");
    List<Group> groups = groupFailSafeClient.getAllWithoutPaging().getResources();
    assumeFalse(groups.isEmpty());

    Optional<Group> groupOptional = groups.stream().filter(condition).findFirst();
    assumeTrue(groupOptional.isPresent(), noGroupsFoundMessage);

    return groupOptional.orElseThrow(IllegalStateException::new);
  }
}
