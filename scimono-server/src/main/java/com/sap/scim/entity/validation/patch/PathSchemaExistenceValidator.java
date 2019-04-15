
package com.sap.scim.entity.validation.patch;

import java.util.Map;

import com.sap.scim.entity.patch.PatchOperation;
import com.sap.scim.entity.schema.Schema;
import com.sap.scim.entity.validation.Validator;
import com.sap.scim.exception.SCIMException;
import com.sap.scim.helper.Strings;

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
