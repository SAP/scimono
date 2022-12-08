
package com.sap.scimono.entity.schema.validation;

import static com.sap.scimono.entity.schema.validation.CustomInputValidator.Type.EMAILS;

import java.util.Map;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.sap.scimono.SCIMApplication;
import com.sap.scimono.entity.validation.ValidationUtil;

public class EmailsValidator implements ConstraintValidator<ValidEmails, Object> {
  private Map<CustomInputValidator.Type, CustomInputValidator> customValidators;

  public EmailsValidator(@Context Application application, @Context UriInfo uriInfo) {
    customValidators = SCIMApplication.from(application).getCustomConstraintValidators();
  }

  @Override
  public boolean isValid(Object emails, ConstraintValidatorContext context) {
    CustomInputValidator emailsValidator = customValidators.get(EMAILS);
    if (emailsValidator != null && !emailsValidator.isValid(emails)) {
      ValidationUtil.interpolateErrorMessage(context, "One of the given emails inputs is not valid!");

      return false;
    }
    return true;
  }

}
