
package com.sap.scimono.entity.validation.patch;

import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.entity.validation.Validator;
import com.sap.scimono.exception.SCIMException;

public class PatchAttributeMutabilityValidator implements Validator<Attribute> {

  private boolean isOperationReplacing;

  public PatchAttributeMutabilityValidator(final boolean isOperationReplacing) {
    this.isOperationReplacing = isOperationReplacing;
  }

  @Override
  public void validate(final Attribute attribute) {
    validateForReadOnly(attribute);
    validateForImmutable(attribute);
  }

  private void validateForReadOnly(final Attribute attribute) {
    if (attribute.getMutability().equals("readOnly")) {
      throw new PatchValidationException(SCIMException.Type.MUTABILITY,
          String.format("Attribute with name '%s' is readOnly and cannot be modified", attribute.getName()));
    }
  }

  private void validateForImmutable(final Attribute attribute) {
    if (isOperationReplacing && (attribute.getMutability().equals("immutable"))) {
      throw new PatchValidationException(SCIMException.Type.MUTABILITY,
          String.format("Attribute with name '%s' cannot be modified because it is immutable", attribute.getName()));
    }
  }

}
