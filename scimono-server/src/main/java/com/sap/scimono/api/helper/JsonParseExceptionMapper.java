
package com.sap.scimono.api.helper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import com.fasterxml.jackson.core.JsonParseException;
import com.sap.scimono.api.API;
import com.sap.scimono.entity.ErrorResponse;

public class JsonParseExceptionMapper implements ExceptionMapper<JsonParseException> {

  private static final String JSON_PARSE_EXCEPTION_MSG = "Error parsing request body.";

  @Override
  public Response toResponse(final JsonParseException exception) {
    ErrorResponse scimError = new ErrorResponse(Response.Status.BAD_REQUEST.getStatusCode(), null, JSON_PARSE_EXCEPTION_MSG);

    return Response.status(Response.Status.BAD_REQUEST).entity(scimError).type(API.APPLICATION_JSON_SCIM).build();
  }
}
