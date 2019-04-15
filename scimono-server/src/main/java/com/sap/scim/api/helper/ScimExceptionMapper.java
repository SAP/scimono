
package com.sap.scim.api.helper;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import com.sap.scim.entity.ErrorResponse;
import com.sap.scim.exception.SCIMException;

public class ScimExceptionMapper implements ExceptionMapper<WebApplicationException> {

  /**
   * Wraps the application exception in a valid SCIM-compliant error response. Exception message is preserved. There's no need for further logging as
   * this is an application exception thrown by the top-most application layer.
   */
  @Override
  public Response toResponse(final WebApplicationException exception) {
    String scimType = null;
    if (exception instanceof SCIMException) {
      scimType = ((SCIMException) exception).getScimType();
    }

    ErrorResponse scimError = new ErrorResponse(exception.getResponse().getStatus(), scimType, exception.getMessage());
    return Response.fromResponse(exception.getResponse()).entity(scimError).build();
  }

}
