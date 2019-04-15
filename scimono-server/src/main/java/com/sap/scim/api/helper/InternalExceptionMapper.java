
package com.sap.scim.api.helper;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.scim.entity.ErrorResponse;

public class InternalExceptionMapper implements ExceptionMapper<Throwable> {
  private static final String EXPOSED_ERROR_MSG = "An internal error occurred.";
  private static final Logger logger = LoggerFactory.getLogger(InternalExceptionMapper.class);

  /**
   * Logs and hides the internal DB error and wraps it in a SCIM-compliant error response.
   */
  @Override
  public Response toResponse(final Throwable exception) {
    logger.error(EXPOSED_ERROR_MSG, exception);

    ErrorResponse scimError = new ErrorResponse(INTERNAL_SERVER_ERROR.getStatusCode(), null, EXPOSED_ERROR_MSG);
    return Response.serverError().entity(scimError).build();
  }

}
