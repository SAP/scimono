
package com.sap.scimono.scim.system.tests;

import com.sap.scimono.api.Schemas;
import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.entity.base.ExtensionFieldType;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.entity.schema.Schema;
import com.sap.scimono.scim.system.tests.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SchemaOperationsHttpResponseCodeTest extends SCIMHttpResponseCodeTest {
  private static int TEST_IDENTIFIER = 0;

  @BeforeEach
  public void setUpBeforeTest() {
    TEST_IDENTIFIER++;
  }

  @Test
  public void testGetSchema200() {
    Schema testSchema = TestData.buildTestSchema(Schema.EXTENSION_SCHEMA_URN.concat("GetSchemaResp" + TEST_IDENTIFIER));
    SCIMResponse<Schema> scimResponse = schemaRequest.createSchema(testSchema);

    assertTrue(scimResponse.isSuccess());
    assertEquals(CREATED.getStatusCode(), scimResponse.getStatusCode());

    scimResponse = schemaRequest.readSingleSchema(scimResponse.get().getId());

    assertTrue(scimResponse.isSuccess());
    assertEquals(OK.getStatusCode(), scimResponse.getStatusCode());
    assertEquals(Schema.EXTENSION_SCHEMA_URN.concat("GetSchemaResp" + TEST_IDENTIFIER), scimResponse.get().getId());
  }

  @Test
  public void testGetSchemaWithIllegalId400() {
    SCIMResponse<Schema> scimResponse = schemaRequest.readSingleSchema("wrongId");

    assertFalse(scimResponse.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), scimResponse.getStatusCode());
  }

  @Test
  public void testGetSchemaWithNonExistingId404() {
    String schemaId = Schema.EXTENSION_SCHEMA_URN.concat("Something");
    SCIMResponse<Schema> scimResponse = schemaRequest.readSingleSchema(schemaId);

    assertFalse(scimResponse.isSuccess());
    assertEquals(NOT_FOUND.getStatusCode(), scimResponse.getStatusCode());
  }

  @Test
  public void testGetAllSchemas200() {
    Schema testSchema = TestData.buildTestSchema(Schema.EXTENSION_SCHEMA_URN.concat("GetAllSchemasResp" + TEST_IDENTIFIER));
    SCIMResponse<Schema> scimResponse = schemaRequest.createSchema(testSchema);

    assertTrue(scimResponse.isSuccess());
    assertEquals(CREATED.getStatusCode(), scimResponse.getStatusCode());

    SCIMResponse<PagedByIndexSearchResult<Schema>> pagedByIndexSearchResultSCIMResponse = schemaRequest.readAllSchemas();

    assertTrue(scimResponse.isSuccess());
    assertEquals(OK.getStatusCode(), pagedByIndexSearchResultSCIMResponse.getStatusCode());
  }

  @Test
  public void testCreateSchema201() {
    Schema testSchema = TestData.buildTestSchema(Schema.EXTENSION_SCHEMA_URN.concat("CrSchemasResp" + TEST_IDENTIFIER));
    SCIMResponse<Schema> scimResponse = schemaRequest.createSchema(testSchema);

    assertTrue(scimResponse.isSuccess());
    assertEquals(CREATED.getStatusCode(), scimResponse.getStatusCode());
  }

  @Test
  public void testCreateSchemaWithUnderscore201() {
    Schema testSchema = TestData.buildTestSchema(Schema.EXTENSION_SCHEMA_URN.concat("CrSchemasResp_" + TEST_IDENTIFIER));
    SCIMResponse<Schema> scimResponse = schemaRequest.createSchema(testSchema);

    assertTrue(scimResponse.isSuccess());
    assertEquals(CREATED.getStatusCode(), scimResponse.getStatusCode());
  }

  @Test
  public void testCreateMoreThan20CustomSchemas400() {
    List<Schema> definedSchemas = getAllSchemas();

    int currentCustomSchemasCount = definedSchemas.size() - Schemas.PREDEFINED_SCHEMAS_COUNT;
    List<String> customSchemaNames = new ArrayList<>();
    SCIMResponse<Schema> scimResponse;
    while (currentCustomSchemasCount < 20) {
      Schema testSchema = TestData.buildTestSchema(Schema.EXTENSION_SCHEMA_URN.concat("CustSchMax" + currentCustomSchemasCount));
      scimResponse = schemaRequest.createSchema(testSchema);

      assertTrue(scimResponse.isSuccess());
      assertEquals(CREATED.getStatusCode(), scimResponse.getStatusCode());

      customSchemaNames.add(testSchema.getId());
      currentCustomSchemasCount++;
    }

    Schema testSchema = TestData.buildTestSchema(Schema.EXTENSION_SCHEMA_URN.concat("CustSchMax" + 21));
    scimResponse = schemaRequest.createSchema(testSchema);

    assertFalse(scimResponse.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), scimResponse.getStatusCode());

    for (String nextCustomSchemaName : customSchemaNames) {
      SCIMResponse<Void> deleteSchemaResponse = schemaRequest.deleteSchema(nextCustomSchemaName);

      assertTrue(deleteSchemaResponse.isSuccess());
      assertEquals(NO_CONTENT.getStatusCode(), deleteSchemaResponse.getStatusCode());
    }
  }

  @Test
  public void testDeleteSchema204() {
    Schema testSchema = TestData.buildTestSchema(Schema.EXTENSION_SCHEMA_URN.concat("DelSchemasResp" + TEST_IDENTIFIER));
    SCIMResponse<Schema> scimResponse = schemaRequest.createSchema(testSchema);

    assertTrue(scimResponse.isSuccess());
    assertEquals(CREATED.getStatusCode(), scimResponse.getStatusCode());

    SCIMResponse<Void> deleteSchemaResponse = schemaRequest.deleteSchema(scimResponse.get().getId());

    assertTrue(deleteSchemaResponse.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), deleteSchemaResponse.getStatusCode());
  }

  @Test
  public void testDeleteSchemaWrongId400() {
    SCIMResponse<Void> scimResponse = schemaRequest.deleteSchema("wrongSchemaId");

    assertFalse(scimResponse.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), scimResponse.getStatusCode());
  }

  @Test
  public void testDeleteSchemaNonExistingId404() {
    String schemaId = Schema.EXTENSION_SCHEMA_URN.concat("Something");
    SCIMResponse<Void> scimResponse = schemaRequest.deleteSchema(schemaId);

    assertFalse(scimResponse.isSuccess());
    assertEquals(NOT_FOUND.getStatusCode(), scimResponse.getStatusCode());
  }

  @Test
  public void testCreateSchemaWithInvalidPrefix400() {
    Schema testSchema = TestData.buildTestSchema("urn:invalid:custom:2.0:Test");
    SCIMResponse<Schema> scimResponse = schemaRequest.createSchema(testSchema);

    assertFalse(scimResponse.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), scimResponse.getStatusCode());
  }

  @Test
  public void testCreateSchemasWithSameNames409() {
    Schema testSchema = TestData.buildTestSchema(Schema.EXTENSION_SCHEMA_URN.concat("SchemaSameNames"));
    SCIMResponse<Schema> scimResponse = schemaRequest.createSchema(testSchema);

    assertTrue(scimResponse.isSuccess());
    assertEquals(CREATED.getStatusCode(), scimResponse.getStatusCode());

    Schema testSchemaDuplicate = TestData.buildTestSchema(Schema.EXTENSION_SCHEMA_URN.concat("SchemaSameNames"));
    SCIMResponse<Schema> responseForDuplicate = schemaRequest.createSchema(testSchemaDuplicate);

    assertFalse(responseForDuplicate.isSuccess());
    assertEquals(CONFLICT.getStatusCode(), responseForDuplicate.getStatusCode());
  }

  @Test
  public void testCreateSchemaWithInvalidName400() {
    Schema testSchema = TestData.buildTestSchema("urn:invalid:custom:2.0:Test*");
    SCIMResponse<Schema> scimResponse = schemaRequest.createSchema(testSchema);

    assertFalse(scimResponse.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), scimResponse.getStatusCode());
  }

  @Test
  public void testCreateSchemaWithTooLongName400() {
    Schema testSchema = TestData.buildTestSchema(Schema.EXTENSION_SCHEMA_URN.concat("TestTestTestTestTestTestTestTestTestTest"));
    SCIMResponse<Schema> scimResponse = schemaRequest.createSchema(testSchema);

    assertFalse(scimResponse.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), scimResponse.getStatusCode());
  }

  @Test
  public void testCreateSchemaWithTooLongAttributeName400() {
    Attribute testAttribute = TestData.setAllDefaultPropertiesToTestAttribute("ThisIsATooLongAttributeNameForTestPurposes", ExtensionFieldType.STRING)
        .build();
    Schema testSchema = TestData.buildCustomSchemaWithAttrs(Schema.EXTENSION_SCHEMA_URN.concat("CrSchemaHTTPResp" + TEST_IDENTIFIER),
        Collections.singletonList(testAttribute));
    SCIMResponse<Schema> scimResponse = schemaRequest.createSchema(testSchema);

    assertFalse(scimResponse.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), scimResponse.getStatusCode());
  }

  @Test
  public void testCreateSchemaWithNonAlpthaNumAttributeName400() {
    Attribute testAttribute = TestData.setAllDefaultPropertiesToTestAttribute("Non*Alphanum", ExtensionFieldType.STRING).build();
    Schema testSchema = TestData.buildCustomSchemaWithAttrs(Schema.EXTENSION_SCHEMA_URN.concat("CrSchemaHTTPResp" + TEST_IDENTIFIER),
        Collections.singletonList(testAttribute));
    SCIMResponse<Schema> scimResponse = schemaRequest.createSchema(testSchema);

    assertFalse(scimResponse.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), scimResponse.getStatusCode());
  }

  @Test
  public void testCreateSchemaWithMoreThan20Attrs400() {
    List<Attribute> testAttributes = new ArrayList<>();
    for (int i = 0; i < 25; i++) {
      Attribute testAttr = TestData.setAllDefaultPropertiesToTestAttribute("Attribute" + i, ExtensionFieldType.STRING).build();
      testAttributes.add(testAttr);
    }

    Schema testSchema = TestData.buildCustomSchemaWithAttrs(Schema.EXTENSION_SCHEMA_URN.concat("CrSchemaHTTPResp" + TEST_IDENTIFIER), testAttributes);
    SCIMResponse<Schema> scimResponse = schemaRequest.createSchema(testSchema);

    assertFalse(scimResponse.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), scimResponse.getStatusCode());
  }
}
