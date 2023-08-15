package com.sap.scimono.client.authentication;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.UriBuilder;

public class OauthAuthenticatorFactory {

  private OauthAuthenticatorFactory() {
  }

  public static OauthClientCredentialsAuthenticator.Builder clientCredentialsGrantAuthenticator(Client httpClient, String oauthUrl, OauthCredentials oauthCredentials) {
    // @formatter:off
    return new OauthClientCredentialsAuthenticator.Builder()
        .setHttpClient(httpClient)
        .setOauthCredentials(oauthCredentials)
        .setOauthUrl(UriBuilder.fromPath(oauthUrl).build());
    // @formatter:on
  }

  public static OauthClientCredentialsAuthenticator.Builder clientCredentialsGrantAuthenticator(String oauthUrl, OauthCredentials oauthCredentials) {
    // @formatter:off
    return new OauthClientCredentialsAuthenticator.Builder()
        .setOauthCredentials(oauthCredentials)
        .setOauthUrl(UriBuilder.fromPath(oauthUrl).build());
    // @formatter:on
  }

}
