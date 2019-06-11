package com.sap.scimono.client.query;

import javax.ws.rs.client.WebTarget;
import java.util.HashMap;
import java.util.Map;

import static com.sap.scimono.api.API.COUNT_PARAM;
import static com.sap.scimono.api.API.START_ID_PARAM;
import static com.sap.scimono.api.API.START_INDEX_PARAM;

public class ResourcePageQuery implements SCIMQuery {
  private static final int DEFAULT_COUNT = 100;
  private static final long DEFAULT_START_INDEX = 1;
  private static final String DEFAULT_START_ID = "00000000-0000-1000-9000-000000000000";

  private final Map<String, Object> queryParams = new HashMap<>();

  ResourcePageQuery() {
  }

  void addQueryParam(String name, Object value) {
    queryParams.put(name, value);
  }

  @Override
  public WebTarget apply(WebTarget target) {
    for (Map.Entry<String, Object> queryParam: queryParams.entrySet()) {
      target = target.queryParam(queryParam.getKey(), queryParam.getValue());
    }

    return target;
  }

  public static IdentityPageQuery identityPageQuery() {
    return new IdentityPageQuery().withStartId(DEFAULT_START_ID).withCount(DEFAULT_COUNT);
  }

  public static IndexPageQuery indexPageQuery() {
    return new IndexPageQuery().withStartIndex(DEFAULT_START_INDEX).withCount(DEFAULT_COUNT);
  }

}
