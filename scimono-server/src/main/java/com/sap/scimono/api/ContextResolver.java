
package com.sap.scimono.api;

import jakarta.servlet.http.HttpServletRequest;

public class ContextResolver {

  private static final ThreadLocal<HttpServletRequest> threadLocal = new ThreadLocal<HttpServletRequest>();

  public static void bind(HttpServletRequest servletRequest) {
    threadLocal.set(servletRequest);
  }

  public static void remove() {
    threadLocal.remove();
  }

  public static <T> T resolve(ServletRequestResolver<T> requestRsolver) {
    HttpServletRequest httpServletRequest = threadLocal.get();
    return requestRsolver.process(httpServletRequest);
  }

}
