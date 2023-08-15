package com.sap.scimono.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.sap.scimono.api.helper.ObjectMapperFactory;
import com.sap.scimono.entity.Meta;
import com.sap.scimono.entity.config.AuthenticationSchemeSetting;
import com.sap.scimono.entity.config.BulkSetting;
import com.sap.scimono.entity.config.FilterSetting;
import com.sap.scimono.entity.config.ServiceProviderConfig;
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
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServiceProviderConfigRequestTest {
  private static final String DEFAULT_URL = "http://localhost:7070";
  private static final ObjectMapper JSON_OBJECT_MAPPER = ObjectMapperFactory.createObjectMapper();
  private static final String SCIM_JSON_RESOURCES_ROOT = "/crud/serviceProviderConfig/";
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
  public void readServiceProviderConfiguration() throws IOException {
    JsonNode jsonResponse = readResponseServiceProviderConfigAsJsonNodeFromFile("read_service_provider_config.json");

    wireMockServer.stubFor(get(urlEqualTo("/ServiceProviderConfig")).willReturn(configureMockedResponse(OK, jsonResponse)));

    // @formatter:off
    ServiceProviderConfig responseServiceProviderConfig = SCIMClientService.builder(DEFAULT_URL)
        .build()
        .buildServiceProviderConfigurationRequest()
        .readServiceProviderConfiguration()
        .get();

    assertAll(getAllServiceProviderConfigAttributeAssertions(responseServiceProviderConfig));
    // @formatter:on
  }

  private JsonNode readResponseServiceProviderConfigAsJsonNodeFromFile(String fileName) throws IOException {
    // @formatter:off
    return JSON_OBJECT_MAPPER
        .readValue(this.getClass().getResourceAsStream(SCIM_JSON_RESOURCES_ROOT + fileName), ObjectNode.class)
        .get(RESPONSE_ATTR);
    // @formatter:on
  }

  private List<Executable> getAllServiceProviderConfigAttributeAssertions(ServiceProviderConfig serviceProviderConfig) {
    List<Executable> assertions = new ArrayList<>();

    assertions.add(() -> assertNotNull(serviceProviderConfig.getSchemas()));
    assertions.add(() -> assertTrue(serviceProviderConfig.getSchemas().contains(ServiceProviderConfig.SCHEMA)));
    assertions.add(() -> assertNotNull(serviceProviderConfig.getMeta()));
    assertions.add(() -> {
          Meta meta = serviceProviderConfig.getMeta();
          assertAll(
              () -> assertNotNull(meta.getCreated()),
              () -> assertNotNull(meta.getLastModified()),
              () -> assertNotNull(meta.getVersion()),
              () -> assertNotNull(meta.getLocation()),
              () -> assertNotNull(meta.getResourceType())
          );
        }
    );

    assertions.add(() -> assertNotNull(serviceProviderConfig.getDocumentationUri()));
    assertions.add(() -> assertNotNull(serviceProviderConfig.getPatch()));
    assertions.add(() -> assertNotNull(serviceProviderConfig.getPatch().isSupported()));
    assertions.add(() -> assertNotNull(serviceProviderConfig.getBulk()));
    assertions.add(() -> {
      BulkSetting bulkSetting = serviceProviderConfig.getBulk();
      assertAll(
          () -> assertNotNull(bulkSetting.isSupported()),
          () -> assertNotNull(bulkSetting.getMaxOperations()),
          () -> assertNotNull(bulkSetting.getMaxPayloadSize())
      );
    });
    assertions.add(() -> assertNotNull(serviceProviderConfig.getFilter()));
    assertions.add(() -> {
      FilterSetting filterSetting = serviceProviderConfig.getFilter();
      assertAll(
          () -> assertNotNull(filterSetting.isSupported()),
          () -> assertNotNull(filterSetting.getMaxResults())
      );
    });
    assertions.add(() -> assertNotNull(serviceProviderConfig.getChangePassword()));
    assertions.add(() -> assertNotNull(serviceProviderConfig.getChangePassword().isSupported()));
    assertions.add(() -> assertNotNull(serviceProviderConfig.getSort()));
    assertions.add(() -> assertNotNull(serviceProviderConfig.getSort().isSupported()));
    assertions.add(() -> assertNotNull(serviceProviderConfig.getEtag()));
    assertions.add(() -> assertNotNull(serviceProviderConfig.getEtag().isSupported()));
    assertions.add(() -> assertNotNull(serviceProviderConfig.getAuthenticationSchemes()));
    assertions.add(() -> {
      List<AuthenticationSchemeSetting> authenticationSchemes = serviceProviderConfig.getAuthenticationSchemes();
      assertFalse(authenticationSchemes.isEmpty());
      authenticationSchemes.forEach(authenticationScheme -> assertAll(
          () -> assertNotNull(authenticationScheme.getType()),
          () -> assertNotNull(authenticationScheme.getName()),
          () -> assertNotNull(authenticationScheme.getDescription()),
          () -> assertNotNull(authenticationScheme.getSpecUri()),
          () -> assertNotNull(authenticationScheme.getDocumentationUri())
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
