package com.sap.scimono.scim.system.tests.extensions;

import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.paging.PagedByIdentitySearchResult;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.patch.PatchBody;

import java.util.LinkedList;
import java.util.List;

import static com.sap.scimono.client.query.ResourcePageQuery.identityPageQuery;
import static com.sap.scimono.client.query.ResourcePageQuery.indexPageQuery;
import static com.sap.scimono.entity.paging.PagedByIdentitySearchResult.PAGINATION_BY_ID_END_PARAM;
import static com.sap.scimono.entity.paging.PagedByIdentitySearchResult.PAGINATION_BY_ID_START_PARAM;
import static com.sap.scimono.entity.paging.PagedByIndexSearchResult.DEFAULT_COUNT;
import static org.junit.jupiter.api.Assertions.fail;

public class GroupFailSafeClient implements ResourceFailSafeClient<Group> {
  private final GroupClientScimResponseExtension groupRequest;

  public GroupFailSafeClient(GroupClientScimResponseExtension groupRequest) {
    this.groupRequest = groupRequest;
  }

  @Override
  public Group create(Group group) {
    return verifyAndGetResponse(groupRequest.createGroup(group));
  }

  @Override
  public Group update(String groupId, Group updatedGroup) {
    return verifyAndGetResponse(groupRequest.updateGroup(updatedGroup));
  }

  @Override
  public void delete(String groupId) {
    verifyAndGetResponse(groupRequest.deleteGroup(groupId));
  }

  @Override
  public void patch(String groupId, PatchBody patchBody) {
    verifyAndGetResponse(groupRequest.patchGroup(patchBody, groupId));
  }

  @Override
  public Group getSingle(String groupId) {
    return verifyAndGetResponse(groupRequest.readSingleGroup(groupId));
  }

  @Override
  public List<Group> getAllWithIdPaging() {
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

  @Override
  public List<Group> getAllWithIndexPaging() {
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

  @Override
  public List<Group> getAllByFilter(String filterExpression) {
    return verifyAndGetResponse(groupRequest.readMultipleGroups(filterExpression)).getResources();
  }

  @Override
  public PagedByIndexSearchResult<Group> getPagedByIndex(int startIndex, int count) {
    return verifyAndGetResponse(groupRequest.readMultipleGroups(indexPageQuery().withStartIndex(startIndex).withCount(count)));
  }

  @Override
  public PagedByIndexSearchResult<Group> getAllWithoutPaging() {
    return verifyAndGetResponse(groupRequest.readMultipleGroupsWithoutPaging());
  }

  @Override
  public PagedByIdentitySearchResult<Group> getPagedById(String startId, int count) {
    return verifyAndGetResponse(groupRequest.readMultipleGroups(identityPageQuery().withStartId(startId).withCount(count)));
  }

  @Override
  public PagedByIndexSearchResult<Group> getByFilteredAndPagedByIndex(int startIndex, int count, String filter) {
    return verifyAndGetResponse(groupRequest.readMultipleGroups(indexPageQuery().withStartIndex(startIndex).withCount(count), filter));
  }

  @Override
  public PagedByIdentitySearchResult<Group> getByFilteredAndPagedById(String startId, int count, String filter) {
    return verifyAndGetResponse(groupRequest.readMultipleGroups(identityPageQuery().withStartId(startId).withCount(count), filter));
  }
}
