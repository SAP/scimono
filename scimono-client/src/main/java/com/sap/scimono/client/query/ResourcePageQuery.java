package com.sap.scimono.client.query;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.ws.rs.client.WebTarget;

public class ResourcePageQuery implements SCIMQuery {
  public static final int DEFAULT_COUNT = 100;
  private static final long DEFAULT_START_INDEX = 1;
  private static final String DEFAULT_START_ID = "00000000-0000-1000-9000-000000000000";

  private final Map<String, Object> queryParams = new LinkedHashMap<>();

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
