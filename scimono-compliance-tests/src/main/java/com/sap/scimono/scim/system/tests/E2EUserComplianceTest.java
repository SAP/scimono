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

import static com.sap.scimono.api.API.COUNT_PARAM;
import static com.sap.scimono.api.API.USERS;
import static com.sap.scimono.client.query.ResourcePageQuery.identityPageQuery;
import static com.sap.scimono.client.query.ResourcePageQuery.indexPageQuery;
import static com.sap.scimono.entity.EnterpriseExtension.ENTERPRISE_URN;
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
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.function.Executable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.entity.Address;
import com.sap.scimono.entity.Email;
import com.sap.scimono.entity.EnterpriseExtension;
import com.sap.scimono.entity.Entitlement;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.Im;
import com.sap.scimono.entity.Manager;
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
import com.sap.scimono.scim.system.tests.conditions.DisableOnEmptyUsersEndpoint;
import com.sap.scimono.scim.system.tests.conditions.EnableOnEmptyUsersEndpoint;
import com.sap.scimono.scim.system.tests.extensions.GroupClientScimResponseExtension;
import com.sap.scimono.scim.system.tests.extensions.GroupFailSafeClient;
import com.sap.scimono.scim.system.tests.extensions.UserClientScimResponseExtension;
import com.sap.scimono.scim.system.tests.extensions.UserFailSafeClient;
import com.sap.scimono.scim.system.tests.util.CustomTargetSystemRestClient;
import com.sap.scimono.scim.system.tests.util.TestData;

public class E2EUserComplianceTest extends SCIMComplianceTest {
  private static final Logger logger = LoggerFactory.getLogger(E2EUserComplianceTest.class);
  private static final int RESOURCES_PER_PAGE = 1000;

  @RegisterExtension
  UserClientScimResponseExtension resourceAwareUserRequest = UserClientScimResponseExtension.forClearingAfterEachExecutions(userRequest);

  @RegisterExtension
  GroupClientScimResponseExtension resourceAwareGroupRequest = GroupClientScimResponseExtension.forClearingAfterEachExecutions(groupRequest);

  private final UserFailSafeClient userFailSafeClient = resourceAwareUserRequest.getFailSafeClient();
  private final GroupFailSafeClient groupFailSafeClient = resourceAwareGroupRequest.getFailSafeClient();

  @Test
  @DisplayName("Test Get users and verify required attributes are fetchedNonEmptyBackend")
  @EnableOnEmptyUsersEndpoint
  public void testGetUsersWithRequiredAttributes() {
    logger.info("Creating User - {}", "testGetUsersWithRequiredAttributes-User1");
    User firstUser = userFailSafeClient.create(new User.Builder("testGetUsersWithRequiredAttributes-User1").build());

    logger.info("Creating User - {}", "testGetUsersWithRequiredAttributes-User2");
    User secondUser = userFailSafeClient.create(new User.Builder("testGetUsersWithRequiredAttributes-User2").build());

    // @formatter:off
    PagedByIndexSearchResult<User> getUsersResponse = userFailSafeClient.getAllWithoutPaging();
    assertAll("Verify both users exist in the response",
        () -> assertEquals(2, getUsersResponse.getTotalResults(), "Verify 'totalResults'"),
        () -> assertTrue(getUsersResponse.getItemsPerPage() >= 2, "Verify 'itemsPerPage' is greater than or equal to: 2"),
        () -> assertFalse(getUsersResponse.getResources().isEmpty(), "Verify 'Resources' is not empty"),
        () -> assertAll("Verify 'Resources' attributes",
            getMultipleUsersAssertions(Arrays.asList(firstUser, secondUser), getUsersResponse.getResources(), this::getRequiredAttributeAssertions))
    );
    // @formatter:on
  }

  @Test
  @DisplayName("Test Get users and verify common attributes are fetchedNonEmptyBackend")
  @DisableOnEmptyUsersEndpoint
  public void testGetUsersWithCommonUsedAttributesNonEmptyBackend() {
    logger.info("Fetching all users");
    int alreadyCreatedUsers = userFailSafeClient.getAllWithIndexPaging().size();

    logger.info("Creating User - {}", "testGetUsersWithCommonUsedAttributes-User1NonEmptyBackend");
    User firstUser = userFailSafeClient.create(buildTestUser("testGetUsersWithCommonUsedAttributes-User1NonEmptyBackend"));

    logger.info("Creating User - {}", "testGetUsersWithCommonUsedAttributes-User2NonEmptyBackend");
    User secondUser = userFailSafeClient.create(buildTestUser("testGetUsersWithCommonUsedAttributes-User2NonEmptyBackend"));

    int allUsers = alreadyCreatedUsers + 2;
    // @formatter:off
    PagedByIndexSearchResult<User> getUsersResponse = userFailSafeClient.getAllWithoutPaging();
    assertAll("Verify both users exist in the response",
        () -> assertEquals(allUsers, getUsersResponse.getTotalResults(), "Verify 'totalResults'"),
        () -> assertTrue(getUsersResponse.getItemsPerPage() >= 2, "Verify 'itemsPerPage' is greater than or equal to: 2"),
        () -> assertFalse(getUsersResponse.getResources().isEmpty(), "Verify 'Resources' is not empty"),
        () -> assertAll("Verify 'Resources' attributes",
            getMultipleUsersAssertions(Arrays.asList(firstUser, secondUser), getUsersResponse.getResources(), this::getCommonUsedAttributeAssertions))
    );
    // @formatter:on
  }

  @Test
  @DisplayName("Test Create user with common attributes")
  public void testCreateUserCommonUsedAttributes() {
    String userName = "testCreateUserCommonUsedAttributes";
    User userBody = buildTestUser(userName);

    logger.info("Creating User: {}", userName);
    User createdTestUser = userFailSafeClient.create(userBody);

    assertAll("Verify CREATE User response attributes", getCommonUsedAttributeAssertions(userBody, createdTestUser));
  }

  @Test
  @DisplayName("Test Create user with common attributes and verify get response")
  public void testCreateUserCommonUsedAttributesAndGet() {
    String userName = "testCreateUserCommonUsedAttributesAndGed";

    logger.info("Creating User: {}", userName);
    User createdTestUser = userFailSafeClient.create(buildTestUser(userName));

    logger.info("Fetching User: {}", userName);
    User fetchedUser = userFailSafeClient.getSingle(createdTestUser.getId());

    assertAll("Verify GET user response attributes", getCommonUsedAttributeAssertions(createdTestUser, fetchedUser));
  }

  @Test
  @DisplayName("Test Create user with required attributes")
  public void testCreateUserRequiredAttributes() {
    String userName = "testCreateUserRequiredAttributes";
    User userBody = buildTestUser(userName);

    logger.info("Creating User: {}", userName);
    User createdTestUser = userFailSafeClient.create(new User.Builder(userName).build());

    assertAll("Verify CREATE User response attributes", getRequiredAttributeAssertions(userBody, createdTestUser));
  }

  @Test
  @DisplayName("Test Create user with required attributes and verify Get response")
  public void testCreateUserRequiredAttributesAndGet() {
    String userName = "testCreateUserRequiredAttributesAndGet";

    logger.info("Creating User: {}", userName);
    User createdTestUser = userFailSafeClient.create(new User.Builder(userName).build());
    logger.info("Fetching User: {}", userName);
    User fetchedUser = userFailSafeClient.getSingle(createdTestUser.getId());

    assertAll("Verify GET User response attributes", getRequiredAttributeAssertions(createdTestUser, fetchedUser));
  }

