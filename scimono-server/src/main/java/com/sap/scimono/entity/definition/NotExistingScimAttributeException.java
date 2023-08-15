
package com.sap.scimono.entity.definition;

import javax.ws.rs.WebApplicationException;

public class NotExistingScimAttributeException extends WebApplicationException {
  public NotExistingScimAttributeException(String attributeName) {
    super(String.format("Not recognized SCIM attribute: '%s'", attributeName));
  }
}
