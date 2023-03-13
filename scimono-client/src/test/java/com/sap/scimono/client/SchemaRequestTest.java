package com.sap.scimono.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.sap.scimono.api.helper.ObjectMapperFactory;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.entity.schema.Schema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.sap.scimono.api.API.APPLICATION_JSON_SCIM;
import static com.sap.scimono.client.util.ResponseBodyAttributeExistenceAssertions.getCommonResourceAssertions;
import static com.sap.scimono.client.util.ResponseBodyAttributeExistenceAssertions.getPagedByIndexResponseAssertions;
import static java.util.UUID.randomUUID;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jakarta.ws.rs.core.Response.Status.CREATED;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SchemaRequestTest {
  private static final String DEFAULT_URL = "http://localhost:7070";
  private static final ObjectMapper JSON_OBJECT_MAPPER = ObjectMapperFactory.createObjectMapper();
  private static final String SCIM_JSON_RESOURCES_ROOT = "/crud/schema/";
  private static final String REQUEST_ATTR = "request";
  private static final String RESPONSE_ATTR = "response";

  private WireMockServer wireMockServer;

  @BeforeEach
  public void setup() {
    wireMockServer = new WireMockServer(7070);
    wireMockServer.start();
  }

  @AfterEach
  public void closeResources() {
    wireMockServer.stop();
  }

  @Test
  public void readAllSchemas() throws IOException {
    JsonNode jsonResponse = readResponseSchemasAsJsonNodeFromFile("read_schemas_index_paging.json");
    wireMockServer.stubFor(get(urlEqualTo("/Schemas")).willReturn(configureMockedResponse(OK, jsonResponse)));

    // @formatter:off
    PagedByIndexSearchResult<Schema> responseSchema = SCIMClientService.builder(DEFAULT_URL)
        .build()
        .buildSchemaRequest()
        .readAllSchemas()
        .get();

    assertAll(
        () -> assertAll(getPagedByIndexResponseAssertions(responseSchema)),
        () -> {
          Schema schema = responseSchema.getResources().stream().findAny().orElseThrow(RuntimeException::new);
          assertAll("Common resourceAttributes", getCommonResourceAssertions(schema, Schema.SCHEMA));
          assertNotNull(schema.getName());
          assertNotNull(schema.getAttributes());
          assertFalse(schema.getAttributes().isEmpty());
        }
      );
    // @formatter:on
  }

  @Test
  public void readSingleSchema() throws IOException {
    String sampleSchemaId = randomUUID().toString();
    JsonNode jsonResponse = readResponseSchemasAsJsonNodeFromFile("read_schema.json");

    wireMockServer.stubFor(get(urlEqualTo("/Schemas/" + sampleSchemaId)).willReturn(configureMockedResponse(OK, jsonResponse)));

    // @formatter:off
    Schema responseSchema = SCIMClientService.builder(DEFAULT_URL)
        .build()
        .buildSchemaRequest()
        .readSingleSchema(sampleSchemaId)
        .get();

    assertAll(getAllSchemaAttributeAssertions(responseSchema));
    // @formatter:on
  }

  @Test
  public void createSchema() throws IOException {
    String schemaJsonDataFileName = "create_schema.json";
    Schema schema = readRequestObjectFromFile(schemaJsonDataFileName, Schema.class);
    JsonNode jsonResponse = readResponseSchemasAsJsonNodeFromFile(schemaJsonDataFileName);

    wireMockServer.stubFor(post(urlEqualTo("/Schemas")).willReturn(configureMockedResponse(CREATED, jsonResponse)));

    // @formatter:off
    Schema responseSchema = SCIMClientService.builder(DEFAULT_URL)
        .build()
        .buildSchemaRequest()
        .createSchema(schema)
        .get();

    assertAll(getAllSchemaAttributeAssertions(responseSchema));
    // @formatter:on
  }

  private <T> T readRequestObjectFromFile(String fileName, Class<T> pojoClass) throws IOException {
    // @formatter:off
    JsonNode schemaJsonNode = JSON_OBJECT_MAPPER
        .readValue(this.getClass().getResourceAsStream(SCIM_JSON_RESOURCES_ROOT + fileName), ObjectNode.class)
        .get(REQUEST_ATTR);
    // @formatter:on

    return JSON_OBJECT_MAPPER.treeToValue(schemaJsonNode, pojoClass);
  }

  private JsonNode readResponseSchemasAsJsonNodeFromFile(String fileName) throws IOException {
    // @formatter:off
    return JSON_OBJECT_MAPPER
        .readValue(this.getClass().getResourceAsStream(SCIM_JSON_RESOURCES_ROOT + fileName), ObjectNode.class)
        .get(RESPONSE_ATTR);
    // @formatter:on
  }

  private List<Executable> getAllSchemaAttributeAssertions(Schema schema) {
    List<Executable> assertions = new ArrayList<>(getCommonResourceAssertions(schema, Schema.SCHEMA));

    assertions.add(() -> assertNotNull(schema.getName()));
    assertions.add(() -> assertNotNull(schema.getDescription()));
    assertions.add(() -> assertNotNull(schema.getAttributes()));
    assertions.add(() -> {
      List<Attribute> attributes = schema.getAttributes();
      assertFalse(attributes.isEmpty());

      attributes.forEach(attribute ->
          assertAll(
            () -> assertNotNull(attribute.getType()),
            () -> assertNotNull(attribute.getName()),
            () -> assertNotNull(attribute.isMultiValued()),
            () -> assertNotNull(attribute.getDescription()),
            () -> assertNotNull(attribute.isRequired()),
            () -> assertNotNull(attribute.isCaseExact()),
            () -> assertNotNull(attribute.getMutability()),
            () -> assertNotNull(attribute.getReturned()),
            () -> assertNotNull(attribute.getUniqueness()),
            () -> assertNotNull(attribute.getCanonicalValues()),
            () -> assertNotNull(attribute.getReferenceTypes())
      ));
    });

    return assertions;
  }

  private ResponseDefinitionBuilder configureMockedResponse(Response.Status responseStatus, Object body) throws JsonProcessingException {
    // @formatter:off
    return aResponse()
        .withStatus(responseStatus.getStatusCode())
        .withHeader(CONTENT_TYPE, APPLICATION_JSON_SCIM)
        .withBody(JSON_OBJECT_MAPPER.writeValueAsString(body));
    // @formatter:on
  }
}
