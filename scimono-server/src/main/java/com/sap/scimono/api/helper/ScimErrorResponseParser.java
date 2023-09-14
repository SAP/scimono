package com.sap.scimono.api.helper;

import jakarta.validation.ValidationException;
import jakarta.ws.rs.WebApplicationException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sap.scimono.entity.ErrorResponse;

public class ScimErrorResponseParser {
  private static final JsonMappingExceptionMapper JSON_MAPPING_EXCEPTION_MAPPER = new JsonMappingExceptionMapper();
  private static final JsonParseExceptionMapper JSON_PARSE_EXCEPTION_MAPPER = new JsonParseExceptionMapper();
  private static final JsonProcessingExceptionMapper JSON_PROCESSING_EXCEPTION_MAPPER = new JsonProcessingExceptionMapper();
  private static final ScimExceptionMapper SCIM_EXCEPTION_MAPPER = new ScimExceptionMapper();
  private static final ValidationExceptionMapper VALIDATION_EXCEPTION_MAPPER = new ValidationExceptionMapper();
  private static final InternalExceptionMapper INTERNAL_EXCEPTION_MAPPER = new InternalExceptionMapper();

  private ScimErrorResponseParser() {

  }

  public static ErrorResponse parseException(Throwable t) {
    if (t instanceof JsonMappingException) {
      return JSON_MAPPING_EXCEPTION_MAPPER.toScimError((JsonMappingException) t);
    }

    if (t instanceof JsonParseException) {
      return JSON_PARSE_EXCEPTION_MAPPER.toScimError((JsonParseException) t);
    }

    if (t instanceof JsonProcessingException) {
      return JSON_PROCESSING_EXCEPTION_MAPPER.toScimError((JsonProcessingException) t);
    }

    if (t instanceof ValidationException) {
      return VALIDATION_EXCEPTION_MAPPER.toScimError((ValidationException) t);
    }

    if (t instanceof WebApplicationException) {
      return SCIM_EXCEPTION_MAPPER.toScimError((WebApplicationException) t);
    }

    return INTERNAL_EXCEPTION_MAPPER.toScimError(t);
  }
}
