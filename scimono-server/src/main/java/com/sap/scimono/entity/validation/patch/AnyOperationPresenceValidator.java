
package com.sap.scimono.entity.validation.patch;

import java.util.List;

import com.sap.scimono.entity.patch.PatchBody;
import com.sap.scimono.entity.patch.PatchOperation;
import com.sap.scimono.entity.validation.Validator;
import com.sap.scimono.exception.SCIMException;



public class AnyOperationPresenceValidator implements Validator<PatchBody> {

  @Override
  public void validate(PatchBody body) {
    List<PatchOperation> operations = body.getOperations();
    if (operations == null || operations.isEmpty()) {
      throw new PatchValidationException(SCIMException.Type.INVALID_SYNTAX, "At least one operation is required");
    }
  }
}
