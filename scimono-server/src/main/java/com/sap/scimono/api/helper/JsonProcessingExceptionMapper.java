
package com.sap.scimono.api.helper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sap.scimono.entity.ErrorResponse;

public class JsonProcessingExceptionMapper implements ExceptionMapper<JsonProcessingException> {

  @Override
  public Response toResponse(final JsonProcessingException exception) {
    ErrorResponse scimError = new ErrorResponse(Response.Status.BAD_REQUEST.getStatusCode(), null, "One of the request inputs is not valid.");

    return Response.status(Response.Status.BAD_REQUEST).entity(scimError).build();
  }

}
