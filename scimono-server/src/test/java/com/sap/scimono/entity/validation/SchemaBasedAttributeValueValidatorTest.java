
package com.sap.scimono.entity.validation;

import static com.sap.scimono.entity.schema.AttributeDataType.BOOLEAN;
import static com.sap.scimono.entity.schema.AttributeDataType.COMPLEX;
import static com.sap.scimono.entity.schema.AttributeDataType.STRING;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.sap.scimono.entity.schema.Schema;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.exception.SCIMException;

public class SchemaBasedAttributeValueValidatorTest {

  private static final String PARENT_ATTRIBUTE_NAME = "parent";
  private static final String TEXT_VALUE = "text";
  private static final String ATTRIBUTE1 = "attribute1";
  private static final String ATTRIBUTE2 = "attribute2";
  private static final String ATTRIBUTE3 = "attribute3";
  private static final String SCHEMA_EXTENSION_ID = "urn:extension:User";

  private static Map<String, Object> value;

  @BeforeAll
  public static void setUp() {
    Map<String, Object> attributeValue = new HashMap<>();
    attributeValue.put(ATTRIBUTE1, TEXT_VALUE);
    attributeValue.put(ATTRIBUTE2, TEXT_VALUE);

    value = new HashMap<>();
    value.put(ATTRIBUTE1, attributeValue);
  }

  @Test
  void validateIsSuccessful() {
    Attribute attribute = new Attribute.Builder().name(ATTRIBUTE1).type(COMPLEX.toString())
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE1).type(STRING.toString()).build())
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE2).type(STRING.toString()).build())
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE3).type(STRING.toString()).build()).build();

    Attribute mainAttribute = new Attribute.Builder().name(PARENT_ATTRIBUTE_NAME).type(COMPLEX.toString())
        .addSubAttribute(attribute).build();

    new SchemaBasedAttributeValueValidator(mainAttribute, Collections.emptyMap()).validate(value);
  }

  @Test
  void validateAMultiValuedAttribute() {
    Attribute attribute = new Attribute.Builder().name(ATTRIBUTE1).type(COMPLEX.toString())
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE1).type(STRING.toString()).required(true).build())
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE2).type(STRING.toString()).build())
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE3).type(STRING.toString()).build()).build();

    Attribute mainAttribute = new Attribute.Builder().name(PARENT_ATTRIBUTE_NAME).type(COMPLEX.toString()).multiValued(true)
        .addSubAttribute(attribute).build();

    new SchemaBasedAttributeValueValidator(mainAttribute, Collections.emptyMap()).validate(Collections.singletonList(value));
  }

  @Test
  void validateWithAttributeForSchemaExtension() {
    Attribute mainAttribute = new Attribute.Builder().name(PARENT_ATTRIBUTE_NAME).type(COMPLEX.toString()).build();

    Schema schema = new Schema.Builder().setId(SCHEMA_EXTENSION_ID)
        .addAttribute(new Attribute.Builder().name(ATTRIBUTE1).type(STRING.toString()).build())
        .build();
    Map<String, Schema> permittedSchemas = Collections.singletonMap(SCHEMA_EXTENSION_ID, schema);

    Map<String, Object> extensionValue = new HashMap<>();
    extensionValue.put(SCHEMA_EXTENSION_ID, Collections.singletonMap(ATTRIBUTE1, TEXT_VALUE));

    SchemaBasedAttributeValueValidator validator = new SchemaBasedAttributeValueValidator(mainAttribute, permittedSchemas);
    validator.validate(extensionValue);
  }

  @Test
  void validateWithInvalidAttributeDataType() {
    Attribute attribute = new Attribute.Builder().name(ATTRIBUTE1).type(COMPLEX.toString())
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE1).type(BOOLEAN.toString()).build())
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE2).type(STRING.toString()).build())
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE3).type(STRING.toString()).build()).build();

    Attribute mainAttribute = new Attribute.Builder().name(PARENT_ATTRIBUTE_NAME).type(COMPLEX.toString())
        .addSubAttribute(attribute).build();

    SchemaBasedAttributeValueValidator validator = new SchemaBasedAttributeValueValidator(mainAttribute, Collections.emptyMap());
    assertThrows(SCIMException.class, () -> validator.validate(value), "Should fail");
  }

  @Test
  void validateWithUnknownAttribute() {
    Attribute attribute = new Attribute.Builder().name(ATTRIBUTE1).type(COMPLEX.toString())
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE1).type(STRING.toString()).build()).build();

    Attribute mainAttribute = new Attribute.Builder().name(PARENT_ATTRIBUTE_NAME).type(COMPLEX.toString())
        .addSubAttribute(attribute).build();

    SchemaBasedAttributeValueValidator validator = new SchemaBasedAttributeValueValidator(mainAttribute, Collections.emptyMap());
    assertThrows(SCIMException.class, () -> validator.validate(value), "Should fail");
  }

  @Test
  void validateWithMissingRequiredAttribute() {
    Attribute attribute = new Attribute.Builder().name(ATTRIBUTE1).type(COMPLEX.toString())
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE1).type(STRING.toString()).build())
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE2).type(STRING.toString()).build())
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE3).type(STRING.toString()).required(true).build()).build();

    Attribute mainAttribute = new Attribute.Builder().name(PARENT_ATTRIBUTE_NAME).type(COMPLEX.toString())
        .addSubAttribute(attribute).build();

    SchemaBasedAttributeValueValidator validator = new SchemaBasedAttributeValueValidator(mainAttribute, Collections.emptyMap());
    assertThrows(SCIMException.class, () -> validator.validate(value), "Should fail");
  }
}
