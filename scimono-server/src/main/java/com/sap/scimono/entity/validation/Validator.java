
package com.sap.scimono.entity.validation;

public interface Validator<T> {
  void validate(T validatableObject);
}
