package com.sap.scimono.client.query;

import javax.ws.rs.client.WebTarget;
import java.util.HashMap;
import java.util.Map;

import static com.sap.scimono.api.API.COUNT_PARAM;
import static com.sap.scimono.api.API.START_ID_PARAM;
import static com.sap.scimono.api.API.START_INDEX_PARAM;

public class ResourcePageQuery implements IdentityPageQuery, IndexPageQuery {
  private static final int DEFAULT_COUNT = 100;
  private static final String DEFAULT_START_ID = "00000000-0000-1000-9000-000000000000";
  private static final long DEFAULT_START_INDEX = 1;

  private final Map<String, Object> queryParams = new HashMap<>();

  private ResourcePageQuery() {
  }

  @Override
  public IdentityPageQuery withStartId(String startId) {
    queryParams.put(START_ID_PARAM, startId);
    return this;
  }

  @Override
  public IdentityPageQuery withStartIdAndCount(String startId, int count) {
    queryParams.put(START_ID_PARAM, startId);
    queryParams.put(COUNT_PARAM, count);
    return this;
  }

  @Override
  public IndexPageQuery withStartIndex(long startIndex) {
    queryParams.put(START_INDEX_PARAM, startIndex);
    return this;
  }

  @Override
  public IndexPageQuery withStartIndexAndCount(long startIndex, int count) {
    queryParams.put(START_INDEX_PARAM, startIndex);
    queryParams.put(COUNT_PARAM, count);
    return this;
  }

  @Override
  public WebTarget apply(WebTarget target) {
    for (Map.Entry<String, Object> queryParam: queryParams.entrySet()) {
      target = target.queryParam(queryParam.getKey(), queryParam.getValue());
    }

    return target;
  }

  public static IdentityPageQuery identityPageQuery() {
    return new ResourcePageQuery().withStartIdAndCount(DEFAULT_START_ID, DEFAULT_COUNT);
  }

  public static IndexPageQuery indexPageQuery() {
    return new ResourcePageQuery().withStartIndexAndCount(DEFAULT_START_INDEX, DEFAULT_COUNT);
  }

}
