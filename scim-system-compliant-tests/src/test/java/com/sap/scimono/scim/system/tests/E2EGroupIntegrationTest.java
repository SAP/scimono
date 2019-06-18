
package com.sap.scimono.scim.system.tests;

import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.MemberRef;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.definition.CoreGroupAttributes;
import com.sap.scimono.entity.definition.CoreUserAttributes;
import com.sap.scimono.entity.paging.PagedByIdentitySearchResult;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.patch.PatchBody;
import com.sap.scimono.entity.patch.PatchOperation;
import com.sap.scimono.scim.system.tests.util.CustomTargetSystemRestClient;
import com.sap.scimono.scim.system.tests.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class E2EGroupIntegrationTest extends CompliantSCIMSystemIntegrationTest {
  private static final int RESOURCES_PER_PAGE = 1000;

  private static final int ASSIGNMENT_UPDATE_DELAY = 30;
  public static final int SECONDS = 1000;

  private static User emo;
  private static User testUser1;
  private static User testUser2;
  private static Group groupWithoutMembers;
  private static Group groupWithMembers;
  private static Group testGroup1;

  private static boolean setupDone;

  @BeforeEach
  public void setUpBeforeTest() {
    if (!setupDone) {
      setupDone = true;

      SCIMResponse<User> createUserResponse = userRequest.createUser(buildTestUser("TestEmoUser"));
      assertTrue(createUserResponse.isSuccess());
      emo = createUserResponse.get();
      assertNotNull(emo);

      SCIMResponse<Group> createGroupResponse = groupRequest.createGroup(buildGroup("TestGroupWithoutMember"));
      assertTrue(createGroupResponse.isSuccess());
      groupWithoutMembers = createGroupResponse.get();
      assertNotNull(groupWithoutMembers);
      assertTrue(groupWithoutMembers.getMembers().isEmpty());

      createGroupResponse = groupRequest.createGroup(buildGroup("TestGroupWithMembers", emo.getId(), groupWithoutMembers.getId()));
      assertTrue(createGroupResponse.isSuccess());
      groupWithMembers = createGroupResponse.get();
      assertNotNull(groupWithMembers);
      assertFalse(groupWithMembers.getMembers().isEmpty());

      createUserResponse = userRequest.createUser(buildTestUser("TestGroupPagingUser1"));
      testUser1 = createUserResponse.get();

      createUserResponse = userRequest.createUser(buildTestUser("TestGroupPagingUser2"));
      testUser2 = createUserResponse.get();

      testGroup1 = createGroup(buildGroup("testPagingGroup1", testUser1.getId(), testUser2.getId()));

      createGroup(buildGroup("testPagingGroup2", testUser1.getId()));
      createGroup(buildGroup("testPagingGroup3", testUser2.getId()));
      createGroup(buildGroup("testPagingGroup4", testGroup1.getId()));
      createGroup(buildGroup("testPagingGroup5", testUser2.getId(), testGroup1.getId()));
      createGroup(buildGroup("testPagingGroup6", testUser1.getId(), testGroup1.getId()));
    }
  }

  @Test
  public void testGetGroups() {
    List<Group> fetchedGroups = getAllGroupsWithIndexPaging();

    List<String> fetchedGroupIds = fetchedGroups.stream().map(Group::getId).collect(Collectors.toList());
    assertTrue(fetchedGroupIds.contains(groupWithoutMembers.getId()));
    assertTrue(fetchedGroupIds.contains(groupWithMembers.getId()));
  }

  @Test
  public void testGetGroup() {
    Group fetchedGroupWithoutMembers = getGroup(groupWithoutMembers.getId());

    assertEquals(groupWithoutMembers.getId(), fetchedGroupWithoutMembers.getId());
    assertEquals(groupWithoutMembers.getDisplayName(), fetchedGroupWithoutMembers.getDisplayName());
    assertTrue(fetchedGroupWithoutMembers.getMembers().isEmpty());

    Group fetchedGroupWithMembers = getGroup(groupWithMembers.getId());
    assertEquals(groupWithMembers.getId(), fetchedGroupWithMembers.getId());
    assertEquals(groupWithMembers.getDisplayName(), fetchedGroupWithMembers.getDisplayName());
    assertFalse(fetchedGroupWithMembers.getMembers().isEmpty());
  }

  @Test
  public void testCreateGroup() {
    String testGroupName = "test2CreateGroupWithMembers";
    Group group = buildGroup(testGroupName, emo.getId(), groupWithoutMembers.getId());
    Group createdTestGroupWithMembers = createGroup(group);

    assertNotNull(createdTestGroupWithMembers);
    assertEquals(36, createdTestGroupWithMembers.getId().length());
    assertEquals(testGroupName, createdTestGroupWithMembers.getDisplayName());
    assertEquals(2, createdTestGroupWithMembers.getMembers().size());

    assertNotNull(createdTestGroupWithMembers.getMeta());
    assertEquals(Group.RESOURCE_TYPE_GROUP, createdTestGroupWithMembers.getMeta().getResourceType());

    assertTrue(createdTestGroupWithMembers.getMeta().getLocation().endsWith(constructResourceLocation(createdTestGroupWithMembers)));
  }

  @Test
  public void testUpdateGroup() {
    int oldSize = groupWithMembers.getMembers().size();

    User testUser = createUser(buildTestUser("testJoroUser"));
    MemberRef groupMemberUser = buildGroupMemberResourceWithId(testUser.getId());
    Group groupForUpdate = new Group.Builder(groupWithMembers).addMember(groupMemberUser).build();

    Group updatedGroupWithMembers = updateGroup(groupForUpdate.getId(), groupForUpdate);
    assertNotNull(updatedGroupWithMembers);
    assertEquals(oldSize + 1, updatedGroupWithMembers.getMembers().size());
  }

  @Test
  public void testCreateGroupWith500Members() {
    Group groupWithManyMembers = createGroup(buildGroup("test2CreateGroupWithMembers500"));

    List<MemberRef> members = new ArrayList<>(500);
    for (int i = 1; i <= 500; i++) {
      User tempUser = createUser(buildTestUser(String.format("usercopy%d", i)));
      members.add(new MemberRef.Builder().setValue(tempUser.getId()).build());
    }

    Group groupWithAddedMembers = new Group.Builder(groupWithManyMembers).addMembers(members).build();
    Group updatedgroupWithManyMembers = updateGroup(groupWithAddedMembers.getId(), groupWithAddedMembers);

    assertNotNull(updatedgroupWithManyMembers);
    assertEquals(500, updatedgroupWithManyMembers.getMembers().size());
  }

  @Test
  public void testDeleteGroup() {
    Group testGroup = buildGroup("TestGroupWithMembersForDeletion", emo.getId(), groupWithoutMembers.getId());
    
    Group groupForDeletion = createGroup(testGroup);
    assertNotNull(groupForDeletion);

    groupForDeletion = getGroup(groupForDeletion.getId());
    assertNotNull(groupForDeletion);

    SCIMResponse<Void> deleteGroupResponse = groupRequest.deleteGroup(groupForDeletion.getId());
    assertTrue(deleteGroupResponse.isSuccess(), "SCIM remove request failed - Group id: " + groupForDeletion.getId());

    SCIMResponse<?> readGroupResponse = groupRequest.readSingleGroup(groupForDeletion.getId());
    assertEquals(NOT_FOUND.getStatusCode(), readGroupResponse.getStatusCode());
  }

  @Test
  public void testCreateGroupWithMemberAndVisitMemberReference() {
    String testGroupName = "testCreateGroupWithMemberAndVisitMemberReference";
    Group createdTestGroupWithMembers = createGroup(buildGroup(testGroupName, emo.getId()));

    assertNotNull(createdTestGroupWithMembers);

    Group createdGroupWithMemberLocation = getGroup(createdTestGroupWithMembers.getId());
    String memberRef = createdGroupWithMemberLocation.getMembers().iterator().next().getReference();

    assertTrue(memberRef.endsWith("Users/" + emo.getId()));

    SCIMResponse<User> scimResponse = configureScimClientService(memberRef.substring(0, memberRef.lastIndexOf("Users/" + emo.getId())))
        .buildUserRequest()
        .readSingleUser(emo.getId());

    assertTrue(scimResponse.isSuccess());
    assertNotNull(scimResponse.get());
  }

  @Test
  public void testGetGroupsAfterGroupMemberDeletion() {
    User createdTestUser = createUser(buildTestUser("testUserForGroupMemDel"));

    String testGroupName = "testGroupForGroupMemDel";
    Group createdTestGroupWithMember = createGroup(buildGroup(testGroupName, createdTestUser.getId()));

    assertTrue(isGroupIdPresentInGroupResponse(createdTestGroupWithMember));

    deleteUser(createdTestUser.getId());

    assertTrue(isGroupIdPresentInGroupResponse(createdTestGroupWithMember));
  }

  @Test
  public void testDeleteGroupMembersUpdate() throws InterruptedException {
    User createdTestUser2 = createUser(buildTestUser("testDeleteGroupMembersUpdateUsr1"));
    Group createdMemberTestGroup = createGroup(buildGroup("testDeleteGroupMembersUpdateGrp1"));
    User createdTestUser3 = createUser(buildTestUser("testDeleteGroupMembersUpdateUsr2"));

    String testGroupName = "testDeleteGroupMembersUpdateGrp2";
    Group createdTestGroupWithMembers = createGroup(
        buildGroup(testGroupName, createdTestUser2.getId(), createdMemberTestGroup.getId(), createdTestUser3.getId()));

    assertEquals(3, getGroup(createdTestGroupWithMembers.getId()).getMembers().size());
    assertTrue(isGroupIdPresentInGroupResponse(createdTestGroupWithMembers));

    deleteUser(createdTestUser2.getId());

    SCIMResponse<?> deleteGroupResponse = groupRequest.deleteGroup(createdMemberTestGroup.getId());
    assertEquals(SC_NO_CONTENT, deleteGroupResponse.getStatusCode());

    deleteUser(createdTestUser3.getId());

    int i = 0;
    int groupSize = 100;
    do {
      groupSize = getGroup(createdTestGroupWithMembers.getId()).getMembers().size();
      if (groupSize == 0) {
        break;
      }
      Thread.sleep(ASSIGNMENT_UPDATE_DELAY * SECONDS);
      i++;
    } while (i < 20);

    assertEquals(0, getGroup(createdTestGroupWithMembers.getId()).getMembers().size());
  }

  @Test
  public void testAddGroupMemberWithPATCH() {
    User originalUser = createUser(buildTestUser("testAddGroupMemberWithPATCH-originalUser"));
    Group createdMemberTestGroup = createGroup(buildGroup("testAddGroupMemberWithPATCH-GR", originalUser.getId()));

    User patchedUser = createUser(buildTestUser("testAddGroupMemberWithPATCH-patchedUser"));
    MemberRef userMemberRef = new MemberRef.Builder().setValue(patchedUser.getId()).setType(MemberRef.Type.USER).build();
    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.ADD, MEMBERS.scimName(), TestData.buildMultivaluedJSONNode(userMemberRef));

    patchGroup(createdMemberTestGroup.getId(), patchBody);

    Group patchedGroup = getGroup(createdMemberTestGroup.getId());
    assertEquals(2, patchedGroup.getMembers().size());
    assertTrue(patchedGroup.getMembers().stream().map(MemberRef::getValue).anyMatch(patchedUser.getId()::equals),
        "Group does not contain member with id: " + patchedUser.getId());
  }

  @Test
  public void testReplaceGroupMemberWithPATCH() {
    User originalUser = createUser(buildTestUser("testReplaceGroupMemberWithPATCH-originalUser"));
    Group createdMemberTestGroup = createGroup(buildGroup("testReplaceGroupMemberWithPATCH-GR", originalUser.getId()));

    User patchedUser = createUser(buildTestUser("testReplaceGroupMemberWithPATCH-patchedUser"));
    MemberRef userMemberRef = new MemberRef.Builder().setValue(patchedUser.getId()).setType(MemberRef.Type.USER).build();
    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, MEMBERS.scimName(), TestData.buildMultivaluedJSONNode(userMemberRef));

    patchGroup(createdMemberTestGroup.getId(), patchBody);

    Group patchedGroup = getGroup(createdMemberTestGroup.getId());
    assertEquals(1, patchedGroup.getMembers().size());
    assertTrue(patchedGroup.getMembers().stream().map(MemberRef::getValue).anyMatch(patchedUser.getId()::equals),
        "Group does not contain member with id: " + patchedUser.getId());
  }

  @Test
  public void testRemoveGroupMemberWithPATCH() {
    User originalUser = createUser(buildTestUser("testRemoveGroupMemberWithPATCH-originalUser"));
    Group createdMemberTestGroup = createGroup(buildGroup("testRemoveGroupMemberWithPATCH-GR", originalUser.getId()));

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REMOVE, MEMBERS.scimName(), null);

    patchGroup(createdMemberTestGroup.getId(), patchBody);

    Group patchedGroup = getGroup(createdMemberTestGroup.getId());
    assertEquals(0, patchedGroup.getMembers().size());
  }

  @Test
  public void testGroupMemberDisplayNameUpdateWithPUTRequest() throws InterruptedException {
    String updateMemberDisplayNameTestUser = "testUserForDisplayNameUpdateWithPUT";
    User createdTestUser = createUser(buildTestUser(updateMemberDisplayNameTestUser));

    String updateMemberDisplayNameTestGroup = "testGroupForDisplayNameUpdateWithPUT";
    Group createdMemberTestGroup = createGroup(buildGroup(updateMemberDisplayNameTestGroup));

    String testGroupName = "testGroupContainingUpdatedWithPUTMembers";
    Group createdTestGroupWithMembers = createGroup(buildGroup(testGroupName, createdTestUser.getId(), createdMemberTestGroup.getId()));

    assertEquals(2, getGroup(createdTestGroupWithMembers.getId()).getMembers().size());
    assertTrue(isGroupIdPresentInGroupResponse(createdTestGroupWithMembers));

    String updateMemberDisplayNameTestUserNewName = updateMemberDisplayNameTestUser.concat("v22");
    User updatedDisplayNameMemberUser = new User.Builder(createdTestUser).setDisplayName(updateMemberDisplayNameTestUserNewName).build();
    updateUser(createdTestUser.getId(), updatedDisplayNameMemberUser);

    String updateMemberDisplayNameTestGroupNewName = updateMemberDisplayNameTestGroup.concat("v22");
    Group updatedDisplayNameMemberGroup = new Group.Builder(createdMemberTestGroup).setDisplayName(updateMemberDisplayNameTestGroupNewName).build();
    updateGroup(createdMemberTestGroup.getId(), updatedDisplayNameMemberGroup);

    Thread.sleep(ASSIGNMENT_UPDATE_DELAY * SECONDS);

    List<String> groupMembersDisplayNames = getGroupMembersDisplayNames(createdTestGroupWithMembers, updateMemberDisplayNameTestUserNewName,
        updateMemberDisplayNameTestGroupNewName);

    assertEquals(2, groupMembersDisplayNames.size());
    assertTrue(groupMembersDisplayNames.contains(updateMemberDisplayNameTestUserNewName));
    assertTrue(groupMembersDisplayNames.contains(updateMemberDisplayNameTestGroupNewName));
  }

  @Test
  public void testGroupMemberDisplayNameUpdateWithPATCHRequest() throws InterruptedException {
    User createdTestUser = createUser(buildTestUser("testUserForDisplayNameUpdateWithPATCH"));
    Group createdMemberTestGroup = createGroup(buildGroup("testGroupForDisplayNameUpdateWithPATCH"));

    Group createdTestGroupWithMembers = createGroup(
        buildGroup("testGroupContainingUpdatedPATCHMembers", createdTestUser.getId(), createdMemberTestGroup.getId()));

    assertEquals(2, getGroup(createdTestGroupWithMembers.getId()).getMembers().size());
    assertTrue(isGroupIdPresentInGroupResponse(createdTestGroupWithMembers));

    String newUserDisplayName = "updatedUserDisplayName";
    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, CoreUserAttributes.DISPLAY_NAME.scimName(), newUserDisplayName);

    patchUser(createdTestUser.getId(), patchBody);

    String newGroupDisplayName = "updatedGroupDisplayName";
    patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, CoreGroupAttributes.DISPLAY_NAME.scimName(), newGroupDisplayName);

    patchGroup(createdMemberTestGroup.getId(), patchBody);

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

    PagedByIndexSearchResult<Group> getPagedGroupsSearchResult = getGroupsPagedByIndex(startIndex, count);

    assertEquals(Long.valueOf(startIndex), getPagedGroupsSearchResult.getStartIndex());
    assertEquals(count, getPagedGroupsSearchResult.getItemsPerPage());
    assertTrue(getPagedGroupsSearchResult.getTotalResults() > 0);

    assertTrue(getPagedGroupsSearchResult.getResources().isEmpty());
  }

  @Test
  public void testGetUsersTotalCountWithStartId() {
    String startId = PAGINATION_BY_ID_START_PARAM;
    int count = 0;

    PagedByIdentitySearchResult<Group> getPagedGroupsSearchResult = getGroupsPagedById(startId, count);

    assertEquals(startId, getPagedGroupsSearchResult.getStartId());
    assertEquals(36, getPagedGroupsSearchResult.getNextId().length());
    assertEquals(count, getPagedGroupsSearchResult.getItemsPerPage());
    assertTrue(getPagedGroupsSearchResult.getTotalResults() > 0);

    assertTrue(getPagedGroupsSearchResult.getResources().isEmpty());
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
      Group createdTestGroup = createGroup(buildGroup(testGroupName + groupNameUniquenessCounter));
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
    Group createdTestGroup = (buildGroup("testGetGroupsPagingStartIdWithUpperCase"));
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
    List<Group> allGroups = getAllGroupsWithIdPaging();

    do {
      getPagedGroupsResult = getGroupsPagedByIndex(startIndex, count);

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
    List<Group> allGroups = getAllGroupsWithIndexPaging();

    do {
      pagedGroups = getGroupsPagedById(startId, count);

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
    Collection<Group> testGroups = IntStream.range(0, 5).mapToObj(operand -> createGroup(buildGroup(displayName))).collect(Collectors.toList());

    Collection<Group> filteredGroups = getGroupsFiltered(String.format("%s eq \"%s\"", CoreGroupAttributes.DISPLAY_NAME.scimName(), displayName));
    assertNotNull(filteredGroups);
    assertTrue(filteredGroups.containsAll(testGroups));
  }

  @Test
  public void testGetFilteredGroupsTotalCount() {
    String testDisplayName = "testGetFilteredGroupsTotalCount";
    createGroup(buildGroup(testDisplayName));

    String filterExpression = String.format("%s eq \"%s\"", CoreGroupAttributes.DISPLAY_NAME.scimName(), testDisplayName);

    PagedByIdentitySearchResult<Group> allGroups = getGroupsPagedById(PAGINATION_BY_ID_START_PARAM, RESOURCES_PER_PAGE);
    PagedByIdentitySearchResult<Group> filteredGroups = getGroupsFilteredAndPagedById(PAGINATION_BY_ID_START_PARAM, RESOURCES_PER_PAGE,
        filterExpression);

    assertEquals(1, filteredGroups.getResources().size());
    assertEquals(1, filteredGroups.getTotalResults());

    assertTrue(allGroups.getTotalResults() > 1);
  }

  private List<String> getGroupMembersDisplayNames(final Group parentGroup, final String newUserDisplayName, final String newGroupDisplayName)
      throws InterruptedException {
    int i = 0;
    List<String> groupMembersDisplayNames = null;
    do {
      groupMembersDisplayNames = new ArrayList<>();
      Group createdTestGroupAfterMembersUpdate = getGroup(parentGroup.getId());
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
}
