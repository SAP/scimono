
package com.sap.scim.entity.validation.patch;

import com.sap.scim.entity.patch.PatchBody;
import com.sap.scim.entity.validation.Validator;
import com.sap.scim.exception.SCIMException;

public class PatchSchemaPresenceValidator implements Validator<PatchBody> {

  @Override
  public void validate(PatchBody body) {
    if (!body.getSchemas().contains(PatchBody.PATCH_SCHEMA)) {
      throw new PatchValidationException(SCIMException.Type.INVALID_SYNTAX,
          String.format("Schema with name: %s is required!", PatchBody.PATCH_SCHEMA));
    }
  }
}
