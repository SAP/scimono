package com.sap.scimono.client;

import com.sap.scimono.entity.Resource;
import com.sap.scimono.entity.paging.PagedByIdentitySearchResult;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.paging.PagedResult;
import com.sap.scimono.entity.patch.PatchBody;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import java.util.LinkedList;
import java.util.List;

import static com.sap.scimono.client.ResourceAction.CREATE_SINGLE;
import static com.sap.scimono.client.ResourceAction.DELETE;
import static com.sap.scimono.client.ResourceAction.GET_ALL;
import static com.sap.scimono.client.ResourceAction.GET_SINGLE;
import static com.sap.scimono.client.ResourceAction.PATCH_UPDATE;
import static com.sap.scimono.client.ResourceAction.PUT_UPDATE;
import static com.sap.scimono.client.ResourcePageQuery.indexPageQuery;
import static com.sap.scimono.entity.paging.PagedByIndexSearchResult.DEFAULT_COUNT;

class SCIMResourceRequest<T extends Resource<T>> {
  private WebTarget targetSystem;
  private SCIMRequest scimRequest;
  private Class<T> resourceClass;

  SCIMResourceRequest(WebTarget targetSystem, SCIMRequest scimRequest, Class<T> resourceClass) {
    this.targetSystem = targetSystem;
    this.scimRequest = scimRequest;
    this.resourceClass = resourceClass;
  }

  static <T extends Resource<T>> SCIMResourceRequest<T> newInstance(WebTarget targetSystem, SCIMRequest scimRequest, Class<T> resourceClass) {
    return new SCIMResourceRequest<>(targetSystem, scimRequest, resourceClass);
  }

  SCIMResponse<T> createResource(T resource) {
    Response response = scimRequest.post(targetSystem, resource);
    return SCIMResponse.newInstance(resourceClass, response, scimRequest.getScimActionResponseStatusConfig(CREATE_SINGLE));
  }

  SCIMResponse<T> readSingleResource(String id) {
    Response response = scimRequest.get(targetSystem.path(id));
    return SCIMResponse.newInstance(resourceClass, response, scimRequest.getScimActionResponseStatusConfig(GET_SINGLE));
  }

  SCIMResponse<PagedByIndexSearchResult<T>> readAllResources(GenericType<PagedByIndexSearchResult<T>> responseType) {
    int startIndex = 1;
    int count = Integer.parseInt(DEFAULT_COUNT);
    long totalResults;

    PagedByIndexSearchResult<T> getPagedResourcesSearchResult;
    Response lastHttpResponse;
    SCIMResponse<PagedByIndexSearchResult<T>> lastResourcesPageResponse;
    List<T> allResources = new LinkedList<>();

    do {
      lastHttpResponse = scimRequest.get(indexPageQuery().withStartIndexAndCount(startIndex, count).apply(targetSystem));
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

    return SCIMResponse.fromEntity(new PagedByIndexSearchResult<>(new PagedResult<>(allResources.size(), allResources), 1L), lastHttpResponse,
        scimRequest.getScimActionResponseStatusConfig(GET_ALL));
  }

  public SCIMResponse<PagedByIndexSearchResult<T>> readMultipleResources(GenericType<PagedByIndexSearchResult<T>> responseType) {
    return readMultipleResources(indexPageQuery(), responseType);
  }

  SCIMResponse<PagedByIdentitySearchResult<T>> readMultipleResources(IdentityPageQuery identityPageQuery, GenericType<PagedByIdentitySearchResult<T>> listResponseTypePaging) {
    Response response = scimRequest.get(identityPageQuery.apply(targetSystem));
    return SCIMResponse.newInstance(listResponseTypePaging, response, scimRequest.getScimActionResponseStatusConfig(GET_ALL));
  }


  SCIMResponse<PagedByIndexSearchResult<T>> readMultipleResources(IndexPageQuery indexPageQuery, GenericType<PagedByIndexSearchResult<T>> responseType) {
    Response response = scimRequest.get(indexPageQuery.apply(targetSystem));
    return readMultipleResourcesIndexed(response, responseType);
  }

  SCIMResponse<PagedByIndexSearchResult<T>> readMultipleResourcesWithoutPaging(GenericType<PagedByIndexSearchResult<T>> listResponseTypePaging) {
    Response response = scimRequest.get(targetSystem);
    return SCIMResponse.newInstance(listResponseTypePaging, response, scimRequest.getScimActionResponseStatusConfig(GET_ALL));
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
}
