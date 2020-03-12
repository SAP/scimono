
package com.sap.scimono.entity.validation;

import java.util.Collections;

import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.entity.Resource;

public class ResourceCustomAttributesValidator<T extends Resource<?>> implements Validator<T> {

  private final SchemasCallback schemaAPI;
  private final boolean isOperationReplacing;

  public ResourceCustomAttributesValidator(final SchemasCallback schemaAPI, final boolean isOperationReplacing) {
    this.schemaAPI = schemaAPI;
    this.isOperationReplacing = isOperationReplacing;
  }

  @Override
  public void validate(final T resource) {
    resource.getExtensions().values()
        .forEach(extension -> new AttributeAndValueValidator(schemaAPI.getSchema(extension.getUrn()), Collections.emptyMap(), isOperationReplacing)
            .validate(extension.getAttributes()));
  }

}
