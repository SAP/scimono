package com.sap.scimono.entity.validation;

import java.util.Map;

import jakarta.ws.rs.core.Response;

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
    validate(attributeDefinition, jsonNodeValue);
  }

  private void validate(final Attribute attribute, final JsonNode value) {
    // remove after issue https://github.com/SAP/scimono/issues/77 is fixed
    if (EnterpriseExtension.ENTERPRISE_URN.equalsIgnoreCase(attribute.getName())) {
      return;
    }
    // end of the workaround connected with https://github.com/SAP/scimono/issues/77

    new AttributeDataTypeValidator(value).validate(attribute);
    new CanonicalValuesValidator(value).validate(attribute);

    if (AttributeDataType.COMPLEX.toString().equals(attribute.getType())) {
      if (attribute.isMultiValued()) {
        for (JsonNode valueElement : value) {
          validateSubAttributes(attribute, valueElement);
        }
      } else {
        validateSubAttributes(attribute, value);
      }
    }
  }

  private void validateSubAttributes(final Attribute attribute, final JsonNode value) {
    if (!SchemasCallback.isCoreSchema(attribute.getName())) {
      new RequiredSubAttributesValidator(value).validate(attribute);
    }

    value.fields().forEachRemaining(field -> {
      String attrName = field.getKey();
      JsonNode attrValue = field.getValue();

      Attribute subAttribute = attribute.getSubAttributes().stream().filter(subAttr -> attrName.equalsIgnoreCase(subAttr.getName())).findFirst()
          .orElseGet(() -> {
            Schema schema = permittedSchemas.get(attrName);
            return schema == null ? null : schema.toAttribute();
          });

      if (subAttribute == null) {
        String message = String.format("Provided attribute with name '%s' does not exist according to the schema", attrName);
        throw new SCIMException(SCIMException.Type.INVALID_SYNTAX, message, Response.Status.BAD_REQUEST);
      }

      validate(subAttribute, attrValue);
    });
  }

}
