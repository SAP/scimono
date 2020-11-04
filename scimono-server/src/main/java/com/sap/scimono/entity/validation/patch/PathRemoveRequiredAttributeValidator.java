package com.sap.scimono.entity.validation.patch;

import static com.sap.scimono.entity.patch.PatchOperation.Type.ADD;
import static com.sap.scimono.entity.patch.PatchOperation.Type.REPLACE;

import com.sap.scimono.callback.resourcetype.ResourceTypesCallback;
import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.entity.patch.PatchOperation;
import com.sap.scimono.entity.schema.Schema;
import com.sap.scimono.entity.schema.SchemaExtension;
import com.sap.scimono.entity.validation.Validator;
import com.sap.scimono.exception.InvalidInputException;

public class PathRemoveRequiredAttributeValidator implements Validator<PatchOperation> {
  private final SchemasCallback schemaAPI;
  private final ResourceTypesCallback resourceTypeAPI;
  private final String resourceType;

  public PathRemoveRequiredAttributeValidator(SchemasCallback schemaAPI, ResourceTypesCallback resourceTypeAPI, String resourceType) {
    this.schemaAPI = schemaAPI;
    this.resourceTypeAPI = resourceTypeAPI;
    this.resourceType = resourceType;
  }

  @Override
  public void validate(PatchOperation patchOperation) {
    PatchOperation.Type operationType = patchOperation.getOp();
    if (ADD.equals(operationType) || REPLACE.equals(operationType)) {
      return;
    }

    String path = patchOperation.getPath();
    Schema schema = schemaAPI.getSchema(path);

    if (schema != null) {
      validateRequiredExtension(schema);
      return;
    }

    validateRequiredAttribute(path);
  }

  private void validateRequiredExtension(Schema schema) {
    String schemaUrn = schema.getId();
    if (SchemasCallback.isCoreSchema(schema.getId())) {
      return;
    }

    // @formatter:off
    boolean isExtensionRequired = resourceTypeAPI.getSchemaExtensions(resourceType).stream()
        .filter(SchemaExtension::isRequired)
        .map(SchemaExtension::getSchema)
        .anyMatch(schemaUrn::equalsIgnoreCase);
    // @formatter:off

    if (isExtensionRequired) {
      throw new InvalidInputException(String.format("Schema %s is required for resource type %s", schemaUrn, resourceType));
    }
  }

  private void validateRequiredAttribute(String path) {
    if (schemaAPI.getAttribute(path).isRequired()) {
      throw new InvalidInputException(String.format("Attribute with name %s is required and cannot not be removed", path));
    }
  }
}
