package com.sap.scimono.client;

import com.sap.scimono.client.query.ResourcePageQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResourcePageQueryTest {
  private static final String DEFAULT_URL = "http://localhost:7070/idds/scim/v2/Users";
  private WebTarget webTarget;

  @BeforeEach
  public void setup() {
    webTarget = ClientBuilder.newClient().target(DEFAULT_URL);
  }

  @Test
  public void testCreateDefaultIndexFilter() {
    String resultRequestPath = ResourcePageQuery.indexPageQuery().apply(webTarget).getUri().toString();
    assertTrue((DEFAULT_URL + "?startIndex=1&count=100").equals(resultRequestPath)
                || (DEFAULT_URL + "?count=100&startIndex=1").equals(resultRequestPath));
  }

  @Test
  public void testCreateDefaultIdentityFilter() {
    String resultRequestPath = ResourcePageQuery.identityPageQuery().apply(webTarget).getUri().toString();
    assertTrue((DEFAULT_URL + "?startId=00000000-0000-1000-9000-000000000000&count=100").equals(resultRequestPath)
                || (DEFAULT_URL + "?count=100&startId=00000000-0000-1000-9000-000000000000").equals(resultRequestPath));
  }

  @Test
  public void testCreateCustomIndexFilter() {
    int startIndex = 1;
    int count = 50;

    String resultRequestPath = ResourcePageQuery.indexPageQuery().withStartIndex(startIndex).withCount(count).apply(webTarget).getUri().toString();
    assertTrue((String.format("%s?startIndex=%d&count=%d", DEFAULT_URL, startIndex, count)).equals(resultRequestPath)
                || (String.format("%s?count=%d&startIndex=%d", DEFAULT_URL, count, startIndex)).equals(resultRequestPath));
  }

  @Test
  public void testCreateCustomIdentityFilter() {
    String startId = "00000000-0000-1000-9000-000000000000";
    int count = 50;

    String resultRequestPath = ResourcePageQuery.identityPageQuery().withStartId(startId).withCount(count).apply(webTarget).getUri().toString();
    assertTrue((String.format("%s?startId=%s&count=%d", DEFAULT_URL, startId, count)).equals(resultRequestPath)
                || (String.format("%s?count=%d&startId=%s", DEFAULT_URL, count, startId)).equals(resultRequestPath));
  }
}
