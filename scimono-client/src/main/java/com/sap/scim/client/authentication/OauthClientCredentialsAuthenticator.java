package com.sap.scim.client.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.scim.api.helper.ObjectMapperFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

public class OauthClientCredentialsAuthenticator implements TargetSystemAuthenticator {
  private static final String OAUTH_GRANT_TYPE = "grant_type";
  private static final String CLIENT_CREDENTIALS_GRANT_VALUE = "client_credentials";
  private static final ObjectMapper JSON_OBJECT_MAPPER = ObjectMapperFactory.createObjectMapper();

  private final WebTarget oauthTarget;
  private final OauthCredentials oauthCredentials;

  private OauthTokenProperties tokenProperties;

  private OauthClientCredentialsAuthenticator(Builder builder) {
    this.oauthTarget = builder.httpClient.target(builder.oauthUrl).queryParam(OAUTH_GRANT_TYPE, CLIENT_CREDENTIALS_GRANT_VALUE);
    this.oauthCredentials = builder.oauthCredentials;
  }

  @Override
  public String authenticate() {
    if(!isActiveTokenAvailable()) {
      tokenProperties = retrieveOauthToken();
    }

    return tokenProperties.getTokenType() + " " + tokenProperties.getAccessToken();
  }

  private OauthTokenProperties retrieveOauthToken() {
    // @formatter:off
    Response response = oauthTarget.request()
        .header(AUTHORIZATION, new TargetSystemBasicAuthenticator(oauthCredentials.getClientId(), oauthCredentials.getSecret()).authenticate())
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
