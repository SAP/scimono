
package com.sap.scimono.entity.validation;

import java.util.Collections;

import jakarta.ws.rs.core.Response;

import com.sap.scimono.callback.resourcetype.ResourceTypesCallback;
import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.entity.Resource;
import com.sap.scimono.entity.base.Extension;
import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.entity.schema.SchemaExtension;
import com.sap.scimono.exception.SCIMException;

public class ResourceCustomAttributesValidator<T extends Resource<T>> implements Validator<T> {

  private final SchemasCallback schemaAPI;
  private final ResourceTypesCallback resourceTypesAPI;
  private final boolean isOperationPut;

  public static <T extends Resource<T>> ResourceCustomAttributesValidator<T> forPost(final SchemasCallback schemaAPI, final ResourceTypesCallback resourceTypesAPI) {
    return new ResourceCustomAttributesValidator<>(schemaAPI, resourceTypesAPI, false);
  }

  public static <T extends Resource<T>> ResourceCustomAttributesValidator<T> forPut(final SchemasCallback schemaAPI, final ResourceTypesCallback resourceTypesAPI) {
    return new ResourceCustomAttributesValidator<>(schemaAPI, resourceTypesAPI, true);
  }

  private ResourceCustomAttributesValidator(final SchemasCallback schemaAPI, final ResourceTypesCallback resourceTypesAPI, final boolean isOperationPut) {
    this.schemaAPI = schemaAPI;
    this.resourceTypesAPI = resourceTypesAPI;
    this.isOperationPut = isOperationPut;
  }

  @Override
  public void validate(final T resource) {
    if (resource.getMeta() != null && resource.getMeta().getResourceType() != null) {
      resourceTypesAPI.getSchemaExtensions(resource.getMeta().getResourceType()).stream().filter(SchemaExtension::isRequired).forEach(schemaExtension -> {
        if (!resource.isExtensionPresent(schemaExtension.getSchema())) {
          throw new SCIMException(SCIMException.Type.INVALID_VALUE,
                  String.format("Extension with schema %s is required.", schemaExtension.getSchema()), Response.Status.BAD_REQUEST);
        }
      });
    }
    resource.getExtensions().values().forEach(extension -> {
      Attribute schemaAttribute = schemaAPI.getSchema(extension.getUrn()).toAttribute();
      new SchemaBasedAttributeValueValidator(schemaAttribute, Collections.emptyMap()).validate(extension.getAttributes());
      if (isOperationPut) {
        validateForImmutableValue(extension, schemaAttribute);
      }
    });
  }

  private void validateForImmutableValue(final Extension extension, final Attribute schemaAttribute) {
    Validator<Attribute> mutabilityValidator = new AttributeImmutableValueValidator();

    extension.getAttributes().keySet().forEach(subAtrributeName -> {

      // @formatter:off
      Attribute subAttribute = schemaAttribute.getSubAttributes().stream()
          .filter(attribute -> subAtrributeName.equalsIgnoreCase(attribute.getName()))
          .findAny()
          .orElseThrow(() -> new SCIMException(SCIMException.Type.INVALID_SYNTAX, String.format("Provided attribute with name '%s' does not exist according to the schema", subAtrributeName),
              Response.Status.BAD_REQUEST));
      // @formatter:on

      if (!subAttribute.isMultiValued()) {
        mutabilityValidator.validate(subAttribute);
      }
    });
  }

}
