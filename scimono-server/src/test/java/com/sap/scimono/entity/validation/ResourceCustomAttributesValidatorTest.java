
package com.sap.scimono.entity.validation;

import static com.sap.scimono.entity.schema.AttributeDataType.COMPLEX;
import static com.sap.scimono.entity.schema.AttributeDataType.STRING;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.*;

import com.sap.scimono.entity.Meta;
import com.sap.scimono.entity.schema.SchemaExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sap.scimono.callback.resourcetype.ResourceTypesCallback;
import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.base.Extension;
import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.entity.schema.Schema;
import com.sap.scimono.exception.SCIMException;

class ResourceCustomAttributesValidatorTest {

  private static final String USER_NAME = "vladi";
  private static final String SCHEMA_NAME = "mySchema";
  private static final String TEXT_VALUE = "text";
  private static final String ATTRIBUTE1 = "attribute1";
  private static final String ATTRIBUTE2 = "attribute2";

  private static Map<String, Object> value;

  @Mock
  SchemasCallback schemaAPI;
  @Mock
  ResourceTypesCallback resourceTypesAPI;

  @BeforeAll
  public static void setUpBeforeClass() {
    value = new HashMap<>();
    Map<String, Object> attributeValue = new HashMap<>();
    attributeValue.put(ATTRIBUTE1, TEXT_VALUE);
    attributeValue.put(ATTRIBUTE2, TEXT_VALUE);
    value.put(ATTRIBUTE1, attributeValue);
  }

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

//  @Test
//  void testValidate() {
//    fail("Not yet implemented");
//  }

  @Test
  public void testValidateReadOnly() {
    Attribute attribute = new Attribute.Builder().name(ATTRIBUTE1).type(COMPLEX.toString()).mutability("readWrite")
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE1).type(STRING.toString()).mutability("readOnly").build())
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE2).type(STRING.toString()).mutability("readWrite").build()).build();

    String schemaId = Schema.EXTENSION_SCHEMA_URN.concat(SCHEMA_NAME);
    Extension extension = new Extension.Builder(schemaId).setAttributes(value).build();

    Schema schema = new Schema.Builder().setId(schemaId).addAttribute(attribute).build();
    Mockito.when(schemaAPI.getSchema(schemaId)).thenReturn(schema);

    User user = new User.Builder(USER_NAME).addExtension(extension).build();

    assertThrows(SCIMException.class, () -> ResourceCustomAttributesValidator.<User> forPut(schemaAPI, resourceTypesAPI).validate(user));
    assertThrows(SCIMException.class, () -> ResourceCustomAttributesValidator.<User> forPost(schemaAPI, resourceTypesAPI).validate(user));
  }

  @Test
  public void testValidateImmutable() {
    Attribute attribute = new Attribute.Builder().name(ATTRIBUTE1).type(COMPLEX.toString()).mutability("immutable")
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE1).type(STRING.toString()).mutability("readWrite").build())
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE2).type(STRING.toString()).mutability("readWrite").build()).build();

    String schemaId = Schema.EXTENSION_SCHEMA_URN.concat(SCHEMA_NAME);
    Extension extension = new Extension.Builder(schemaId).setAttributes(value).build();

    Schema schema = new Schema.Builder().setId(schemaId).addAttribute(attribute).build();
    Mockito.when(schemaAPI.getSchema(schemaId)).thenReturn(schema);

    User user = new User.Builder(USER_NAME).addExtension(extension).build();

    assertThrows(SCIMException.class, () -> ResourceCustomAttributesValidator.<User> forPut(schemaAPI, resourceTypesAPI).validate(user));
    ResourceCustomAttributesValidator.<User> forPost(schemaAPI, resourceTypesAPI).validate(user);
  }

  @Test
  public void testValidateImmutableWithinMultivaluedAttribute() {
    Map<String, Object> customValue = new HashMap<>();
    Map<String, Object> attributeValue = new HashMap<>();
    attributeValue.put(ATTRIBUTE1, TEXT_VALUE);
    attributeValue.put(ATTRIBUTE2, TEXT_VALUE);
    customValue.put(ATTRIBUTE1, Arrays.asList(attributeValue));

    Attribute attribute = new Attribute.Builder().name(ATTRIBUTE1).type(COMPLEX.toString()).mutability("immutable").multiValued(true)
            .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE1).type(STRING.toString()).mutability("readWrite").build())
            .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE2).type(STRING.toString()).mutability("readWrite").build()).build();

    String schemaId = Schema.EXTENSION_SCHEMA_URN.concat(SCHEMA_NAME);
    Extension extension = new Extension.Builder(schemaId).setAttributes(customValue).build();

    Schema schema = new Schema.Builder().setId(schemaId).addAttribute(attribute).build();
    Mockito.when(schemaAPI.getSchema(schemaId)).thenReturn(schema);

    User user = new User.Builder(USER_NAME).addExtension(extension).build();

    ResourceCustomAttributesValidator.<User> forPut(schemaAPI, resourceTypesAPI).validate(user);
    ResourceCustomAttributesValidator.<User> forPost(schemaAPI, resourceTypesAPI).validate(user);
  }

  @Test
  public void testValidateRequiredExtensionMissing() {
    assertThrows(SCIMException.class, () -> testValidateExtension(true, true, false));
    assertThrows(SCIMException.class, () -> testValidateExtension(false, true, false));
  }

  @Test
  public void testValidateOptionalExtensionMissing() {
    assertDoesNotThrow(() -> testValidateExtension(true, false, true));
    assertDoesNotThrow(() -> testValidateExtension(false, false, true));
    assertDoesNotThrow(() -> testValidateExtension(true, false, false));
    assertDoesNotThrow(() -> testValidateExtension(false, false, false));
  }

  @Test
  public void testValidateRequiredExtension() {
    assertDoesNotThrow(() -> testValidateExtension(true, true, true));
    assertDoesNotThrow(() -> testValidateExtension(false, true, true));
  }

  private void testValidateExtension(boolean isPostOperation, boolean isRequiredExtension, boolean hasExtension) {
    Attribute attribute = new Attribute.Builder().name(ATTRIBUTE1).type(COMPLEX.toString()).mutability("readWrite")
            .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE1).type(STRING.toString()).mutability("readWrite").build())
            .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE2).type(STRING.toString()).mutability("readWrite").build()).build();

    String schemaId = Schema.EXTENSION_SCHEMA_URN.concat(SCHEMA_NAME);
    Extension extension = new Extension.Builder(schemaId).setAttributes(value).build();

    Schema schema = new Schema.Builder().setId(schemaId).addAttribute(attribute).build();
    Mockito.when(schemaAPI.getSchema(schemaId)).thenReturn(schema);

    SchemaExtension schemaExtension = new SchemaExtension(schemaId, isRequiredExtension);
    Mockito.when(resourceTypesAPI.getSchemaExtensions(User.RESOURCE_TYPE_USER)).thenReturn(Collections.singletonList(schemaExtension));

    Meta userMeta = new Meta.Builder().setResourceType(User.RESOURCE_TYPE_USER).build();
    User.Builder userBuilder = new User.Builder(USER_NAME).setMeta(userMeta);
    if (hasExtension) {
      userBuilder.addExtension(extension);
    }

    if (isPostOperation) {
      ResourceCustomAttributesValidator.<User> forPost(schemaAPI, resourceTypesAPI).validate(userBuilder.build());
    } else {
      ResourceCustomAttributesValidator.<User> forPut(schemaAPI, resourceTypesAPI).validate(userBuilder.build());
    }
  }
}
