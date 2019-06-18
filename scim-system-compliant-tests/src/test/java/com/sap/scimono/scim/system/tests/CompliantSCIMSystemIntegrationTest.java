/**
 * Copyright (c) 2017 by SAP Labs Bulgaria, url: http://www.sap.com All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP SE, Walldorf. You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered into with SAP.
 *
 * Created on Mar 17, 2017 by i061675
 *
 */

package com.sap.scimono.scim.system.tests;

import com.sap.scimono.client.GroupRequest;
import com.sap.scimono.client.SCIMClientService;
import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.client.SchemaRequest;
import com.sap.scimono.client.UserRequest;
import com.sap.scimono.client.authentication.OauthCredentials;
import com.sap.scimono.client.authentication.TargetSystemBasicAuthenticator;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.base.ExtensionFieldType;
import com.sap.scimono.entity.paging.PagedByIdentitySearchResult;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.patch.PatchBody;
import com.sap.scimono.entity.schema.Schema;
import com.sap.scimono.scim.system.tests.util.TestData;
import com.sap.scimono.scim.system.tests.util.TestReporter;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.sap.scimono.client.authentication.OauthAuthenticatorFactory.clientCredentialsGrantAuthenticator;
import static com.sap.scimono.client.query.ResourcePageQuery.identityPageQuery;
import static com.sap.scimono.client.query.ResourcePageQuery.indexPageQuery;
import static com.sap.scimono.entity.paging.PagedByIdentitySearchResult.PAGINATION_BY_ID_END_PARAM;
import static com.sap.scimono.entity.paging.PagedByIdentitySearchResult.PAGINATION_BY_ID_START_PARAM;
import static com.sap.scimono.entity.paging.PagedByIndexSearchResult.DEFAULT_COUNT;
import static com.sap.scimono.scim.system.tests.util.TestProperties.BASIC_AUTH_ENABLED;
import static com.sap.scimono.scim.system.tests.util.TestProperties.BASIC_AUTH_PASSWORD;
import static com.sap.scimono.scim.system.tests.util.TestProperties.BASIC_AUTH_USER;
import static com.sap.scimono.scim.system.tests.util.TestProperties.OAUTH_CLIENT_ID;
import static com.sap.scimono.scim.system.tests.util.TestProperties.OAUTH_SECRET;
import static com.sap.scimono.scim.system.tests.util.TestProperties.OAUTH_SERVICE_URL;
import static com.sap.scimono.scim.system.tests.util.TestProperties.SERVICE_URL;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(TestReporter.class)
public abstract class CompliantSCIMSystemIntegrationTest {

  protected final UserRequest userRequest;
  protected final GroupRequest groupRequest;
  protected final SchemaRequest schemaRequest;

  protected CompliantSCIMSystemIntegrationTest() {
    SCIMClientService scimClientService = configureScimClientService(SERVICE_URL);
    userRequest = scimClientService.buildUserRequest();
    groupRequest = scimClientService.buildGroupRequest();
    schemaRequest = scimClientService.buildSchemaRequest();
  }

