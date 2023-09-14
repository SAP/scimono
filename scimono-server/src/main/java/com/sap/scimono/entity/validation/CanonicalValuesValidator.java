
package com.sap.scimono.entity.validation;

import jakarta.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.exception.SCIMException;
import com.sap.scimono.helper.Strings;

public class CanonicalValuesValidator implements Validator<Attribute> {

  private JsonNode value;

  public CanonicalValuesValidator(final JsonNode value) {
    this.value = value;
  }

  @Override
  public void validate(final Attribute attribute) {
    if (attribute.getCanonicalValues().isEmpty()) {
      return;
    }
    if (!isComplexValue(value) && !attribute.getCanonicalValues().contains(value.asText())) {
      throw new SCIMException(SCIMException.Type.INVALID_VALUE,
          String.format("Provided value is not part of '%s' canonical values", attribute.getName()), Response.Status.BAD_REQUEST);
    }
  }

  private static boolean isComplexValue(final JsonNode value) {
    return Strings.isNullOrEmpty(value.asText());
  }

}
