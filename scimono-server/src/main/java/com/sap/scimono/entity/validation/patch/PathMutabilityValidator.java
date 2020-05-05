
package com.sap.scimono.entity.validation.patch;

import java.util.ArrayList;
import java.util.List;

import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.entity.patch.PatchOperation;
import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.entity.validation.AttributeImmutableValueValidator;
import com.sap.scimono.entity.validation.AttributeReadOnlyValidator;
import com.sap.scimono.entity.validation.Validator;
import com.sap.scimono.helper.Strings;

public class PathMutabilityValidator implements Validator<PatchOperation> {

  private final SchemasCallback schemaAPI;

  public PathMutabilityValidator(final SchemasCallback schemaAPI) {
    this.schemaAPI = schemaAPI;
  }

  @Override
  public void validate(final PatchOperation operation) {
    String path = operation.getPath();

    if (isPathRepresentSchema(path)) {
      return;
    }

    Attribute targetAttribute = schemaAPI.getAttribute(path);

    List<Validator<Attribute>> mutabilityValidators = new ArrayList<>();
    mutabilityValidators.add(new AttributeReadOnlyValidator());
    if (PatchOperation.Type.REPLACE.equals(operation.getOp())) {
      mutabilityValidators.add(new AttributeImmutableValueValidator());
    }
    mutabilityValidators.forEach(validator -> validator.validate(targetAttribute));
  }

  private boolean isPathRepresentSchema(final String path) {
    return Strings.isNullOrEmpty(path) || schemaAPI.getSchema(path) != null;
  }
}
