package com.sap.scimono.client;

import com.sap.scimono.entity.User;
import com.sap.scimono.entity.paging.PagedByIdentitySearchResult;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.patch.PatchBody;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;

import static com.sap.scimono.api.API.USERS;

public class UserRequest {
  private SCIMResourceRequest<User> resourceRequestDelegate;

  public UserRequest(WebTarget targetSystem, SCIMRequest scimRequest) {
    this.resourceRequestDelegate = new SCIMResourceRequest<>(targetSystem.path(USERS), scimRequest, User.class);
  }

  public SCIMResponse<User> createUser(User user) {
    return resourceRequestDelegate.createResource(user);
  }

  public SCIMResponse<User> readSingleUser(String id) {
    return resourceRequestDelegate.readSingleResource(id);
  }

  public SCIMResponse<PagedByIndexSearchResult<User>> readAllUsers() {
    return resourceRequestDelegate.readAllResources(new GenericType<PagedByIndexSearchResult<User>>(){});
  }

  public SCIMResponse<PagedByIndexSearchResult<User>> readMultipleUsers() {
    return resourceRequestDelegate.readMultipleResources(new GenericType<PagedByIndexSearchResult<User>>(){});
  }

  public SCIMResponse<PagedByIdentitySearchResult<User>> readMultipleUsers(IdentityPageQuery identityPageQuery) {
    return resourceRequestDelegate.readMultipleResources(identityPageQuery, new GenericType<PagedByIdentitySearchResult<User>>(){});
  }

  public SCIMResponse<PagedByIndexSearchResult<User>> readMultipleUsers(IndexPageQuery indexPageQuery) {
    return resourceRequestDelegate.readMultipleResources(indexPageQuery, new GenericType<PagedByIndexSearchResult<User>>(){});
  }

  public SCIMResponse<PagedByIndexSearchResult<User>> readMultipleUsersWithoutPaging() {
    return resourceRequestDelegate.readMultipleResourcesWithoutPaging(new GenericType<PagedByIndexSearchResult<User>>(){});
  }

  public SCIMResponse<User> updateUser(User user) {
    return resourceRequestDelegate.updateResource(user);
  }

  public SCIMResponse<Void> patchUser(PatchBody patchBody, String userId) {
    return resourceRequestDelegate.patchResource(patchBody, userId);
  }

  public SCIMResponse<Void> deleteUser(String userId) {
    return resourceRequestDelegate.deleteResource(userId);
  }
}
