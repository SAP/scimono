package com.sap.scimono.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.sap.scimono.api.helper.ObjectMapperFactory;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.schema.ResourceType;
import com.sap.scimono.entity.schema.SchemaExtension;
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
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.sap.scimono.api.API.APPLICATION_JSON_SCIM;
import static com.sap.scimono.client.util.ResponseBodyAttributeExistenceAssertions.getCommonResourceAssertions;
import static com.sap.scimono.client.util.ResponseBodyAttributeExistenceAssertions.getPagedByIndexResponseAssertions;
import static java.util.UUID.randomUUID;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ResourceTypeRequestTest {
  private static final String DEFAULT_URL = "http://localhost:7070";
  private static final ObjectMapper JSON_OBJECT_MAPPER = ObjectMapperFactory.createObjectMapper();
  private static final String SCIM_JSON_RESOURCES_ROOT = "/crud/resourceType/";
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
  public void readAllResourceTypes() throws IOException {
    JsonNode jsonResponse = readResponseResourceTypesAsJsonNodeFromFile("read_resource_types_index_paging.json");
    wireMockServer.stubFor(get(urlEqualTo("/ResourceTypes")).willReturn(configureMockedResponse(OK, jsonResponse)));

    // @formatter:off
    PagedByIndexSearchResult<ResourceType> responseResourceType = SCIMClientService.builder(DEFAULT_URL)
        .build()
        .buildResourceTypeRequest()
        .readAllResourceTypes()
        .get();

    assertAll(
        () -> assertAll(getPagedByIndexResponseAssertions(responseResourceType)),
        () -> {
          ResourceType resourceType = responseResourceType.getResources().stream().findAny().orElseThrow(RuntimeException::new);
          assertAll("Common resourceAttributes", getCommonResourceAssertions(resourceType, ResourceType.SCHEMA));
          assertNotNull(resourceType.getName());
          assertNotNull(resourceType.getSchema());
          assertNotNull(resourceType.getEndpoint());
        }
      );
    // @formatter:on
  }

  @Test
  public void readSingleResourceType() throws IOException {
    String sampleResourceTypeId = randomUUID().toString();
    JsonNode jsonResponse = readResponseResourceTypesAsJsonNodeFromFile("read_resource_type.json");

    wireMockServer.stubFor(get(urlEqualTo("/ResourceTypes/" + sampleResourceTypeId)).willReturn(configureMockedResponse(OK, jsonResponse)));

    // @formatter:off
    ResourceType responseResourceType = SCIMClientService.builder(DEFAULT_URL)
        .build()
        .buildResourceTypeRequest()
        .readSingleResourceType(sampleResourceTypeId)
        .get();

    assertAll(getAllResourceTypeAttributeAssertions(responseResourceType));
    // @formatter:on
  }

  private JsonNode readResponseResourceTypesAsJsonNodeFromFile(String fileName) throws IOException {
    // @formatter:off
    return JSON_OBJECT_MAPPER
        .readValue(this.getClass().getResourceAsStream(SCIM_JSON_RESOURCES_ROOT + fileName), ObjectNode.class)
        .get(RESPONSE_ATTR);
    // @formatter:on
  }

  private List<Executable> getAllResourceTypeAttributeAssertions(ResourceType resourceType) {
    List<Executable> assertions = new ArrayList<>(getCommonResourceAssertions(resourceType, ResourceType.SCHEMA));

    assertions.add(() -> assertNotNull(resourceType.getName()));
    assertions.add(() -> assertNotNull(resourceType.getSchema()));
    assertions.add(() -> assertNotNull(resourceType.getEndpoint()));
    assertions.add(() -> assertNotNull(resourceType.getDescription()));
    assertions.add(() -> assertNotNull(resourceType.getSchemaExtensions()));
    assertions.add(() -> {
      List<SchemaExtension> schemaExtensions = resourceType.getSchemaExtensions();
      assertFalse(schemaExtensions.isEmpty());

      schemaExtensions.forEach(schemaExtension ->
          assertAll(
            () -> assertNotNull(schemaExtension.getSchema()),
            () -> assertNotNull(schemaExtension.isRequired())
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
