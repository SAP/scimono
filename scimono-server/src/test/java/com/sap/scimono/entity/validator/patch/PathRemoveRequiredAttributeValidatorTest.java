package com.sap.scimono.entity.validator.patch;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.sap.scimono.callback.resourcetype.ResourceTypesCallback;
import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.patch.PatchOperation;
import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.entity.schema.Schema;
import com.sap.scimono.entity.schema.SchemaExtension;
import com.sap.scimono.entity.validation.patch.PathRemoveRequiredAttributeValidator;
import com.sap.scimono.exception.InvalidInputException;

public class PathRemoveRequiredAttributeValidatorTest {
  private final SchemasCallback schemaAPI = mock(SchemasCallback.class);
  private final ResourceTypesCallback resourceTypeAPI = mock(ResourceTypesCallback.class);

  @ParameterizedTest(name = "Test validation of not required attribute: {0}")
  @ValueSource(strings = {
      "urn:ietf:params:scim:schemas:core:2.0:User:title",
      "urn:org:params:scim:schemas:custom:2.0:Test:blabla"
  })
  public void testValidateNotRequireAttribute(String path) {
    doReturn(null).when(schemaAPI).getSchema(eq(path));
    doReturn(new Attribute.Builder().required(false).build()).when(schemaAPI).getAttribute(eq(path));

    PatchOperation patchOperation = newRemoveOperation(path);
    assertDoesNotThrow(() -> newValidator(User.RESOURCE_TYPE_USER).validate(patchOperation));
  }

  @ParameterizedTest(name = "Test validation of required attribute: {0}")
  @ValueSource(strings = {
      "urn:ietf:params:scim:schemas:core:2.0:User:title",
      "urn:org:params:scim:schemas:custom:2.0:Test:blabla"
  })
  public void tesValidateRequiredAttribute(String path) {
    doReturn(null).when(schemaAPI).getSchema(eq(path));
    doReturn(new Attribute.Builder().required(true).build()).when(schemaAPI).getAttribute(eq(path));

    PatchOperation patchOperation = newRemoveOperation(path);
    String resourceType = "unused";
    assertThrows(InvalidInputException.class, () -> newValidator(resourceType).validate(patchOperation));
  }

  @Test
  public void testValidateRequiredSchemaExtension() {
    assertThrows(InvalidInputException.class, () -> validateSchemaExtension(true));
  }

  @Test
  public void testValidateNotRequiredSchemaExtension() {
    assertDoesNotThrow(() -> validateSchemaExtension(false));
  }

  private void validateSchemaExtension(boolean isRequired) {
    String path = "urn:org:params:scim:schemas:custom:2.0:Test";
    Schema requiredSchema = new Schema.Builder().setId(path).build();
    String resourceType = User.RESOURCE_TYPE_USER;

    doReturn(requiredSchema).when(schemaAPI).getSchema(eq(path));
    doReturn(Collections.singletonList(new SchemaExtension(path, isRequired))).when(resourceTypeAPI).getSchemaExtensions(eq(resourceType));

    newValidator(resourceType).validate(newRemoveOperation(path));
  }

  private PathRemoveRequiredAttributeValidator newValidator(String resourceType) {
    return new PathRemoveRequiredAttributeValidator(schemaAPI, resourceTypeAPI, resourceType);
  }

  private PatchOperation newRemoveOperation(String path) {
    return new PatchOperation.Builder().setOp(PatchOperation.Type.REMOVE).setPath(path).build();
  }
}
