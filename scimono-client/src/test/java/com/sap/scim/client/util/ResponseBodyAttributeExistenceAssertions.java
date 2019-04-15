package com.sap.scim.client.util;

import com.sap.scim.entity.Meta;
import com.sap.scim.entity.Resource;
import com.sap.scim.entity.paging.PagedByIdentitySearchResult;
import com.sap.scim.entity.paging.PagedByIndexSearchResult;
import org.junit.jupiter.api.function.Executable;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResponseBodyAttributeExistenceAssertions {
  private static final String LIST_RESPONSE_SCHEMA = "urn:ietf:params:scim:api:messages:2.0:ListResponse";

  private ResponseBodyAttributeExistenceAssertions() {
  }

  public static List<Executable> getCommonResourceAssertions(Resource<?> resource, String resourceSchema) {
    // @formatter:off
    return Arrays.asList(
        () -> assertNotNull(resource.getSchemas()),
        () -> assertTrue(resource.getSchemas().contains(resourceSchema)),
        () -> assertNotNull(resource.getId()),
        () -> assertNotNull(resource.getMeta()),
        () -> {
          Meta meta = resource.getMeta();
          assertAll(
              () -> assertNotNull(meta.getCreated()),
              () -> assertNotNull(meta.getLastModified()),
              () -> assertNotNull(meta.getVersion()),
              () -> assertNotNull(meta.getLocation()),
              () -> assertNotNull(meta.getResourceType())
          );
        }
    );
    // @formatter:on
  }

  public static List<Executable> getPagedByIndexResponseAssertions(PagedByIndexSearchResult<?> listResponse) {
    // @formatter:off
    return Arrays.asList(
        () -> assertNotNull(listResponse.getStartIndex()),
        () -> assertNotNull(listResponse.getItemsPerPage()),
        () -> assertNotNull(listResponse.getTotalResults()),
        () -> assertNotNull(listResponse.getSchemas()),
        () -> assertTrue(listResponse.getSchemas().contains(LIST_RESPONSE_SCHEMA)),
        () -> assertNotNull(listResponse.getResources()),
        () -> assertFalse(listResponse.getResources().isEmpty())
    );
    // @formatter:on
  }

  public static List<Executable> getPagedByIdentityResponseAssertions(PagedByIdentitySearchResult<?> listResponse) {
    // @formatter:off
    return Arrays.asList(
        () -> assertNotNull(listResponse.getStartId()),
        () -> assertNotNull(listResponse.getNextId()),
        () -> assertNotNull(listResponse.getItemsPerPage()),
        () -> assertNotNull(listResponse.getTotalResults()),
        () -> assertNotNull(listResponse.getSchemas()),
        () -> assertTrue(listResponse.getSchemas().contains(LIST_RESPONSE_SCHEMA)),
        () -> assertNotNull(listResponse.getResources()),
        () -> assertFalse(listResponse.getResources().isEmpty())
    );
    // @formatter:on
  }
}
