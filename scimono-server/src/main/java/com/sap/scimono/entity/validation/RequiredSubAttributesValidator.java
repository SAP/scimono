
package com.sap.scimono.entity.validation;

import jakarta.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.exception.SCIMException;

public class RequiredSubAttributesValidator implements Validator<Attribute> {

  private final JsonNode value;

  public RequiredSubAttributesValidator(final JsonNode value) {
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
      throw new SCIMException(SCIMException.Type.INVALID_VALUE,
          String.format("Attribute with name %s is required.", requiredSubAttribute.getName()), Response.Status.BAD_REQUEST);
    });
    // @formatter:on
  }

}
