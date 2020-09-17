
package com.sap.scimono.helper;

import static com.sap.scimono.entity.schema.AttributeDataType.COMPLEX;
import static com.sap.scimono.entity.schema.AttributeDataType.STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.base.Extension;
import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.entity.schema.Schema;

class ReadOnlyAttributesEraserTest {

  private static final String TEST_SCHEMA_NAME = "testSchema";
  private static final String ATTRIBUTE2 = "attribute2";
  private static final String TEXT_VALUE = "text";
  private static final String ATTRIBUTE1 = "attribute1";

  @Mock
  SchemasCallback schemaAPI;

  @Test
  public void testEraseAttributes() {
    MockitoAnnotations.initMocks(this);

    Attribute attribute = new Attribute.Builder().name(ATTRIBUTE1).type(COMPLEX.toString()).mutability("readWrite")
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE1).type(STRING.toString()).mutability("readWrite").build())
        .addSubAttribute(new Attribute.Builder().name(ATTRIBUTE2).type(STRING.toString()).mutability("readOnly").build()).build();

    Mockito.when(schemaAPI.getSchema(TEST_SCHEMA_NAME)).thenReturn(new Schema.Builder().setId(TEST_SCHEMA_NAME).addAttribute(attribute).build());

    Map<String, Object> attributes = new HashMap<>();
    Map<String, Object> attributeValue = new HashMap<>();
    attributeValue.put(ATTRIBUTE1, TEXT_VALUE);
    attributeValue.put(ATTRIBUTE2, TEXT_VALUE);
    attributes.put(ATTRIBUTE1, attributeValue);

    Extension extension = new Extension.Builder(TEST_SCHEMA_NAME).setAttributes(attributes).build();
    User user = new User.Builder("vladi").addExtension(extension).build();

    Map<String, Object> expectedAttributes = new HashMap<>();
    Map<String, Object> expectedAttributeValue = new HashMap<>();
    expectedAttributeValue.put(ATTRIBUTE1, TEXT_VALUE);
    expectedAttributes.put(ATTRIBUTE1, expectedAttributeValue);

    Extension expectedExtension = new Extension.Builder(TEST_SCHEMA_NAME).setAttributes(expectedAttributes).build();
    User expectedUser = new User.Builder("vladi").addExtension(expectedExtension).build();

    ReadOnlyAttributesEraser<User> readOnlyAttributesEraser = new ReadOnlyAttributesEraser<>(schemaAPI);
    User actualUser = readOnlyAttributesEraser.eraseAllFormCustomExtensions(user);

    assertEquals(expectedUser, actualUser);
  }

}
