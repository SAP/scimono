
package com.sap.scimono.api.helper;

import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.scimono.api.API;
import com.sap.scimono.entity.ErrorResponse;

public class InternalExceptionMapper implements ExceptionMapper<Throwable> {
  private static final String EXPOSED_ERROR_MSG = "An internal error occurred.";
  private static final Logger logger = LoggerFactory.getLogger(InternalExceptionMapper.class);

  /**
   * Logs and hides the internal DB error and wraps it in a SCIM-compliant error response.
   */
  @Override
  public Response toResponse(final Throwable exception) {
    logger.error(EXPOSED_ERROR_MSG, exception);
    return Response.serverError().entity(toScimError(exception)).type(API.APPLICATION_JSON_SCIM).build();
  }

  public ErrorResponse toScimError(final Throwable exception) {
    return new ErrorResponse(INTERNAL_SERVER_ERROR.getStatusCode(), null, EXPOSED_ERROR_MSG);
  }
}
