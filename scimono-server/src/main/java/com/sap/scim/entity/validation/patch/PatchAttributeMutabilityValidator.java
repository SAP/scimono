
package com.sap.scim.entity.validation.patch;

import com.sap.scim.entity.patch.PatchOperation.Type;
import com.sap.scim.entity.schema.Attribute;
import com.sap.scim.entity.validation.Validator;
import com.sap.scim.exception.SCIMException;

public class PatchAttributeMutabilityValidator implements Validator<Attribute> {

  private Type operationType;

  public PatchAttributeMutabilityValidator(Type operationType) {
    this.operationType = operationType;
  }

  @Override
  public void validate(Attribute attribute) {
    validateForReadOnly(attribute);
    validateForImmutable(attribute);
  }

  private void validateForReadOnly(Attribute attribute) {
    if (attribute.getMutability().equals("readOnly")) {
      throw new PatchValidationException(SCIMException.Type.MUTABILITY,
          String.format("Attribute with name '%s' is readOnly and cannot be modified", attribute.getName()));
    }
  }

  private void validateForImmutable(Attribute attribute) {
    if (operationType.equals(Type.REPLACE) && (attribute.getMutability().equals("immutable"))) {
      throw new PatchValidationException(SCIMException.Type.UNIQUENESS,
          String.format("Attribute with name '%s' cannot be modified because it is immutable", attribute.getName()));
    }
  }

}
