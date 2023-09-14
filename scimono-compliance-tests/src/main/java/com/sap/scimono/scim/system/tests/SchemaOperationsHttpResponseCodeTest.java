
package com.sap.scimono.scim.system.tests;

import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.entity.base.ExtensionFieldType;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.entity.schema.Schema;
import com.sap.scimono.scim.system.tests.extensions.SchemaClientScimResponseExtension;
import com.sap.scimono.scim.system.tests.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.CONFLICT;
import static jakarta.ws.rs.core.Response.Status.CREATED;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class SchemaOperationsHttpResponseCodeTest extends SCIMHttpResponseCodeTest {
  private static final Logger logger = LoggerFactory.getLogger(SchemaOperationsHttpResponseCodeTest.class);

  private static int TEST_IDENTIFIER = 0;

  @RegisterExtension 
  SchemaClientScimResponseExtension resourceAwareSchemaRequest = SchemaClientScimResponseExtension.forClearingAfterEachExecutions(schemaRequest);

  @BeforeEach
  public void setUpBeforeTest() {
    TEST_IDENTIFIER++;
  }

  @Test
  @DisplayName("Test Get custom schema and verify Http status code: 200")
  public void testGetSchema200() {
    Schema testSchema = createCustomSchemaAndVerifyResponse("GetSchemaResp");

    logger.info("Fetching Schema: {}", testSchema.getId());
    SCIMResponse<Schema> scimResponse = resourceAwareSchemaRequest.readSingleSchema(testSchema.getId());

    assertEquals(Schema.EXTENSION_SCHEMA_URN.concat("GetSchemaResp" + TEST_IDENTIFIER), scimResponse.get().getId(), "Verify schema id");
  }

  @Test
  @DisplayName("Test Get schema with illegal id and verify Http status code: 400")
  public void testGetSchemaWithIllegalId400() {
    logger.info("Fetching Schema with wrond id");
    SCIMResponse<Schema> scimResponse = resourceAwareSchemaRequest.readSingleSchema("wrongId");

    assertAll("Verify Fetch custom Schema response", getResponseStatusAssertions(scimResponse, false, BAD_REQUEST));
  }

  @Test
  @DisplayName("Test Get custom schema with non existing id and verify Http status code: 404")
  public void testGetSchemaWithNonExistingId404() {
    String schemaId = Schema.EXTENSION_SCHEMA_URN.concat("Something");
    SCIMResponse<Schema> scimResponse = resourceAwareSchemaRequest.readSingleSchema(schemaId);

    logger.info("Fetching Schema with non existing id");
    assertAll("Verify Fetch custom Schema response", getResponseStatusAssertions(scimResponse, false, NOT_FOUND));
  }

  @Test
  @DisplayName("Test Get all schemas and verify Http status code: 200")
  public void testGetAllSchemas200() {
    createCustomSchemaAndVerifyResponse("GetAllSchemasResp");

    logger.info("Fetching all schemas");
    SCIMResponse<PagedByIndexSearchResult<Schema>> pagedByIndexSearchResultSCIMResponse = resourceAwareSchemaRequest.readAllSchemas();

    assertAll("Verify Create custom Schema response", getResponseStatusAssertions(pagedByIndexSearchResultSCIMResponse, true, OK));
  }

  @Test
  @DisplayName("Test Create custom schema and verify Http status code: 201")
  public void testCreateSchema201() {
    createCustomSchemaAndVerifyResponse("CrSchemasResp");
  }

  @Test
  @DisplayName("Test Create custom schema with underscore character in name (_) and verify Http status code: 201")
  public void testCreateSchemaWithUnderscore201() {
    createCustomSchemaAndVerifyResponse("CrSchemasResp_");
  }

  @Test
  @DisplayName("Test Delete custom schema and verify Http status code: 204")
  public void testDeleteSchema204() {
    Schema createdSchema = createCustomSchemaAndVerifyResponse("DelSchemasResp");

    logger.info("Deleting custom Schema: {}", createdSchema.getId());
    SCIMResponse<Void> deleteSchemaResponse = resourceAwareSchemaRequest.deleteSchema(createdSchema.getId());

    assertAll("Verify Delete custom Schema response", getResponseStatusAssertions(deleteSchemaResponse, true, NO_CONTENT));
  }

  @Test
  @DisplayName("Test Delete custom schema with wrong id and verify Http status code: 400")
  public void testDeleteSchemaWrongId400() {
    logger.info("Deleting custom Schema with wrong id");
    SCIMResponse<Void> scimResponse = resourceAwareSchemaRequest.deleteSchema("wrongSchemaId");

    assertAll("Verify Delete custom Schema response", getResponseStatusAssertions(scimResponse, false, BAD_REQUEST));
  }

  @Test
  @DisplayName("Test Delete custom schema with non existing id and verify Http status code: 404")
  public void testDeleteSchemaNonExistingId404() {
    String schemaId = Schema.EXTENSION_SCHEMA_URN.concat("Something");
    logger.info("Deleting custom Schema with non existing id");
    SCIMResponse<Void> scimResponse = resourceAwareSchemaRequest.deleteSchema(schemaId);

    assertAll("Verify Delete custom Schema response", getResponseStatusAssertions(scimResponse, false, NOT_FOUND));
  }

  @Test
  @DisplayName("Test Create custom schema with invalid prefix and verify Http status code: 400")
  public void testCreateSchemaWithInvalidPrefix400() {
    Schema testSchema = TestData.buildTestSchema("urn:invalid:custom:2.0:Test");

    logger.info("Creating custom Schema with invalid prefix");
    SCIMResponse<Schema> scimResponse = resourceAwareSchemaRequest.createSchema(testSchema);

    assertAll("Verify Create custom Schema response", getResponseStatusAssertions(scimResponse, false, BAD_REQUEST));
  }

  @Test
  @DisplayName("Test Create custom schema with already existing name and verify Http status code: 409")
  public void testCreateSchemasWithSameNames409() {
    String schemaName = Schema.EXTENSION_SCHEMA_URN.concat("SchemaSameNames");
    Schema testSchemaBody = TestData.buildTestSchema(schemaName);

    logger.info("Creating custom Schema again: {}", schemaName);
    SCIMResponse<Schema> createSchemaResponse = resourceAwareSchemaRequest.createSchema(testSchemaBody);
    assertAll("Verify Create custom Schema response", getResponseStatusAssertions(createSchemaResponse, true, CREATED));

    logger.info("Creating same custom Schema again");
    SCIMResponse<Schema> responseForDuplicate = resourceAwareSchemaRequest.createSchema(testSchemaBody);

    assertAll("Verify Create custom Schema response", getResponseStatusAssertions(responseForDuplicate, false, CONFLICT));
  }

  @Test
  @DisplayName("Test Create custom schema with invalid name and verify Http status code: 400")
  public void testCreateSchemaWithInvalidName400() {
    Schema testSchema = TestData.buildTestSchema("urn:invalid:custom:2.0:Test*");

    logger.info("Create custom Schema with invalid name: {}", testSchema.getName());
    SCIMResponse<Schema> scimResponse = resourceAwareSchemaRequest.createSchema(testSchema);

    assertAll("Verify Create custom Schema response", getResponseStatusAssertions(scimResponse, false, BAD_REQUEST));
  }

  @Test
  @DisplayName("Test Create custom schema with too long name and verify Http status code: 400")
  public void testCreateSchemaWithTooLongName400() {
    Schema testSchema = TestData.buildTestSchema(Schema.EXTENSION_SCHEMA_URN.concat("TestTestTestTestTestTestTestTestTestTest"));

    logger.info("Creating custom schema with long name: {}", testSchema.getName());
    SCIMResponse<Schema> scimResponse = resourceAwareSchemaRequest.createSchema(testSchema);

    assertAll("Verify Create custom Schema response", getResponseStatusAssertions(scimResponse, false, BAD_REQUEST));
  }

  @Test
  @DisplayName("Test Create custom schema with too long attribute name and verify Http status code: 400")
  public void testCreateSchemaWithTooLongAttributeName400() {
    Attribute testAttribute = TestData.setAllDefaultPropertiesToTestAttribute("ThisIsATooLongAttributeNameForTestPurposes", ExtensionFieldType.STRING)
        .build();
    Schema testSchema = TestData.buildCustomSchemaWithAttrs(Schema.EXTENSION_SCHEMA_URN.concat("CrSchemaHTTPResp" + TEST_IDENTIFIER),
        Collections.singletonList(testAttribute));

    logger.info("Creating custom schema with long attribute name: {}", testSchema.getName());
    SCIMResponse<Schema> scimResponse = resourceAwareSchemaRequest.createSchema(testSchema);

    assertAll("Verify Create custom Schema response", getResponseStatusAssertions(scimResponse, false, BAD_REQUEST));
  }

  @Test
  @DisplayName("Test Create custom schema with non alpha numeric attribute in name and verify Http status code: 400")
  public void testCreateSchemaWithNonAlpthaNumAttributeName400() {
    Attribute testAttribute = TestData.setAllDefaultPropertiesToTestAttribute("Non*Alphanum", ExtensionFieldType.STRING).build();
    Schema testSchema = TestData.buildCustomSchemaWithAttrs(Schema.EXTENSION_SCHEMA_URN.concat("CrSchemaHTTPResp" + TEST_IDENTIFIER),
        Collections.singletonList(testAttribute));

    logger.info("Create custom Schema with non alpha numeric attribute: {}", testSchema.getId());
    SCIMResponse<Schema> scimResponse = resourceAwareSchemaRequest.createSchema(testSchema);

    assertAll("Verify Create custom Schema response", getResponseStatusAssertions(scimResponse, false, BAD_REQUEST));
  }

  @Test
  @DisplayName("Test Create custom schema with more that 20 attributes and verify Http status code: 400")
  public void testCreateSchemaWithMoreThan20Attrs400() {
    List<Attribute> testAttributes = new ArrayList<>();
    for (int i = 0; i < 25; i++) {
      Attribute testAttr = TestData.setAllDefaultPropertiesToTestAttribute("Attribute" + i, ExtensionFieldType.STRING).build();
      testAttributes.add(testAttr);
    }

    Schema testSchema = TestData.buildCustomSchemaWithAttrs(Schema.EXTENSION_SCHEMA_URN.concat("CrSchemaHTTPResp" + TEST_IDENTIFIER), testAttributes);

    logger.info("Create custom Schema with more than 20 attributes: {}", testSchema.getId());
    SCIMResponse<Schema> scimResponse = resourceAwareSchemaRequest.createSchema(testSchema);

    assertAll("Verify Create custom Schema response", getResponseStatusAssertions(scimResponse, false, BAD_REQUEST));
  }

  private Schema createCustomSchemaAndVerifyResponse(String schemaName) {
    Schema testSchema = TestData.buildTestSchema(Schema.EXTENSION_SCHEMA_URN.concat(schemaName + TEST_IDENTIFIER));

    logger.info("Creating custom Schema: {}", testSchema.getId());
    SCIMResponse<Schema> scimResponse = resourceAwareSchemaRequest.createSchema(testSchema);

    assertAll("Verify Create custom Schema response", getResponseStatusAssertions(scimResponse, true, CREATED));
    return scimResponse.get();
  }
}
