
package com.sap.scimono.callback.resourcetype;

import com.sap.scimono.entity.paging.PagedResult;
import com.sap.scimono.entity.schema.ResourceType;
import com.sap.scimono.entity.schema.SchemaExtension;

import java.util.Collections;
import java.util.List;

public interface ResourceTypesCallback {

  /**
   * Return all custom resource types defined by the implementation (e.g. Role, Privilege, etc.)
   * @return list of all custom resources
   */
  PagedResult<ResourceType> getCustomResourceTypes();
  //TODO add proper paging support

  /**
   * Return the custom resource type with the specified unique id.
   * @param typeId
   * @return the custom resource type or null if it does not exist
   */
  ResourceType getCustomResourceType(final String typeId);

  /**
   * Return the custom schema extensions for the specified resource type id.
   * @param resourceTypeId
   * @return list of custom schema extensions
   */
  default List<SchemaExtension> getSchemaExtensions(final String resourceTypeId) {
    return Collections.emptyList();
  }
}
