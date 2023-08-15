
package com.sap.scimono.api;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

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
