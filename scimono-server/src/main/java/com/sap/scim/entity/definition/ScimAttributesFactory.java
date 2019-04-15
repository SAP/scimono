
package com.sap.scim.entity.definition;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.sap.scim.callback.schemas.SchemasCallback;

public class ScimAttributesFactory<T extends ScimAttribute<T>> {
  private List<T> scimAttributes;
  private String schemaId;

  public ScimAttributesFactory(Supplier<T[]> enumValues) {
    this.scimAttributes = Arrays.asList(enumValues.get());

    if (!scimAttributes.isEmpty()) {
      this.schemaId = scimAttributes.get(0).getSchemaId();
    }
  }

  /**
   * Parses the attribute name (which can either be its short name, e.g. <b>userName</b>
   * or its long name, e.g. <b>urn:ietf:params:scim:schemas:core:2.0:User.userName</b>)
   * and returns the ScimAttribute object representing it.
   *
   * @param attributeName either short or full attribute name
   * @return
   */
  public T from(String attributeName) {
    // @formatter:off
    return scimAttributes.stream()
        .filter(attribute -> attribute.isTopLevelAttribute() && attributeName.equalsIgnoreCase(attribute.scimName()))
        .findFirst()
        .orElseGet(() ->
            fromFullAttributePath(attributeName)
                .orElseThrow(() -> new NotExistingScimAttributeException(attributeName)));
    // @formatter:on
  }

  private Optional<T> fromFullAttributePath(String attributeName) {
    String fullAttributePath = SchemasCallback.addSchemaToPathIfNotExist(attributeName, schemaId);

    // @formatter:off
    return scimAttributes.stream()
        .filter(attribute -> fullAttributePath.equalsIgnoreCase(attribute.fullAttributePath()))
        .findFirst();
    // @formatter:on
  }
}
