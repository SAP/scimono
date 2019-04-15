
package com.sap.scimono.entity.definition;


import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.sap.scimono.callback.schemas.SchemasCallback;

class ScimAttributeImpl<T extends ScimAttribute<T>> implements ScimAttribute<T> {

  private String attributeName;
  private String schemaId;
  private T parent;
  private Supplier<T[]> allSchemaAttributes;

  ScimAttributeImpl(String attributeName, String schemaId, T parent, Supplier<T[]> allSchemaAttributes) {
    this.attributeName = attributeName;
    this.schemaId = schemaId;
    this.parent = parent;
    this.allSchemaAttributes = allSchemaAttributes;
  }

  @Override
  public boolean isTopLevelAttribute() {
    return parent == null;
  }

  @Override
  public String scimName() {
    return attributeName;
  }

  @Override
  public T subAttributeFrom(String childAttributeName) {
    // @formatter:off
    return Stream.of(allSchemaAttributes.get())
        .filter(scimAttribute -> !scimAttribute.isTopLevelAttribute())
        .filter(scimAttribute -> this.equals(scimAttribute.getParent()))
        .filter(scimAttribute -> childAttributeName.equalsIgnoreCase(scimAttribute.scimName()))
        .findFirst()
        .orElseThrow(() -> new NotExistingScimAttributeException(childAttributeName));
    // @formatter:on
  }

  @Override
  public T getParent() {
    return parent;
  }

  @Override
  public String getSchemaId() {
    return schemaId;
  }

  @Override
  public String fullAttributePath() {
    if(parent == null) {
      return SchemasCallback.addSchemaToPathIfNotExist(attributeName, schemaId);
    }
    return String.join(".", parent.fullAttributePath(), attributeName);
  }

  @Override
  public String relativePath() {
    if(parent == null) {
      return attributeName;
    }
    return String.join(".", parent.relativePath(), attributeName);
  }

  @SuppressWarnings("Method does not check that the class of it's parameter is equals to the one from the current instance. "
      + "In the current implementation and SCIM specification there is now way to have multiple attributes with same name, schema and parent")
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null) {
      return false;
    }

    ScimAttribute<T> that = (ScimAttribute<T>) o;

    return Objects.equals(scimName(), that.scimName())
        && Objects.equals(getSchemaId(), that.getSchemaId())
        && Objects.equals(getParent(), that.getParent());
  }

  @Override
  public int hashCode() {
    return Objects.hash(attributeName, schemaId, parent, allSchemaAttributes);
  }
}
