package com.sap.scimono.client.util;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.sap.scimono.api.API.APPLICATION_JSON_SCIM;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;

import jakarta.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.sap.scimono.api.helper.ObjectMapperFactory;

public class WireMockUtils {
  private static final ObjectMapper JSON_OBJECT_MAPPER = ObjectMapperFactory.createObjectMapper();

  private WireMockUtils() {

  }

  public static ResponseDefinitionBuilder configureMockedResponse(Response.Status responseStatus, Object body) throws JsonProcessingException {
    return emptyResponse(responseStatus).withBody(JSON_OBJECT_MAPPER.writeValueAsString(body));
  }

  public static ResponseDefinitionBuilder emptyResponse(Response.Status responseStatus) {
    return aResponse().withStatus(responseStatus.getStatusCode()).withHeader(CONTENT_TYPE, APPLICATION_JSON_SCIM);
  }
}
