package com.sap.scimono.client;

public class ClientResponseException extends RuntimeException {
  public ClientResponseException(String message) {
    super(message);
  }

  public ClientResponseException(String message, Throwable cause) {
    super(message, cause);
  }
}