  protected SCIMClientService configureScimClientService(String serviceUrl) {
    SCIMClientService.Builder clientServiceBuilder = SCIMClientService
        .builder(serviceUrl).addProperty(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
        .addProperty(TestProperties.LOG_TRAFFIC, true).addProperty(TestProperties.DUMP_ENTITY, true)
        .addProperty(LoggingFeature.LOGGING_FEATURE_VERBOSITY_CLIENT, LoggingFeature.Verbosity.PAYLOAD_ANY)
        .addProperty(LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL_CLIENT, "WARNING");

    if("true".equalsIgnoreCase(BASIC_AUTH_ENABLED)) {
      clientServiceBuilder.addAuthenticator(TargetSystemBasicAuthenticator.create(BASIC_AUTH_USER, BASIC_AUTH_PASSWORD));
    } else {
      clientServiceBuilder.addAuthenticator(clientCredentialsGrantAuthenticator(getOauthClient(), OAUTH_SERVICE_URL, new OauthCredentials(
          OAUTH_CLIENT_ID, OAUTH_SECRET)));
    }

    return clientServiceBuilder.build();
  }
  private Client getOauthClient() {
    Logger testLogger = Logger.getLogger(CompliantSCIMSystemIntegrationTest.class.getName());
    LoggingFeature loggingFeature = new LoggingFeature(testLogger, Level.WARNING, LoggingFeature.Verbosity.PAYLOAD_ANY, null);

    Client client = ClientBuilder.newClient();
    client.register(loggingFeature);
    return client;
  }

  protected User getUser(final String userId) {
    return verifyAndGetResponse(userRequest.readSingleUser(userId));
  }

  protected List<User> getAllUsersWithIdPaging() {
    String startId = PAGINATION_BY_ID_START_PARAM;
    int count = Integer.parseInt(DEFAULT_COUNT);
    PagedByIdentitySearchResult<User> pagedUsers;
    List<User> allUsers = new LinkedList<>();
    do {
      SCIMResponse<PagedByIdentitySearchResult<User>> scimResponse = userRequest
          .readMultipleUsers(identityPageQuery().withStartId(startId).withCount(count));

      if(!scimResponse.isSuccess()) {
        fail("Request to Idds Service does not finished successfully");
      }
      pagedUsers = scimResponse.get();

      List<User> usersPerPage = pagedUsers.getResources();
      allUsers.addAll(usersPerPage);

      startId = pagedUsers.getNextId();
    } while (!startId.equals(PAGINATION_BY_ID_END_PARAM));

    return allUsers;
  }

  protected List<User> getAllUsersWithIndexPaging() {
    int startIndex = 1;
    int count = Integer.parseInt(DEFAULT_COUNT);
    long totalResults = 0;
    PagedByIndexSearchResult<User> getPagedUsersSearchResult;
    List<User> allUsers = new LinkedList<>();
    do {
      SCIMResponse<PagedByIndexSearchResult<User>> scimResponse = userRequest
          .readMultipleUsers(indexPageQuery().withStartIndex(startIndex).withCount(count));

      if(!scimResponse.isSuccess()) {
        fail("Request to Idds Service does not finished successfully");
      }
      getPagedUsersSearchResult = scimResponse.get();

      totalResults = getPagedUsersSearchResult.getTotalResults();

      List<User> usersPerPage = getPagedUsersSearchResult.getResources();
      allUsers.addAll(usersPerPage);

      startIndex = startIndex + count;
    } while (startIndex <= totalResults);

    return allUsers;
  }

  protected List<User> getUsersFiltered(final String filterExpression) {
    return verifyAndGetResponse(userRequest.readMultipleUsers(filterExpression)).getResources();
  }

  protected PagedByIndexSearchResult<User> getUsersPagedByIndex(final int startIndex, final int count) {
    return verifyAndGetResponse(userRequest.readMultipleUsers(indexPageQuery().withStartIndex(startIndex).withCount(count)));
  }

  protected PagedByIdentitySearchResult<User> getUsersPagedById(final String startId, final int count) {
    return verifyAndGetResponse(userRequest.readMultipleUsers(identityPageQuery().withStartId(startId).withCount(count)));
  }

  protected PagedByIndexSearchResult<User> getUsersFilteredAndPagedByIndex(final int startIndex, final int count, final String filter) {
    return verifyAndGetResponse(userRequest.readMultipleUsers(indexPageQuery().withStartIndex(startIndex).withCount(count), filter));
  }

  protected PagedByIdentitySearchResult<User> getUsersFilteredAndPagedById(final String startId, final int count, final String filter) {
    return verifyAndGetResponse(userRequest.readMultipleUsers(identityPageQuery().withStartId(startId).withCount(count), filter));
  }

  protected User createUser(final User user) {
    return verifyAndGetResponse(userRequest.createUser(user));
  }

  protected User updateUser(final String userId, final User updatedUser) {
    return verifyAndGetResponse(userRequest.updateUser(updatedUser));
  }

  protected void deleteUser(final String userId) {
    verifyAndGetResponse(userRequest.deleteUser(userId));
  }

  protected List<Schema> getAllSchemas() {
    return verifyAndGetResponse(schemaRequest.readAllSchemas()).getResources();
  }

  protected Schema getSchema(final String schemaId) {
    return verifyAndGetResponse(schemaRequest.readSingleSchema(schemaId));
  }

  protected Schema createSchema(final Schema schema) {
    return verifyAndGetResponse(schemaRequest.createSchema(schema));
  }

  protected void deleteSchema(final String schemaId) {
    verifyAndGetResponse(schemaRequest.deleteSchema(schemaId));
  }

  protected Group getGroup(final String groupId) {
    return verifyAndGetResponse(groupRequest.readSingleGroup(groupId));
  }

  protected List<Group> getAllGroupsWithIdPaging() {
    String startId = PAGINATION_BY_ID_START_PARAM;
    int count = Integer.parseInt(DEFAULT_COUNT);
    PagedByIdentitySearchResult<Group> pagedGroups;
    List<Group> allGroups = new LinkedList<>();

    do {
      SCIMResponse<PagedByIdentitySearchResult<Group>> scimResponse = groupRequest
          .readMultipleGroups(identityPageQuery().withStartId(startId).withCount(count));

      if(!scimResponse.isSuccess()) {
        fail("Request to Idds Service does not finished successfully");
      }
      pagedGroups = scimResponse.get();

      List<Group> groupsPerPage = pagedGroups.getResources();
      allGroups.addAll(groupsPerPage);

      startId = pagedGroups.getNextId();
    } while (!startId.equals(PAGINATION_BY_ID_END_PARAM));

    return allGroups;
  }

  protected List<Group> getAllGroupsWithIndexPaging() {
    int startIndex = 1;
    int count = Integer.parseInt(DEFAULT_COUNT);
    long totalResults = 0;
    PagedByIndexSearchResult<Group> getPagedGroupsResult;
    List<Group> allGroups = new LinkedList<>();

    do {
      SCIMResponse<PagedByIndexSearchResult<Group>> scimResponse = groupRequest
          .readMultipleGroups(indexPageQuery().withStartIndex(startIndex).withCount(count));

      if(!scimResponse.isSuccess()) {
        fail("Request to Idds Service does not finished successfully");
      }

      getPagedGroupsResult = scimResponse.get();
      totalResults = getPagedGroupsResult.getTotalResults();

      List<Group> groupsPerPage = getPagedGroupsResult.getResources();
      allGroups.addAll(groupsPerPage);

      startIndex = startIndex + count;
    } while (startIndex <= totalResults);

    return allGroups;
  }

  protected PagedByIdentitySearchResult<Group> getGroupsPagedById(final String startId, final int count) {
    return verifyAndGetResponse(groupRequest.readMultipleGroups(identityPageQuery().withStartId(startId).withCount(count)));
  }

  protected PagedByIndexSearchResult<Group> getGroupsPagedByIndex(final int startIndex, final int count) {
    return verifyAndGetResponse(groupRequest.readMultipleGroups(indexPageQuery().withStartIndex(startIndex).withCount(count)));
  }

  protected List<Group> getGroupsFiltered(final String filterExpression) {
    return verifyAndGetResponse(groupRequest.readMultipleGroups(filterExpression)).getResources();
  }

  protected PagedByIndexSearchResult<Group> getGroupsFilteredAndPagedByIndex(final int startIndex, final int count, final String filter) {
    return verifyAndGetResponse(groupRequest.readMultipleGroups(indexPageQuery().withStartIndex(startIndex).withCount(count), filter));
  }

  protected PagedByIdentitySearchResult<Group> getGroupsFilteredAndPagedById(final String startId, final int count, final String filter) {
    return verifyAndGetResponse(groupRequest.readMultipleGroups(identityPageQuery().withStartId(startId).withCount(count), filter));
  }

  protected Group createGroup(final Group group) {
    return verifyAndGetResponse(groupRequest.createGroup(group));
  }

  protected Group updateGroup(final String groupId, final Group updatedGroup) {
    return verifyAndGetResponse(groupRequest.updateGroup(updatedGroup));
  }

  protected Schema createCustomTestSchema(final String schemaId, final String custAttr1, final String custAttr2) {
    Map<String, ExtensionFieldType<?>> customAttrsNameToType = new HashMap<>();
    customAttrsNameToType.put(custAttr1, ExtensionFieldType.STRING);
    customAttrsNameToType.put(custAttr2, ExtensionFieldType.STRING);
    Schema testSchema = TestData.buildCustomSchemaWithAttrs(schemaId, customAttrsNameToType);

    return createSchema(testSchema);
  }

  protected void patchUser(final String resourceId, final PatchBody patchBody) {
    verifyAndGetResponse(userRequest.patchUser(patchBody, resourceId));
  }

  protected void patchGroup(final String resourceId, final PatchBody patchBody) {
    verifyAndGetResponse(groupRequest.patchGroup(patchBody, resourceId));
  }

  protected boolean isGroupIdPresentInGroupResponse(final Group testGroup) {
    List<Group> allGroups = getAllGroupsWithIndexPaging();
    for (Group nextGroup : allGroups) {
      if (testGroup.getId().equals(nextGroup.getId())) {

        return true;
      }
    }

    return false;
  }

  private  <T> T verifyAndGetResponse(SCIMResponse<T> scimResponse) {
    if(!scimResponse.isSuccess()) {
      fail("Scim request executed and unexpected response received: " + scimResponse.getError().asUnknownException());
    }

    return scimResponse.get();
  }
  protected boolean isUserFetchedInResponse(final String userId, final List<User> fetchedUsers) {
    return fetchedUsers.stream().anyMatch(user -> user.getId().equals(userId));
  }

  protected boolean isGroupFetchedInResponse(final String groupId, final List<Group> fetchedGroups) {
    return fetchedGroups.stream().anyMatch(group -> group.getId().equals(groupId));
  }

}