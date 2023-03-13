package com.sap.scimono.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.HttpHeaders;
import java.util.Collection;
import java.util.LinkedList;

import static com.sap.scimono.api.API.APPLICATION_JSON_SCIM;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SCIMRequestTest {
  private static final String DEFAULT_URL = "http://localhost:7070/idds/scim/v2";

  @Test
  public void testDefaultConfiguration() {
    Collection<Executable> capturedAssertions = new LinkedList<>();
    // @formatter:off
    WebTarget webTarget = ClientBuilder.newClient()
        .register((ClientRequestFilter) clientRequestContext -> {
          capturedAssertions.add(() -> assertEquals(APPLICATION_JSON_SCIM, clientRequestContext.getHeaderString(CONTENT_TYPE)));
          capturedAssertions.add(() -> assertEquals(DEFAULT_URL, clientRequestContext.getUri().toString()));
        })
        .target(DEFAULT_URL);
    // @formatter:on

    SCIMRequest scimRequest = SCIMRequest.newBuilder().build();
    processAssertionsOnAllHttpMethods(scimRequest, webTarget, capturedAssertions);
  }

  @Test
  public void testWithCustomHeader() {
    String customHeaderName = HttpHeaders.ACCEPT;
    String customHeaderValue = "application/json";

    Collection<Executable> capturedAssertions = new LinkedList<>();
    // @formatter:off
    WebTarget webTarget = ClientBuilder.newClient()
        .register((ClientRequestFilter) clientRequestContext -> {
          capturedAssertions.add(() -> assertEquals(APPLICATION_JSON_SCIM, clientRequestContext.getHeaderString(CONTENT_TYPE)));
          capturedAssertions.add(() -> assertEquals(DEFAULT_URL, clientRequestContext.getUri().toString()));
          capturedAssertions.add(() -> assertEquals(customHeaderValue, clientRequestContext.getHeaderString(customHeaderName)));
        })
        .target(DEFAULT_URL);
    // @formatter:on

    SCIMRequest scimRequest = SCIMRequest.newBuilder().addHeader(customHeaderName, customHeaderValue).build();
    processAssertionsOnAllHttpMethods(scimRequest, webTarget, capturedAssertions);
  }

  private void processAssertionsOnAllHttpMethods(SCIMRequest scimRequest, WebTarget webTarget, Collection<Executable> capturedAssertions) {
    // @formatter:off
    assertAll(
        () -> assertThrows(ProcessingException.class, () -> scimRequest.post(webTarget, "{}")),
        () -> assertThrows(ProcessingException.class, () -> scimRequest.get(webTarget)),
        () -> assertThrows(ProcessingException.class, () -> scimRequest.put(webTarget, "{}")),
        () -> assertThrows(ProcessingException.class, () -> scimRequest.patch(webTarget, "{}"))
    );
    // @formatter:on
    assertAll(capturedAssertions);
  }
}
