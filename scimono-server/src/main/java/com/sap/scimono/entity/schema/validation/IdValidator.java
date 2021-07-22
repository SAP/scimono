
package com.sap.scimono.entity.schema.validation;

import static com.sap.scimono.entity.schema.validation.CustomInputValidator.Type.GROUP_ID;
import static com.sap.scimono.entity.schema.validation.CustomInputValidator.Type.RESOURCE_ID;
import static com.sap.scimono.entity.schema.validation.CustomInputValidator.Type.USER_ID;

import java.util.Map;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.sap.scimono.SCIMApplication;
import com.sap.scimono.api.API;
import com.sap.scimono.entity.validation.ValidationUtil;

public class IdValidator implements ConstraintValidator<ValidId, Object> {
  private Map<CustomInputValidator.Type, CustomInputValidator> customValidators;
  private UriInfo uriInfo;

  public IdValidator(@Context Application application, @Context UriInfo uriInfo) {
    customValidators = SCIMApplication.from(application).getCustomConstraintValidators();
    this.uriInfo = uriInfo;

  }

  @Override
  public boolean isValid(Object resourceId, ConstraintValidatorContext constraintValidatorContext) {
    if (!isValid(resourceId)) {
      ValidationUtil.interpolateErrorMessage(constraintValidatorContext, generateViolationMessage(resourceId));
      return false;
    }

    return true;
  }

  public boolean isValid(Object resourceId) {
    if (uriInfo.getPath().startsWith(API.GROUPS) && customValidators.containsKey(GROUP_ID)) {
      return  customValidators.get(GROUP_ID).isValid(resourceId);
    } else if (uriInfo.getPath().startsWith(API.USERS) && customValidators.containsKey(USER_ID)) {
      return customValidators.get(USER_ID).isValid(resourceId);
    } else if (customValidators.containsKey(RESOURCE_ID)) {
      return customValidators.get(RESOURCE_ID).isValid(resourceId);
    }

    return true;
  }

  private String generateViolationMessage(Object resourceId) {
    return String.format("\"%s\" is not a valid identifier!", resourceId);
  }
}
