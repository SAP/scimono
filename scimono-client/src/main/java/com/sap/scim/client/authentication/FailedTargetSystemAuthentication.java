package com.sap.scim.client.authentication;

public class FailedTargetSystemAuthentication extends RuntimeException {
  FailedTargetSystemAuthentication(String message) {
    super(message);
  }

  FailedTargetSystemAuthentication(String message, Throwable cause) {
    super(message, cause);
  }
}
