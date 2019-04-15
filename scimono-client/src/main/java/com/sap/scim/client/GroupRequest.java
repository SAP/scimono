package com.sap.scim.client;

import com.sap.scim.entity.Group;
import com.sap.scim.entity.paging.PagedByIdentitySearchResult;
import com.sap.scim.entity.paging.PagedByIndexSearchResult;
import com.sap.scim.entity.patch.PatchBody;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import static com.sap.scim.api.API.GROUPS;

public class GroupRequest {
  private SCIMResourceRequest<Group> resourceRequestDelegate;

  public GroupRequest(WebTarget targetSystem, SCIMRequest scimRequest) {
    this.resourceRequestDelegate = new SCIMResourceRequest<>(targetSystem.path(GROUPS), scimRequest, Group.class);
  }

  public SCIMResponse<Group> createGroup(Group group) {
    return resourceRequestDelegate.createResource(group);
  }

  public SCIMResponse<Group> readSingleGroup(String id) {
    return resourceRequestDelegate.readSingleResource(id);
  }

  public SCIMResponse<PagedByIndexSearchResult<Group>> readAllGroups() {
    return resourceRequestDelegate.readAllResources(new GenericType<PagedByIndexSearchResult<Group>>(){});
  }

  public SCIMResponse<PagedByIndexSearchResult<Group>> readMultipleGroups() {
    return resourceRequestDelegate.readMultipleResources(new GenericType<PagedByIndexSearchResult<Group>>(){});
  }

  public SCIMResponse<PagedByIdentitySearchResult<Group>> readMultipleGroups(IdentityPageQuery identityPageQuery) {
    return resourceRequestDelegate.readMultipleResources(identityPageQuery, new GenericType<PagedByIdentitySearchResult<Group>>(){});
  }

  public SCIMResponse<PagedByIndexSearchResult<Group>> readMultipleGroups(IndexPageQuery indexPageQuery) {
    return resourceRequestDelegate.readMultipleResources(indexPageQuery, new GenericType<PagedByIndexSearchResult<Group>>(){});
  }

  public SCIMResponse<PagedByIndexSearchResult<Group>> readMultipleGroupsWithoutPaging() {
    return resourceRequestDelegate.readMultipleResourcesWithoutPaging(new GenericType<PagedByIndexSearchResult<Group>>(){});
  }

  public SCIMResponse<PagedByIndexSearchResult<Group>> readMultipleGroupsIndexed(Response response) {
    return resourceRequestDelegate.readMultipleResourcesIndexed(response, new GenericType<PagedByIndexSearchResult<Group>>(){});
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