  @Test
  @DisplayName("Test Create user with multiple emails")
  public void testCreateUserWithMultipleEmails() {
    Email personalEmail = TestData.buildPersonalEmailWithDefaultAttrs();
    Email workEmail = TestData.buildWorkEmailWithDefaultAttrs();

    Collection<Email> userEmails = new ArrayList<>();
    userEmails.add(personalEmail);
    userEmails.add(workEmail);

    String userName = "testCreateUserWithMultipleEmails201";
    logger.info("Creating User: {}, with personal and work email", userName);
    User testUser = userFailSafeClient.create(TestData.setAttributesToATestUser(userName).addEmails(userEmails).build());

    logger.info("Fetching User: {}, ", userName);
    User createdUser = userFailSafeClient.getSingle(testUser.getId());

    assertEquals(2, createdUser.getEmails().size(), "Verify fetched User contains both emails");
  }

  @Test
  @DisplayName("Test Create full test user and verify get response")
  public void createFullTestUserAndGet() {
    String managerUserName = "createFullTestUserAndGet-Manager";
    logger.info("Creating User: -{}- who will be used as a manager", managerUserName);
    User managerUser = userFailSafeClient.create(buildTestUser(managerUserName));

    String fullUserUserName = "createFullTestUserAndGet-User";
    logger.info("Creating User -{}- with all attributes", managerUserName);
    User fullUserRepresentation = userFailSafeClient.create(buildFullUserRepresentation(fullUserUserName, managerUser.getId()));

    logger.info("Fetching User: {}", fullUserUserName);
    User fetchedUser = userFailSafeClient.getSingle(fullUserRepresentation.getId());

    assertAll("Verify GET User response", getFullUserAssertions(fullUserRepresentation, fetchedUser));
  }

  @Test
  @DisplayName("Test Create full test user")
  public void createFullTestUser() {
    String managerUserName = "createFullTestUser-Manager";
    logger.info("Creating User: -{}- who will be used as a manager", managerUserName);
    User managerUser = userFailSafeClient.create(buildTestUser(managerUserName));

    String fullUserUserName = "createFullTestUser-User";
    logger.info("Creating User -{}- with all attributes", managerUserName);

    User userBody = buildFullUserRepresentation(fullUserUserName, managerUser.getId());
    User createdUserFullRepresentation = userFailSafeClient.create(userBody);

    assertAll("Verify CREATE User response", getFullUserAssertions(userBody, createdUserFullRepresentation));
  }

  @Test
  @DisplayName("Test Update user with PUT")
  public void testUpdateUser() {
    String userName = "testUpdateUser";
    logger.info("Creating User: {}", userName);
    User oldUser = userFailSafeClient.create(TestData.setAllAttributesToTestUser(userName, null).build());

    User userToUpdate = new User.Builder(oldUser.getUserName()).setId(oldUser.getId())
        .setName(new Name.Builder().setGivenName("updateGivenName").setMiddleName("updatedMiddleName").setFamilyName("updatedFamilyName").build())
        .setNickName("topalka").build();

    logger.info("Updating User: {}", userName);
    User updatedUser = userFailSafeClient.update(oldUser.getId(), userToUpdate);
    // check all previous data is intact

    // @formatter:off
    assertAll("Check updated attributes exist in the response", () -> assertEquals(oldUser.getId(), updatedUser.getId(), "Verify 'id' attribute"),
        () -> assertEquals(userToUpdate.getUserName(), updatedUser.getUserName(), "Verify 'userName' attribute"),
        () -> assertEquals(userToUpdate.getName().getGivenName(), updatedUser.getName().getGivenName(), "Verify 'name.givenName' attribute"),
        () -> assertEquals(userToUpdate.getName().getMiddleName(), updatedUser.getName().getMiddleName(), "Verify 'name.middleName' attribute"),
        () -> assertEquals(userToUpdate.getName().getFamilyName(), updatedUser.getName().getFamilyName(), "Verify 'name.familyName' attribute"),
        () -> assertEquals(userToUpdate.getNickName(), updatedUser.getNickName(), "Verify 'nickName' attribute"),
        () -> assertNotNull(updatedUser.getMeta(), "Verify 'meta' is not null"),
        () -> assertEquals(User.RESOURCE_TYPE_USER, updatedUser.getMeta().getResourceType(), "Verify 'meta.resourceType' attribute"),
        () -> assertEquals(oldUser.getMeta().getLocation(), updatedUser.getMeta().getLocation(), "Verify 'meta.location' attribute")
    );

    assertAll("Check no old attributes left", () -> assertNull(updatedUser.getProfileUrl(), "Verify 'prfileUrl' is missing"),
        () -> assertNull(updatedUser.getTitle(), "Verify 'title' is missing"),
        () -> assertNull(updatedUser.getUserType(), "Verify 'userType' is missing"),
        () -> assertNull(updatedUser.getPreferredLanguage(), "Verify 'preferredLanguage' is missing"),
        () -> assertNull(updatedUser.getLocale(), "Verify 'locale' is missing"),
        () -> assertNull(updatedUser.getTimezone(), "Verify 'timezone' is missing"),
        () -> assertNull(updatedUser.getExternalId(), "Verify 'externalId' is missing"),
        () -> assertTrue(updatedUser.getEmails().isEmpty(), "Verify 'emails' has no values"),
        () -> assertTrue(updatedUser.getPhotos().isEmpty(), "Verify 'photos' has no values"),
        () -> assertTrue(updatedUser.getPhoneNumbers().isEmpty(), "Verify 'phoneNumbers' has no values"),
        () -> assertTrue(updatedUser.getRoles().isEmpty(), "Verify 'roles' has no values"),
        () -> assertTrue(updatedUser.getAddresses().isEmpty(), "Verify 'addresses' has no values"),
        () -> assertTrue(updatedUser.getGroups().isEmpty(), "Verify 'groups' has no values"),
        () -> assertTrue(updatedUser.getEntitlements().isEmpty(), "Verify 'entitlements' has no values"),
        () -> assertTrue(updatedUser.getIms().isEmpty(), "Verify 'ims' has no values")
    );
    // @formatter:on
  }

  @Test
  @DisplayName("Test Get users and verify empty list is received")
  @EnableOnEmptyUsersEndpoint
  public void testGetUsersEmptyList() {
    logger.info("Fetching all Users - without paging attributes");
    PagedByIndexSearchResult<User> usersPage = userFailSafeClient.getAllWithoutPaging();

    // @formatter:off
    assertAll("Verify empty list response is received",
        () -> assertEquals(0, usersPage.getTotalResults(), "Verify 'totalResults' is 0"),
        () -> assertTrue(usersPage.getItemsPerPage() >= 0, "Verify 'itemsPerPage' is greater than or equal to: 0"),
        () -> assertEquals(1, usersPage.getStartIndex(), "Verify startIndex is equal to the one provided in request starIndex param"),
        () -> assertTrue(usersPage.getResources().isEmpty(), "Verify 'Resources' is empty list")
    );
    // @formatter:on
  }

  @Test
  @DisplayName("Test Delete user and verify Get response")
  public void testDeleteUser() {
    String userName = "test5DeleteUser";
    logger.info("Updating User: {}", userName);
    User createdTestUser = userFailSafeClient.create(buildTestUser(userName));

    logger.info("Fetching User: {}", userName);
    createdTestUser = userFailSafeClient.getSingle(createdTestUser.getId());

    logger.info("Deleting User: {}", userName);
    SCIMResponse<Void> scimResponse = resourceAwareUserRequest.deleteUser(createdTestUser.getId());

    assertAll("Verify DELETE Response", () -> assertTrue(scimResponse.isSuccess(), "Verify DELETE Request finished successfully"),
        () -> assertEquals(SC_NO_CONTENT, scimResponse.getStatusCode(), "Verify response status"));

    logger.info("Fetching User: {}, again", userName);
    SCIMResponse<User> getUserResponse = resourceAwareUserRequest.readSingleUser(createdTestUser.getId());

    assertAll("Verify GET Response", () -> assertFalse(getUserResponse.isSuccess(), "Verify GET Request finished successfully"),
        () -> assertEquals(SC_NOT_FOUND, getUserResponse.getStatusCode(), "Verify response status"));
  }

