
package com.sap.scim.entity.schema.validation;

@FunctionalInterface
public interface CustomInputValidator {
  enum Type {
    RESOURCE_ID
  }

  boolean isValid(Object validatebleObject);
}
