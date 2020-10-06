
package com.sap.scimono.callback.resourcetype;

import com.sap.scimono.entity.paging.PagedResult;
import com.sap.scimono.entity.schema.ResourceType;
import com.sap.scimono.entity.schema.SchemaExtension;

import java.util.Collections;
import java.util.List;

public class DefaultResourceTypesCallback implements ResourceTypesCallback {
  @Override
  public PagedResult<ResourceType> getCustomResourceTypes() {
    return new PagedResult<>(0, Collections.emptyList());
  }

  @Override
  public ResourceType getCustomResourceType(String typeId) {
    return null;
  }

  @Override
  public List<SchemaExtension> getSchemaExtensions(String resourceTypeId) {
    return Collections.emptyList();
  }
}
