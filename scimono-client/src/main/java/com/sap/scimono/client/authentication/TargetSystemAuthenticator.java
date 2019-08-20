package com.sap.scimono.client.authentication;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

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
