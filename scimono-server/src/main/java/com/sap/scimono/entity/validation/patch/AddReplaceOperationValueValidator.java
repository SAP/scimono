
package com.sap.scimono.entity.validation.patch;

import com.sap.scimono.entity.patch.PatchOperation;
import com.sap.scimono.entity.patch.PatchOperation.Type;
import com.sap.scimono.entity.validation.Validator;
import com.sap.scimono.exception.SCIMException;

public class AddReplaceOperationValueValidator implements Validator<PatchOperation> {

  @Override
  public void validate(PatchOperation operation) {
    Type type = operation.getOp();
    if ((type.equals(Type.ADD) || type.equals(Type.REPLACE)) && operation.getValue().isNull()) {
      throw new PatchValidationException(SCIMException.Type.INVALID_SYNTAX,
          String.format("Attribute 'value' is required for operation types: %s, %s", Type.ADD, Type.REPLACE));
    }
  }

}
