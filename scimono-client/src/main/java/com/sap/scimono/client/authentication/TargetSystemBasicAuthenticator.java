package com.sap.scimono.client.authentication;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Base64;

public class TargetSystemBasicAuthenticator implements TargetSystemAuthenticator {
  private String userName;
  private String password;

  private TargetSystemBasicAuthenticator(String userName, String password) {
    this.userName = userName;
    this.password = password;
  }

  public static TargetSystemAuthenticator.Builder<TargetSystemBasicAuthenticator> create(String userName, String password) {
    return new Builder<TargetSystemBasicAuthenticator>() {
      @Override
      public TargetSystemBasicAuthenticator build() {
        return new TargetSystemBasicAuthenticator(userName, password);
      }
    };
  }

  @Override
  public String authenticate() {
    String headerValue = Base64.getEncoder().encodeToString(userName.concat(":").concat(password).getBytes(UTF_8));
    return "Basic " + headerValue;
  }
}
