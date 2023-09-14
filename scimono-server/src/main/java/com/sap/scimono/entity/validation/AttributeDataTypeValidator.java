
package com.sap.scimono.entity.validation;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import jakarta.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.entity.schema.AttributeDataType;
import com.sap.scimono.exception.SCIMException;

public class AttributeDataTypeValidator implements Validator<Attribute> {

  private static final String BASE_64_PERMITTED_CHARACTERS_PATTERN = "^[A-Za-z0-9+\\/=]+$";
  private static final String ERROR_MESSAGE = "The value data type is not correct";
  private static final SCIMException.Type ERROR_TYPE = SCIMException.Type.INVALID_VALUE;

  private final JsonNode value;

  public AttributeDataTypeValidator(final JsonNode value) {
    this.value = value;
  }

  @Override
  public void validate(final Attribute attribute) {
    if (attribute.isMultiValued()) {
      validateMultivaluedValueDataType(attribute, value);
      return;
    }
    validateSingleValueDataType(attribute, value);
  }

  private void validateSingleValueDataType(final Attribute attribute, final JsonNode value) {
    if (!isValueDataTypeCorrect(attribute.getType(), value)) {
      throw new SCIMException(ERROR_TYPE, ERROR_MESSAGE, Response.Status.BAD_REQUEST);
    }
  }

  private void validateMultivaluedValueDataType(final Attribute attribute, final JsonNode value) {
    if (!value.isArray()) {
      throw new SCIMException(ERROR_TYPE, ERROR_MESSAGE, Response.Status.BAD_REQUEST);
    }

    for (JsonNode valueElement : value) {
      if (!isValueDataTypeCorrect(attribute.getType(), valueElement)) {
        throw new SCIMException(ERROR_TYPE, ERROR_MESSAGE, Response.Status.BAD_REQUEST);
      }
    }
  }

  private boolean isValueDataTypeCorrect(final String type, final JsonNode value) {
    switch (AttributeDataType.of(type)) {
      case STRING:
        return value.isTextual();
      case INTEGER:
        return value.isIntegralNumber();
      case BOOLEAN:
        return value.isBoolean();
      case COMPLEX:
        return value.isObject();
      case DECIMAL:
        return value.isBigDecimal() || value.isDouble() || value.isIntegralNumber();
      case REFERENCE:
        return value.isTextual() && isValidURI(value.asText());
      case BINARY:
        return value.isTextual() && value.asText().matches(BASE_64_PERMITTED_CHARACTERS_PATTERN);
      case DATE_TIME:
        if (value.isTextual()) {
          try {
            DateTimeFormatter.ISO_INSTANT.parse(value.asText());
            return true;
          } catch (DateTimeParseException e) {
            return false;
          }
        }
        return false;
      default:
        return false;
    }
  }

  private boolean isValidURI(String str) {
    try {
      new URI(str);
      return true;
    } catch (final URISyntaxException e) {
      return false;
    }
  }
}
