
package com.sap.scimono.api.helper;

import javax.annotation.Priority;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.sap.scimono.api.API;
import com.sap.scimono.entity.ErrorResponse;

@Priority(1)
public class JsonMappingExceptionMapper implements ExceptionMapper<JsonMappingException> {

  @Override
  public Response toResponse(final JsonMappingException exception) {
    return Response.status(Response.Status.BAD_REQUEST).entity(toScimError(exception)).type(API.APPLICATION_JSON_SCIM).build();
  }

  public ErrorResponse toScimError(final JsonMappingException exception) {
    return new ErrorResponse(Response.Status.BAD_REQUEST.getStatusCode(), null, exception.getOriginalMessage());
  }

}
