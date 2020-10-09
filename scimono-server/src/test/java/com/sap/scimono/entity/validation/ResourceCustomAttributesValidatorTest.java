
package com.sap.scimono.entity.validation;

import static com.sap.scimono.entity.schema.AttributeDataType.COMPLEX;
import static com.sap.scimono.entity.schema.AttributeDataType.STRING;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.sap.scimono.entity.Meta;
import com.sap.scimono.entity.schema.SchemaExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;
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

  @TestFactory
  public Collection<DynamicTest> testValidateExtension() {
    Attribute attribute = new Attribute.Builder().name(ATTRIBUTE1).type(COMPLEX.toString()).mutability("readWrite")
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE1).type(STRING.toString()).mutability("readWrite").build())
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE2).type(STRING.toString()).mutability("readWrite").build()).build();

    String schemaId = Schema.EXTENSION_SCHEMA_URN.concat(SCHEMA_NAME);
    Extension extension = new Extension.Builder(schemaId).setAttributes(value).build();

    Schema schema = new Schema.Builder().setId(schemaId).addAttribute(attribute).build();
    Mockito.when(schemaAPI.getSchema(schemaId)).thenReturn(schema);

    SchemaExtension requiredExtension = new SchemaExtension(schemaId, true);
    SchemaExtension optionalExtension = new SchemaExtension(schemaId, false);

    Meta meta = new Meta.Builder().setResourceType(User.RESOURCE_TYPE_USER).build();
    Supplier<User.Builder> userBuilder = () -> new User.Builder(USER_NAME).setMeta(meta);

    return Arrays.asList(
        dynamicTest("Test validation when REQUIRED extension is missing",  () ->
          testValidateExtensionNegative(requiredExtension, userBuilder.get().build())
        ),
        dynamicTest("Test validation when OPTIONAL extension is missing",  () ->
            testValidateExtensionPositive(optionalExtension, userBuilder.get().build())
        ),
        dynamicTest("Test validation when REQUIRED extension is present",  () ->
          testValidateExtensionPositive(requiredExtension, userBuilder.get().addExtension(extension).build())
        ),
        dynamicTest("Test validation when OPTIONAL extension is present",  () ->
          testValidateExtensionPositive(requiredExtension, userBuilder.get().addExtension(extension).build())
        )
    );
  }

  private void testValidateExtensionNegative(SchemaExtension schemaExtension, User user) {
    Collection<Executable> assertions = getValidateExtensionTriggers(user).stream()
        .map(trigger -> (Executable) () -> assertThrows(SCIMException.class, trigger))
        .collect(Collectors.toList());

    testValidateExtension(schemaExtension, assertions);
  }

  private Collection<Executable> getValidateExtensionTriggers(User user) {
    return Arrays.asList(
        () -> ResourceCustomAttributesValidator.<User> forPost(schemaAPI, resourceTypesAPI).validate(user),
        () -> ResourceCustomAttributesValidator.<User> forPut(schemaAPI, resourceTypesAPI).validate(user)
    );
  }

  private void testValidateExtensionPositive(SchemaExtension schemaExtension, User user) {
    Collection<Executable> assertions = getValidateExtensionTriggers(user).stream()
        .map(trigger -> (Executable) () -> assertDoesNotThrow(trigger))
        .collect(Collectors.toList());

    testValidateExtension(schemaExtension, assertions);
  }

  private void testValidateExtension(SchemaExtension schemaExtension, Collection<Executable> assertions) {
    Mockito.when(resourceTypesAPI.getSchemaExtensions(User.RESOURCE_TYPE_USER)).thenReturn(Collections.singletonList(schemaExtension));
    assertAll(assertions);
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
}