  @Test
  @DisplayName("Test Create deleted user")
  public void testCreateDeletedUser() {
    String userName = "testCreateDeletedUser";
    User user = buildTestUser(userName);

    logger.info("Creating User: {}", userName);
    User createdTestUser = userFailSafeClient.create(user);

    logger.info("Deleting User: {}", userName);
    userFailSafeClient.delete(createdTestUser.getId());

    logger.info("Creating User: {}, again", userName);
    userFailSafeClient.create(user);
  }

  @Test
  @DisplayName("Test Get User with groups and multivalue attributes")
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

    logger.info("Creating User: {}, with home and work email and will be used as a member", testUserName);
    User createdTestUser = userFailSafeClient.create(testUserWithMultiValueAttrs);
    assertEquals(2, createdTestUser.getEmails().size(), "Verify CREATE response contains both emails");

    logger.info("Creating Group: testGroupWithMember1, with User member: {}", testUserName);
    Group testGroupWithMember1 = groupFailSafeClient.create(buildGroup("testGroupWithMember1", createdTestUser.getId()));

    logger.info("Creating Group: testGroupWithMember2, with User member: {}, and Group member: {}", testUserName,
        testGroupWithMember1.getDisplayName());
    Group testGroupWithMember2 = groupFailSafeClient
        .create(buildGroup("testGroupWithMember2", createdTestUser.getId(), testGroupWithMember1.getId()));

    logger.info("Creating Group: testGroupWithMember3, with User member: {}, and Group members: {}, {}", testUserName,
        testGroupWithMember1.getDisplayName(), testGroupWithMember2.getDisplayName());
    groupFailSafeClient
        .create(buildGroup("testGroupWithMember3", createdTestUser.getId(), testGroupWithMember1.getId(), testGroupWithMember2.getId()));

    logger.info("Fetching ");
    User returnedTestUser = userFailSafeClient.getSingle(createdTestUser.getId());

