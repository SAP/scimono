package com.sap.scimono.scim.system.tests.extensions;

import com.sap.scimono.client.GroupRequest;
import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.client.query.IdentityPageQuery;
import com.sap.scimono.client.query.IndexPageQuery;
import com.sap.scimono.entity.Group;
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

import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public abstract class GroupClientScimResponseExtension implements BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback {
  private static final Logger logger = LoggerFactory.getLogger(GroupClientScimResponseExtension.class);

  private final Map<String, Group> managedResources = new HashMap<>();
  private final GroupRequest groupRequest;
  private final GroupFailSafeClient groupFailSafeClient;

  public GroupClientScimResponseExtension(GroupRequest groupRequest) {
    this.groupRequest = groupRequest;
    this.groupFailSafeClient = new GroupFailSafeClient(this);
  }

  public void after(ExtensionContext extensionContext) {
    logger.info("Deleting managed resources by test: {}", extensionContext.getDisplayName());
    clearManagedResources();
  }

  public void before(ExtensionContext extensionContext) {

  }

  public void clearManagedResources() {
    new HashSet<>(managedResources.keySet()).forEach(groupFailSafeClient::delete);
    managedResources.clear();
  }

  public GroupFailSafeClient getFailSafeClient() {
    return groupFailSafeClient;
  }

  public SCIMResponse<Group> createGroup(Group group) {
    SCIMResponse<Group> createGroupResponse = groupRequest.createGroup(group);
    if (createGroupResponse.isSuccess()) {
      Group createdGroup = createGroupResponse.get();
      managedResources.put(createdGroup.getId(), createdGroup);
    }
    return createGroupResponse;
  }

  public SCIMResponse<Void> deleteGroup(String groupId) {
    SCIMResponse<Void> scimResponse = groupRequest.deleteGroup(groupId);
    if(scimResponse.isSuccess()) {
      managedResources.remove(groupId);
    }
    return scimResponse;
  }

  public SCIMResponse<Group> readSingleGroup(String id) {
    return groupRequest.readSingleGroup(id);
  }

  public SCIMResponse<PagedByIndexSearchResult<Group>> readAllGroups() {
    return groupRequest.readAllGroups();
  }

  public SCIMResponse<PagedByIndexSearchResult<Group>> readAllGroups(String filter) {
    return groupRequest.readAllGroups(filter);
  }

  public SCIMResponse<PagedByIndexSearchResult<Group>> readMultipleGroups() {
    return groupRequest.readMultipleGroups();
  }

  public SCIMResponse<PagedByIndexSearchResult<Group>> readMultipleGroups(String filter) {
    return groupRequest.readMultipleGroups(filter);
  }

  public SCIMResponse<PagedByIdentitySearchResult<Group>> readMultipleGroups(IdentityPageQuery identityPageQuery) {
    return groupRequest.readMultipleGroups(identityPageQuery);
  }

  public SCIMResponse<PagedByIdentitySearchResult<Group>> readMultipleGroups(IdentityPageQuery identityPageQuery, String filter) {
    return groupRequest.readMultipleGroups(identityPageQuery, filter);
  }

  public SCIMResponse<PagedByIndexSearchResult<Group>> readMultipleGroups(IndexPageQuery indexPageQuery) {
    return groupRequest.readMultipleGroups(indexPageQuery);
  }

  public SCIMResponse<PagedByIndexSearchResult<Group>> readMultipleGroups(IndexPageQuery indexPageQuery, String filter) {
    return groupRequest.readMultipleGroups(indexPageQuery, filter);
  }

  public SCIMResponse<PagedByIndexSearchResult<Group>> readMultipleGroupsWithoutPaging() {
    return groupRequest.readMultipleGroupsWithoutPaging();
  }

  public SCIMResponse<PagedByIndexSearchResult<Group>> readMultipleGroupsWithoutPaging(String filter) {
    return groupRequest.readMultipleGroupsWithoutPaging(filter);
  }

  public SCIMResponse<PagedByIndexSearchResult<Group>> readMultipleGroupsIndexed(Response response) {
    return groupRequest.readMultipleGroupsIndexed(response);
  }

  public SCIMResponse<Group> updateGroup(Group group) {
    return groupRequest.updateGroup(group);
  }

  public SCIMResponse<Void> patchGroup(PatchBody patchBody, String groupId) {
    return groupRequest.patchGroup(patchBody, groupId);
  }

  public static GroupClientScimResponseExtension forClearingAfterAllExecutions(GroupRequest groupRequest) {
    return new GroupClientScimResponseExtensionAroundAll(groupRequest);
  }

  public static GroupClientScimResponseExtension forClearingAfterEachExecutions(GroupRequest groupRequest) {
    return new GroupClientScimResponseExtensionAroundEach(groupRequest);
  }

  public static  class GroupClientScimResponseExtensionAroundEach extends GroupClientScimResponseExtension {
    private GroupClientScimResponseExtensionAroundEach(GroupRequest groupRequest) {
      super(groupRequest);
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

  public static class GroupClientScimResponseExtensionAroundAll extends GroupClientScimResponseExtension {
    private GroupClientScimResponseExtensionAroundAll(GroupRequest groupRequest) {
      super(groupRequest);
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
      super.before(extensionContext);
    }

    @Override public void afterAll(ExtensionContext extensionContext) {
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
