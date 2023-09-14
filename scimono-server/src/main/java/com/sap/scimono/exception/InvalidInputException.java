
package com.sap.scimono.exception;

import jakarta.ws.rs.core.Response;

public class InvalidInputException extends SCIMException {
  private static final long serialVersionUID = -3458413407035304091L;

  public InvalidInputException(String errorMessage) {
    super(Type.INVALID_VALUE, errorMessage, Response.Status.BAD_REQUEST);
  }
}
