
package com.sap.scimono.entity.validation.patch;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.entity.patch.PatchOperation;
import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.entity.schema.Schema;
import com.sap.scimono.entity.validation.Validator;
import com.sap.scimono.exception.SCIMException;
import com.sap.scimono.helper.Strings;

public class AttributeAndValueValidator implements Validator<PatchOperation> {

  private final SchemasCallback schemaAPI;
  private final String coreSchemaId;
  private final Map<String, Schema> permittedSchemas;

  public AttributeAndValueValidator(SchemasCallback schemaAPI, String coreSchemaId, Map<String, Schema> permittedSchemas) {
    this.schemaAPI = schemaAPI;
    this.coreSchemaId = coreSchemaId;
    this.permittedSchemas = permittedSchemas;
  }

  @Override
  public void validate(PatchOperation operation) {
    String path = operation.getPath();
    JsonNode value = operation.getValue();
    PatchOperation.Type opType = operation.getOp();

    Attribute targetAttribute;
    if (Strings.isNullOrEmpty(path)) {
      targetAttribute = schemaToAttribute(coreSchemaId);
      validateSchemaAttributes(targetAttribute, operation);
    } else if (schemaAPI.getSchema(path) != null) {
      targetAttribute = schemaToAttribute(path);
      validateSchemaAttributes(targetAttribute, operation);
    } else {
      String pathWithoutFilter = schemaAPI.removeValueFilterFromAttributeNotation(path);
      targetAttribute = schemaAPI.getAttribute(pathWithoutFilter);
      validatePathAttribute(targetAttribute, operation);
    }

    validateValueAttributes(targetAttribute, value, opType);
  }

  private Attribute schemaToAttribute(String schemaId) {
    // @formatter:off
    return new Attribute.Builder()
        .name(schemaId)
        .multiValued(false)
        .type("complex")
        .mutability("readWrite")
        .addSubAttributes(schemaAPI.getSchema(schemaId).getAttributes())
        .build();
    // @formatter:on
  }

  private void validateValueAttributes(Attribute attribute, JsonNode value, PatchOperation.Type opType) {
    if (attribute.isMultiValued() && value.isArray()) {
      for (JsonNode valueElement : value) {
        validateValueAttributes(attribute, valueElement, opType);
      }
    } else if (attribute.getType().equals("complex")) {
      validateComplexAttribute(value, attribute.getSubAttributes(), opType);
    } else {
      validateSimpleAttribute(attribute, value);
    }
  }

  private void validateSimpleAttribute(Attribute attr, JsonNode value) {
    if (value.isContainerNode()) {
      throw new PatchValidationException(SCIMException.Type.INVALID_SYNTAX, "Simple attribute cannot hava complex or multivalued value");
    }

    List<Validator<Attribute>> validators = new LinkedList<>();
    validators.add(new CanonicalValuesValidator(value));
    validators.add(new AttributeDataTypeValidator(value));
    validators.forEach(v -> v.validate(attr));
  }

  private void validateComplexAttribute(JsonNode value, List<Attribute> permittedAttributes, PatchOperation.Type opType) {
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
      if (permittedSchemas.keySet().stream().anyMatch(attrName::equalsIgnoreCase)) {
        targetAttribute = new Attribute.Builder()
            .multiValued(false)
            .type("complex")
            .addSubAttributes(permittedSchemas.get(attrName).getAttributes())
            .build();
      } else {

        targetAttribute = permittedAttributes.stream()
          .filter(attr -> attrName.equalsIgnoreCase(attr.getName()))
          .findAny()
          .orElseThrow(() -> new PatchValidationException(SCIMException.Type.INVALID_PATH, String.format("Value attribute with name %s does not exist", attrName)));
      }

      validateValueAttributes(targetAttribute, attrValue, opType);
   // @formatter:on
    }
  }

  private void validatePathAttribute(Attribute attribute, PatchOperation operation) {
    JsonNode value = operation.getValue();
    Validator<Attribute> dataTypeValidator = new AttributeDataTypeValidator(value);
    dataTypeValidator.validate(attribute);

    Validator<Attribute> mutabilityValidator = new PatchAttributeMutabilityValidator(operation.getOp());
    if (!value.isObject()) {
      mutabilityValidator.validate(attribute);
    }
  }

  private void validateSchemaAttributes(Attribute schemaAttribute, PatchOperation operation) {
    JsonNode value = operation.getValue();
    Validator<Attribute> dataTypeValidator = new AttributeDataTypeValidator(value);
    dataTypeValidator.validate(schemaAttribute);

    Validator<Attribute> mutabilityValidator = new PatchAttributeMutabilityValidator(operation.getOp());
    Iterator<Map.Entry<String, JsonNode>> fieldsIterator = value.fields();

    while (fieldsIterator.hasNext()) {
      String subAttrName = fieldsIterator.next().getKey();

   // @formatter:off
      Attribute subAttribute = schemaAttribute.getSubAttributes().stream()
          .filter(attr -> subAttrName.equals(attr.getName()))
          .findAny()
          .orElseThrow(() -> new PatchValidationException(SCIMException.Type.INVALID_PATH, String.format("Value attribute with name %s does not exist", subAttrName)));
      // @formatter:on

      mutabilityValidator.validate(subAttribute);
    }
  }
}
