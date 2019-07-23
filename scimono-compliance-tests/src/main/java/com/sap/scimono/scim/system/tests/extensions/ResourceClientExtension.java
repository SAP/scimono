package com.sap.scimono.scim.system.tests.extensions;

import com.sap.scimono.entity.Resource;
import com.sap.scimono.entity.paging.PagedByIdentitySearchResult;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.patch.PatchBody;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceClientExtension<T extends Resource<T>> implements BeforeEachCallback, AfterEachCallback, ResourceFailSafeClient<T> {
  private static final Logger logger = LoggerFactory.getLogger(ResourceClientExtension.class);

  private final Map<String, T> managedResources = new HashMap<>();
  private ResourceFailSafeClient<T> resourceFailsSafeClientDelegate;

  public ResourceClientExtension(ResourceFailSafeClient<T> resourceFailsSafeClientDelegate) {
    this.resourceFailsSafeClientDelegate = resourceFailsSafeClientDelegate;
  }

  public void addManagedResource(T resource) {
    managedResources.put(resource.getId(), resource);
  }

  public void removeManagedResource(String id) {
    managedResources.remove(id);
  }

  @Override
  public void afterEach(ExtensionContext extensionContext) {
    logger.info("Deleting managed resources by test: {}", extensionContext.getDisplayName());
    managedResources.keySet().forEach(resourceFailsSafeClientDelegate::delete);
  }

  @Override
  public void beforeEach(ExtensionContext extensionContext) {

  }

  @Override
  public T create(T resource) {
    T createdResource = resourceFailsSafeClientDelegate.create(resource);
    managedResources.put(createdResource.getId(), createdResource);

    return createdResource;
  }

  @Override
  public T update(String resourceId, T resource) {
    return resourceFailsSafeClientDelegate.update(resourceId, resource);
  }

  @Override
  public void delete(String resourceId) {
    resourceFailsSafeClientDelegate.delete(resourceId);
    managedResources.remove(resourceId);
  }

  @Override
  public void patch(String resourceId, PatchBody patchBody) {
    resourceFailsSafeClientDelegate.patch(resourceId, patchBody);
  }

  @Override
  public T getSingle(String resourceId) {
    return resourceFailsSafeClientDelegate.getSingle(resourceId);
  }

  @Override
  public List<T> getAllWithIdPaging() {
    return resourceFailsSafeClientDelegate.getAllWithIdPaging();
  }

  @Override
  public List<T> getAllWithIndexPaging() {
    return resourceFailsSafeClientDelegate.getAllWithIndexPaging();
  }

  @Override
  public List<T> getAllByFilter(String filterExpression) {
    return resourceFailsSafeClientDelegate.getAllByFilter(filterExpression);
  }

  @Override
  public PagedByIndexSearchResult<T> getPagedByIndex(int startIndex, int count) {
    return resourceFailsSafeClientDelegate.getPagedByIndex(startIndex, count);
  }

  @Override
  public PagedByIdentitySearchResult<T> getPagedById(String startId, int count) {
    return resourceFailsSafeClientDelegate.getPagedById(startId, count);
  }

  @Override
  public PagedByIndexSearchResult<T> getByFilteredAndPagedByIndex(int startIndex, int count, String filter) {
    return resourceFailsSafeClientDelegate.getByFilteredAndPagedByIndex(startIndex, count, filter);
  }

  @Override
  public PagedByIdentitySearchResult<T> getByFilteredAndPagedById(String startId, int count, String filter) {
    return resourceFailsSafeClientDelegate.getByFilteredAndPagedById(startId, count, filter);
  }
}
