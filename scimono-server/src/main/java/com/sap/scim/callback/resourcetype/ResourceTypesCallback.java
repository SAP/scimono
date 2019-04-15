
package com.sap.scim.callback.resourcetype;

import com.sap.scim.entity.paging.PagedResult;
import com.sap.scim.entity.schema.ResourceType;

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
}
