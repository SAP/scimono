package com.sap.scimono.client.query;

import jakarta.ws.rs.client.WebTarget;

import static com.sap.scimono.api.API.COUNT_PARAM;
import static com.sap.scimono.api.API.START_INDEX_PARAM;

public class IndexPageQuery implements SCIMQuery {
  private ResourcePageQuery resourcePageQuery;

  IndexPageQuery() {
    resourcePageQuery = new ResourcePageQuery();
  }

  public IndexPageQuery withStartIndex(long startIndex) {
    resourcePageQuery.addQueryParam(START_INDEX_PARAM, startIndex);
    return this;
  }


  public IndexPageQuery withCount(int count) {
    resourcePageQuery.addQueryParam(COUNT_PARAM, count);
    return this;
  }

  @Override
  public WebTarget apply(WebTarget target) {
    return resourcePageQuery.apply(target);
  }
}
