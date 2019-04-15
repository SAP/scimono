
package com.sap.scim.entity.schema.validation;

import com.sap.scim.SCIMApplication;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import java.util.Map;

import static com.sap.scim.entity.schema.validation.CustomInputValidator.Type.RESOURCE_ID;

public class IdValidator implements ConstraintValidator<ValidId, Object> {
  private Map<CustomInputValidator.Type, CustomInputValidator> customValidators;

  public IdValidator(@Context Application application) {
    this.customValidators = SCIMApplication.from(application).getCustomConstraintValidators();
  }

  @Override
  public boolean isValid(Object resourceId, ConstraintValidatorContext constraintValidatorContext) {
    if(!customValidators.containsKey(RESOURCE_ID) || customValidators.get(RESOURCE_ID).isValid(resourceId)) {
      return true;
    }

    ValidationUtil.interpolateErrorMessage(constraintValidatorContext, generateViolationMessage(resourceId));
    return false;
  }

  private String generateViolationMessage(Object resourceId) {
    return String.format("\"%s\" is not a valid identifier!", resourceId);
  }
}
