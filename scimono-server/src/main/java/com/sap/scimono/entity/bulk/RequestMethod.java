package com.sap.scimono.entity.bulk;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import jakarta.ws.rs.core.Response;

public enum RequestMethod {
  POST, PUT, PATCH, DELETE;

  private static final Map<RequestMethod, Response.Status> SUCCESSFUL_STATUS_CODES = new EnumMap<>(RequestMethod.class);

  static {
    SUCCESSFUL_STATUS_CODES.put(POST, Response.Status.CREATED);
    SUCCESSFUL_STATUS_CODES.put(PUT, Response.Status.OK);
    SUCCESSFUL_STATUS_CODES.put(PATCH, Response.Status.NO_CONTENT);
    SUCCESSFUL_STATUS_CODES.put(DELETE, Response.Status.NO_CONTENT);
  }
  public static RequestMethod from(String methodName) {
    return Arrays.stream(values()).filter(method -> method.toString().equalsIgnoreCase(methodName)).findAny().orElse(null);
  }

  public Response.Status getSuccessfulStatus() {
    return SUCCESSFUL_STATUS_CODES.get(this);
  }
}
