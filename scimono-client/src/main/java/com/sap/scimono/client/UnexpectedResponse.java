package com.sap.scimono.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.scimono.api.helper.ObjectMapperFactory;
import com.sap.scimono.entity.ErrorResponse;

import java.io.IOException;
import java.util.Collection;

public class UnexpectedResponse {
  private static final ObjectMapper JSON_OBJECT_MAPPER = ObjectMapperFactory.createObjectMapper();
  private String responseBody;

  public UnexpectedResponse(String responseBody) {
    this.responseBody = responseBody;
  }

  public boolean isSCIMError() {
    try {
      JSON_OBJECT_MAPPER.readValue(responseBody, new TypeReference<Collection<ErrorResponse>>(){});
      return true;
    } catch (IOException ex) {
      return false;
    }
  }

  public Collection<ErrorResponse> asSCIMErrors() {
    try {
      return JSON_OBJECT_MAPPER.readValue(responseBody, new TypeReference<Collection<ErrorResponse>>(){});
    } catch (IOException ex) {
      throw new ClientResponseException("Cannot read response body", ex);
    }
  }

  public String asUnknownException() {
    return responseBody;
  }
}
