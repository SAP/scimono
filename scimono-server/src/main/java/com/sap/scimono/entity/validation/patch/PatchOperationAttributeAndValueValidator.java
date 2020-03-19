
package com.sap.scimono.entity.validation.patch;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.entity.patch.PatchOperation;
import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.entity.schema.Schema;
import com.sap.scimono.entity.validation.AttributeAndValueValidator;
import com.sap.scimono.entity.validation.Validator;
import com.sap.scimono.helper.Strings;

public class PatchOperationAttributeAndValueValidator implements Validator<PatchOperation> {

  private final SchemasCallback schemaAPI;
  private final String coreSchemaId;
  private final Map<String, Schema> permittedSchemas;

  public PatchOperationAttributeAndValueValidator(final SchemasCallback schemaAPI, final String coreSchemaId,
      final Map<String, Schema> permittedSchemas) {
    this.schemaAPI = schemaAPI;
    this.coreSchemaId = coreSchemaId;
    this.permittedSchemas = permittedSchemas;
  }

  @Override
  public void validate(final PatchOperation operation) {
    String path = operation.getPath();
    JsonNode value = operation.getValue();

    AttributeAndValueValidator attributeAndValueValidator;
    if (Strings.isNullOrEmpty(path)) {
      attributeAndValueValidator = new AttributeAndValueValidator(schemaAPI.getSchema(coreSchemaId), permittedSchemas);
    } else if (schemaAPI.getSchema(path) != null) {
      attributeAndValueValidator = new AttributeAndValueValidator(schemaAPI.getSchema(path), permittedSchemas);
    } else {
      String pathWithoutFilter = schemaAPI.removeValueFilterFromAttributeNotation(path);
      Attribute targetAttribute = schemaAPI.getAttribute(pathWithoutFilter);
      attributeAndValueValidator = new AttributeAndValueValidator(targetAttribute, permittedSchemas);
    }

    attributeAndValueValidator.validate(value);
  }

}
