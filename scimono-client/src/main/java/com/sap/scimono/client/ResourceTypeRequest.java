package com.sap.scimono.client;

import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.schema.ResourceType;

import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;

import static com.sap.scimono.api.API.RESOURCE_TYPES;

public class ResourceTypeRequest {
  private final SCIMResourceRequest<ResourceType> resourceRequestDelegate;

  public ResourceTypeRequest(WebTarget targetSystem, SCIMRequest scimRequest) {
    this.resourceRequestDelegate = new SCIMResourceRequest<>(targetSystem.path(RESOURCE_TYPES), scimRequest, ResourceType.class);
  }

  public SCIMResponse<ResourceType> readSingleResourceType(String id) {
    return resourceRequestDelegate.readSingleResource(id, RequestDetails.DEFAULT);
  }

  public SCIMResponse<PagedByIndexSearchResult<ResourceType>> readAllResourceTypes() {
    return resourceRequestDelegate.readMultipleResources(new GenericType<PagedByIndexSearchResult<ResourceType>>(){}, RequestDetails.DEFAULT);
  }

}
