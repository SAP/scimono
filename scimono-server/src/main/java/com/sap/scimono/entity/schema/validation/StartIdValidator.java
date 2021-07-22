
package com.sap.scimono.entity.schema.validation;

import static com.sap.scimono.entity.paging.PagedByIdentitySearchResult.PAGINATION_BY_ID_END_PARAM;
import static com.sap.scimono.entity.paging.PagedByIdentitySearchResult.PAGINATION_BY_ID_START_PARAM;
import static com.sap.scimono.helper.Strings.isNullOrEmpty;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.sap.scimono.entity.validation.ValidationUtil;

class StartIdValidator implements ConstraintValidator<ValidStartId, String> {
  private IdValidator resourceIdValidator;

  public StartIdValidator(@Context Application application, @Context UriInfo uriInfo) {
    resourceIdValidator = new IdValidator(application, uriInfo);
  }

  @Override
  public boolean isValid(String startId, ConstraintValidatorContext context) {
    if (isNullOrEmpty(startId) || isValidStartId(startId)) {
      return true;
    }

    ValidationUtil.interpolateErrorMessage(context, generateViolationMessage(startId));
    return false;
  }

  private String generateViolationMessage(Object resourceId) {
    return String.format("\"%s\" is not a valid startId!", resourceId);
  }

  private boolean isValidStartId(String startId) {
    // @formatter:off
    return PAGINATION_BY_ID_START_PARAM.equalsIgnoreCase(startId)
        || PAGINATION_BY_ID_END_PARAM.equalsIgnoreCase(startId)
        || resourceIdValidator.isValid(startId);
    // @formatter:on
  }
}
