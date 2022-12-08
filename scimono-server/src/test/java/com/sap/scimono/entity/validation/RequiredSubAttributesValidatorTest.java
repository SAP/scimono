package com.sap.scimono.entity.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.exception.SCIMException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

class RequiredSubAttributesValidatorTest {

  @Test
  void validateIsSuccessful() {
    Attribute attribute = new Attribute.Builder().name("parent")
        .addSubAttribute(new Attribute.Builder().name("subAttribute").required(true).build())
        .build();

    Map<String, JsonNode> fields = Collections.singletonMap("subAttribute", new TextNode("abc"));
    ObjectNode value = new ObjectNode(null, fields);

    new RequiredSubAttributesValidator(value).validate(attribute);
  }

  @Test
  void validateShouldFailIfRequiredAttributeIsMissing() {
    Attribute attribute = new Attribute.Builder().name("parent")
        .addSubAttribute(new Attribute.Builder().name("subAttribute").required(true).build())
        .build();

    JsonNode value = JsonNodeFactory.instance.objectNode();

    RequiredSubAttributesValidator validator = new RequiredSubAttributesValidator(value);
    assertThrows(SCIMException.class, () -> validator.validate(attribute), "Should fail");
  }
}