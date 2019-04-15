
package com.sap.scim.entity.validation.patch;

import java.util.Map;

import com.sap.scim.callback.schemas.SchemasCallback;
import com.sap.scim.entity.patch.PatchOperation;
import com.sap.scim.entity.schema.Schema;
import com.sap.scim.entity.validation.Validator;
import com.sap.scim.filter.QueryFilterParser;
import com.sap.scim.filter.patch.ValuePathAttributesValidationVisitor;

public class ValuePathAttributesValidator implements Validator<PatchOperation> {

  private Map<String, Schema> requiredSchemas;
  private SchemasCallback schemaAPI;
  private String coreSchemaId;

  public ValuePathAttributesValidator(Map<String, Schema> requiredSchemas, SchemasCallback schemaAPI, String coreSchemaId) {
    this.requiredSchemas = requiredSchemas;
    this.schemaAPI = schemaAPI;
    this.coreSchemaId = coreSchemaId;
  }

  public Map<String, Schema> getRequiredSchemas() {
    return requiredSchemas;
  }

  public SchemasCallback getSchemaAPI() {
    return schemaAPI;
  }

  public String getCoreSchemaId() {
    return coreSchemaId;
  }

  @Override
  public void validate(PatchOperation operation) {
    QueryFilterParser.parse(operation.getPath(), new ValuePathAttributesValidationVisitor(this, operation));
  }


}
