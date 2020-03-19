
package com.sap.scimono.entity.validation;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.entity.schema.AttributeDataType;
import com.sap.scimono.entity.validation.patch.PatchValidationException;
import com.sap.scimono.exception.SCIMException;

public class AttributeDataTypeValidator implements Validator<Attribute> {

  private static final String BASE_64_PERMITTED_CHARACTERS_PATTERN = "^[A-Za-z0-9+\\/=]+$";
  private static final String URI_PATTERN = "((?<=\\()[A-Za-z][A-Za-z0-9\\+\\.\\-]*:([A-Za-z0-9\\.\\-_~:\\/\\?#\\[\\]@!\\$&'\\(\\)\\*\\+,;=]|%[A-Fa-f0-9]{2})+(?=\\)))|([A-Za-z][A-Za-z0-9\\+\\.\\-]*:([A-Za-z0-9\\.\\-_~:\\/\\?#\\[\\]@!\\$&'\\(\\)\\*\\+,;=]|%[A-Fa-f0-9]{2})+)";
  private static final String ERROR_MESSAGE = "The value data type is not correct";
  private static final SCIMException.Type ERROR_TYPE = SCIMException.Type.INVALID_VALUE;

  private JsonNode value;

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
      throw new PatchValidationException(ERROR_TYPE, ERROR_MESSAGE);
    }
  }

  private void validateMultivaluedValueDataType(final Attribute attribute, final JsonNode value) {
    if (!value.isArray()) {
      throw new PatchValidationException(ERROR_TYPE, ERROR_MESSAGE);
    }

    for (JsonNode valueElement : value) {
      if (!isValueDataTypeCorrect(attribute.getType(), valueElement)) {
        throw new PatchValidationException(ERROR_TYPE, ERROR_MESSAGE);
      }
    }
  }

  private boolean isValueDataTypeCorrect(final String type, final JsonNode value) {
    if (value.isTextual() && value.toString().isEmpty()) {
      return true;
    }

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
        return value.isDouble();
      case REFERENCE:
        return value.isTextual() && value.asText().matches(URI_PATTERN);
      case BINARY:
        return value.isTextual() && value.asText().matches(BASE_64_PERMITTED_CHARACTERS_PATTERN);
      case DATE_TIME:
        if (value.isTextual()) {
          try {
            DateTimeFormatter.ISO_INSTANT.parse(value.asText());
          } catch (DateTimeParseException e) {
            return false;
          }
        }
        return true;
      default:
        return false;
    }
  }
}
