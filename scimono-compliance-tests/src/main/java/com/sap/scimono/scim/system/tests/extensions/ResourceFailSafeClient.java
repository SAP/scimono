
package com.sap.scimono.scim.system.tests.extensions;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.entity.Resource;
import com.sap.scimono.entity.paging.PagedByIdentitySearchResult;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.patch.PatchBody;

public interface ResourceFailSafeClient<T extends Resource<T>> {
  T create(final T resource);

  T update(final String resourceId, final T resource);

  void delete(final String resourceId);

  void patch(final String resourceId, final PatchBody patchBody);

  T getSingle(final String resourceId);

  List<T> getAllWithIdPaging();

  List<T> getAllWithIndexPaging();

  List<T> getAllByFilter(final String filterExpression);

  PagedByIndexSearchResult<T> getPagedByIndex(final int startIndex, final int count);

  PagedByIndexSearchResult<T> getAllWithoutPaging();

  PagedByIdentitySearchResult<T> getPagedById(final String startId, final int count);

  PagedByIndexSearchResult<T> getByFilteredAndPagedByIndex(final int startIndex, final int count, final String filter);

  PagedByIdentitySearchResult<T> getByFilteredAndPagedById(final String startId, final int count, final String filter);

  default <T> T verifyAndGetResponse(final SCIMResponse<T> scimResponse) {
    if (!scimResponse.isSuccess()) {
      fail("Scim request executed and unexpected response received: " + scimResponse.getError().asUnknownException());
    }

    return scimResponse.get();
  }
}