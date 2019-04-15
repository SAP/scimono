
package com.sap.scim.api;

import javax.servlet.http.HttpServletRequest;

public interface ServletRequestResolver<T> {
  public T process(HttpServletRequest servletRequest);
}
