package com.sap.scimono.scim.system.tests.extensions;

import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.client.UserRequest;
import com.sap.scimono.client.query.IdentityPageQuery;
import com.sap.scimono.client.query.IndexPageQuery;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.paging.PagedByIdentitySearchResult;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.patch.PatchBody;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public abstract class UserClientScimResponseExtension implements BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback  {
  private static final Logger logger = LoggerFactory.getLogger(UserClientScimResponseExtension.class);

  private final Map<String, User> managedResources = new HashMap<>();
  private final UserRequest userRequest;
  private final UserFailSafeClient userFailsSafeClient;

  public UserClientScimResponseExtension(UserRequest userRequest) {
    this.userRequest = userRequest;
    this.userFailsSafeClient = new UserFailSafeClient(this);
  }

  public void after(ExtensionContext extensionContext) {
    logger.info("Deleting managed resources by test: {}", extensionContext.getDisplayName());
    clearManagedResources();
  }

  public void before(ExtensionContext extensionContext) {

  }

  public void clearManagedResources() {
    new HashSet<>(managedResources.keySet()).forEach(userFailsSafeClient::delete);
    managedResources.clear();
  }

  public UserFailSafeClient getFailSafeClient() {
    return userFailsSafeClient;
  }

  public SCIMResponse<User> createUser(User user) {
    SCIMResponse<User> createUserResponse = userRequest.createUser(user);
    if (createUserResponse.isSuccess()) {
      User createdUser = createUserResponse.get();
      managedResources.put(createdUser.getId(), createdUser);
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
      managedResources.remove(userId);
    }
    return scimResponse;
  }

  public static UserClientScimResponseExtension forClearingAfterAllExecutions(UserRequest userRequest) {
    return new UserClientScimResponseExtensionAroundAll(userRequest);
  }

  public static UserClientScimResponseExtension forClearingAfterEachExecutions(UserRequest userRequest) {
    return new UserClientScimResponseExtensionAroundEach(userRequest);
  }

  public static  class UserClientScimResponseExtensionAroundEach extends UserClientScimResponseExtension {
    private UserClientScimResponseExtensionAroundEach(UserRequest userRequest) {
      super(userRequest);
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
      // Not implemented
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
      // Not implemented
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
      super.before(extensionContext);
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
      super.after(extensionContext);
    }
  }

  public static  class UserClientScimResponseExtensionAroundAll extends UserClientScimResponseExtension {
    private UserClientScimResponseExtensionAroundAll(UserRequest userRequest) {
      super(userRequest);
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
      super.before(extensionContext);
    }
    @Override
    public void afterAll(ExtensionContext extensionContext) {
      super.after(extensionContext);
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
      // Not implemented
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
      // Not implemented
    }
  }
}
