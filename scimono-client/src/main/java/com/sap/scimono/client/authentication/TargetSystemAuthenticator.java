package com.sap.scimono.client.authentication;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;

public interface TargetSystemAuthenticator extends ClientRequestFilter {
  String OAUTH_GRANT_TYPE = "grant_type";

  String authenticate();

  @Override
  default void filter(ClientRequestContext clientRequestContext) {
    MultivaluedMap<String, Object> headers = clientRequestContext.getHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, authenticate());
  }

  abstract class Builder<T extends TargetSystemAuthenticator> {
    public abstract T build();
  }
}
