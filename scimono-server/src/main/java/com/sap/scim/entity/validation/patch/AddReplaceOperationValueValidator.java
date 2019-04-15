
package com.sap.scim.entity.validation.patch;

import com.sap.scim.entity.patch.PatchOperation;
import com.sap.scim.entity.patch.PatchOperation.Type;
import com.sap.scim.entity.validation.Validator;
import com.sap.scim.exception.SCIMException;

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
