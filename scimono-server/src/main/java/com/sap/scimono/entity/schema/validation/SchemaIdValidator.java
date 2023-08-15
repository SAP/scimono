
package com.sap.scimono.entity.schema.validation;

import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.entity.validation.ValidationUtil;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class SchemaIdValidator implements ConstraintValidator<ValidSchemaId, String> {
  private static final Pattern SCHEMA_NAME_ALLOWED_PATTERN = Pattern.compile("(^[a-zA-Z])(\\w)+");

  @Override
  public boolean isValid(String schemaId, ConstraintValidatorContext context) {
    return isValidSchemaId(schemaId, context) && isValidIdentifierName(schemaId, context);
  }

  private boolean isValidSchemaId(final String schemaId, ConstraintValidatorContext context) {
    if (SchemasCallback.isCustomSchema(schemaId)) {

      return true;
    }
    ValidationUtil.interpolateErrorMessage(context, generateViolationMessage(schemaId));

    return false;
  }

  private boolean isValidIdentifierName(final String schemaUrn, ConstraintValidatorContext context) {
    final int indexOfSchemaDelimiter = schemaUrn.lastIndexOf(SchemasCallback.SCHEMA_URN_DELIMETER);
    if (indexOfSchemaDelimiter == -1) {
      return false;
    }
    final String identifierName = schemaUrn.substring(indexOfSchemaDelimiter + 1);
    return isAlphanumeric(identifierName) && isIdenifierLenghtValid(identifierName, context);

  }

  private boolean isIdenifierLenghtValid(String identifier, ConstraintValidatorContext context) {
    if (identifier == null || identifier.isEmpty() || identifier.length() > 21) {
      ValidationUtil.interpolateErrorMessage(context, generateViolationMessage(identifier));

      return false;
    }

    return true;
  }

  private String generateViolationMessage(String attributeName) {
    return String.format("The attribute value \"%s\" has invalid value!", attributeName);
  }

  private boolean isAlphanumeric(final String schemaName) {
    return SCHEMA_NAME_ALLOWED_PATTERN.matcher(schemaName).matches();
  }
}
