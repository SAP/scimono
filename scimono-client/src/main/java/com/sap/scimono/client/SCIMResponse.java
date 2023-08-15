package com.sap.scimono.client;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

public class SCIMResponse<T> {
  private final ResponseEntityReader<T> responseEntityReader;

  private final Response response;
  private final ActionResponseStatusConfig responseStatusConfig;

  private SCIMResponse(ResponseEntityReader<T> responseEntityReader, Response response, ActionResponseStatusConfig responseStatusConfig) {
    this.responseEntityReader = responseEntityReader;
    this.response = response;
    this.responseStatusConfig = responseStatusConfig;
  }

  public static <T> SCIMResponse<T> newInstance(Class<T> responseClass, Response response, ActionResponseStatusConfig responseStatusConfig) {
    return new SCIMResponse<>(createResponseEntityReader(responseClass), response, responseStatusConfig);
  }

  public static <T> SCIMResponse<T> newInstance(GenericType<T> genericType, Response response, ActionResponseStatusConfig responseStatusConfig) {
    return new SCIMResponse<>(createResponseEntityReader(genericType), response, responseStatusConfig);
  }

  public static SCIMResponse<Void> fromEmpty(Response response, ActionResponseStatusConfig responseStatusConfig) {
    return new SCIMResponse<>(rs -> null, response, responseStatusConfig);
  }

  public static <T> SCIMResponse<T> fromEntity(T entity, Response response, ActionResponseStatusConfig responseStatusConfig) {
    return new SCIMResponse<>(rs -> entity, response, responseStatusConfig);
  }

  public int getStatusCode() {
    return response.getStatus();
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

  private static <T> ResponseEntityReader<T> createResponseEntityReader(Class<T> responseClass) {
    return new ResponseEntityReader<T>() {
      private T responseEntity;

      @Override
      public T read(Response response) {
        if(responseEntity == null) {
          responseEntity = response.readEntity(responseClass);
        }

        return responseEntity;
      }
    };
  }

  private static <T> ResponseEntityReader<T> createResponseEntityReader(GenericType<T> genericType) {
    return new ResponseEntityReader<T>() {
      private T responseEntity;

      @Override
      public T read(Response response) {
        if(responseEntity == null) {
          responseEntity = response.readEntity(genericType);
        }

        return responseEntity;
      }
    };
  }
  @FunctionalInterface
  private interface ResponseEntityReader<T> {
    T read(Response response);
  }
}
