
package com.sap.scim.callback.resourcetype;

import com.sap.scim.entity.paging.PagedResult;
import com.sap.scim.entity.schema.ResourceType;

import java.util.Collections;

public class DefaultResourceTypesCallback implements ResourceTypesCallback {
  @Override
  public PagedResult<ResourceType> getCustomResourceTypes() {
    return new PagedResult<>(0, Collections.emptyList());
  }

  @Override
  public ResourceType getCustomResourceType(String typeId) {
    return null;
  }
}
