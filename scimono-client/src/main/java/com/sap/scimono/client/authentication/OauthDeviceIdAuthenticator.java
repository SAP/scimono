package com.sap.scimono.client.authentication;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import java.net.URI;

public class OauthDeviceIdAuthenticator implements TargetSystemAuthenticator {
  private OauthClientCredentialsAuthenticator clientCredentialsAuthenticator;

  private OauthDeviceIdAuthenticator(Builder builder) {
    WebTarget oauthTarget = builder.httpClient.target(builder.oauthUrl).queryParam(OAUTH_GRANT_TYPE, "device_id");
    this.clientCredentialsAuthenticator = new OauthClientCredentialsAuthenticator(builder.oauthCredentials, oauthTarget);
  }

  @Override
  public String authenticate() {
    return clientCredentialsAuthenticator.authenticate();
  }

  public static class Builder extends TargetSystemAuthenticator.Builder<OauthDeviceIdAuthenticator>  {
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
    public OauthDeviceIdAuthenticator build() {
      return new OauthDeviceIdAuthenticator(this);
    }
  }
}
