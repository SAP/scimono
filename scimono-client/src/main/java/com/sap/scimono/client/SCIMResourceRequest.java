package com.sap.scimono.client;

import static com.sap.scimono.client.ResourceAction.CREATE_SINGLE;
import static com.sap.scimono.client.ResourceAction.DELETE;
import static com.sap.scimono.client.ResourceAction.GET_ALL;
import static com.sap.scimono.client.ResourceAction.GET_SINGLE;
import static com.sap.scimono.client.ResourceAction.PATCH_UPDATE;
import static com.sap.scimono.client.ResourceAction.PUT_UPDATE;
import static com.sap.scimono.client.query.ResourcePageQuery.indexPageQuery;
import static com.sap.scimono.entity.paging.PagedByIndexSearchResult.DEFAULT_COUNT;

import java.util.LinkedList;
import java.util.List;

import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

import com.sap.scimono.client.query.SCIMQuery;
import com.sap.scimono.entity.Resource;
import com.sap.scimono.entity.paging.PagedByIdentitySearchResult;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.paging.PagedResult;
import com.sap.scimono.entity.patch.PatchBody;

class SCIMResourceRequest<T extends Resource<T>> {
  private final WebTarget targetSystem;
  private final SCIMRequest scimRequest;
  private final Class<T> resourceClass;

  SCIMResourceRequest(WebTarget targetSystem, SCIMRequest scimRequest, Class<T> resourceClass) {
    this.targetSystem = targetSystem;
    this.scimRequest = scimRequest;
    this.resourceClass = resourceClass;
  }

  @Deprecated
  static <T extends Resource<T>> SCIMResourceRequest<T> newInstance(WebTarget targetSystem, SCIMRequest scimRequest, Class<T> resourceClass) {
    return new SCIMResourceRequest<>(targetSystem, scimRequest, resourceClass);
  }

  SCIMResponse<T> createResource(T resource) {
    Response response = scimRequest.post(targetSystem, resource);
    return SCIMResponse.newInstance(resourceClass, response, scimRequest.getScimActionResponseStatusConfig(CREATE_SINGLE));
  }

  SCIMResponse<T> readSingleResource(String id, RequestDetails requestDetails) {
    Response response = scimRequest.get(requestDetails.getAttributes().apply(targetSystem.path(id)));
    return SCIMResponse.newInstance(resourceClass, response, scimRequest.getScimActionResponseStatusConfig(GET_SINGLE));
  }

  SCIMResponse<PagedByIndexSearchResult<T>> readMultipleResourcesIndexed(Response response, GenericType<PagedByIndexSearchResult<T>> responseType) {
    return SCIMResponse.newInstance(responseType, response, scimRequest.getScimActionResponseStatusConfig(GET_ALL));
  }

  SCIMResponse<T> updateResource(T resource) {
    Response response = scimRequest.put(targetSystem.path(resource.getId()), resource);
    return SCIMResponse.newInstance(resourceClass, response, scimRequest.getScimActionResponseStatusConfig(PUT_UPDATE));
  }

  SCIMResponse<Void> patchResource(PatchBody patchBody, String resourceId) {
    Response response = scimRequest.patch(targetSystem.path(resourceId), patchBody);
    return SCIMResponse.fromEmpty(response, scimRequest.getScimActionResponseStatusConfig(PATCH_UPDATE));
  }

  SCIMResponse<Void> deleteResource(String resourceId) {
    Response response = scimRequest.delete(targetSystem.path(resourceId));
    return SCIMResponse.fromEmpty(response, scimRequest.getScimActionResponseStatusConfig(DELETE));
  }

  SCIMResponse<PagedByIndexSearchResult<T>> readMultipleResources(GenericType<PagedByIndexSearchResult<T>> responseType, RequestDetails requestDetails) {
    Response response = scimRequest.get(requestDetails.apply(targetSystem));
    return readMultipleResourcesIndexed(response, responseType);
  }

  SCIMResponse<PagedByIdentitySearchResult<T>> readMultipleResourcesById(GenericType<PagedByIdentitySearchResult<T>> listResponseTypePaging, RequestDetails requestDetails) {
    Response response = scimRequest.get(requestDetails.apply(targetSystem));
    return SCIMResponse.newInstance(listResponseTypePaging, response, scimRequest.getScimActionResponseStatusConfig(GET_ALL));
  }

  SCIMResponse<PagedByIndexSearchResult<T>> readAllResources(GenericType<PagedByIndexSearchResult<T>> responseType, RequestDetails requestDetails) {
    int startIndex = 1;
    int count = Integer.parseInt(DEFAULT_COUNT);
    long totalResults;

    PagedByIndexSearchResult<T> getPagedResourcesSearchResult;
    Response lastHttpResponse;
    SCIMResponse<PagedByIndexSearchResult<T>> lastResourcesPageResponse;
    List<T> allResources = new LinkedList<>();

    do {
      lastHttpResponse = scimRequest.get(new SCIMQuery.SCIMQueryBuilder(targetSystem)
          .apply(indexPageQuery().withStartIndex(startIndex).withCount(count))
          .apply(requestDetails.getFilter())
          .apply(requestDetails.getAttributes())
          .get());
      lastResourcesPageResponse = readMultipleResourcesIndexed(lastHttpResponse, responseType);

      if(!lastResourcesPageResponse.isSuccess()) {
        return lastResourcesPageResponse;
      }

      getPagedResourcesSearchResult = lastResourcesPageResponse.get();
      totalResults = getPagedResourcesSearchResult.getTotalResults();

      List<T> resourcesPerPage = getPagedResourcesSearchResult.getResources();
      allResources.addAll(resourcesPerPage);

      startIndex = startIndex + count;
    } while (startIndex <= totalResults);

    return SCIMResponse.fromEntity(new PagedByIndexSearchResult<>(new PagedResult<>(allResources.size(), allResources), 1), lastHttpResponse,
        scimRequest.getScimActionResponseStatusConfig(GET_ALL));
  }
}
