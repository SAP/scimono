
package com.sap.scimono.api.helper;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import com.sap.scimono.api.API;
import com.sap.scimono.entity.ErrorResponse;
import com.sap.scimono.entity.validation.patch.PatchValidationException;
import com.sap.scimono.exception.SCIMException;

public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

  @Override
  public Response toResponse(final ValidationException exception) {
    return buildValidationResponse(exception, MediaType.valueOf(API.APPLICATION_JSON_SCIM));
  }

  public ErrorResponse toScimError(final ValidationException exception) {
    if (exception instanceof ConstraintViolationException) {
      return getFirstConstraintValidationScimError((ConstraintViolationException) exception);
    } else if (exception instanceof PatchValidationException) {
      return buildPatchValidationScimError((PatchValidationException) exception);
    } else {
      return new InternalExceptionMapper().toScimError(exception);
    }
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
    ErrorResponse scimError = buildPatchValidationScimError(pve);

    return Response.status(responseStatus).type(mediaType).entity(scimError).build();
  }

  private ErrorResponse buildPatchValidationScimError(final PatchValidationException pve) {
    Response.Status responseStatus = Response.Status.BAD_REQUEST;
    SCIMException scimException = pve.toScimException();

    return new ErrorResponse(responseStatus.getStatusCode(), scimException.getScimType(), scimException.getMessage());
  }

  private Response buildConstraintValidationResponse(final ConstraintViolationException cve, final MediaType mediaType) {
    Response.Status responseStatus = getResponseStatus(cve);

    return Response.status(responseStatus)
        .type(mediaType)
        .entity(new GenericEntity<>(constraintViolationToErrorResponses(cve), new GenericType<List<ErrorResponse>>() {
        }.getType()))
        .build();
  }

  private ErrorResponse getFirstConstraintValidationScimError(final ConstraintViolationException cve) {
    List<ErrorResponse> errorResponses = constraintViolationToErrorResponses(cve);
    return errorResponses.isEmpty() ? null : errorResponses.get(0);
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
