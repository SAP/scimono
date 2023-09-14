package com.sap.scimono.client.query;

import jakarta.ws.rs.client.WebTarget;

import static com.sap.scimono.api.API.COUNT_PARAM;
import static com.sap.scimono.api.API.START_ID_PARAM;

public class IdentityPageQuery implements SCIMQuery {
  private ResourcePageQuery resourcePageQuery;

  IdentityPageQuery() {
    resourcePageQuery = new ResourcePageQuery();
  }

  public IdentityPageQuery withStartId(String startId) {
    resourcePageQuery.addQueryParam(START_ID_PARAM, startId);
    return this;
  }

  public IdentityPageQuery withCount(int count) {
    resourcePageQuery.addQueryParam(COUNT_PARAM, count);
    return this;
  }

  @Override
  public WebTarget apply(WebTarget target) {
    return resourcePageQuery.apply(target);
  }
}
