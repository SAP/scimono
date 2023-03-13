
package com.sap.scimono.entity.validation;

import jakarta.ws.rs.core.Response;

import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.exception.SCIMException;

public class AttributeImmutableValueValidator implements Validator<Attribute> {

  @Override
  public void validate(final Attribute attribute) {
    if (attribute.getMutability().equals("immutable")) {
      throw new SCIMException(SCIMException.Type.MUTABILITY,
          String.format("Attribute with name '%s' cannot be modified because it is immutable", attribute.getName()), Response.Status.BAD_REQUEST);
    }
  }
}
