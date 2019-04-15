package com.sap.scimono.client.authentication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OauthCredentials {

  @JsonProperty("client_id")
  private String clientId;

  @JsonProperty("secret")
  private String secret;

  public OauthCredentials(String clientId, String secret) {
    this.clientId = clientId;
    this.secret = secret;
  }

  public String getClientId() {
    return clientId;
  }

  public String getSecret() {
    return secret;
  }
}
