
package com.sap.scimono.entity.validation.patch;

import com.sap.scimono.entity.patch.PatchOperation;
import com.sap.scimono.entity.patch.PatchOperation.Type;
import com.sap.scimono.entity.validation.Validator;
import com.sap.scimono.exception.SCIMException;

public class OperationTypeValidator implements Validator<PatchOperation> {

  @Override
  public void validate(PatchOperation operation) {
    Type type = operation.getOp();
    if (!type.equals(Type.ADD) && !type.equals(Type.REPLACE) && !type.equals(Type.REMOVE)) {
      throw new PatchValidationException(SCIMException.Type.INVALID_VALUE,
          "The operation type is invalid. It must be one of the following: add, replace, remove");
    }
  }

}
