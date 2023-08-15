package com.sap.scimono.client.authentication;

import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.scimono.api.helper.ObjectMapperFactory;

public class OauthClientCredentialsAuthenticator implements TargetSystemAuthenticator {
  private static final String CLIENT_CREDENTIALS_GRANT_VALUE = "client_credentials";
  private static final ObjectMapper JSON_OBJECT_MAPPER = ObjectMapperFactory.createObjectMapper();

  private final WebTarget oauthTarget;
  private final OauthCredentials oauthCredentials;

  private OauthTokenProperties tokenProperties;

  private OauthClientCredentialsAuthenticator(Builder builder) {
    this.oauthTarget = builder.httpClient.target(builder.oauthUrl).queryParam(OAUTH_GRANT_TYPE, CLIENT_CREDENTIALS_GRANT_VALUE);
    this.oauthCredentials = builder.oauthCredentials;
  }

  OauthClientCredentialsAuthenticator(OauthCredentials oauthCredentials, WebTarget oauthTarget) {
    this.oauthTarget = oauthTarget;
    this.oauthCredentials = oauthCredentials;
  }

  @Override
  public String authenticate() {
    if(!isActiveTokenAvailable()) {
      tokenProperties = retrieveOauthToken();
    }

    String tokenType = tokenProperties.getTokenType();
    if ("Bearer".equalsIgnoreCase(tokenType)) {
      tokenType = "Bearer";
    }
    
    return tokenType + " " + tokenProperties.getAccessToken();
  }

  private OauthTokenProperties retrieveOauthToken() {
    // @formatter:off
    Response response = oauthTarget.request()
        .header(AUTHORIZATION, TargetSystemBasicAuthenticator.create(oauthCredentials.getClientId(), oauthCredentials.getSecret()).build().authenticate())
        .post(null);
    // @formatter:on

    if(response.getStatus() >= 300) {
      throw new FailedTargetSystemAuthentication(response.readEntity(String.class));
    }

    try {
      return JSON_OBJECT_MAPPER.readValue(response.readEntity(String.class), OauthTokenProperties.class);
    } catch (IOException e) {
      throw new FailedTargetSystemAuthentication("Cannot read response body", e);
    }
  }

  private boolean isActiveTokenAvailable() {
    return tokenProperties != null && Instant.now().isBefore(tokenProperties.getTokenExpirationTime());
  }

  public static class Builder extends TargetSystemAuthenticator.Builder<OauthClientCredentialsAuthenticator> {
    private Client httpClient;
    private URI oauthUrl;
    private OauthCredentials oauthCredentials;

    public Builder setHttpClient(Client httpClient) {
      this.httpClient = httpClient;
      return this;
    }

    public Builder setOauthUrl(URI oauthUrl) {
      this.oauthUrl = oauthUrl;
      return this;
    }

    public Builder setOauthCredentials(OauthCredentials oauthCredentials) {
      this.oauthCredentials = oauthCredentials;
      return this;
    }

    public Client getHttpClient() {
      return httpClient;
    }

    @Override
    public OauthClientCredentialsAuthenticator build() {
      return new OauthClientCredentialsAuthenticator(this);
    }
  }
}
