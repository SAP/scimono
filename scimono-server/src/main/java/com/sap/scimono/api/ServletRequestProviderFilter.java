
package com.sap.scimono.api;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;

@Provider
@ServletRequestProvider
public class ServletRequestProviderFilter implements ContainerRequestFilter, ContainerResponseFilter {

  @Context
  HttpServletRequest servletRequest;

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    ContextResolver.bind(servletRequest);
  }

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
    removeServletRequestFromContext();
  }

  void removeServletRequestFromContext() {
    ContextResolver.remove();
  }
}
