
package com.sap.scimono.entity.validation.patch;

import java.util.Map;

import com.sap.scimono.entity.patch.PatchOperation;
import com.sap.scimono.entity.schema.Schema;
import com.sap.scimono.entity.validation.Validator;
import com.sap.scimono.exception.SCIMException;
import com.sap.scimono.helper.Strings;

public class PathSchemaExistenceValidator implements Validator<PatchOperation> {

  private Map<String, Schema> schemas;

  public PathSchemaExistenceValidator(Map<String, Schema> schemas) {
    this.schemas = schemas;
  }

  @Override
  public void validate(PatchOperation operation) {
    String path = operation.getPath();
    if (Strings.isNullOrEmpty(path)) {
      return;
    }

    if (schemas.keySet().stream().noneMatch(path::startsWith)) {
      throw new PatchValidationException(SCIMException.Type.INVALID_VALUE, "Resource schema does not exist or is not permitted");
    }
  }
}
