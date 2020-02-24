
package com.sap.scimono.api.helper;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ElementKind;
import javax.validation.Path;
import javax.validation.ValidationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import com.sap.scimono.api.API;
import com.sap.scimono.entity.ErrorResponse;
import com.sap.scimono.entity.validation.patch.PatchValidationException;
import com.sap.scimono.exception.SCIMException;

public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

  @Override
  public Response toResponse(final ValidationException exception) {
    return buildValidationResponse(exception, MediaType.valueOf(API.APPLICATION_JSON_SCIM));
  }

  private Response buildValidationResponse(final ValidationException exception, final MediaType mediaType) {
    if (exception instanceof ConstraintViolationException) {
      return buildConstraintValidationResponse((ConstraintViolationException) exception, mediaType);
    } else if (exception instanceof PatchValidationException) {
      return buildPatchValidationResponse((PatchValidationException) exception, mediaType);
    } else {
      return Response.serverError().entity(exception.getMessage()).type(mediaType).build();
    }
  }

  private Response buildPatchValidationResponse(final PatchValidationException pve, final MediaType mediaType) {
    Response.Status responseStatus = Response.Status.BAD_REQUEST;
    SCIMException scimException = pve.toScimException();

    ErrorResponse scimError = new ErrorResponse(responseStatus.getStatusCode(), scimException.getScimType(), scimException.getMessage());

    return Response.status(responseStatus).type(mediaType).entity(scimError).build();
  }

  private Response buildConstraintValidationResponse(final ConstraintViolationException cve, final MediaType mediaType) {
    Response.Status responseStatus = getResponseStatus(cve);

    return Response.status(responseStatus).type(mediaType)
        .entity(new GenericEntity<>(constraintViolationToErrorResponses(cve), new GenericType<List<ErrorResponse>>() {
        }.getType())).build();
  }

  private static Response.Status getResponseStatus(final ConstraintViolationException violation) {
    final Iterator<ConstraintViolation<?>> iterator = violation.getConstraintViolations().iterator();

    if (!iterator.hasNext()) {
      return Response.Status.BAD_REQUEST;
    }

    for (final Path.Node node : iterator.next().getPropertyPath()) {
      final ElementKind kind = node.getKind();

      if (ElementKind.RETURN_VALUE.equals(kind)) {
        return Response.Status.INTERNAL_SERVER_ERROR;
      }
    }

    return Response.Status.BAD_REQUEST;
  }

  private static List<ErrorResponse> constraintViolationToErrorResponses(final ConstraintViolationException violations) {
    // @formatter:off
    return violations.getConstraintViolations()
        .stream()
        .map(violation -> new ErrorResponse(Response.Status.BAD_REQUEST.getStatusCode(), null, violation.getMessage()))
        .collect(Collectors.toList());
   // @formatter:on
  }
}
