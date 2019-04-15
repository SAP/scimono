
package com.sap.scimono.entity.paging;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.scimono.entity.Resource;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PagedByIdentitySearchResult<T extends Resource<T>> extends PagedByIndexSearchResult<T> {

  public static final String PAGINATION_BY_ID_START_PARAM = "initial";
  public static final String PAGINATION_BY_ID_END_PARAM = "end";
  private final String startId;
  private final String nextId;

  @JsonCreator
  public PagedByIdentitySearchResult(@JsonProperty("Resources") final List<T> resources, @JsonProperty("totalResults") final long totalResults,
                                      @JsonProperty("itemsPerPage") final long itemsPerPage, @JsonProperty("startId") final String startId,
                                      @JsonProperty("nextId") final String nextId) {
    super(resources, totalResults, itemsPerPage, null);

    this.startId = startId;
    this.nextId = nextId;
  }

  public PagedByIdentitySearchResult(PagedResult<T> resources, final String startId,
                                     final String nextId) {
    this(resources.getResources(), resources.getTotalResourceCount(), resources.getResourcesCount(), startId, nextId);
  }

  public String getStartId() {
    return startId;
  }

  public String getNextId() {
    return nextId;
  }
}
