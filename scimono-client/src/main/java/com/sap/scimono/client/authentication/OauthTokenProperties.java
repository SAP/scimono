package com.sap.scimono.client.authentication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

import static java.time.temporal.ChronoUnit.SECONDS;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OauthTokenProperties {
  private String accessToken;
  private String tokenType;
  private int expiresIn;
  private String scope;
  private Instant tokenReceiveTime;

  // @formatter:off
  @JsonCreator
  protected OauthTokenProperties(
      @JsonProperty(value = "access_token", required = true) String accessToken,
      @JsonProperty(value = "token_type", defaultValue = "Bearer") String tokenType,
      @JsonProperty("expires_in") int expiresIn,
      @JsonProperty("scope") String scope
  ) {
    this.accessToken = accessToken;
    this.tokenType = tokenType;
    this.expiresIn = expiresIn;
    this.scope = scope;
    this.tokenReceiveTime = Instant.now();
  }
  // @formatter:on

  public String getAccessToken() {
    return accessToken;
  }

  public String getTokenType() {
    return tokenType;
  }

  public int getExpiresIn() {
    return expiresIn;
  }

  public String getScope() {
    return scope;
  }

  public Instant getTokenExpirationTime() {
    return tokenReceiveTime.plus(expiresIn, SECONDS).minus(10, SECONDS);
  }
}
