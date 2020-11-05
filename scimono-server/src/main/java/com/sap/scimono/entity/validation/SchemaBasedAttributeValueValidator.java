
package com.sap.scimono.entity.validation;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.entity.EnterpriseExtension;
import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.entity.schema.AttributeDataType;
import com.sap.scimono.entity.schema.Schema;
import com.sap.scimono.exception.SCIMException;

public class SchemaBasedAttributeValueValidator implements Validator<Object> {

  private final Attribute attributeDefinition;
  private final Map<String, Schema> permittedSchemas;

  public SchemaBasedAttributeValueValidator(final Attribute attributeDefinition, final Map<String, Schema> permittedSchemas) {
    this.attributeDefinition = attributeDefinition;
    this.permittedSchemas = permittedSchemas;
  }

  @Override
  public void validate(final Object value) {
    JsonNode jsonNodeValue = (value instanceof JsonNode) ? (JsonNode) value : new ObjectMapper().valueToTree(value);
    validateValueAttributes(attributeDefinition, jsonNodeValue);
  }

  private void validateValueAttributes(final Attribute attribute, final JsonNode value) {
    // remove after issue https://github.com/SAP/scimono/issues/77 is fixed
    if (EnterpriseExtension.ENTERPRISE_URN.equalsIgnoreCase(attribute.getName())) {
      return;
    }
    // end of the workaround connected with https://github.com/SAP/scimono/issues/77

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
    } else if (attribute.isMultiValued() && !value.isArray()) {
      throw new SCIMException(SCIMException.Type.INVALID_SYNTAX, "Value that should be multivalued is not.", Response.Status.BAD_REQUEST);
    } else {
      validateAttribute(attribute, value);
      if (AttributeDataType.COMPLEX.toString().equals(attribute.getType())) {
        validateComplexAttribute(value, attribute.getSubAttributes());
      } else {
        validateSimpleAttribute(value);
      }
    }
  }

  private void validateSimpleAttribute(final JsonNode value) {
    if (value.isContainerNode()) {
      throw new SCIMException(SCIMException.Type.INVALID_SYNTAX, "Simple attribute cannot hava complex or multivalued value",
          Response.Status.BAD_REQUEST);
    }
  }

  private void validateComplexAttribute(final JsonNode value, final List<Attribute> permittedAttributes) {
    if (!value.isObject()) {
      throw new SCIMException(SCIMException.Type.INVALID_SYNTAX, "Value is not object.", Response.Status.BAD_REQUEST);
    }

    Iterator<Map.Entry<String, JsonNode>> fieldsIterator = value.fields();
    while (fieldsIterator.hasNext()) {
      Map.Entry<String, JsonNode> field = fieldsIterator.next();
      String attrName = field.getKey();
      JsonNode attrValue = field.getValue();

      // @formatter:off
      Attribute attributeDefinition;
      Optional<String> schemaName = permittedSchemas.keySet().stream().filter(attrName::equalsIgnoreCase).findAny();
      if (schemaName.isPresent()) {
        attributeDefinition = new Attribute.Builder()
            .multiValued(false)
            .required(false)
            .type(AttributeDataType.COMPLEX.toString())
            .addSubAttributes(permittedSchemas.get(schemaName.get()).getAttributes())
            .build();
      } else {
        attributeDefinition = permittedAttributes.stream()
          .filter(attr -> attrName.equalsIgnoreCase(attr.getName()))
          .findAny()
          .orElseThrow(() -> new SCIMException(SCIMException.Type.INVALID_SYNTAX,
              String.format("Provided attribute with name '%s' does not exist according to the schema", attrName),
              Response.Status.BAD_REQUEST));
      }
      // @formatter:on

      validateValueAttributes(attributeDefinition, attrValue);
    }
  }

  private void validateAttribute(final Attribute attribute, final JsonNode value) {
    List<Validator<Attribute>> validators = new LinkedList<>();
    validators.add(new CanonicalValuesValidator(value));
    validators.add(new AttributeDataTypeValidator(value));
    validators.add(new AttributeReadOnlyValidator());
    if (!SchemasCallback.isCoreSchema(attribute.getName())) {
      validators.add(new RequiredSubAttributesValidator(value));
    }
    validators.forEach(v -> v.validate(attribute));
  }

}
