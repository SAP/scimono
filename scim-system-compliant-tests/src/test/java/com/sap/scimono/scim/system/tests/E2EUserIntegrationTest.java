/**
 * Copyright (c) 2016 by SAP Labs Bulgaria, url: http://www.sap.com All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP SE, Walldorf. You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered into with SAP.
 *
 * Created on Dec 16, 2016 by i061675
 *
 */

package com.sap.scimono.scim.system.tests;

import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.entity.Address;
import com.sap.scimono.entity.Email;
import com.sap.scimono.entity.EnterpriseExtension;
import com.sap.scimono.entity.Entitlement;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.Im;
import com.sap.scimono.entity.Name;
import com.sap.scimono.entity.PhoneNumber;
import com.sap.scimono.entity.Photo;
import com.sap.scimono.entity.Role;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.X509Certificate;
import com.sap.scimono.entity.base.Extension;
import com.sap.scimono.entity.base.MultiValuedAttribute;
import com.sap.scimono.entity.base.MultiValuedAttributeType;
import com.sap.scimono.entity.paging.PagedByIdentitySearchResult;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.scim.system.tests.util.CustomTargetSystemRestClient;
import com.sap.scimono.scim.system.tests.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.sap.scimono.api.API.COUNT_PARAM;
import static com.sap.scimono.api.API.USERS;
import static com.sap.scimono.client.query.ResourcePageQuery.identityPageQuery;
import static com.sap.scimono.client.query.ResourcePageQuery.indexPageQuery;
import static com.sap.scimono.entity.paging.PagedByIdentitySearchResult.PAGINATION_BY_ID_END_PARAM;
import static com.sap.scimono.entity.paging.PagedByIdentitySearchResult.PAGINATION_BY_ID_START_PARAM;
import static com.sap.scimono.entity.paging.PagedByIndexSearchResult.DEFAULT_COUNT;
import static com.sap.scimono.scim.system.tests.util.CustomTargetSystemRestClient.USER_LIST_RESPONSE_TYPE_INDEX_PAGING;
import static com.sap.scimono.scim.system.tests.util.TestData.buildFullUserRepresentation;
import static com.sap.scimono.scim.system.tests.util.TestData.buildGroup;
import static com.sap.scimono.scim.system.tests.util.TestData.buildTestUser;
import static com.sap.scimono.scim.system.tests.util.TestData.buildTestUserWithExtentions;
import static com.sap.scimono.scim.system.tests.util.TestData.extractUserIds;
import static com.sap.scimono.scim.system.tests.util.TestUtil.constructResourceLocation;
import static java.util.Collections.singletonMap;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class E2EUserIntegrationTest extends CompliantSCIMSystemIntegrationTest {

  private static boolean setupDone;
  private static final int RESOURCES_PER_PAGE = 1000;

  private static User emo;
  private static User stefan;
  private static User manager;

  @Override
  protected List<User> getUsersFiltered(String filterExpression) {
    return super.getUsersFiltered(filterExpression);
  }

  @BeforeEach
  public void setUpBeforeTest() {
    if (!setupDone) {
      setupDone = true;

      emo = createUser(buildTestUser("TestUserEmo"));
      stefan = createUser(buildTestUser("TestUserStefan"));
      manager = createUser(buildTestUser("TestUserManager"));

      createUser(buildTestUser("TestPagingUser1"));
      createUser(buildTestUser("TestPagingUser2"));
      createUser(buildTestUser("TestPagingUser3"));
      createUser(buildTestUser("TestPagingUser4"));
      createUser(buildTestUser("TestPagingUser5"));
      createUser(buildTestUser("TestPagingUser6"));

      assertNotNull(emo);
      assertNotNull(stefan);
    }
  }

  @Test
  public void testGetUsers() {
    List<User> fetchedUsers = getAllUsersWithIndexPaging();
    assertNotNull(fetchedUsers);
    assertTrue(fetchedUsers.size() > 2);
  }

  @Test
  public void testCreateUser() {
    User createdTestUser = createUser(buildTestUser("test2CreateUser"));

    assertNotNull(createdTestUser);
    assertTrue(createdTestUser.isActive());
    assertEquals(36, createdTestUser.getId().length());

    User fetchedUser = getUser(createdTestUser.getId());

    assertEquals(createdTestUser.getUserName(), fetchedUser.getUserName());
    assertEquals(createdTestUser.getName().getGivenName(), fetchedUser.getName().getGivenName());
    assertEquals(createdTestUser.getName().getFamilyName(), fetchedUser.getName().getFamilyName());
    assertEquals(createdTestUser.getName().getHonorificSuffix(), fetchedUser.getName().getHonorificSuffix());
    assertNull(fetchedUser.getNickName());

    assertNotNull(fetchedUser.getMeta());
    assertEquals(User.RESOURCE_TYPE_USER, fetchedUser.getMeta().getResourceType());

    assertTrue(fetchedUser.getMeta().getLocation().endsWith(constructResourceLocation(fetchedUser)));
  }

  @Test
  public void testCreateUserWithMultipleEmails() {
    Email personalEmail = TestData.buildPersonalEmailWithDefaultAttrs();
    Email workEmail = TestData.buildWorkEmailWithDefaultAttrs();

    Collection<Email> userEmails = new ArrayList<>();
    userEmails.add(personalEmail);
    userEmails.add(workEmail);

    User testUser = createUser(TestData.setAttributesToATestUser("testCreateUserWithMultipleEmails201").addEmails(userEmails).build());
    User createdUser = getUser(testUser.getId());

    assertEquals(2, createdUser.getEmails().size());
  }

  @Test
  public void createFullTestUser() {
    User fullUserRepresentation = createUser(buildFullUserRepresentation("test3createUser", manager.getId()));
    assertNotNull(fullUserRepresentation);
    User fetchedUser = getUser(fullUserRepresentation.getId());
    assertUserAllSingleValueAttribute(fullUserRepresentation, fetchedUser);

    assertEquals(User.RESOURCE_TYPE_USER, fetchedUser.getMeta().getResourceType());

    assertTrue(fetchedUser.getMeta().getLocation().endsWith(constructResourceLocation(fetchedUser)));

    assertUserAllSingleValueAttribute(fullUserRepresentation, fetchedUser);
    assertEnterpriseAttributes(fullUserRepresentation, fetchedUser);

    Email expectedPrimaryMail = fullUserRepresentation.getEmails().get(0);
    Email expectedNotPrimaryMail = fullUserRepresentation.getEmails().get(1);

    Email actualPrimaryMail = fetchedUser.getEmails().stream().filter(email -> email.isPrimary()).findAny().get();
    Email actualNotPrimaryMail = fetchedUser.getEmails().stream().filter(email -> !email.isPrimary()).findAny().get();

    Photo expectedPhoto = fullUserRepresentation.getPhotos().get(0);
    Photo actualPhoto = fetchedUser.getPhotos().get(0);

    Im expectedIm = fullUserRepresentation.getIms().get(0);
    Im actualIm = fetchedUser.getIms().get(0);

    PhoneNumber expectedPhoneNumber = fullUserRepresentation.getPhoneNumbers().get(0);
    PhoneNumber actualPhoneNumber = fetchedUser.getPhoneNumbers().get(0);

    Entitlement expectedEntitlement = fullUserRepresentation.getEntitlements().get(0);
    Entitlement actualEntitlement = fetchedUser.getEntitlements().get(0);

    Role expectedrole = fullUserRepresentation.getRoles().get(0);
    Role actualRole = fetchedUser.getRoles().get(0);

    X509Certificate expectedCertificate = fullUserRepresentation.getX509Certificates().get(0);
    X509Certificate actualCertificate = fetchedUser.getX509Certificates().get(0);

    Address expectedAddress = fullUserRepresentation.getAddresses().get(0);
    Address actualAddress = fetchedUser.getAddresses().get(0);

    assertUserMultivaluedAttibute(expectedPrimaryMail, expectedPrimaryMail.getType(), actualPrimaryMail, actualPrimaryMail.getType());
    assertUserMultivaluedAttibute(expectedNotPrimaryMail, expectedNotPrimaryMail.getType(), actualNotPrimaryMail, actualNotPrimaryMail.getType());
    assertUserMultivaluedAttibute(expectedPhoto, expectedPhoto.getType(), actualPhoto, actualPhoto.getType());
    assertUserMultivaluedAttibute(expectedIm, expectedIm.getType(), actualIm, actualIm.getType());
    assertUserMultivaluedAttibute(expectedPhoneNumber, expectedPhoneNumber.getType(), actualPhoneNumber, actualPhoneNumber.getType());
    assertUserMultivaluedAttibute(expectedEntitlement, expectedEntitlement.getType(), actualEntitlement, actualEntitlement.getType());
    assertUserMultivaluedAttibute(expectedrole, expectedrole.getType(), actualRole, actualRole.getType());
    assertUserMultivaluedAttibute(expectedCertificate, expectedCertificate.getType(), actualCertificate, actualCertificate.getType());
    assertEquals(expectedAddress.getType(), actualAddress.getType());
    assertEquals(expectedAddress.getCountry(), actualAddress.getCountry());
    assertEquals(expectedAddress.getFormatted(), actualAddress.getFormatted());
    assertEquals(expectedAddress.getLocality(), actualAddress.getLocality());
    assertEquals(expectedAddress.getRegion(), actualAddress.getRegion());
    assertEquals(expectedAddress.getStreetAddress(), actualAddress.getStreetAddress());
    assertEquals(expectedAddress.getPostalCode(), actualAddress.getPostalCode());
  }

  @Test
  public void testUpdateUser() {
    User emoNewNickname = new User.Builder(emo).setNickName("topalka").build();
    User updatedEmo = updateUser(emo.getId(), emoNewNickname);
    assertNotNull(updatedEmo);
    // check all previous data is intact
    assertEquals(emo.isActive(), updatedEmo.isActive());
    assertEquals(emo.getId(), updatedEmo.getId());
    assertEquals(emo.getUserName(), updatedEmo.getUserName());
    assertEquals(emo.getName().getGivenName(), updatedEmo.getName().getGivenName());
    assertEquals(emo.getName().getFamilyName(), updatedEmo.getName().getFamilyName());
    assertEquals(emo.getName().getHonorificSuffix(), updatedEmo.getName().getHonorificSuffix());

    assertEquals("topalka", updatedEmo.getNickName());

    assertNotNull(emo.getMeta());
    assertEquals(User.RESOURCE_TYPE_USER, emo.getMeta().getResourceType());
    assertEquals(emo.getMeta().getLocation(), updatedEmo.getMeta().getLocation());
  }

  @Test
  public void testGetUser() {
    User fetchedUser = getUser(stefan.getId());
    assertEquals(stefan, fetchedUser);
  }

  @Test
  public void testGetUserByNotExistingUsername() {
    SCIMResponse<User> scimResponse =  userRequest.readSingleUser("Me");

    assertFalse(scimResponse.isSuccess());
    assertEquals(NOT_FOUND.getStatusCode(), scimResponse.getStatusCode());
  }

  @Test
  public void testGetUserByUsername() {
    // oauth_client_$clientID$ is the user, used to run integration tests on canary
    User me = createUser(buildTestUser("atestuser"));

    User fetchedUser = getUser(me.getId());
    assertEquals(me, fetchedUser);
  }

  @Test
  public void testDeleteUser() {
    User createdTestUser = createUser(buildTestUser("test5DeleteUser"));
    assertNotNull(createdTestUser);

    createdTestUser = getUser(createdTestUser.getId());
    assertNotNull(createdTestUser);

    SCIMResponse<Void> scimResponse = userRequest.deleteUser(createdTestUser.getId());
    assertTrue(scimResponse.isSuccess());
    assertEquals(SC_NO_CONTENT, scimResponse.getStatusCode());

    SCIMResponse<User> getUserResponse = userRequest.readSingleUser(createdTestUser.getId());
    assertFalse(getUserResponse.isSuccess());
    assertEquals(SC_NOT_FOUND, getUserResponse.getStatusCode());
  }

  @Test
  public void testGetUserWithGroupsAndMultiValueAttrs() {
    Email workMail = TestData.buildWorkEmailWithDefaultAttrs();
    Email homeMail = TestData.buildPersonalEmailWithDefaultAttrs();

    String testUserName = "testGetUserWithGroupsAndMultiValueAttrs";

    // @formatter:off
    User testUserWithMultiValueAttrs = new User.Builder(testUserName).setName(
        new Name.Builder()
        .setGivenName("Topalka")
        .setFamilyName("Angelova")
        .setHonorificSuffix("II")
        .build())
        .setDisplayName(testUserName)
        .setActive(true)
        .addEmail(workMail)
        .addEmail(homeMail)
        .build();
    // @formatter:on

    User createdTestUser = createUser(testUserWithMultiValueAttrs);
    assertEquals(2, createdTestUser.getEmails().size());

    Group testGroupWithMember1 = createGroup(buildGroup("testGroupWithMember1", createdTestUser.getId()));
    Group testGroupWithMember2 = createGroup(buildGroup("testGroupWithMember2", createdTestUser.getId(), testGroupWithMember1.getId()));
    createGroup(buildGroup("testGroupWithMember3", createdTestUser.getId(), testGroupWithMember1.getId(), testGroupWithMember2.getId()));

    User returnedTestUser = getUser(createdTestUser.getId());

    assertEquals(2, returnedTestUser.getEmails().size());
  }

  @Test
  public void testGetUsersFilteredByUsername() {
    User testUser = createUser(buildTestUser("FilteredUsername"));

    List<User> filteredUsers = getUsersFiltered("userName eq \"FilteredUsername\"");
    assertNotNull(filteredUsers);
    assertEquals(1, filteredUsers.size());
    assertEquals(testUser, filteredUsers.get(0));
  }

  @Test
  public void testGetUsersFilteredByFullCoreSchemaAttributePath() {
    String username = "FilteredFullUsernamePath";
    User testUser = createUser(buildTestUser(username));

    List<User> filteredUsers = getUsersFiltered(String.format("%s:userName eq \"%s\"", User.SCHEMA, username));
    assertNotNull(filteredUsers);
    assertEquals(1, filteredUsers.size());
    assertEquals(testUser, filteredUsers.get(0));
  }

  @Test
  public void testGetUsersFilteredByEnterpriseSchemaAttribute() {
    String userName = "FilteredFullEmpolyeeNumberPath-Username";
    String employeeNumber = String.valueOf(System.currentTimeMillis());

    Extension schemaExtension = new EnterpriseExtension.Builder().setEmployeeNumber(employeeNumber).build();
    User testUser = createUser(buildTestUserWithExtentions(userName, Collections.singletonList(schemaExtension)));

    List<User> filteredUsers = getUsersFiltered(String.format("%s:employeeNumber eq \"%s\"", EnterpriseExtension.ENTERPRISE_URN, employeeNumber));

    assertNotNull(filteredUsers);
    assertEquals(1, filteredUsers.size());
    assertEquals(testUser, filteredUsers.get(0));
  }

  @Test
  public void testGetUsersFilteredRoles() {
    User testUser = buildTestUser("FilteredRoles");
    User testUserWithRole = createUser(testUser.builder().addRole(new Role(null, "FilteredRoles", null, false, null, Role.Type.of("test"))).build());

    List<User> filteredUsers = getUsersFiltered("roles.value eq \"FilteredRoles\"");
    assertNotNull(filteredUsers);
    assertEquals(1, filteredUsers.size());
    assertEquals(testUserWithRole, filteredUsers.get(0));
  }

  @Test
  public void testGetUsersFilteredGroups() {
    User testUser = createUser(buildTestUser("FilteredGroupsUser"));
    Group testGroup = createGroup(buildGroup("FilteredGroups", testUser.getId()));

    List<User> filteredUsers = getUsersFiltered("groups.display eq \"FilteredGroups\"");
    assertNotNull(filteredUsers);
    assertEquals(1, filteredUsers.size());
    User filteredUser = filteredUsers.get(0);
    assertEquals(testUser.getId(), filteredUser.getId());

    assertTrue(filteredUser.getGroups().get(0).getReference().endsWith(constructResourceLocation(testGroup)));
  }

  @Test
  public void testGetUsersTotalCountWithStartIndex() {
    int startIndex = 1;
    int count = 0;

    PagedByIndexSearchResult<User> getPagedUsersSearchResult = getUsersPagedByIndex(startIndex, count);

    assertEquals(Long.valueOf(startIndex), getPagedUsersSearchResult.getStartIndex());
    assertEquals(count, getPagedUsersSearchResult.getItemsPerPage());
    assertTrue(getPagedUsersSearchResult.getTotalResults() > 0);

    assertTrue(getPagedUsersSearchResult.getResources().isEmpty());
  }

  @Test
  public void testGetUsersTotalCountWithStartId() {
    String startId = PAGINATION_BY_ID_START_PARAM;
    int count = 0;

    PagedByIdentitySearchResult<User> getPagedUsersSearchResult = getUsersPagedById(startId, count);

    assertEquals(startId, getPagedUsersSearchResult.getStartId());
    assertEquals(36, getPagedUsersSearchResult.getNextId().length());
    assertEquals(count, getPagedUsersSearchResult.getItemsPerPage());
    assertTrue(getPagedUsersSearchResult.getTotalResults() > 0);

    assertTrue(getPagedUsersSearchResult.getResources().isEmpty());
  }

  @Test
  public void testGetUsersNegativeCountAndStartIndex() {
    int startIndex = 1;
    int count = -10;
    PagedByIndexSearchResult<User> getPagedUsersSearchResult = getUsersPagedByIndex(startIndex, count);

    assertEquals(Long.valueOf(startIndex), getPagedUsersSearchResult.getStartIndex());
    assertEquals(0, getPagedUsersSearchResult.getItemsPerPage());
    assertTrue(getPagedUsersSearchResult.getTotalResults() > 0);

    assertTrue(getPagedUsersSearchResult.getResources().isEmpty());
  }

  @Test
  public void testGetUsersNegativeCountAndStartId() {
    String startId = PAGINATION_BY_ID_START_PARAM;
    int count = -10;
    PagedByIdentitySearchResult<User> getPagedUsersSearchResult = getUsersPagedById(startId, count);

    assertEquals(startId, getPagedUsersSearchResult.getStartId());
    assertEquals(36, getPagedUsersSearchResult.getNextId().length());
    assertEquals(0, getPagedUsersSearchResult.getItemsPerPage());
    assertTrue(getPagedUsersSearchResult.getTotalResults() > 0);

    assertTrue(getPagedUsersSearchResult.getResources().isEmpty());
  }

  @Test
  public void testGetUsersDefaultStartIndex() {
    int count = 1;

    PagedByIndexSearchResult<User> getPagedUsersSearchResult = CustomTargetSystemRestClient.INSTANCE.getEntitiesHttpResponse(USERS, singletonMap(COUNT_PARAM, count))
        .readEntity(USER_LIST_RESPONSE_TYPE_INDEX_PAGING);

    assertEquals(Long.valueOf(1), getPagedUsersSearchResult.getStartIndex());
    assertEquals(count, getPagedUsersSearchResult.getItemsPerPage());
    assertTrue(getPagedUsersSearchResult.getTotalResults() > 0);

    assertEquals(1, getPagedUsersSearchResult.getResources().size());
  }

  @Test
  public void testGetUsersDefaultCountWithStartIndex() {
    int startIndex = 1;

    SCIMResponse<PagedByIndexSearchResult<User>> pagedUsersResponse = userRequest.readMultipleUsers(indexPageQuery().withStartIndex(startIndex));
    assertTrue(pagedUsersResponse.isSuccess());

    PagedByIndexSearchResult<User> pagedUsersResult = pagedUsersResponse.get();
    assertEquals(Long.valueOf(1), pagedUsersResult.getStartIndex());
    assertTrue(pagedUsersResult.getTotalResults() > 0);

    if (pagedUsersResult.getTotalResults() <= Long.parseLong(DEFAULT_COUNT)) {
      assertEquals(pagedUsersResult.getTotalResults(), pagedUsersResult.getItemsPerPage());
    } else {
      assertEquals(Long.parseLong(DEFAULT_COUNT), pagedUsersResult.getItemsPerPage());
    }
  }

  @Test
  public void testGetUsersDefaultCountWithStartId() {
    String startId = PAGINATION_BY_ID_START_PARAM;

    SCIMResponse<PagedByIdentitySearchResult<User>> pagedUsersResponse = userRequest.readMultipleUsers(identityPageQuery().withStartId(startId));
    assertTrue(pagedUsersResponse.isSuccess());

    PagedByIdentitySearchResult<User> pagedUsersResult = pagedUsersResponse.get();

    assertEquals(startId, pagedUsersResult.getStartId());
    assertTrue(pagedUsersResult.getTotalResults() > 0);

    if (pagedUsersResult.getTotalResults() <= Long.parseLong(DEFAULT_COUNT)) {
      assertEquals(pagedUsersResult.getTotalResults(), pagedUsersResult.getItemsPerPage());
      assertEquals(PAGINATION_BY_ID_END_PARAM, pagedUsersResult.getNextId());

    } else {
      assertEquals(Long.parseLong(DEFAULT_COUNT), pagedUsersResult.getItemsPerPage());
      assertEquals(36, pagedUsersResult.getNextId().length());
    }
  }

  @Test
  public void testGetUsersPagingStartIdEqEnd() {
    String testUserName = "testGetUsersPagingStartIdEqEnd";
    String testUserId;
    int userNameUniquenessCounter = 1;

    do {
      User createdTestUser = createUser(buildTestUser(testUserName + userNameUniquenessCounter));
      testUserId = createdTestUser.getId();
      userNameUniquenessCounter++;

    } while (!testUserId.startsWith("f"));

    String startId = PAGINATION_BY_ID_END_PARAM;

    SCIMResponse<PagedByIdentitySearchResult<User>> pagedUsersResponse = userRequest.readMultipleUsers(identityPageQuery().withStartId(startId));
    assertTrue(pagedUsersResponse.isSuccess());

    PagedByIdentitySearchResult<User> pagedUsersResult = pagedUsersResponse.get();

    assertTrue(pagedUsersResult.getTotalResults() > 0);
    assertEquals(0, pagedUsersResult.getItemsPerPage());
    assertEquals(startId, pagedUsersResult.getStartId());
    assertEquals(PAGINATION_BY_ID_END_PARAM, pagedUsersResult.getNextId());

  }

  @Test
  public void testGetUsersPagingStartIdWithUpperCase() {
    User createdTestUser = createUser(buildTestUser("testGetUsersPagingStartIdWithUpperCase"));
    SCIMResponse<PagedByIdentitySearchResult<User>> pagedUsersResponse = userRequest.readMultipleUsers(identityPageQuery().withStartId(createdTestUser.getId()));
    assertTrue(pagedUsersResponse.isSuccess());

    PagedByIdentitySearchResult<User> pagedUsersResult = pagedUsersResponse.get();

    assertTrue(pagedUsersResult.getTotalResults() > 0);
    assertTrue(pagedUsersResult.getItemsPerPage() > 0);
    assertEquals(createdTestUser.getId(), pagedUsersResult.getStartId());

    String biggestValidUUID = "FFFFFFFF-FFFF-1FFF-BFFF-FFFFFFFFFFFF";

    pagedUsersResponse = userRequest.readMultipleUsers(identityPageQuery().withStartId(biggestValidUUID));
    assertTrue(pagedUsersResponse.isSuccess());

    pagedUsersResult = pagedUsersResponse.get();
    assertTrue(pagedUsersResult.getTotalResults() > 0);
    assertEquals(0, pagedUsersResult.getItemsPerPage());
    assertEquals(biggestValidUUID, pagedUsersResult.getStartId());
    assertEquals(PAGINATION_BY_ID_END_PARAM, pagedUsersResult.getNextId());
  }

  @Test
  public void testGetUsersSeveralPagesUsingIndex() {
    int startIndex = 1;
    int count = 3;
    long totalResults = 0;
    PagedByIndexSearchResult<User> getPagedUsersSearchResult;
    List<User> usersFromAllPages = new LinkedList<>();
    List<User> allUsers = getAllUsersWithIdPaging();

    do {
      getPagedUsersSearchResult = getUsersPagedByIndex(startIndex, count);

      assertEquals(Long.valueOf(startIndex), getPagedUsersSearchResult.getStartIndex());

      totalResults = getPagedUsersSearchResult.getTotalResults();
      assertTrue(totalResults > 0);
      assertEquals(allUsers.size(), totalResults);

      List<User> usersPerPage = getPagedUsersSearchResult.getResources();
      usersFromAllPages.addAll(usersPerPage);

      startIndex = startIndex + count;
    } while (startIndex <= totalResults);

    assertEquals(allUsers.size(), usersFromAllPages.size());

    List<String> userIdsFromAllPages = extractUserIds(usersFromAllPages);
    List<String> allUsersIds = extractUserIds(allUsers);

    userIdsFromAllPages.removeAll(allUsersIds);
    assertEquals(0, userIdsFromAllPages.size());
  }

  @Test
  public void testGetUsersSeveralPagesUsingId() {
    String startId = PAGINATION_BY_ID_START_PARAM;
    int count = 3;
    long totalResults = 0;
    PagedByIdentitySearchResult<User> pagedUsers;
    List<User> usersFromAllPages = new LinkedList<>();
    List<User> allUsers = getAllUsersWithIndexPaging();

    do {
      pagedUsers = getUsersPagedById(startId, count);

      assertEquals(startId, pagedUsers.getStartId());

      totalResults = pagedUsers.getTotalResults();
      assertTrue(totalResults > 0);
      assertEquals(allUsers.size(), totalResults);

      List<User> usersPerPage = pagedUsers.getResources();
      usersFromAllPages.addAll(usersPerPage);

      startId = pagedUsers.getNextId();
    } while (!startId.equals(PAGINATION_BY_ID_END_PARAM));

    assertEquals(usersFromAllPages.size(), allUsers.size());

    List<String> userIdsFromAllPages = extractUserIds(usersFromAllPages);
    List<String> allUsersIds = extractUserIds(allUsers);

    userIdsFromAllPages.removeAll(allUsersIds);
    assertEquals(0, userIdsFromAllPages.size());
  }

  @Test
  public void testPagingByIdWithFiltering() {

  }

  @Test
  public void testGetPagedUsersWithFiltering() {
    String testUserDisplayName = "pagedUserTest";
    createUser(buildTestUser(testUserDisplayName));

    String filterExpression = String.format("userName eq \"%s\"", testUserDisplayName);

    List<User> filteredUsers = getUsersFiltered(filterExpression);
    List<User> pagedFilteredUsers = getUsersFilteredAndPagedByIndex(1, RESOURCES_PER_PAGE, filterExpression).getResources();

    assertTrue(pagedFilteredUsers.containsAll(filteredUsers));
  }

  @Test
  public void testGetFilteredUsersTotalCount() {
    String testUserName = "uniqueName1543258";
    createUser(buildTestUser(testUserName));

    String filterExpression = String.format("userName eq \"%s\"", testUserName);

    PagedByIdentitySearchResult<User> allUsers = getUsersPagedById(PAGINATION_BY_ID_START_PARAM, RESOURCES_PER_PAGE);
    PagedByIdentitySearchResult<User> filteredUsers = getUsersFilteredAndPagedById(PAGINATION_BY_ID_START_PARAM,
        RESOURCES_PER_PAGE, filterExpression);

    assertEquals(1, filteredUsers.getResources().size());
    assertEquals(1, filteredUsers.getTotalResults());

    assertTrue(allUsers.getTotalResults() > 1);
  }

  @Test
  public void testCreateUserWithMultivaluedAttributesAndUpdatedManager(){
    User managerUser = createUser(TestData.setAttributesToATestUser("testCreateUserWithMultivaluedAttributesAndUpdatedManager-Manager").build());
    User managerUserCreated = getUser(managerUser.getId());

    managerUserCreated = updateUser(managerUserCreated.getId(), managerUserCreated);

    Address defaultAddress = TestData.buildHomeAddressDefaultAttrs();

    User.Builder employeeUserRequestBuilder = TestData.setAttributesToATestUser("testCreateUserWithMultivaluedAttributesAndUpdatedManager-Employee").addAddress(defaultAddress);
    User employeeUserRequest = TestData.addAllEnterpriseAttributesToTestUser(employeeUserRequestBuilder, managerUserCreated.getId()).build();

    User createdEmployeeUser = createUser(employeeUserRequest);
    updateUser(createdEmployeeUser.getId(), createdEmployeeUser);

    User fetchedEmployeeUser = getUser(createdEmployeeUser.getId());

    assertEquals(1, fetchedEmployeeUser.getAddresses().size());
    assertEquals(defaultAddress, fetchedEmployeeUser.getAddresses().get(0));
    assertEquals(managerUserCreated.getId(), ((EnterpriseExtension) fetchedEmployeeUser.getExtension(EnterpriseExtension.ENTERPRISE_URN)).getManager().getValue());
  }

  private void assertUserAllSingleValueAttribute(final User expected, final User actual) {
    assertEquals(expected.isActive(), actual.isActive());
    assertEquals(36, actual.getId().length());
    assertEquals(expected.getUserName(), actual.getUserName());
    assertEquals(expected.getName().getGivenName(), actual.getName().getGivenName());
    assertEquals(expected.getName().getFamilyName(), actual.getName().getFamilyName());
    assertEquals(expected.getName().getHonorificSuffix(), actual.getName().getHonorificSuffix());
    assertEquals(expected.getName().getHonorificPrefix(), actual.getName().getHonorificPrefix());
    assertEquals(expected.getName().getMiddleName(), actual.getName().getMiddleName());
    assertEquals(expected.getDisplayName(), actual.getDisplayName());
    assertEquals(expected.getNickName(), actual.getNickName());
    assertEquals(expected.getProfileUrl(), actual.getProfileUrl());
    assertEquals(expected.getTitle(), actual.getTitle());
    assertEquals(expected.getUserType(), actual.getUserType());
    assertEquals(expected.getPreferredLanguage(), actual.getPreferredLanguage());
    assertEquals(expected.getLocale(), actual.getLocale());
    assertEquals(expected.getTimezone(), actual.getTimezone());
    assertEquals(expected.getExternalId(), actual.getExternalId());
  }

  private void assertUserMultivaluedAttibute(final MultiValuedAttribute expected, final MultiValuedAttributeType expectedType,
      final MultiValuedAttribute actual, final MultiValuedAttributeType actualType) {
    assertEquals(expected.getDisplay(), actual.getDisplay());
    assertEquals(expected.isPrimary(), actual.isPrimary());
    assertEquals(expected.getValue(), actual.getValue());
    assertEquals(expectedType.getValue(), actualType.getValue());
  }

  private void assertEnterpriseAttributes(final User expected, final User actual) {
    EnterpriseExtension expectedExtension = (EnterpriseExtension) expected.getExtension(EnterpriseExtension.ENTERPRISE_URN);
    EnterpriseExtension actualExtension = (EnterpriseExtension) actual.getExtension(EnterpriseExtension.ENTERPRISE_URN);
    assertEquals(expectedExtension.getCostCenter(), actualExtension.getCostCenter());
    assertEquals(expectedExtension.getOrganization(), actualExtension.getOrganization());
    assertEquals(expectedExtension.getDivision(), actualExtension.getDivision());
    assertEquals(expectedExtension.getDepartment(), actualExtension.getDepartment());
    assertEquals(expectedExtension.getEmployeeNumber(), actualExtension.getEmployeeNumber());
    assertEquals(expectedExtension.getManager().getValue(), actualExtension.getManager().getValue());
  }
}
