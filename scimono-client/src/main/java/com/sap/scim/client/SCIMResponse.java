package com.sap.scim.client;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

public class SCIMResponse<T> {
  private ResponseEntityReader<T> responseEntityReader;

  private Response response;
  private ActionResponseStatusConfig responseStatusConfig;

  private SCIMResponse(ResponseEntityReader<T> responseEntityReader, Response response, ActionResponseStatusConfig responseStatusConfig) {
    this.responseEntityReader = responseEntityReader;
    this.response = response;
    this.responseStatusConfig = responseStatusConfig;
  }

  public static <T> SCIMResponse<T> newInstance(Class<T> responseClass, Response response, ActionResponseStatusConfig responseStatusConfig) {
    return new SCIMResponse<>(rs -> rs.readEntity(responseClass), response, responseStatusConfig);
  }

  public static <T> SCIMResponse<T> newInstance(GenericType<T> genericType, Response response, ActionResponseStatusConfig responseStatusConfig) {
    return new SCIMResponse<>(rs -> rs.readEntity(genericType), response, responseStatusConfig);
  }

  public static SCIMResponse<Void> fromEmpty(Response response, ActionResponseStatusConfig responseStatusConfig) {
    return new SCIMResponse<>(rs -> null, response, responseStatusConfig);
  }

  public static <T> SCIMResponse<T> fromEntity(T entity, Response response, ActionResponseStatusConfig responseStatusConfig) {
    return new SCIMResponse<>(rs -> entity, response, responseStatusConfig);
  }

  public T get() {
    if(!isSuccess()) {
      throw new WebApplicationException("Request does not finished successfully");
    }

    return responseEntityReader.read(response);
  }

  public UnexpectedResponse getError() {
    if(isSuccess()) {
      throw new WebApplicationException("Request finished successfully");
    }

    return new UnexpectedResponse(response.readEntity(String.class));
  }

  public boolean isSuccess() {
    return responseStatusConfig.isSuccess(response.getStatus());
  }

  @FunctionalInterface
  private interface ResponseEntityReader<T> {
    T read(Response response);
  }
}
