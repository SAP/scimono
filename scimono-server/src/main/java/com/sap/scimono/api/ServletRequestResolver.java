
package com.sap.scimono.api;

import javax.servlet.http.HttpServletRequest;

public interface ServletRequestResolver<T> {
  public T process(HttpServletRequest servletRequest);
}
