
package com.sap.scimono.api.helper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sap.scimono.api.API;
import com.sap.scimono.entity.ErrorResponse;

public class JsonProcessingExceptionMapper implements ExceptionMapper<JsonProcessingException> {

  private static final String JSON_PROCESSING_EXCEPTION_MSG = "Error parsing request body.";

  @Override
  public Response toResponse(final JsonProcessingException exception) {
    ErrorResponse scimError = new ErrorResponse(Response.Status.BAD_REQUEST.getStatusCode(), null, JSON_PROCESSING_EXCEPTION_MSG);

    return Response.status(Response.Status.BAD_REQUEST).entity(scimError).type(API.APPLICATION_JSON_SCIM).build();
  }

}
