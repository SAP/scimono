
package com.sap.scimono.api.helper;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import com.sap.scimono.api.API;
import com.sap.scimono.entity.ErrorResponse;
import com.sap.scimono.exception.SCIMException;

public class ScimExceptionMapper implements ExceptionMapper<WebApplicationException> {

  /**
   * Wraps the application exception in a valid SCIM-compliant error response. Exception message is preserved. There's no need for further logging as
   * this is an application exception thrown by the top-most application layer.
   */
  @Override
  public Response toResponse(final WebApplicationException exception) {
    return Response.fromResponse(exception.getResponse()).entity(toScimError(exception)).type(API.APPLICATION_JSON_SCIM).build();
  }

  public ErrorResponse toScimError(final WebApplicationException exception) {
    String scimType = null;
    if (exception instanceof SCIMException) {
      scimType = ((SCIMException) exception).getScimType();
    }

    return new ErrorResponse(exception.getResponse().getStatus(), scimType, exception.getMessage());
  }
}
