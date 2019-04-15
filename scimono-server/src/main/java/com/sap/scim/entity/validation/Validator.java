
package com.sap.scim.entity.validation;

public interface Validator<T> {
  void validate(T validatableObject);
}
