
package com.sap.scimono.entity.schema.validation;

import javax.validation.ConstraintValidatorContext;

class ValidationUtil {

  public static void interpolateErrorMessage(ConstraintValidatorContext context, String errorMessage) {
    context.disableDefaultConstraintViolation();
    context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
  }

}
