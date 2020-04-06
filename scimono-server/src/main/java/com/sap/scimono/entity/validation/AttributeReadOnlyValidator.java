
package com.sap.scimono.entity.validation;

import javax.ws.rs.core.Response;

import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.exception.SCIMException;

public class AttributeReadOnlyValidator implements Validator<Attribute> {

  @Override
  public void validate(final Attribute attribute) {
    if (attribute.getMutability().equals("readOnly")) {
      throw new SCIMException(SCIMException.Type.MUTABILITY,
          String.format("Attribute with name '%s' is readOnly and cannot be modified", attribute.getName()), Response.Status.BAD_REQUEST);
    }
  }
}
