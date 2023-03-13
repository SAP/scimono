
package com.sap.scimono.api;

import jakarta.servlet.http.HttpServletRequest;

public interface ServletRequestResolver<T> {
  public T process(HttpServletRequest servletRequest);
}
