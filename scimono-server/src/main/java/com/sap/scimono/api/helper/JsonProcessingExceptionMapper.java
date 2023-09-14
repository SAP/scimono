
package com.sap.scimono.api.helper;

import javax.annotation.Priority;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sap.scimono.api.API;
import com.sap.scimono.entity.ErrorResponse;

@Priority(1)
public class JsonProcessingExceptionMapper implements ExceptionMapper<JsonProcessingException> {

  private static final String JSON_PROCESSING_EXCEPTION_MSG = "Error parsing request body.";

  @Override
  public Response toResponse(final JsonProcessingException exception) {
    return Response.status(Response.Status.BAD_REQUEST).entity(toScimError(exception)).type(API.APPLICATION_JSON_SCIM).build();
  }

  public ErrorResponse toScimError(final JsonProcessingException exception) {
    return new ErrorResponse(Response.Status.BAD_REQUEST.getStatusCode(), null, JSON_PROCESSING_EXCEPTION_MSG);
  }
}
