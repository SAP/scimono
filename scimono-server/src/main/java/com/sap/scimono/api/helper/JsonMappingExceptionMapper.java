
package com.sap.scimono.api.helper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.sap.scimono.api.API;
import com.sap.scimono.entity.ErrorResponse;

public class JsonMappingExceptionMapper implements ExceptionMapper<JsonMappingException> {

  private static final String INVALID_FIELDS_MSG = "Invalid fields.";

  @Override
  public Response toResponse(final JsonMappingException exception) {
    ErrorResponse scimError = new ErrorResponse(Response.Status.BAD_REQUEST.getStatusCode(), null, INVALID_FIELDS_MSG);

    return Response.status(Response.Status.BAD_REQUEST).entity(scimError).type(API.APPLICATION_JSON_SCIM).build();
  }

}
