package com.sap.scimono.entity.validator.patch;

import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.entity.EnterpriseExtension;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.patch.PatchOperation;
import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.entity.schema.Schema;
import com.sap.scimono.entity.schema.resources.SchemaCSVReader;
import com.sap.scimono.entity.validation.patch.PatchValidationException;
import com.sap.scimono.entity.validation.patch.ValuePathAttributesValidator;
import com.sap.scimono.filter.QueryFilterParser;
import com.sap.scimono.filter.patch.ValuePathAttributesValidationVisitor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.invocation.InvocationOnMock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class ValuePathAttributesValidationVisitorTest {
  private static final String VALID_EXTENSION_SCHEMA_ID = String.format("%s:%s", Schema.EXTENSION_SCHEMA_URN, "default");
  private static final String CORE_SCHEMA_ID = User.SCHEMA;

  private static final Map<String, Schema> DEFAULT_SCHEMAS = SchemaCSVReader.getImportedSchemasFromCSVs();
  private static final Map<String, Schema> REQUIRED_SCHEMAS = new HashMap<>();

  private ValuePathAttributesValidator validator;
  private SchemasCallback schemaAPI;

  @BeforeAll
  public static void setupStatic() {
    REQUIRED_SCHEMAS.put(VALID_EXTENSION_SCHEMA_ID, createCustomSchema(VALID_EXTENSION_SCHEMA_ID));
    // @formatter:off
    DEFAULT_SCHEMAS.entrySet().stream()
        .filter(stringSchemaEntry -> {
          String schemaId = stringSchemaEntry.getKey();
          return User.SCHEMA.equals(schemaId) || EnterpriseExtension.ENTERPRISE_URN.equals(schemaId);
        })
        .forEach(schema -> REQUIRED_SCHEMAS.put(schema.getKey(), schema.getValue()));
    // @formatter:on
  }

  @BeforeEach
  public void setup() {
    schemaAPI = spy(SchemasCallback.class);
    // @formatter:off
    doReturn(new ArrayList<>(REQUIRED_SCHEMAS.values())).when(schemaAPI).getSchemas();
    doAnswer((InvocationOnMock invocation) -> REQUIRED_SCHEMAS.get((String) invocation.getArguments()[0]))
        .when(schemaAPI)
        .getSchema(any());
    doAnswer((InvocationOnMock invocation) -> REQUIRED_SCHEMAS.get((String) invocation.getArguments()[0]))
        .when(schemaAPI)
        .getCustomSchema(any());
    // @formatter:on

    validator = new ValuePathAttributesValidator(REQUIRED_SCHEMAS, schemaAPI, CORE_SCHEMA_ID);
}

  // @formatter:off
  @DisplayName("Test for valid filter semantics")
  @ParameterizedTest(name = "Test that --- {0} --- is valid filter")
  @ValueSource(strings = {
      "userName pr",
      "urn:ietf:params:scim:schemas:core:2.0:User:userName pr",
      "urn:ietf:params:scim:schemas:core:2.0:User:name.givenName pr",
      "userName eq \"bjensen\")",
      "urn:ietf:params:scim:schemas:core:2.0:User:userName eq \"bjensen\"",
      "urn:ietf:params:scim:schemas:core:2.0:User:name.givenName eq \"bjensen\"",
      "userName pr AND name pr",
      "name.givenName pr OR name.familyName pr",
      "urn:ietf:params:scim:schemas:core:2.0:User:userName pr AND urn:ietf:params:scim:schemas:core:2.0:User:name.givenName pr",
      "urn:ietf:params:scim:schemas:core:2.0:User:name.givenName pr OR urn:ietf:params:scim:schemas:core:2.0:User:name.familyName pr",
      "emails[type eq \"work\"]",
      "urn:ietf:params:scim:schemas:core:2.0:User:emails[type eq \"work\"]",
      "urn:ietf:params:scim:schemas:core:2.0:User:emails[(type eq \"work\")]",
      "emails[type eq \"work\" and value co \"example.com\"]",
      "userType eq \"Employee\" and emails[type eq \"work\" and value co \"@example.com\"]",
      "emails[type eq \"work\" and value co \"@example.com\"] or ims[type eq \"xmpp\" and value co \"@foo.com\"]",
      "userType eq \"Employee\" and (emails.type eq \"work\")",
      "userType eq \"Employee\" and emails [type eq \"work\" and value co \"@example.com\"]",
      "emails[type eq \"work\" and value co \"@example.com\"] or ims[type eq \"xmpp\" and value co \"@foo.com\"]"
  })
  public void testValidateFilterWithCorrectAttributeSemantics(String validExpressionInput) {
    assertDoesNotThrow(() -> execute(validExpressionInput));
  }
  // @formatter:on

  // @formatter:off
  @DisplayName("Test for valid filter semantics")
  @ParameterizedTest(name = "Test that --- {0} --- is valid filter")
  @ValueSource(strings = {
      "userNameasd pr",
      "urn:ietf:params:scim:schemas:core:2.0:Userrrrrrrrr:userName pr",
      "urn:ietf:params:scim:schemas:core:2.0:User:namerrrrrrrrr.givenName pr",
      "userNameeeeeeeessasd eq \"bjensen\"",
      "urn:ietf:params:scim:schemas:core:2.0:User:userNamessssssssss eq \"bjensen\"",
      "urn:ietf:params:scim:schemas:core:2.0:User:name.givenNamessssss eq \"bjensen\"",
      "userName pr AND nameasdasdasd pr",
      "name.givenNamessssssssss pr OR name.familyName pr",
      "urn:ietf:params:scim:schemas:core:2.0:User:userNamessssssss pr AND urn:ietf:params:scim:schemas:core:2.0:User:name.givenName pr",
      "urn:ietf:params:scim:schemas:core:2.0:User:name.givenName pr OR urn:ietf:params:scim:schemas:core:2.0:User:name.familyNamessssss pr",
      "emails[typeasdasd eq \"work\"]",
      "userName[type eq \"work\"]",
      "urn:ietf:params:scim:schemas:core:2.0:User:emailsssss[type eq \"work\"]",
      "urn:ietf:params:scim:schemas:core:2.0:User:emailsssss[(typessssss eq \"work\")]",
      "emails[type eq \"work\" and valueasdasd co \"example.com\"]",
      "userType eq \"Employee\" and emailsddddd[type eq \"work\" and value co \"@example.com\"]",
      "emails[type eq \"work\" and value co \"@example.com\"] or ims[typesssss eq \"xmpp\" and value co \"@foo.com\"]",
      "userTypessssss eq \"Employee\" and (emails.type eq \"work\")",
      "userType eq \"Employee\" and email [type eq \"work\" and valuesss co \"@example.com\"]",
      "emails[type eq \"work\" and value co \"@example.com\"] or imsssss[type eq \"xmpp\" and value co \"@foo.com\"]"
  })
  public void testValidateFilterWithIncorrectAttributeSemantics(String incorrectFilterExpression) {
    assertThrows(PatchValidationException.class, () -> execute(incorrectFilterExpression));
  }
  // @formatter:on

  private void execute(String expressionInput) {
    PatchOperation operation = new PatchOperation.Builder().setOp(PatchOperation.Type.ADD).setPath(expressionInput).build();
    QueryFilterParser.parse(expressionInput, new ValuePathAttributesValidationVisitor(validator, operation));
  }

  private static Schema createCustomSchema(String schemaUrn) {
    // @formatter:off
    return new Schema.Builder()
        .setId(schemaUrn)
        .addAttribute(createCustomAttribute("customAttr1"))
        .addAttribute(createCustomAttribute("customAttr2"))
        .build();
 // @formatter:on
  }

  private static Attribute createCustomAttribute(String name) {
    // @formatter:off
    return new Attribute.Builder()
        .name(name)
        .multiValued(false)
        .type("string")
        .build();
 // @formatter:on
  }
}
