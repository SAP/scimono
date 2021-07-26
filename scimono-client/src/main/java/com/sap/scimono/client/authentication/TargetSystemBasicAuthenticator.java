package com.sap.scimono.client.authentication;

import jakarta.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;

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
    return "Basic " + DatatypeConverter.printBase64Binary(userName.concat(":").concat(password).getBytes(StandardCharsets.UTF_8));
  }
}
