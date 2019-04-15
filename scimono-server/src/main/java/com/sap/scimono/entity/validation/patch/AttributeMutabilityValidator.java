
package com.sap.scimono.entity.validation.patch;

import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.entity.validation.Validator;
import com.sap.scimono.exception.SCIMException;

public class AttributeMutabilityValidator implements Validator<Attribute> {

  @Override
  public void validate(Attribute attribute) {
    String attrMutability = attribute.getMutability();

    if (!attrMutability.equals("readWrite") && !attrMutability.equals("writeOnly")) {
      throw new PatchValidationException(SCIMException.Type.MUTABILITY,
          String.format("Attribute with name '%s' cannot be modified", attribute.getName()));
    }
  }

}
