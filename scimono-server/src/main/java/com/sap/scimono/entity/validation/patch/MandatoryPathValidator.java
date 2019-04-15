
package com.sap.scimono.entity.validation.patch;

import com.sap.scimono.entity.patch.PatchOperation;
import com.sap.scimono.entity.patch.PatchOperation.Type;
import com.sap.scimono.entity.validation.Validator;
import com.sap.scimono.exception.SCIMException;
import com.sap.scimono.helper.Strings;

public class MandatoryPathValidator implements Validator<PatchOperation> {

  @Override
  public void validate(PatchOperation operation) {
    if (operation.getOp().equals(Type.REMOVE) && Strings.isNullOrEmpty(operation.getPath())) {
      throw new PatchValidationException(SCIMException.Type.NO_TARGET,
          String.format("Attribute 'path' is required for operation type %s", Type.REMOVE));
    }
  }

}