    assertEquals(2, returnedTestUser.getEmails().size());
  }

  @Test
  @DisplayName("Test Get Users by groups filter")
  public void testGetUsersFilteredGroups() {
    logger.info("Creating User: FilteredGroupsUser");
    User testUser = userFailSafeClient.create(buildTestUser("FilteredGroupsUser"));

    logger.info("Creating Group: FilteredGroups, with user member: {}", testUser.getUserName());
    groupFailSafeClient.create(buildGroup("FilteredGroups", testUser.getId()));

    String filterExpression = "groups.display eq \"FilteredGroups\"";
    logger.info("Fetching Users by filter expression: {}", filterExpression);
    List<User> filteredUsers = userFailSafeClient.getAllByFilter(filterExpression);

    assertAll("Verify GET filtered Users response", () -> assertEquals(1, filteredUsers.size(), "Verify exact number of Users is fetched"),
        () -> assertEquals(testUser.getId(), filteredUsers.get(0).getId(), "Verify User with same Id is fetched"));
  }

  @TestFactory
  @DisplayName("Test Get users by filter")
  public Collection<DynamicTest> testGetUsersFiltered() {
    // @formatter:off
    return Arrays.asList(
        getGetUsersFilteredDynamicTest(
            "Test GET Users filtered by userName",
            buildTestUser("FilteredUsername"),
            String.format("userName eq \"%s\"", "FilteredUsername")
        ),
        getGetUsersFilteredDynamicTest(
            "Test GET Users filtered by full core schema attribute path",
            buildTestUser("FilteredFullUsernamePath"),
            String.format("%s:userName eq \"%s\"", User.SCHEMA, "FilteredFullUsernamePath")
        ),
        getGetUsersFilteredDynamicTest(
            "Test GET Users filtered by enterprise schema attribute",
            buildTestUserWithExtentions("FilteredFullEmpolyeeNumberPath-Username",
                Collections.singletonList(new EnterpriseExtension.Builder().setEmployeeNumber("1110111").build())),
            String.format("%s:employeeNumber eq \"%s\"", ENTERPRISE_URN, "1110111")
        ),
        getGetUsersFilteredDynamicTest(
            "Test GET Users filtered by roles",
            buildTestUser("FilteredRoles-User").builder()
                .addRole(new Role(null, "FilteredRoles", null, false, null, Role.Type.of("test")))
                .build(),
            "roles.value eq \"FilteredRoles\""
        )
    );
    // @formatter:on
  }

  private DynamicTest getGetUsersFilteredDynamicTest(final String testName, final User userBody, final String filterExpression) {
    // @formatter:off
    return DynamicTest.dynamicTest(testName,
        () -> {
          logger.info("Creating User: {}", userBody.getUserName());
          User testUser = userFailSafeClient.create(userBody);

          logger.info("Fetching Users by filter expression: {}", filterExpression);
          List<User> filteredUsers = userFailSafeClient.getAllByFilter(filterExpression);

          assertAll("Verify GET filtered Users response",
              () -> assertEquals(1, filteredUsers.size(), "Verify exact number of Users is fetched"),
              () -> assertEquals(testUser.getId(), filteredUsers.get(0).getId(), "Verify User with same Id is fetched")
          );
        }
    );
    // @formatter:on
  }

  @Test
  @DisplayName("Test Get users with Index paging and count=0")
  public void testGetUsersTotalCountWithStartIndex() {
    int startIndex = 1;
    int count = 0;

    createMultipleUsers("testGetUsersTotalCountWithStartIndex", 3);

    logger.info("Fetching multiple users with starIndex: {} and count: {}", startIndex, count);
    PagedByIndexSearchResult<User> getPagedUsersSearchResult = userFailSafeClient.getPagedByIndex(startIndex, count);

    // @formatter:off
    assertAll("Verify List Response",
        () -> assertEquals(startIndex, getPagedUsersSearchResult.getStartIndex(), "Verify 'startIndex"),
        () -> assertTrue(getPagedUsersSearchResult.getItemsPerPage() >= count, "Verify 'itemsPerPage' is greater than or equal to: " + count),
        () -> assertTrue(getPagedUsersSearchResult.getTotalResults() > 0, "Verify 'totalResults' is bigger 0"),
        () -> assertTrue(getPagedUsersSearchResult.getResources().isEmpty(), "Verify 'Resources' list size is not empty")
    );
    // @formatter:on
  }

  @Test
  @DisplayName("Test Get users with index paging and startIndex=totalResults")
  @EnableOnEmptyUsersEndpoint
  public void testGetUsersWithStarIndexEqualTotalResults() {
    String displayName = "testGetUsersWithStarIndexEqualTotalResults-User";
    int usersCount = 3;

    List<User> createdUsers = createMultipleUsers(displayName, usersCount);

    int readCount = 100;
    int startIndex = usersCount;

    logger.info("Fetching Users with startIndex: {} and count: {}", startIndex, readCount);
    PagedByIndexSearchResult<User> usersPage = userFailSafeClient.getPagedByIndex(startIndex, readCount);

    // @formatter:off
    assertAll("Verify Correct ListResponse values",
        () -> assertEquals(startIndex, usersPage.getStartIndex(), "Verify 'startIndex"),
        () -> assertEquals(usersCount, usersPage.getTotalResults(), "Verify 'totalResults' is equal to created Users"),
        () -> assertTrue(usersPage.getItemsPerPage() <= readCount, "Verify 'itemsPerPage' is less than or equal to count param: " + readCount),
        () -> assertEquals(1, usersPage.getResources().size(), "Verify 'Resources' list size is equal to 'ItemsPerPage'"),
        () -> {
          String firstUserIdFromGetResponse = usersPage.getResources().get(0).getId();
          assertTrue(createdUsers.stream().map(User::getId).anyMatch(firstUserIdFromGetResponse::equals), "Verify fetched user is part of previously created Users");
        });
    // @formatter:on
  }

  @Test
  @DisplayName("Test Get users with index paging and startIndex out of range (more than total results)")
  public void testGetUsersWithStarIndexOutOfRange() {
    logger.info("Fetching all users");
    int alreadyCreatedUsers = userFailSafeClient.getAllWithIndexPaging().size();
    String displayName = "testGetUsersWithStarIndexOutOfRange-User";
    int usersCount = 3;

    createMultipleUsers(displayName, usersCount);

    int allUsers = alreadyCreatedUsers + usersCount;

    int readCount = 100;
    int startIndex = allUsers + 1;

    logger.info("Fetching Users with startIndex: {} and count: {}", startIndex, readCount);
    PagedByIndexSearchResult<User> usersPage = userFailSafeClient.getPagedByIndex(startIndex, readCount);

    // @formatter:off
    assertAll("Verify Correct ListResponse values",
        () -> assertEquals(startIndex, usersPage.getStartIndex(), "Verify 'startIndex"),
        () -> assertEquals(allUsers, usersPage.getTotalResults(), "Verify 'totalResults' is equal to created Users"),
        () -> assertTrue(usersPage.getItemsPerPage() <= readCount, "Verify 'count' is equal or less to 'itemsPerPage'"),
        () -> assertTrue(usersPage.getResources().isEmpty(), "Verify 'Resources' list size is empty'"));
    // @formatter:on
  }

  @Test
  @DisplayName("Test Get users with Id paging and count=0")
  public void testGetUsersTotalCountWithStartId() {
    String startId = PAGINATION_BY_ID_START_PARAM;
    int count = 0;

    createMultipleUsers("testGetUsersWithStarIndexEqualTotalResults", 3);

    logger.info("Fetching Users with startId: {} and count: {}", startId, count);
    PagedByIdentitySearchResult<User> getPagedUsersSearchResult = userFailSafeClient.getPagedById(startId, count);

    // @formatter:off
    assertAll("Verify List Response",
        () -> assertEquals(startId, getPagedUsersSearchResult.getStartId(), "Verify 'startId'"),
        () -> assertTrue(count <= getPagedUsersSearchResult.getItemsPerPage(), "Verify 'count' is equal or less to 'itemsPerPage'"),
        () -> assertTrue(getPagedUsersSearchResult.getTotalResults() > 0, "Verify 'totalResults' is greater than 0"),
        () -> assertTrue(getPagedUsersSearchResult.getResources().isEmpty(), "Verify 'Resources' list size is empty'"),
        () -> assertTrue(getPagedUsersSearchResult.getResources().size() <= getPagedUsersSearchResult.getItemsPerPage(),
            "Verify 'Resources' list size is less than or equal to 'itemsPerPage''")
    );
    // @formatter:on
  }

  @Test
  @DisplayName("Test Get users with index paging and negative count")
  public void testGetUsersNegativeCountAndStartIndex() {
    int startIndex = 1;
    int count = -10;

    createMultipleUsers("testGetUsersNegativeCountAndStartIndex", 3);

    logger.info("Fetching Users with startIndex: {} and count: {}", startIndex, count);
    PagedByIndexSearchResult<User> getPagedUsersSearchResult = userFailSafeClient.getPagedByIndex(startIndex, count);

    // @formatter:off
    assertAll("Verify List response",
        () -> assertEquals(startIndex, getPagedUsersSearchResult.getStartIndex(), "Verify 'startIndex'"),
        () -> assertTrue(0 <= getPagedUsersSearchResult.getItemsPerPage(), "Verify 'ItemsPerPage' is greater or equal to 0"),
        () -> assertTrue(getPagedUsersSearchResult.getTotalResults() > 0, "Verify 'totalResults' is greater than 0"),
        () -> assertTrue(getPagedUsersSearchResult.getResources().size() <= getPagedUsersSearchResult.getItemsPerPage(),
            "Verify 'Resources' list size is less than or equal to 'itemsPerPage''")
    );
    // @formatter:on
  }

  @Test
  @DisplayName("Test Get users with Id paging and negative count")
  public void testGetUsersNegativeCountAndStartId() {
    String startId = PAGINATION_BY_ID_START_PARAM;
    int count = -10;

    createMultipleUsers("testGetUsersNegativeCountAndStartIndex", 3);

    logger.info("Fetching Users with startId: {} and count: {}", startId, count);
    PagedByIdentitySearchResult<User> getPagedUsersSearchResult = userFailSafeClient.getPagedById(startId, count);

    // @formatter:off
    assertAll("Verify List Response",
        () -> assertEquals(startId, getPagedUsersSearchResult.getStartId(), "Verify 'startId'"),
        () -> assertTrue(0 <= getPagedUsersSearchResult.getItemsPerPage(), "Verify 'ItemsPerPage' is greater or equal to 0"),
        () -> assertTrue(getPagedUsersSearchResult.getTotalResults() > 0, "Verify 'totalResults' is greater than 0"),
        () -> assertTrue(getPagedUsersSearchResult.getResources().size() <= getPagedUsersSearchResult.getItemsPerPage(),
            "Verify 'Resources' list size is less than or equal to 'itemsPerPage''")
    );
    // @formatter:on
  }

  @Test
  @DisplayName("Test Get users with default startIndex")
  public void testGetUsersDefaultStartIndex() {
    int count = 1;

    createMultipleUsers("testGetUsersDefaultStartIndex", 3);
    PagedByIndexSearchResult<User> getPagedUsersSearchResult = CustomTargetSystemRestClient.INSTANCE
        .getEntitiesHttpResponse(USERS, singletonMap(COUNT_PARAM, count)).readEntity(USER_LIST_RESPONSE_TYPE_INDEX_PAGING);

    // @formatter:off
    assertAll("Verify List Response",
        () -> assertEquals(1, getPagedUsersSearchResult.getStartIndex(), "Verify 'startIndex'"),
        () -> assertTrue(count <= getPagedUsersSearchResult.getItemsPerPage(), "Verify 'count' is equal or less to 'itemsPerPage'"),
        () -> assertTrue(getPagedUsersSearchResult.getTotalResults() > 0, "Verify 'totalResults' is greater than 0"),
        () -> assertEquals(1, getPagedUsersSearchResult.getResources().size(), "Verify 'Resources' list size")
    );
    // @formatter:on
  }

  @Test
  @DisplayName("Test Get users with default count")
  public void testGetUsersDefaultCountWithStartIndex() {
    int startIndex = 1;

    createMultipleUsers("testGetUsersDefaultCountWithStartIndex", 3);

    logger.info("Fetching Multiple Users with startIndex: {}, and default count", startIndex);
    SCIMResponse<PagedByIndexSearchResult<User>> pagedUsersResponse = resourceAwareUserRequest
        .readMultipleUsers(indexPageQuery().withStartIndex(startIndex));
    assertTrue(pagedUsersResponse.isSuccess(), "Verify GET response is success");

    PagedByIndexSearchResult<User> pagedUsersResult = pagedUsersResponse.get();

    // @formatter:off
    assertAll("Verify List response",
        () -> assertEquals(1, pagedUsersResult.getStartIndex(), "Verify 'startIndex'"),
        () -> assertTrue(pagedUsersResult.getTotalResults() > 0, "Verify 'totalResults' is greater than 0")
    );
    // @formatter:on
  }

  @Test
  @DisplayName("Test Get users with default count and Id paging")
  public void testGetUsersDefaultCountWithStartId() {
    String startId = PAGINATION_BY_ID_START_PARAM;
    createMultipleUsers("testGetUsersDefaultCountWithStartId", 6);

    logger.info("Fetching Multiple Users with startId: {}, and default count", startId);
    SCIMResponse<PagedByIdentitySearchResult<User>> pagedUsersResponse = resourceAwareUserRequest
        .readMultipleUsers(identityPageQuery().withStartId(startId));
    assertTrue(pagedUsersResponse.isSuccess(), "Verify GET response is success");

    PagedByIdentitySearchResult<User> pagedUsersResult = pagedUsersResponse.get();

    // @formatter:off
    assertAll("Verify List response",
        () -> assertEquals(startId, pagedUsersResult.getStartId(), "Verify 'startId'"),
        () -> assertTrue(pagedUsersResult.getTotalResults() > 0, "Verify 'totalResults' is greater than 0")
    );
    // @formatter:on

    assertEquals(startId, pagedUsersResult.getStartId());
    assertTrue(pagedUsersResult.getTotalResults() > 0);

    if (pagedUsersResult.getTotalResults() <= Long.parseLong(DEFAULT_COUNT)) {
      assertTrue(pagedUsersResult.getItemsPerPage() >= Integer.parseInt(DEFAULT_COUNT),
          "Verify 'itemsPerPage' is greater than or equal to: " + DEFAULT_COUNT);
      assertEquals(PAGINATION_BY_ID_END_PARAM, pagedUsersResult.getNextId(), "Verify 'nextId'");
    } else {
      assertEquals(Integer.parseInt(DEFAULT_COUNT), pagedUsersResult.getItemsPerPage(), "Verify 'itemsPerPage'");
    }
  }

  @Test
  @DisplayName("Test Get users with Id paging and startId=end")
  public void testGetUsersPagingStartIdEqEnd() {
    String testUserName = "testGetUsersPagingStartIdEqEnd";
    String testUserId;
    int userNameUniquenessCounter = 1;

    do {
      String currentUserName = testUserName + userNameUniquenessCounter;
      logger.info("Creating User: {}", currentUserName);
      User createdTestUser = userFailSafeClient.create(buildTestUser(currentUserName));
      testUserId = createdTestUser.getId();
      userNameUniquenessCounter++;

    } while (!testUserId.startsWith("f"));

    String startId = PAGINATION_BY_ID_END_PARAM;

    logger.info("Fetching Multiple users with startId: {}, and default count", startId);
    SCIMResponse<PagedByIdentitySearchResult<User>> pagedUsersResponse = resourceAwareUserRequest
        .readMultipleUsers(identityPageQuery().withStartId(startId));
    assertTrue(pagedUsersResponse.isSuccess(), "Verify GET response is success");

    PagedByIdentitySearchResult<User> pagedUsersResult = pagedUsersResponse.get();

    // @formatter:off
    assertAll("Verify List Response", () -> assertTrue(pagedUsersResult.getTotalResults() > 0, "Verify 'totalResults' is greater than 0"),
        () -> assertTrue(Integer.parseInt(DEFAULT_COUNT) <= pagedUsersResult.getItemsPerPage(), "Verify 'count' is equal or less to 'itemsPerPage'"),
        () -> assertEquals(startId, pagedUsersResult.getStartId(), "Verify 'startId'"),
        () -> assertEquals(PAGINATION_BY_ID_END_PARAM, pagedUsersResult.getNextId(), "Verify 'nextId'")
    );
    // @formatter:on
  }

  @Test
  @DisplayName("Test Get users with Id paging and startId in upper case")
  public void testGetUsersPagingStartIdWithUpperCase() {
    createMultipleUsers("testGetUsersPagingStartIdWithUpperCase", 6);

    String testUsername = "testGetUsersPagingStartIdWithUpperCase";
    logger.info("Creating User: {}", testUsername);
    User createdTestUser = userFailSafeClient.create(buildTestUser(testUsername));

    logger.info("Fetching Multiple Users with startId: {}, and default count", createdTestUser.getId());
    SCIMResponse<PagedByIdentitySearchResult<User>> pagedUsersResponse = resourceAwareUserRequest
        .readMultipleUsers(identityPageQuery().withStartId(createdTestUser.getId()));

    assertTrue(pagedUsersResponse.isSuccess(), "Verify GET response is success");

    final PagedByIdentitySearchResult<User> firstPagedUsersResult = pagedUsersResponse.get();

    // @formatter:off
    assertAll("Verify first GET Users response",
        () -> assertTrue(firstPagedUsersResult.getTotalResults() > 0, "Verify 'totalResults' is greater than 0"),
        () -> assertTrue(firstPagedUsersResult.getItemsPerPage() > 0, "Verify 'itemsPerPage' is greater than 0"),
        () -> assertEquals(createdTestUser.getId(), firstPagedUsersResult.getStartId(), "Verify 'startId'")
    );
    // @formatter:on

    String biggestValidUUID = "FFFFFFFF-FFFF-1FFF-BFFF-FFFFFFFFFFFF";
    logger.info("Fetching Multiple Users with startId: {}, and default count", biggestValidUUID);
    pagedUsersResponse = resourceAwareUserRequest.readMultipleUsers(identityPageQuery().withStartId(biggestValidUUID));

    assertTrue(pagedUsersResponse.isSuccess(), "Verify GET response is success");
    final PagedByIdentitySearchResult<User> secondPagedUsersResult = pagedUsersResponse.get();

    // @formatter:off
    assertAll("Verify first GET Users response",
        () -> assertTrue(secondPagedUsersResult.getTotalResults() > 0, "Verify 'totalResults' is greater than 0"),
        () -> assertTrue(secondPagedUsersResult.getItemsPerPage() > 0, "Verify 'itemsPerPage' is greater than 0"),
        () -> assertEquals(biggestValidUUID, secondPagedUsersResult.getStartId(), "Verify 'startId'"),
        () -> assertEquals(PAGINATION_BY_ID_END_PARAM, secondPagedUsersResult.getNextId(), "Verify 'nextId'")
    );
    // @formatter:on
  }

  @Test
  @DisplayName("Test Get users all pages with index paging")
  @EnableOnEmptyUsersEndpoint
  public void testGetUsersSeveralPagesUsingIndex() {
    int startIndex = 1;
    int count = 3;
    long totalResults = 0;
    createMultipleUsers("testGetUsersSeveralPagesUsingIndex", 10);

    List<User> usersFromAllPages = new LinkedList<>();

    logger.info("Fetching Users without paging");
    List<User> allUsers = userFailSafeClient.getAllWithoutPaging().getResources();

    do {
      logger.info("Fetching Multiple Users with startIndex: {}, and count: {}", startIndex, count);
      PagedByIndexSearchResult<User> getPagedUsersSearchResult = userFailSafeClient.getPagedByIndex(startIndex, count);

      final int startIndexCopy = startIndex;
      assertAll("Verify List Response", () -> assertEquals(startIndexCopy, getPagedUsersSearchResult.getStartIndex(), "Verify 'startIndex'"),
          () -> assertTrue(getPagedUsersSearchResult.getTotalResults() > 0, "Verify 'totalResults' is greater that 0"),
          () -> assertEquals(allUsers.size(), getPagedUsersSearchResult.getTotalResults(), "Verify 'totalResult' size"));
      totalResults = getPagedUsersSearchResult.getTotalResults();

      List<User> usersPerPage = getPagedUsersSearchResult.getResources();
      usersFromAllPages.addAll(usersPerPage);

      startIndex = startIndex + count;
    } while (startIndex <= totalResults);

    assertEquals(allUsers.size(), usersFromAllPages.size(), "Verify all Users count is equal to sum of those extracted from all pages");

    List<String> userIdsFromAllPages = extractUserIds(usersFromAllPages);
    List<String> allUsersIds = extractUserIds(allUsers);
    userIdsFromAllPages.removeAll(allUsersIds);

    assertEquals(0, userIdsFromAllPages.size(), "Verify paged Users are same sa All users");
  }

  @Test
  @DisplayName("Test Get users all pages with index pagingNonEmptyBackend")
  @DisableOnEmptyUsersEndpoint
  public void testGetUsersSeveralPagesUsingIndexNonEmptyBackend() {
    logger.info("Fetching Users without paging");
    List<User> allUsers = userFailSafeClient.getAllWithoutPaging().getResources();
    if (allUsers.size() < 2) {
      allUsers.addAll(createMultipleUsers("testGetUsersSeveralPagesUsingIndexNonEmptyBackend", 1));
    }

    int startIndex = 1;
    // read on at least 2 pages
    int count = allUsers.size() / 2;
    long totalResults = 0;

    List<User> usersFromAllPages = new LinkedList<>();

    do {
      logger.info("Fetching Multiple Users with startIndex: {}, and count: {}", startIndex, count);
      PagedByIndexSearchResult<User> getPagedUsersSearchResult = userFailSafeClient.getPagedByIndex(startIndex, count);

      final int startIndexCopy = startIndex;

      // @formatter:off
      assertAll("Verify List Response", () -> assertEquals(startIndexCopy, getPagedUsersSearchResult.getStartIndex(), "Verify 'startIndex'"),
          () -> assertTrue(getPagedUsersSearchResult.getTotalResults() > 0, "Verify 'totalResults' is greater that 0"),
          () -> assertEquals(allUsers.size(), getPagedUsersSearchResult.getTotalResults(), "Verify 'totalResult' size")
      );
      // @formatter:on
      totalResults = getPagedUsersSearchResult.getTotalResults();

      List<User> usersPerPage = getPagedUsersSearchResult.getResources();
      usersFromAllPages.addAll(usersPerPage);

      startIndex = startIndex + count;
    } while (startIndex <= totalResults);

    assertEquals(allUsers.size(), usersFromAllPages.size(), "Verify all Users count is equal to sum of those extracted from all pages");

    List<String> userIdsFromAllPages = extractUserIds(usersFromAllPages);
    List<String> allUsersIds = extractUserIds(allUsers);
    userIdsFromAllPages.removeAll(allUsersIds);

    assertEquals(0, userIdsFromAllPages.size(), "Verify paged Users are same sa All users");
  }

  @Test
  @DisplayName("Test Get users all pages with Id paging")
  @EnableOnEmptyUsersEndpoint
  public void testGetUsersSeveralPagesUsingId() {
    String startId = PAGINATION_BY_ID_START_PARAM;
    int count = 3;

    createMultipleUsers("testGetUsersSeveralPagesUsingId", 10);
    List<User> usersFromAllPages = new LinkedList<>();

    logger.info("Fetching Users without paging");
    List<User> allUsers = userFailSafeClient.getAllWithoutPaging().getResources();

    do {
      logger.info("Fetching Multiple Users with startId: {}, and count: {}", startId, count);
      PagedByIdentitySearchResult<User> pagedUsers = userFailSafeClient.getPagedById(startId, count);

      final String startIdCopy = startId;

      // @formatter:off
      assertAll("Verify List Response", () -> assertEquals(startIdCopy, pagedUsers.getStartId(), "Verify 'startId'"),
          () -> assertTrue(pagedUsers.getTotalResults() > 0, "Verify 'totalResults' is greater that 0"),
          () -> assertEquals(allUsers.size(), pagedUsers.getTotalResults(), "Verify 'totalResult' size")
      );
      // @formatter:off

      List<User> usersPerPage = pagedUsers.getResources();
      usersFromAllPages.addAll(usersPerPage);

      startId = pagedUsers.getNextId();
    } while (!startId.equals(PAGINATION_BY_ID_END_PARAM));

    assertEquals(allUsers.size(), usersFromAllPages.size(), "Verify all Users count is equal to sum of those extracted from all pages");

    List<String> userIdsFromAllPages = extractUserIds(usersFromAllPages);
    List<String> allUsersIds = extractUserIds(allUsers);

    userIdsFromAllPages.removeAll(allUsersIds);
    assertEquals(0, userIdsFromAllPages.size(), "Verify paged Users are same sa All users");
  }

  @Test
  @DisplayName("Test Get users all pages with Id pagingNonEmptyBackend")
  @DisableOnEmptyUsersEndpoint
  public void testGetUsersSeveralPagesUsingIdNonEmptyBackend() {
    logger.info("Fetching Users without paging");
    List<User> allUsers = userFailSafeClient.getAllWithoutPaging().getResources();
    if (allUsers.size() < 2) {
      allUsers.addAll(createMultipleUsers("testGetUsersSeveralPagesUsingIdNonEmptyBackend", 1));
    }

    String startId = PAGINATION_BY_ID_START_PARAM;
    // read on at least 2 pages
    int count = allUsers.size() / 2;

    createMultipleUsers("testGetUsersSeveralPagesUsingId", 10);
    List<User> usersFromAllPages = new LinkedList<>();

    do {
      logger.info("Fetching Multiple Users with startId: {}, and count: {}", startId, count);
      PagedByIdentitySearchResult<User> pagedUsers = userFailSafeClient.getPagedById(startId, count);

      final String startIdCopy = startId;

      // @formatter:off
      assertAll("Verify List Response", () -> assertEquals(startIdCopy, pagedUsers.getStartId(), "Verify 'startId'"),
          () -> assertTrue(pagedUsers.getTotalResults() > 0, "Verify 'totalResults' is greater that 0"),
          () -> assertEquals(allUsers.size(), pagedUsers.getTotalResults(), "Verify 'totalResult' size")
      );
      // @formatter:off

      List<User> usersPerPage = pagedUsers.getResources();
      usersFromAllPages.addAll(usersPerPage);

      startId = pagedUsers.getNextId();
    } while (!startId.equals(PAGINATION_BY_ID_END_PARAM));

    assertEquals(allUsers.size(), usersFromAllPages.size(), "Verify all Users count is equal to sum of those extracted from all pages");

    List<String> userIdsFromAllPages = extractUserIds(usersFromAllPages);
    List<String> allUsersIds = extractUserIds(allUsers);

    userIdsFromAllPages.removeAll(allUsersIds);
    assertEquals(0, userIdsFromAllPages.size(), "Verify paged Users are same sa All users");
  }

  @Test
  @DisplayName("Test Get users with userName filter")
  public void testGetPagedUsersWithFiltering() {
    String testUserName = "pagedUserTest";
    logger.info("Creating User: {}", testUserName);
    userFailSafeClient.create(buildTestUser(testUserName));

    String filterExpression = String.format("userName eq \"%s\"", testUserName);

    logger.info("Fetching all Users by filter: {}", filterExpression);
    List<User> filteredUsers = userFailSafeClient.getAllByFilter(filterExpression);

    logger.info("Fetching all Users by filter: {}, with paging", filterExpression);
    List<User> pagedFilteredUsers = userFailSafeClient.getByFilteredAndPagedByIndex(1, RESOURCES_PER_PAGE, filterExpression).getResources();

    List<String> userIdsFromFilteredUsers = extractUserIds(filteredUsers);
    List<String> userIdsFromFilteredUsersWithPaging = extractUserIds(pagedFilteredUsers);

    assertTrue(userIdsFromFilteredUsers.containsAll(userIdsFromFilteredUsersWithPaging), "Verify user Ids");
  }

  @Test
  @DisplayName("Test Get users with userName filter and id paging")
  public void testGetFilteredPagedByIdUsersTotalCount() {
    createMultipleUsers("testGetFilteredPagedByIdUsersTotalCount", 3);

    String testUserName = "uniqueName1543258";
    logger.info("Creating User: {}", testUserName);
    userFailSafeClient.create(buildTestUser(testUserName));

    String filterExpression = String.format("userName eq \"%s\"", testUserName);

    logger.info("Fetching Multiple Users with startId: {}, and count: {}", PAGINATION_BY_ID_START_PARAM, RESOURCES_PER_PAGE);
    PagedByIdentitySearchResult<User> allUsers = userFailSafeClient.getPagedById(PAGINATION_BY_ID_START_PARAM, RESOURCES_PER_PAGE);

    logger.info("Fetching Multiple Users with startId: {}, and count: {} and filter expression: {}", PAGINATION_BY_ID_START_PARAM, RESOURCES_PER_PAGE,
        filterExpression);
    PagedByIdentitySearchResult<User> filteredUsers = userFailSafeClient.getByFilteredAndPagedById(PAGINATION_BY_ID_START_PARAM, RESOURCES_PER_PAGE,
        filterExpression);

    // @formatter:off
    assertAll("Verify GET Responses", () -> assertEquals(1, filteredUsers.getResources().size(), "Verify filtered Users list size"),
        () -> assertEquals(1, filteredUsers.getTotalResults(), "Verify 'totalResults' of Users fetched with filter"),
        () -> assertTrue(allUsers.getTotalResults() > 1, "Verify 'totalResults' of Users fetched without filter is bigger than 1")
    );
    // @formatter:on
  }

  @Test
  @DisplayName("Test Get users with userName filter and index paging")
  public void testGetFilteredPagedByIndexUsersTotalCount() {
    createMultipleUsers("testGetFilteredPagedByIndexUsersTotalCount", 3);

    String testUserName = "uniqueName1543258";
    logger.info("Creating User: {}", testUserName);
    userFailSafeClient.create(buildTestUser(testUserName));

    String filterExpression = String.format("userName eq \"%s\"", testUserName);

    logger.info("Fetching Multiple Users with startIndex: {}, and count: {}", 1, RESOURCES_PER_PAGE);
    PagedByIndexSearchResult<User> allUsers = userFailSafeClient.getPagedByIndex(1, RESOURCES_PER_PAGE);

    logger.info("Fetching Multiple Users with startIndex: {}, and count: {} and filter expression: {}", PAGINATION_BY_ID_START_PARAM,
        RESOURCES_PER_PAGE, filterExpression);
    PagedByIndexSearchResult<User> filteredUsers = userFailSafeClient.getByFilteredAndPagedByIndex(1, RESOURCES_PER_PAGE, filterExpression);

    // @formatter:off
    assertAll("Verify GET Responses", () -> assertEquals(1, filteredUsers.getResources().size(), "Verify filtered Users list size"),
        () -> assertEquals(1, filteredUsers.getTotalResults(), "Verify 'totalResults' of User fetched with filter"),
        () -> assertTrue(allUsers.getTotalResults() > 1, "Verify 'totalResults' of User fetched without filter is bigger than 1")
    );
    // @formatter:on
  }

  @Test
  @DisplayName("Test Create users with multivalued attributes and updated manager")
  public void testCreateUserWithMultivaluedAttributesAndUpdatedManager() {
    String managerUserName = "testCreateUserWithMultivaluedAttributesAndUpdatedManager-Manager";
    logger.info("Creating User: {}, who will me used as a manager", managerUserName);
    User managerUser = userFailSafeClient.create(TestData.setAttributesToATestUser(managerUserName).build());

    logger.info("Fetching manager User: {}", managerUserName);
    User managerUserCreated = userFailSafeClient.getSingle(managerUser.getId());

    logger.info("Updating manager User: {}", managerUserName);
    managerUserCreated = userFailSafeClient.update(managerUserCreated.getId(), managerUserCreated);

    Address defaultAddress = TestData.buildHomeAddressDefaultAttrs();

    User.Builder employeeUserRequestBuilder = TestData.setAttributesToATestUser("testCreateUserWithMultivaluedAttributesAndUpdatedManager-Employee")
        .addAddress(defaultAddress);
    User employeeUserRequest = TestData.addAllEnterpriseAttributesToTestUser(employeeUserRequestBuilder, managerUserCreated.getId()).build();

    logger.info("Crating User: {} with manager: {}", employeeUserRequest.getUserName(), managerUserName);
    User createdEmployeeUser = userFailSafeClient.create(employeeUserRequest);

    logger.info("Updating User: {} with manager: {}", employeeUserRequest.getUserName(), managerUserName);
    userFailSafeClient.update(createdEmployeeUser.getId(), createdEmployeeUser);

    logger.info("Fetching User: {}", managerUserName);
    User fetchedEmployeeUser = userFailSafeClient.getSingle(createdEmployeeUser.getId());

    final String managerUserId = managerUserCreated.getId();

    // @formatter:off
    assertAll("Verify GET User by Id response", () -> assertEquals(1, fetchedEmployeeUser.getAddresses().size(), "Verify addresses size"),
        () -> assertEquals(defaultAddress, fetchedEmployeeUser.getAddresses().get(0), "Verify address"), () -> assertEquals(managerUserId,
            ((EnterpriseExtension) fetchedEmployeeUser.getExtension(ENTERPRISE_URN)).getManager().getValue(), "Verify manager 'value'")
    );
    // @formatter:on
  }

  private Collection<Executable> getFullUserAssertions(final User expected, final User actual) {
    // @formatter:off
    return Arrays.asList(getMetaAssertions(actual, User.RESOURCE_TYPE_USER),
        () -> assertAll("Verify all single-value attributes", getAllUserSingleValueAttributeAssertions(expected, actual)),
        () -> assertAll("Verify all multi-value attributes", getAllUserMultivaluedAttributeAssertions(expected, actual)),
        () -> assertAll("Verify all enterprise schema attributes", getAllUserEnterpriseAttributeAssertions(expected, actual))
    );
    // @formatter:on
  }

  private Collection<Executable> getAllUserSingleValueAttributeAssertions(final User expected, final User actual) {
    // @formatter:off
    return Arrays.asList(() -> assertEquals(expected.isActive(), actual.isActive(), "Verify 'active' attribute"),
        () -> assertEquals(expected.getUserName(), actual.getUserName(), "Verify 'userName' attribute"),
        () -> assertAll("Verify 'name' attribute", getNameAssertions(expected.getName(), actual.getName())),
        () -> assertEquals(expected.getDisplayName(), actual.getDisplayName(), "Verify 'displayName' attribute"),
        () -> assertEquals(expected.getNickName(), actual.getNickName(), "Verify 'nickName' attribute"),
        () -> assertEquals(expected.getProfileUrl(), actual.getProfileUrl(), "Verify 'profileUrl' attribute"),
        () -> assertEquals(expected.getTitle(), actual.getTitle(), "Verify 'title' attribute"),
        () -> assertEquals(expected.getUserType(), actual.getUserType(), "Verify 'userType' attribute"),
        () -> assertEquals(expected.getPreferredLanguage(), actual.getPreferredLanguage(), "Verify 'preferredLanguage' attribute"),
        () -> assertEquals(expected.getLocale(), actual.getLocale(), "Verify 'locale' attribute"),
        () -> assertEquals(expected.getTimezone(), actual.getTimezone(), "Verify 'timezone' attribute"),
        () -> assertEquals(expected.getExternalId(), actual.getExternalId(), "Verify 'externalId' attribute")
    );
    // @formatter:on
  }

  private Collection<Executable> getNameAssertions(final Name expected, final Name actual) {
    if (expected == null) {
      return Collections.emptyList();
    }

    if (actual == null) {
      fail("actual 'name' could not be null");
    }

    // @formatter:off
    return Arrays.asList(() -> assertEquals(expected.getGivenName(), actual.getGivenName(), "Verify 'name.givenName' attribute"),
        () -> assertEquals(expected.getFamilyName(), actual.getFamilyName(), "Verify 'name.familyName' attribute"),
        () -> assertEquals(expected.getHonorificSuffix(), actual.getHonorificSuffix(), "Verify 'name.honorificSuffix' attribute"),
        () -> assertEquals(expected.getHonorificPrefix(), actual.getHonorificPrefix(), "Verify 'name.honorificPrefix' attribute"),
        () -> assertEquals(expected.getMiddleName(), actual.getMiddleName(), "Verify 'name.middleName' attribute")
    );
    // @formatter:on
  }

  private Collection<Executable> getAllUserMultivaluedAttributeAssertions(final User expected, final User actual) {
    // @formatter:off
    return Arrays.asList(
        () -> assertAll("Verify User emails", getMultivaluedAttrAssertions(expected.getEmails(), actual.getEmails(), Email::getType)),
        () -> assertAll("Verify User roles", getMultivaluedAttrAssertions(expected.getRoles(), actual.getRoles(), Role::getType)),
        () -> assertAll("Verify User photos", getMultivaluedAttrAssertions(expected.getPhotos(), actual.getPhotos(), Photo::getType)),
        () -> assertAll("Verify User phone numbers",
            getMultivaluedAttrAssertions(expected.getPhoneNumbers(), actual.getPhoneNumbers(), PhoneNumber::getType)),
        () -> assertAll("Verify User ims", getMultivaluedAttrAssertions(expected.getIms(), actual.getIms(), Im::getType)),
        () -> assertAll("Verify User entitlements",
            getMultivaluedAttrAssertions(expected.getEntitlements(), actual.getEntitlements(), Entitlement::getType)),
        () -> assertAll("Verify User x509Certificates",
            getMultivaluedAttrAssertions(expected.getX509Certificates(), actual.getX509Certificates(), X509Certificate::getType)),
        () -> assertTrue(areAddressesPresent(expected.getAddresses(), actual.getAddresses()), "Verify User addresses")
    );
    // @formatter:on
  }

  private Collection<Executable> getAllUserEnterpriseAttributeAssertions(final User expected, final User actual) {
    Map<String, Extension> expectedExtensions = expected.getExtensions();
    Map<String, Extension> actualExtensions = actual.getExtensions();

    if (!Stream.of(expectedExtensions, actualExtensions).allMatch(extension -> extension.containsKey(ENTERPRISE_URN))) {
      return Collections.emptyList();
    }

    EnterpriseExtension expectedEnterpriseExtension = (EnterpriseExtension) expectedExtensions.get(ENTERPRISE_URN);
    EnterpriseExtension actualEnterpriseExtension = (EnterpriseExtension) actualExtensions.get(ENTERPRISE_URN);

    // @formatter:off
    return Arrays.asList(
        () -> assertEquals(expectedEnterpriseExtension.getCostCenter(), actualEnterpriseExtension.getCostCenter(), "Verify 'costCenter' attribute"),
        () -> assertEquals(expectedEnterpriseExtension.getDepartment(), actualEnterpriseExtension.getDepartment(), "Verify 'department' attribute"),
        () -> assertEquals(expectedEnterpriseExtension.getDivision(), actualEnterpriseExtension.getDivision(), "Verify 'division' attribute"),
        () -> assertEquals(expectedEnterpriseExtension.getEmployeeNumber(), actualEnterpriseExtension.getEmployeeNumber(),
            "Verify 'employeeNumber' attribute"),
        () -> assertEquals(expectedEnterpriseExtension.getOrganization(), actualEnterpriseExtension.getOrganization(),
            "Verify 'organization' attribute"),
        () -> assertAll("Verify manager", getManagerAssertions(expectedEnterpriseExtension.getManager(), actualEnterpriseExtension.getManager()))
    );
    // @formatter:on
  }

  private Collection<Executable> getManagerAssertions(final Manager expected, final Manager actual) {
    if (expected == null) {
      return Collections.emptyList();
    }
    return Collections.singletonList(() -> assertEquals(expected.getValue(), actual.getValue(), "Verify manager 'value' attribute"));
  }

  private <T extends MultiValuedAttribute, S extends MultiValuedAttributeType> Collection<Executable> getMultivaluedAttrAssertions(
      final Collection<T> expectedCollection, final Collection<T> actualCollection, final Function<T, S> attributeTypeRetriever) {

    Collection<Executable> executables = expectedCollection.stream()
        .map(mattr -> (Executable) () -> assertAll("Verify assertions for current multivalue attribute: " + mattr.getClass().getSimpleName(),
            () -> assertTrue(actualCollection.stream().map(MultiValuedAttribute::getValue).anyMatch(mattr.getValue()::equals),
                "Verify multivalued attribute exist"),
            () -> {
              T currentActualAttribute = actualCollection.stream().filter(currentAttr -> mattr.getValue().equals(currentAttr.getValue())).findFirst()
                  .orElseThrow(() -> new RuntimeException("Multivalued attribute was not found"));

              assertAll(String.format("Verify Multivalued attribute - %s, sub-attributes", currentActualAttribute.getClass().getSimpleName()),
                  getMultivaluedAttrAssertions(mattr, attributeTypeRetriever.apply(mattr), currentActualAttribute,
                      attributeTypeRetriever.apply(currentActualAttribute)));
            }))
        .collect(Collectors.toList());

    return Arrays.asList(() -> assertEquals(expectedCollection.size(), actualCollection.size()),
        () -> assertAll("Verify multivalued attribute existence", executables));
  }

  private Collection<Executable> getMultipleUsersAssertions(final Collection<User> expectedUsers, final Collection<User> actualUsers,
      final BiFunction<User, User, Collection<Executable>> singleUserAssertions) {
    return expectedUsers.stream()
        .map(createdUser -> (Executable) () -> assertAll("Verify assertions for current '" + createdUser.getUserName() + "' user",
            () -> assertTrue(isUserExistInCollection(createdUser, actualUsers), "Verify existence in GET Users response"), () -> {
              User fetchedUser = actualUsers.stream().filter(currentUser -> createdUser.getId().equals(currentUser.getId())).findFirst()
                  .orElseThrow(() -> new RuntimeException("User was not found"));

              assertAll(singleUserAssertions.apply(createdUser, fetchedUser));
            }))
        .collect(Collectors.toList());
  }

  private boolean isUserExistInCollection(final User user, final Collection<User> userCollection) {
    return userCollection.stream().map(User::getId).anyMatch(user.getId()::equals);
  }

  private boolean areAddressesPresent(final Collection<Address> expected, final Collection<Address> actual) {
    return expected.size() == actual.size() && expected.stream().allMatch(address -> actual.stream().anyMatch(address::equals));
  }

  private Collection<Executable> getMultivaluedAttrAssertions(final MultiValuedAttribute expected, final MultiValuedAttributeType expectedType,
      final MultiValuedAttribute actual, final MultiValuedAttributeType actualType) {

    // @formatter:off
    return Arrays.asList(() -> assertEquals(expected.getDisplay(), actual.getDisplay(), "Verify 'display' attribute"),
        () -> assertEquals(expected.isPrimary(), actual.isPrimary(), "Verify 'primary' attribute"),
        () -> assertEquals(expected.getValue(), actual.getValue(), "Verify 'value' attribute"),
        () -> assertEquals(expectedType.getValue(), actualType.getValue(), "Verify 'type' attribute")
    );
    // @formatter:on
  }

  private Collection<Executable> getCommonUsedAttributeAssertions(final User expected, final User actual) {
    // @formatter:off
    return Arrays.asList(() -> assertEquals(expected.getUserName(), actual.getUserName(), "Verify 'userName' attribute"),
        () -> assertAll("Verify 'name' attribute", getNameAssertions(expected.getName(), actual.getName())),
        () -> assertEquals(expected.getNickName(), actual.getNickName(), "Verify 'nickName' attribute"),
        () -> assertTrue(actual.isActive() != null && actual.isActive(), "Verify 'active' attribute"),
        () -> getMetaAssertions(actual, User.RESOURCE_TYPE_USER),
        () -> assertTrue(actual.getMeta().getLocation().endsWith(constructResourceLocation(actual)), "Verify 'meta.location' attribute")
    );
    // @formatter:on
  }

  private Collection<Executable> getRequiredAttributeAssertions(final User expectedUser, final User actualUser) {
    return Arrays.asList(() -> assertEquals(expectedUser.getUserName(), actualUser.getUserName(), "Verify userName is assigned"),
        () -> assertNotNull(actualUser.getId(), "Verify Id is provided by the server"), getMetaAssertions(actualUser, User.RESOURCE_TYPE_USER));
  }

  private List<User> createMultipleUsers(final String commonUserNamePart, final int count) {
    return IntStream.rangeClosed(1, count).mapToObj(number -> commonUserNamePart + number)
        .peek(currentDisplayName -> logger.info("Creating User -{}-", currentDisplayName)).map(TestData::buildTestUser)
        .map(userFailSafeClient::create).collect(Collectors.toList());
  }
}
