
package com.sap.scimono.entity.validation.patch;

import static com.sap.scimono.helper.Strings.isNullOrEmpty;

import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.entity.patch.PatchOperation;
import com.sap.scimono.entity.validation.Validator;
import com.sap.scimono.exception.SCIMException;

public class PathAttributeExistanceValidator implements Validator<PatchOperation> {


  private SchemasCallback schemaAPI;

  public PathAttributeExistanceValidator(final SchemasCallback schemaAPI) {
    this.schemaAPI = schemaAPI;
  }

  @Override
  public void validate(PatchOperation operation) {
    String path = operation.getPath();

    if (!isValidFullAttributeNotation(path) && schemaAPI.getCustomSchema(path) == null) {
      throw new PatchValidationException(SCIMException.Type.INVALID_PATH, String.format("Attribute with name '%s' does not exist", path));
    }
  }

  private boolean isValidFullAttributeNotation(final String fullAttrNotation) {
    String schemaId = schemaAPI.getSchemaIdFromAttributeNotation(fullAttrNotation);
    if (isNullOrEmpty(schemaId)) {
      return false;
    }

    return !schemaAPI.getComplexAttributePath(fullAttrNotation).isEmpty();
  }
}
