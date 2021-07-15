package com.sap.scimono.client.authentication;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;

public class TargetSystemAccessTokenAuthenticator implements TargetSystemAuthenticator {
  private final String accessToken;

  private TargetSystemAccessTokenAuthenticator(final String accessToken) {
    this.accessToken = accessToken;
  }

  public static TargetSystemAuthenticator.Builder<TargetSystemAccessTokenAuthenticator> create(String accessToken) {
    return new Builder<TargetSystemAccessTokenAuthenticator>() {
      @Override
      public TargetSystemAccessTokenAuthenticator build() {
        return new TargetSystemAccessTokenAuthenticator(accessToken);
      }
    };
  }

  @Override
  public String authenticate() {
    return "Bearer " + DatatypeConverter.printBase64Binary(accessToken.getBytes(StandardCharsets.UTF_8));
  }
}
