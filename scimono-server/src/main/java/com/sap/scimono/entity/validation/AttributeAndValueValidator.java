
package com.sap.scimono.entity.validation;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.entity.schema.AttributeDataType;
import com.sap.scimono.entity.schema.Schema;
import com.sap.scimono.entity.validation.patch.AttributeDataTypeValidator;
import com.sap.scimono.entity.validation.patch.CanonicalValuesValidator;
import com.sap.scimono.entity.validation.patch.PatchAttributeMutabilityValidator;
import com.sap.scimono.entity.validation.patch.PatchValidationException;
import com.sap.scimono.exception.SCIMException;

public class AttributeAndValueValidator implements Validator<Object> {

  private final Attribute targetAttribute;
  private final Map<String, Schema> permittedSchemas;
  private final boolean isOperationReplacing;

  public AttributeAndValueValidator(final Attribute targetAttribute, final Map<String, Schema> permittedSchemas, final boolean isOperationReplacing) {
    this.targetAttribute = targetAttribute;
    this.permittedSchemas = permittedSchemas;
    this.isOperationReplacing = isOperationReplacing;
  }

  public AttributeAndValueValidator(final Schema schema, final Map<String, Schema> permittedSchemas, final boolean isOperationReplacing) {
    this(schema.toAttribute(), permittedSchemas, isOperationReplacing);
  }

  @Override
  public void validate(final Object value) {
    JsonNode jsonNodeValue;
    if (value instanceof JsonNode) {
      jsonNodeValue = (JsonNode) value;
    } else {
      jsonNodeValue = new ObjectMapper().valueToTree(value);
    }
    validateValueAttributes(targetAttribute, jsonNodeValue, false);
  }

  private void validateValueAttributes(final Attribute attribute, final JsonNode value, final boolean isArrayFound) {
    if (attribute.isMultiValued() && value.isArray()) {
      for (JsonNode valueElement : value) {
        validateValueAttributes(attribute, valueElement, true);
      }
    } else {
      validateAttribute(attribute, value, isArrayFound);
      if (AttributeDataType.COMPLEX.toString().equals(attribute.getType())) {
        validateComplexAttribute(value, attribute.getSubAttributes(), isArrayFound);
      } else {
        validateSimpleAttribute(attribute, value);
      }
    }
  }

  private void validateSimpleAttribute(final Attribute attr, final JsonNode value) {
    if (value.isContainerNode()) {
      throw new PatchValidationException(SCIMException.Type.INVALID_SYNTAX, "Simple attribute cannot hava complex or multivalued value");
    }
  }

  private void validateComplexAttribute(final JsonNode value, final List<Attribute> permittedAttributes, final boolean isArrayFound) {
    if (!value.isObject()) {
      throw new PatchValidationException(SCIMException.Type.INVALID_SYNTAX, "value is not object");
    }

    Iterator<Map.Entry<String, JsonNode>> fieldsIterator = value.fields();
    while (fieldsIterator.hasNext()) {
      Map.Entry<String, JsonNode> field = fieldsIterator.next();
      String attrName = field.getKey();
      JsonNode attrValue = field.getValue();

      // @formatter:off
      Attribute targetAttribute = null;
      Optional<String> schemaName = permittedSchemas.keySet().stream().filter(attrName::equalsIgnoreCase).findAny();
      if (schemaName.isPresent()) {
        targetAttribute = new Attribute.Builder()
            .multiValued(false)
            .type(AttributeDataType.COMPLEX.toString())
            .addSubAttributes(permittedSchemas.get(schemaName.get()).getAttributes())
            .build();
      } else {
        targetAttribute = permittedAttributes.stream()
          .filter(attr -> attrName.equalsIgnoreCase(attr.getName()))
          .findAny()
          .orElseThrow(() -> new PatchValidationException(SCIMException.Type.INVALID_PATH, String.format("Value attribute with name %s does not exist", attrName)));
      }

      validateValueAttributes(targetAttribute, attrValue, isArrayFound);
   // @formatter:on
    }
  }

  private void validateAttribute(final Attribute аttribute, final JsonNode value, final boolean isArrayFound) {
    List<Validator<Attribute>> validators = new LinkedList<>();
    validators.add(new CanonicalValuesValidator(value));
    validators.add(new AttributeDataTypeValidator(value));
    if (!isArrayFound) {
      validators.add(new PatchAttributeMutabilityValidator(isOperationReplacing));
    }
    validators.forEach(v -> v.validate(аttribute));
  }

}
