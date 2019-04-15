
package com.sap.scim.entity.schema.validation;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.sap.scim.entity.schema.Attribute;
import com.sap.scim.entity.schema.Schema;

import java.util.regex.Pattern;

class SchemaValidator implements ConstraintValidator<ValidSchema, Schema> {
  private static final Pattern SCHEMA_NAME_ALLOWED_PATTERN = Pattern.compile("(^[a-zA-Z])(\\w)+");

  @Override
  public boolean isValid(Schema schema, ConstraintValidatorContext context) {
    if (schema == null) {
      ValidationUtil.interpolateErrorMessage(context, "One of the request inputs is not valid!");

      return false;
    }

    SchemaIdValidator schemaIdValidator = new SchemaIdValidator();
    //@formatter:off
      return schemaIdValidator.isValid(schema.getId(), context)
          && areSchemaAttributesValid(schema.getAttributes(), context)
          && areSchemaIdWithoutPrefixSameAsSchemaName(schema, context);
      //@formatter:on
  }

  private boolean isValidIdentifierName(final String identifierName, ConstraintValidatorContext context) {
    return isAlphanumeric(identifierName) && isIdenifierLenghtValid(identifierName, context);

  }

  private boolean isIdenifierLenghtValid(String identifier, ConstraintValidatorContext context) {
    if (identifier == null || identifier.isEmpty() || identifier.length() >= 20) {
      ValidationUtil.interpolateErrorMessage(context, generateViolationMessage(identifier));

      return false;
    }

    return true;
  }

  private boolean areSchemaAttributeNamesValid(List<Attribute> schemaAttributes, ConstraintValidatorContext context) {
    if (schemaAttributes == null) {

      return true;
    }

    for (Attribute nextAttribute : schemaAttributes) {
      if (!isValidIdentifierName(nextAttribute.getName(), context)) {

        return false;
      }
    }

    return true;
  }

  private boolean areSchemaAttributesValid(List<Attribute> schemaAttributes, ConstraintValidatorContext context) {
    return schemaAttributes.size() < 20 && areSchemaAttributeNamesValid(schemaAttributes, context);

  }

  private boolean areSchemaIdWithoutPrefixSameAsSchemaName(Schema schema, ConstraintValidatorContext context) {
    if (schema.getName().equals(schema.getId().substring(Schema.EXTENSION_SCHEMA_URN.length()))) {
      ValidationUtil.interpolateErrorMessage(context, "Schema name and id does not match!");

      return true;
    }

    return false;
  }

  private String generateViolationMessage(String attributeName) {
    return String.format("The attribute value \"%s\" has invalid value!", attributeName);
  }

  private boolean isAlphanumeric(final String schemaName) {
    return SCHEMA_NAME_ALLOWED_PATTERN.matcher(schemaName).matches();
  }
}
