
package com.sap.scimono.entity.validation.patch;

import com.sap.scimono.entity.patch.PatchBody;
import com.sap.scimono.entity.validation.Validator;
import com.sap.scimono.exception.SCIMException;

public class PatchSchemaPresenceValidator implements Validator<PatchBody> {

  @Override
  public void validate(PatchBody body) {
    if (!body.getSchemas().contains(PatchBody.PATCH_SCHEMA)) {
      throw new PatchValidationException(SCIMException.Type.INVALID_SYNTAX,
          String.format("Schema with name: %s is required!", PatchBody.PATCH_SCHEMA));
    }
  }
}
