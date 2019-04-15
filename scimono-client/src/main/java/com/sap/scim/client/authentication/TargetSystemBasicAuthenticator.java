package com.sap.scim.client.authentication;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;

public class TargetSystemBasicAuthenticator implements TargetSystemAuthenticator {
  private String userName;
  private String password;

  public TargetSystemBasicAuthenticator(String userName, String password) {
    this.userName = userName;
    this.password = password;
  }

  @Override
  public String authenticate() {
    return "Basic " + DatatypeConverter.printBase64Binary(userName.concat(":").concat(password).getBytes(StandardCharsets.UTF_8));
  }
}
