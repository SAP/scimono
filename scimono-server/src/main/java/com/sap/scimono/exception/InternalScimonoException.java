package com.sap.scimono.exception;

public class InternalScimonoException extends RuntimeException {
  public InternalScimonoException(String message) {
    super(message);
  }

  public InternalScimonoException(String message, Throwable cause) {
    super(message, cause);
  }
}
