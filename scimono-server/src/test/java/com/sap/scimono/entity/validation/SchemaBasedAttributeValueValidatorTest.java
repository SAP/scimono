
package com.sap.scimono.entity.validation;

import static com.sap.scimono.entity.schema.AttributeDataType.BOOLEAN;
import static com.sap.scimono.entity.schema.AttributeDataType.COMPLEX;
import static com.sap.scimono.entity.schema.AttributeDataType.STRING;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

  private static Map<String, Object> value;

  @BeforeAll
  public static void setUp() {
    value = new HashMap<>();
    Map<String, Object> attributeValue = new HashMap<>();
    attributeValue.put(ATTRIBUTE1, TEXT_VALUE);
    attributeValue.put(ATTRIBUTE2, TEXT_VALUE);
    value.put(ATTRIBUTE1, attributeValue);
  }

  @Test
  public void testValidate() {
    Attribute attribute = new Attribute.Builder().name(ATTRIBUTE1).type(COMPLEX.toString()).mutability("readWrite")
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE1).type(STRING.toString()).mutability("readWrite").build())
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE2).type(STRING.toString()).mutability("readWrite").build()).build();
    Attribute mainAttribute = new Attribute.Builder().name(PARENT_ATTRIBUTE_NAME).type(COMPLEX.toString()).mutability("readWrite")
        .addSubAttribute(attribute).build();

    new SchemaBasedAttributeValueValidator(mainAttribute, Collections.emptyMap()).validate(value);
  }

  @Test
  public void testValidateDataType() {
    Attribute attribute = new Attribute.Builder().name(ATTRIBUTE1).type(COMPLEX.toString()).mutability("readWrite")
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE1).type(BOOLEAN.toString()).mutability("readWrite").build())
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE2).type(STRING.toString()).mutability("readWrite").build()).build();
    Attribute mainAttribute = new Attribute.Builder().name(PARENT_ATTRIBUTE_NAME).type(COMPLEX.toString()).mutability("readWrite")
        .addSubAttribute(attribute).build();

    assertThrows(SCIMException.class, () -> new SchemaBasedAttributeValueValidator(mainAttribute, Collections.emptyMap()).validate(value));
  }

  @Test
  public void testValidateWhenAttributeWithMoreSubAttributesThanValue() {
    Attribute attribute = new Attribute.Builder().name(ATTRIBUTE1).type(COMPLEX.toString()).mutability("readWrite")
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE1).type(STRING.toString()).mutability("readWrite").build())
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE2).type(STRING.toString()).mutability("readWrite").build())
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE3).type(STRING.toString()).mutability("readWrite").build()).build();
    Attribute mainAttribute = new Attribute.Builder().name(PARENT_ATTRIBUTE_NAME).type(COMPLEX.toString()).mutability("readWrite")
        .addSubAttribute(attribute).build();

    new SchemaBasedAttributeValueValidator(mainAttribute, Collections.emptyMap()).validate(value);
  }

  @Test
  public void testValidateWhenAttributeWithLessSubAttributesThanValue() {
    Attribute attribute = new Attribute.Builder().name(ATTRIBUTE1).type(COMPLEX.toString()).mutability("readWrite")
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE1).type(STRING.toString()).mutability("readWrite").build()).build();
    Attribute mainAttribute = new Attribute.Builder().name(PARENT_ATTRIBUTE_NAME).type(COMPLEX.toString()).mutability("readWrite")
        .addSubAttribute(attribute).build();

    assertThrows(SCIMException.class, () -> new SchemaBasedAttributeValueValidator(mainAttribute, Collections.emptyMap()).validate(value));
  }

  @Test
  public void testValidateWhenAttributeWithMoreSubAttributesThanValueAndAreRequired() {
    Attribute attribute = new Attribute.Builder().name(ATTRIBUTE1).type(COMPLEX.toString()).mutability("readWrite")
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE1).type(STRING.toString()).mutability("readWrite").build())
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE2).type(STRING.toString()).mutability("readWrite").build())
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE3).type(STRING.toString()).mutability("readWrite").required(true).build()).build();
    Attribute mainAttribute = new Attribute.Builder().name(PARENT_ATTRIBUTE_NAME).type(COMPLEX.toString()).mutability("readWrite")
        .addSubAttribute(attribute).build();

    assertThrows(SCIMException.class, () -> new SchemaBasedAttributeValueValidator(mainAttribute, Collections.emptyMap()).validate(value));
  }

}
