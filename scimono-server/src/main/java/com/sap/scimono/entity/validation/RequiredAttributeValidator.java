
package com.sap.scimono.entity.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.entity.validation.patch.PatchValidationException;
import com.sap.scimono.exception.SCIMException;

public class RequiredAttributeValidator implements Validator<Attribute> {

  private JsonNode value;

  public RequiredAttributeValidator(final JsonNode value) {
    this.value = value;
  }

  @Override
  public void validate(final Attribute attribute) {
    // @formatter:off
    attribute.getSubAttributes().stream()
    .filter(Attribute::isRequired)
    .filter(requiredSubAttribute -> value.get(requiredSubAttribute.getName()) == null)
    .findAny()
    .ifPresent(requiredSubAttribute -> {
      throw new PatchValidationException(SCIMException.Type.INVALID_VALUE,
          String.format("Attribute with name %s is required.", requiredSubAttribute.getName()));
    });
    // @formatter:on
  }

}
