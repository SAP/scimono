package com.sap.scimono.scim.system.tests.extensions;

import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.client.UserRequest;
import com.sap.scimono.client.query.IdentityPageQuery;
import com.sap.scimono.client.query.IndexPageQuery;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.paging.PagedByIdentitySearchResult;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.patch.PatchBody;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.HashMap;
import java.util.Map;

public class UserClientScimResponseExtension implements BeforeEachCallback, AfterEachCallback {
  private final Map<String, User> managedResourceIds = new HashMap<>();
  private final UserRequest userRequest;

  public UserClientScimResponseExtension(UserRequest userRequest) {
    this.userRequest = userRequest;
  }

  @Override
  public void afterEach(ExtensionContext extensionContext) throws Exception {

  }

  @Override
  public void beforeEach(ExtensionContext extensionContext) throws Exception {

  }

  public SCIMResponse<User> createUser(User user) {
    SCIMResponse<User> createUserResponse = userRequest.createUser(user);
    if (createUserResponse.isSuccess()) {
      User createdUser = createUserResponse.get();
      managedResourceIds.put(createdUser.getId(), createdUser);
    }
    return createUserResponse;
  }

  public SCIMResponse<User> readSingleUser(String id) {
    return userRequest.readSingleUser(id);
  }

  public SCIMResponse<PagedByIndexSearchResult<User>> readAllUsers() {
    return userRequest.readAllUsers();
  }

  public SCIMResponse<PagedByIndexSearchResult<User>> readAllUsers(String scimFilter) {
    return userRequest.readAllUsers(scimFilter);
  }

  public SCIMResponse<PagedByIndexSearchResult<User>> readMultipleUsers() {
    return userRequest.readMultipleUsers();
  }

  public SCIMResponse<PagedByIndexSearchResult<User>> readMultipleUsers(String filter) {
    return userRequest.readMultipleUsers(filter);
  }

  public SCIMResponse<PagedByIdentitySearchResult<User>> readMultipleUsers(IdentityPageQuery identityPageQuery) {
    return userRequest.readMultipleUsers(identityPageQuery);
  }

  public SCIMResponse<PagedByIdentitySearchResult<User>> readMultipleUsers(IdentityPageQuery identityPageQuery, String filter) {
    return userRequest.readMultipleUsers(identityPageQuery, filter);
  }

  public SCIMResponse<PagedByIndexSearchResult<User>> readMultipleUsers(IndexPageQuery indexPageQuery) {
    return userRequest.readMultipleUsers(indexPageQuery);
  }

  public SCIMResponse<PagedByIndexSearchResult<User>> readMultipleUsers(IndexPageQuery indexPageQuery, String filter) {
    return userRequest.readMultipleUsers(indexPageQuery, filter);
  }

  public SCIMResponse<PagedByIndexSearchResult<User>> readMultipleUsersWithoutPaging() {
    return userRequest.readMultipleUsersWithoutPaging();
  }

  public SCIMResponse<PagedByIndexSearchResult<User>> readMultipleUsersWithoutPaging(String filter) {
    return userRequest.readMultipleUsersWithoutPaging(filter);
  }

  public SCIMResponse<User> updateUser(User user) {
    return userRequest.updateUser(user);
  }

  public SCIMResponse<Void> patchUser(PatchBody patchBody, String userId) {
    return userRequest.patchUser(patchBody, userId);
  }

  public SCIMResponse<Void> deleteUser(String userId) {
    SCIMResponse<Void> scimResponse = userRequest.deleteUser(userId);
    if(scimResponse.isSuccess()) {
      managedResourceIds.remove(userId);
    }
    return scimResponse;
  }
}
