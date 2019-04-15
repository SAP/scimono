
package com.sap.scimono.entity.schema.validation;

import static com.sap.scimono.entity.paging.PagedByIdentitySearchResult.PAGINATION_BY_ID_END_PARAM;
import static com.sap.scimono.entity.paging.PagedByIdentitySearchResult.PAGINATION_BY_ID_START_PARAM;
import static com.sap.scimono.helper.Strings.isNullOrEmpty;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;

class StartIdValidator implements ConstraintValidator<ValidStartId, String> {
  private IdValidator resourceIdValidator;

  public StartIdValidator(@Context Application application) {
    this.resourceIdValidator = new IdValidator(application);
  }

  @Override
  public boolean isValid(String startId, ConstraintValidatorContext context) {
    if(isNullOrEmpty(startId) || isValidStartId(startId, context)) {
      return true;
    }

    ValidationUtil.interpolateErrorMessage(context, generateViolationMessage(startId));
    return false;
  }

  private String generateViolationMessage(Object resourceId) {
    return String.format("\"%s\" is not a valid startId!", resourceId);
  }

  private boolean isValidStartId(String startId, ConstraintValidatorContext context) {
    // @formatter:off
    return PAGINATION_BY_ID_START_PARAM.equalsIgnoreCase(startId)
        || PAGINATION_BY_ID_END_PARAM.equalsIgnoreCase(startId)
        || resourceIdValidator.isValid(startId, context);
    // @formatter:on
  }
}
