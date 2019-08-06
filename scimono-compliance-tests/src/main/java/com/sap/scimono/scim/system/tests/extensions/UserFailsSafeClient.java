package com.sap.scimono.scim.system.tests.extensions;

import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.entity.User;
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

public class UserFailsSafeClient implements ResourceFailSafeClient<User>{
  private final UserClientScimResponseExtension userRequest;

  public UserFailsSafeClient(UserClientScimResponseExtension userRequest) {
    this.userRequest = userRequest;
  }

  @Override
  public User create(User user) {
    return verifyAndGetResponse(userRequest.createUser(user));
  }

  @Override
  public User update(String resourceId, User updatedUser) {
    return verifyAndGetResponse(userRequest.updateUser(updatedUser));
  }

  @Override
  public void delete(String userId) {
    verifyAndGetResponse(userRequest.deleteUser(userId));
  }

  @Override
  public void patch(String userId, PatchBody patchBody) {
    verifyAndGetResponse(userRequest.patchUser(patchBody, userId));
  }

  @Override
  public User getSingle(String userId) {
    return verifyAndGetResponse(userRequest.readSingleUser(userId));
  }

  @Override
  public List<User> getAllWithIdPaging() {
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

  @Override
  public List<User> getAllWithIndexPaging() {
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

  @Override
  public List<User> getAllByFilter(String filterExpression) {
    return verifyAndGetResponse(userRequest.readMultipleUsers(filterExpression)).getResources();
  }

  @Override
  public PagedByIndexSearchResult<User> getPagedByIndex(int startIndex, int count) {
    return verifyAndGetResponse(userRequest.readMultipleUsers(indexPageQuery().withStartIndex(startIndex).withCount(count)));
  }

  @Override
  public PagedByIndexSearchResult<User> getAllWithoutPaging() {
    return verifyAndGetResponse(userRequest.readMultipleUsersWithoutPaging());
  }

  @Override
  public PagedByIdentitySearchResult<User> getPagedById(String startId, int count) {
    return verifyAndGetResponse(userRequest.readMultipleUsers(identityPageQuery().withStartId(startId).withCount(count)));
  }

  @Override
  public PagedByIndexSearchResult<User> getByFilteredAndPagedByIndex(int startIndex, int count, String filter) {
    return verifyAndGetResponse(userRequest.readMultipleUsers(indexPageQuery().withStartIndex(startIndex).withCount(count), filter));
  }

  @Override
  public PagedByIdentitySearchResult<User> getByFilteredAndPagedById(String startId, int count, String filter) {
    return verifyAndGetResponse(userRequest.readMultipleUsers(identityPageQuery().withStartId(startId).withCount(count), filter));
  }
}
