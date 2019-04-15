
package com.sap.scim.entity.validation.patch;

import javax.validation.ValidationException;
import javax.ws.rs.core.Response;

import com.sap.scim.exception.SCIMException;
import com.sap.scim.exception.SCIMException.Type;

public class PatchValidationException extends ValidationException {

  /**
   *
   */
  private static final long serialVersionUID = 6357876157886901844L;
  private SCIMException scimException;

  public PatchValidationException(final Type scimType, final String message) {
    super(message);
    scimException = new SCIMException(scimType, message);
  }

  public PatchValidationException(final Type scimType, final String message, final Response.Status status) {
    super(message);
    this.scimException = new SCIMException(scimType, message, status);
  }

  public SCIMException toScimException() {
    return this.scimException;
  }

}
