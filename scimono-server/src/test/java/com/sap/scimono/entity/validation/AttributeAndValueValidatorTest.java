
package com.sap.scimono.entity.validation;

import static com.sap.scimono.entity.schema.AttributeDataType.BOOLEAN;
import static com.sap.scimono.entity.schema.AttributeDataType.COMPLEX;
import static com.sap.scimono.entity.schema.AttributeDataType.STRING;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.entity.validation.patch.PatchValidationException;

public class AttributeAndValueValidatorTest {

  private static final String MAIN_ATTRIBUTE_NAME = "target";
  private static final String ATTRIBUTE2 = "attribute2";
  private static final String TEXT_VALUE = "text";
  private static final String ATTRIBUTE1 = "attribute1";

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
    Attribute mainAttribute = new Attribute.Builder().name(MAIN_ATTRIBUTE_NAME).type(COMPLEX.toString()).mutability("readWrite")
        .addSubAttribute(attribute).build();

    new AttributeAndValueValidator(mainAttribute, Collections.emptyMap(), true).validate(value);
  }

  @Test
  public void testValidateReadOnly() {
    Attribute attribute = new Attribute.Builder().name(ATTRIBUTE1).type(COMPLEX.toString()).mutability("readWrite")
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE1).type(STRING.toString()).mutability("readOnly").build())
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE2).type(STRING.toString()).mutability("readWrite").build()).build();
    Attribute mainAttribute = new Attribute.Builder().name(MAIN_ATTRIBUTE_NAME).type(COMPLEX.toString()).mutability("readWrite")
        .addSubAttribute(attribute).build();

    assertThrows(PatchValidationException.class, () -> new AttributeAndValueValidator(mainAttribute, Collections.emptyMap(), true).validate(value));
  }

  @Test
  public void testValidateImmutable() {
    Attribute attribute = new Attribute.Builder().name(ATTRIBUTE1).type(COMPLEX.toString()).mutability("readWrite")
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE1).type(STRING.toString()).mutability("immutable").build())
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE2).type(STRING.toString()).mutability("readWrite").build()).build();
    Attribute mainAttribute = new Attribute.Builder().name(MAIN_ATTRIBUTE_NAME).type(COMPLEX.toString()).mutability("readWrite")
        .addSubAttribute(attribute).build();

    assertThrows(PatchValidationException.class, () -> new AttributeAndValueValidator(mainAttribute, Collections.emptyMap(), true).validate(value));
    new AttributeAndValueValidator(mainAttribute, Collections.emptyMap(), false).validate(value);
  }

  @Test
  public void testValidateImmutableWithinMultivaluedAttribute() {
    Map<String, Object> customValue = new HashMap<>();
    Map<String, Object> attributeValue = new HashMap<>();
    attributeValue.put(ATTRIBUTE1, TEXT_VALUE);
    attributeValue.put(ATTRIBUTE2, TEXT_VALUE);
    customValue.put(ATTRIBUTE1, Arrays.asList(attributeValue));

    Attribute attribute = new Attribute.Builder().name(ATTRIBUTE1).type(COMPLEX.toString()).mutability("readWrite").multiValued(true)
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE1).type(STRING.toString()).mutability("immutable").build())
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE2).type(STRING.toString()).mutability("readWrite").build()).build();
    Attribute mainAttribute = new Attribute.Builder().name(MAIN_ATTRIBUTE_NAME).type(COMPLEX.toString()).mutability("readWrite")
        .addSubAttribute(attribute).build();

    new AttributeAndValueValidator(mainAttribute, Collections.emptyMap(), true).validate(customValue);
  }

  @Test
  public void testValidateDataType() {
    Attribute attribute = new Attribute.Builder().name(ATTRIBUTE1).type(COMPLEX.toString()).mutability("readWrite")
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE1).type(BOOLEAN.toString().toString()).mutability("readWrite").build())
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE2).type(STRING.toString()).mutability("readWrite").build()).build();
    Attribute mainAttribute = new Attribute.Builder().name(MAIN_ATTRIBUTE_NAME).type(COMPLEX.toString()).mutability("readWrite")
        .addSubAttribute(attribute).build();

    assertThrows(PatchValidationException.class, () -> new AttributeAndValueValidator(mainAttribute, Collections.emptyMap(), true).validate(value));
  }

}
