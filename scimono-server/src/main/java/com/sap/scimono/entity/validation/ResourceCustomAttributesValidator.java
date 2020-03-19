
package com.sap.scimono.entity.validation;

import java.util.Collections;

import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.entity.Resource;

public class ResourceCustomAttributesValidator<T extends Resource<T>> implements Validator<T> {

  private final SchemasCallback schemaAPI;

  public ResourceCustomAttributesValidator(final SchemasCallback schemaAPI) {
    this.schemaAPI = schemaAPI;
  }

  @Override
  public void validate(final T resource) {
    resource.getExtensions().values()
        .forEach(extension -> new AttributeAndValueValidator(schemaAPI.getSchema(extension.getUrn()), Collections.emptyMap())
            .validate(extension.getAttributes()));
  }

}
