
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
import com.sap.scimono.entity.validation.patch.PatchValidationException;
import com.sap.scimono.exception.SCIMException;

public class AttributeAndValueValidator implements Validator<Object> {

  private final Attribute targetAttribute;
  private final Map<String, Schema> permittedSchemas;

  public AttributeAndValueValidator(final Attribute targetAttribute, final Map<String, Schema> permittedSchemas) {
    this.targetAttribute = targetAttribute;
    this.permittedSchemas = permittedSchemas;
  }

  public AttributeAndValueValidator(final Schema schema, final Map<String, Schema> permittedSchemas) {
    this(schema.toAttribute(), permittedSchemas);
  }

  @Override
  public void validate(final Object value) {
    JsonNode jsonNodeValue;
    if (value instanceof JsonNode) {
      jsonNodeValue = (JsonNode) value;
    } else {
      jsonNodeValue = new ObjectMapper().valueToTree(value);
    }
    validateValueAttributes(targetAttribute, jsonNodeValue);
  }

  private void validateValueAttributes(final Attribute attribute, final JsonNode value) {
    if (attribute.isMultiValued() && value.isArray()) {
      // @formatter:off
      Attribute singleValuedAttribute = new Attribute.Builder()
          .name(attribute.getName())
          .multiValued(false)
          .required(false)
          .type(attribute.getType())
          .mutability(attribute.getMutability())
          .addSubAttributes(attribute.getSubAttributes())
          .build();
      // @formatter:on

      for (JsonNode valueElement : value) {
        validateValueAttributes(singleValuedAttribute, valueElement);
      }
    } else {
      validateAttribute(attribute, value);
      if (AttributeDataType.COMPLEX.toString().equals(attribute.getType())) {
        validateComplexAttribute(value, attribute.getSubAttributes());
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

  private void validateComplexAttribute(final JsonNode value, final List<Attribute> permittedAttributes) {
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
            .required(false)
            .type(AttributeDataType.COMPLEX.toString())
            .addSubAttributes(permittedSchemas.get(schemaName.get()).getAttributes())
            .build();
      } else {
        targetAttribute = permittedAttributes.stream()
          .filter(attr -> attrName.equalsIgnoreCase(attr.getName()))
          .findAny()
          .orElseThrow(() -> new PatchValidationException(SCIMException.Type.INVALID_PATH, String.format("Value attribute with name %s does not exist", attrName)));
      }
      // @formatter:on

      validateValueAttributes(targetAttribute, attrValue);
    }
  }

  private void validateAttribute(final Attribute аttribute, final JsonNode value) {
    List<Validator<Attribute>> validators = new LinkedList<>();
    validators.add(new CanonicalValuesValidator(value));
    validators.add(new AttributeDataTypeValidator(value));
    validators.add(new RequiredAttributeValidator(value));
    validators.forEach(v -> v.validate(аttribute));
  }

}
