
package com.sap.scim.entity.definition;

public interface ScimAttribute<T extends ScimAttribute<T>> {

  boolean isTopLevelAttribute();

  String scimName();

  String fullAttributePath();

  String relativePath();

  T subAttributeFrom(String childAttributeName);

  T getParent();

  String getSchemaId();
}
