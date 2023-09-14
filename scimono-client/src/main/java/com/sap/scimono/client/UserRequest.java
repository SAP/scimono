package com.sap.scimono.client;

import static com.sap.scimono.api.API.USERS;
import static com.sap.scimono.client.query.ResourcePageQuery.indexPageQuery;

import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;

import com.sap.scimono.client.query.IdentityPageQuery;
import com.sap.scimono.client.query.IndexPageQuery;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.paging.PagedByIdentitySearchResult;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.patch.PatchBody;

public class UserRequest {
  private final SCIMResourceRequest<User> resourceRequestDelegate;

  public UserRequest(WebTarget targetSystem, SCIMRequest scimRequest) {
    this.resourceRequestDelegate = new SCIMResourceRequest<>(targetSystem.path(USERS), scimRequest, User.class);
  }

  public SCIMResponse<User> createUser(User user) {
    return resourceRequestDelegate.createResource(user);
  }

  public SCIMResponse<User> readSingleUser(String id) {
    return readSingleUser(id, RequestDetails.DEFAULT);
  }

  public SCIMResponse<PagedByIndexSearchResult<User>> readAllUsers() {
    return readAllUsers(RequestDetails.DEFAULT);
  }

  public SCIMResponse<PagedByIndexSearchResult<User>> readAllUsers(String scimFilter) {
    return readAllUsers(RequestDetails.builder().withFilter(scimFilter).build());
  }

  public SCIMResponse<PagedByIndexSearchResult<User>> readMultipleUsers() {
    return readMultipleUsers(RequestDetails.builder().withPageQuery(indexPageQuery()).build());
  }

  @Deprecated
  public SCIMResponse<PagedByIndexSearchResult<User>> readMultipleUsers(String filter) {
    return readMultipleUsers(RequestDetails.builder().withPageQuery(indexPageQuery()).withFilter(filter).build());
  }

  @Deprecated
  public SCIMResponse<PagedByIdentitySearchResult<User>> readMultipleUsers(IdentityPageQuery identityPageQuery) {
    return readMultipleUsersById(RequestDetails.builder().withPageQuery(identityPageQuery).build());
  }

  @Deprecated
  public SCIMResponse<PagedByIdentitySearchResult<User>> readMultipleUsers(IdentityPageQuery identityPageQuery, String filter) {
    return readMultipleUsersById(RequestDetails.builder().withPageQuery(identityPageQuery).withFilter(filter).build());
  }

  @Deprecated
  public SCIMResponse<PagedByIndexSearchResult<User>> readMultipleUsers(IndexPageQuery indexPageQuery) {
    return readMultipleUsers(RequestDetails.builder().withPageQuery(indexPageQuery).build());
  }

  @Deprecated
  public SCIMResponse<PagedByIndexSearchResult<User>> readMultipleUsers(IndexPageQuery indexPageQuery, String filter) {
    return readMultipleUsers(RequestDetails.builder().withPageQuery(indexPageQuery).withFilter(filter).build());
  }

  @Deprecated
  public SCIMResponse<PagedByIndexSearchResult<User>> readMultipleUsersWithoutPaging() {
    return readMultipleUsers(RequestDetails.DEFAULT);
  }

  @Deprecated
  public SCIMResponse<PagedByIndexSearchResult<User>> readMultipleUsersWithoutPaging(String filter) {
    return readMultipleUsers(RequestDetails.builder().withFilter(filter).build());
  }

  public SCIMResponse<PagedByIndexSearchResult<User>> readAllUsers(RequestDetails requestDetails) {
    return resourceRequestDelegate.readAllResources(new GenericType<PagedByIndexSearchResult<User>>(){}, requestDetails);
  }

  public SCIMResponse<PagedByIndexSearchResult<User>> readMultipleUsers(RequestDetails requestDetails) {
    return resourceRequestDelegate.readMultipleResources(new GenericType<PagedByIndexSearchResult<User>>(){}, requestDetails);
  }

  public SCIMResponse<PagedByIdentitySearchResult<User>> readMultipleUsersById(RequestDetails requestDetails) {
    return resourceRequestDelegate.readMultipleResourcesById(new GenericType<PagedByIdentitySearchResult<User>>(){}, requestDetails);
  }

  public SCIMResponse<User> readSingleUser(String id, RequestDetails requestDetails) {
    return resourceRequestDelegate.readSingleResource(id, requestDetails);
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
