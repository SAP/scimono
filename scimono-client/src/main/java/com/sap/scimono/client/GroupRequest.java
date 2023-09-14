package com.sap.scimono.client;

import static com.sap.scimono.api.API.GROUPS;
import static com.sap.scimono.client.query.ResourcePageQuery.indexPageQuery;

import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

import com.sap.scimono.client.query.IdentityPageQuery;
import com.sap.scimono.client.query.IndexPageQuery;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.paging.PagedByIdentitySearchResult;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.patch.PatchBody;

public class GroupRequest {
  private final SCIMResourceRequest<Group> resourceRequestDelegate;

  public GroupRequest(WebTarget targetSystem, SCIMRequest scimRequest) {
    this.resourceRequestDelegate = new SCIMResourceRequest<>(targetSystem.path(GROUPS), scimRequest, Group.class);
  }

  public SCIMResponse<Group> createGroup(Group group) {
    return resourceRequestDelegate.createResource(group);
  }

  public SCIMResponse<Group> readSingleGroup(String id) {
    return readSingleGroup(id, RequestDetails.DEFAULT);
  }

  public SCIMResponse<PagedByIndexSearchResult<Group>> readAllGroups() {
    return readAllGroups(RequestDetails.DEFAULT);
  }

  @Deprecated
  public SCIMResponse<PagedByIndexSearchResult<Group>> readAllGroups(String filter) {
    return readAllGroups(RequestDetails.builder().withFilter(filter).build());
  }

  public SCIMResponse<PagedByIndexSearchResult<Group>> readMultipleGroups() {
    return readMultipleGroups(RequestDetails.builder().withPageQuery(indexPageQuery()).build());
  }

  @Deprecated
  public SCIMResponse<PagedByIndexSearchResult<Group>> readMultipleGroups(String filter) {
    return readMultipleGroups(RequestDetails.builder().withPageQuery(indexPageQuery()).withFilter(filter).build());
  }

  @Deprecated
  public SCIMResponse<PagedByIdentitySearchResult<Group>> readMultipleGroups(IdentityPageQuery identityPageQuery) {
    return readMultipleGroupsById(RequestDetails.builder().withPageQuery(identityPageQuery).build());
  }

  @Deprecated
  public SCIMResponse<PagedByIdentitySearchResult<Group>> readMultipleGroups(IdentityPageQuery identityPageQuery, String filter) {
    return readMultipleGroupsById(RequestDetails.builder().withPageQuery(identityPageQuery).withFilter(filter).build());
  }

  @Deprecated
  public SCIMResponse<PagedByIndexSearchResult<Group>> readMultipleGroups(IndexPageQuery indexPageQuery) {
    return readMultipleGroups(RequestDetails.builder().withPageQuery(indexPageQuery).build());
  }

  @Deprecated
  public SCIMResponse<PagedByIndexSearchResult<Group>> readMultipleGroups(IndexPageQuery indexPageQuery, String filter) {
    return readMultipleGroups(RequestDetails.builder().withPageQuery(indexPageQuery).withFilter(filter).build());
  }

  @Deprecated
  public SCIMResponse<PagedByIndexSearchResult<Group>> readMultipleGroupsWithoutPaging() {
    return readMultipleGroups(RequestDetails.DEFAULT);
  }

  @Deprecated
  public SCIMResponse<PagedByIndexSearchResult<Group>> readMultipleGroupsWithoutPaging(String filter) {
    return readMultipleGroups(RequestDetails.builder().withFilter(filter).build());
  }

  public SCIMResponse<PagedByIndexSearchResult<Group>> readMultipleGroupsIndexed(Response response) {
    return resourceRequestDelegate.readMultipleResourcesIndexed(response, new GenericType<PagedByIndexSearchResult<Group>>(){});
  }

  public SCIMResponse<PagedByIndexSearchResult<Group>> readAllGroups(RequestDetails requestDetails) {
    return resourceRequestDelegate.readAllResources(new GenericType<PagedByIndexSearchResult<Group>>(){}, requestDetails);
  }

  public SCIMResponse<PagedByIndexSearchResult<Group>> readMultipleGroups(RequestDetails requestDetails) {
    return resourceRequestDelegate.readMultipleResources(new GenericType<PagedByIndexSearchResult<Group>>(){}, requestDetails);
  }

  public SCIMResponse<PagedByIdentitySearchResult<Group>> readMultipleGroupsById(RequestDetails requestDetails) {
    return resourceRequestDelegate.readMultipleResourcesById(new GenericType<PagedByIdentitySearchResult<Group>>(){}, requestDetails);
  }

  public SCIMResponse<Group> readSingleGroup(String id, RequestDetails requestDetails) {
    return resourceRequestDelegate.readSingleResource(id, requestDetails);
  }

  public SCIMResponse<Group> updateGroup(Group group) {
    return resourceRequestDelegate.updateResource(group);
  }

  public SCIMResponse<Void> patchGroup(PatchBody patchBody, String groupId) {
    return resourceRequestDelegate.patchResource(patchBody, groupId);
  }

  public SCIMResponse<Void> deleteGroup(String groupId) {
    return resourceRequestDelegate.deleteResource(groupId);
  }
}
