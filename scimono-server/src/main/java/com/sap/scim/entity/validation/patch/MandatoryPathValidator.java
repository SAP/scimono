
package com.sap.scim.entity.validation.patch;

import com.sap.scim.entity.patch.PatchOperation;
import com.sap.scim.entity.patch.PatchOperation.Type;
import com.sap.scim.entity.validation.Validator;
import com.sap.scim.exception.SCIMException;
import com.sap.scim.helper.Strings;

public class MandatoryPathValidator implements Validator<PatchOperation> {

  @Override
  public void validate(PatchOperation operation) {
    if (operation.getOp().equals(Type.REMOVE) && Strings.isNullOrEmpty(operation.getPath())) {
      throw new PatchValidationException(SCIMException.Type.NO_TARGET,
          String.format("Attribute 'path' is required for operation type %s", Type.REMOVE));
    }
  }

}
