package com.sap.scimono.entity.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.exception.SCIMException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.sap.scimono.entity.schema.AttributeDataType.BINARY;
import static com.sap.scimono.entity.schema.AttributeDataType.BOOLEAN;
import static com.sap.scimono.entity.schema.AttributeDataType.COMPLEX;
import static com.sap.scimono.entity.schema.AttributeDataType.DATE_TIME;
import static com.sap.scimono.entity.schema.AttributeDataType.DECIMAL;
import static com.sap.scimono.entity.schema.AttributeDataType.INTEGER;
import static com.sap.scimono.entity.schema.AttributeDataType.REFERENCE;
import static com.sap.scimono.entity.schema.AttributeDataType.STRING;
import static org.junit.jupiter.api.Assertions.*;

class AttributeDataTypeValidatorTest {

  @ParameterizedTest(name = "Attribute: {0}")
  @MethodSource("validData")
  void validAttributeValues(Attribute attribute, JsonNode value) {
    new AttributeDataTypeValidator(value).validate(attribute);
  }

  private static Stream<Arguments> validData() {
    // @formatter:off
    return Stream.of(
        Arguments.of(new Attribute.Builder().type(STRING.toString()).build(), new TextNode("abc")),
        Arguments.of(new Attribute.Builder().type(INTEGER.toString()).build(), new IntNode(0)),
        Arguments.of(new Attribute.Builder().type(BOOLEAN.toString()).build(), BooleanNode.TRUE),
        Arguments.of(new Attribute.Builder().type(COMPLEX.toString()).build(), new ObjectNode(null)),
        Arguments.of(new Attribute.Builder().type(DECIMAL.toString()).build(), new DecimalNode(null)),
        Arguments.of(new Attribute.Builder().type(REFERENCE.toString()).build(), new TextNode("http://uri.com")),
        Arguments.of(new Attribute.Builder().type(BINARY.toString()).build(), new TextNode("FF")),
        Arguments.of(new Attribute.Builder().type(DATE_TIME.toString()).build(), new TextNode("2011-12-03T10:15:30Z")),
        Arguments.of(new Attribute.Builder().type(STRING.toString()).multiValued(true).build(), new ArrayNode(JsonNodeFactory.instance).add("a"))
    );
    // @formatter:on
  }

  @ParameterizedTest(name = "Attribute: {0}")
  @MethodSource("invalidData")
  void invalidAttributeValues(Attribute attribute, JsonNode value) {
    AttributeDataTypeValidator validator = new AttributeDataTypeValidator(value);
    assertThrows(SCIMException.class, () -> validator.validate(attribute), "Should fail");
  }

  private static Stream<Arguments> invalidData() {
    // @formatter:off
    return Stream.of(
        Arguments.of(new Attribute.Builder().type(STRING.toString()).build(), new IntNode(0)),
        Arguments.of(new Attribute.Builder().type(INTEGER.toString()).build(), new TextNode("abv")),
        Arguments.of(new Attribute.Builder().type(BOOLEAN.toString()).build(), new TextNode("true")),
        Arguments.of(new Attribute.Builder().type(COMPLEX.toString()).build(), new ArrayNode(null)),
        Arguments.of(new Attribute.Builder().type(DECIMAL.toString()).build(), new TextNode("0")),
        Arguments.of(new Attribute.Builder().type(REFERENCE.toString()).build(), new TextNode("invalid uri")),
        Arguments.of(new Attribute.Builder().type(BINARY.toString()).build(), new TextNode("non-base64")),
        Arguments.of(new Attribute.Builder().type(DATE_TIME.toString()).build(), new TextNode("2011-13-03T10:15:30Z")),
        Arguments.of(new Attribute.Builder().type(DATE_TIME.toString()).build(), new IntNode(0)),
        Arguments.of(new Attribute.Builder().type(STRING.toString()).multiValued(true).build(), new ObjectNode(null)),
        Arguments.of(new Attribute.Builder().type(STRING.toString()).multiValued(true).build(), new ArrayNode(JsonNodeFactory.instance).add(1))
    );
    // @formatter:on
  }

}