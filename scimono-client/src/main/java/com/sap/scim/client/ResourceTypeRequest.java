package com.sap.scim.client;

import com.sap.scim.entity.paging.PagedByIndexSearchResult;
import com.sap.scim.entity.schema.ResourceType;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;

import static com.sap.scim.api.API.RESOURCE_TYPES;

public class ResourceTypeRequest {
  private SCIMResourceRequest<ResourceType> resourceRequestDelegate;

  public ResourceTypeRequest(WebTarget targetSystem, SCIMRequest scimRequest) {
    this.resourceRequestDelegate = new SCIMResourceRequest<>(targetSystem.path(RESOURCE_TYPES), scimRequest, ResourceType.class);
  }

  public SCIMResponse<ResourceType> readSingleResourceType(String id) {
    return resourceRequestDelegate.readSingleResource(id);
  }

  public SCIMResponse<PagedByIndexSearchResult<ResourceType>> readAllResourceTypes() {
    return resourceRequestDelegate.readMultipleResourcesWithoutPaging(new GenericType<PagedByIndexSearchResult<ResourceType>>(){});
  }

}
