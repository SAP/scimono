
package com.sap.scimono.entity.schema.validation;

import static com.sap.scimono.entity.paging.PagedByIdentitySearchResult.PAGINATION_BY_ID_END_PARAM;
import static com.sap.scimono.entity.paging.PagedByIdentitySearchResult.PAGINATION_BY_ID_START_PARAM;
import static com.sap.scimono.entity.schema.validation.CustomInputValidator.Type.*;
import static com.sap.scimono.entity.schema.validation.CustomInputValidator.Type.RESOURCE_ID;
import static com.sap.scimono.helper.Strings.isNullOrEmpty;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

import com.sap.scimono.SCIMApplication;
import com.sap.scimono.api.API;
import com.sap.scimono.entity.validation.ValidationUtil;

import java.util.Map;

class StartIdValidator implements ConstraintValidator<ValidStartId, String> {
  private Map<CustomInputValidator.Type, CustomInputValidator> customValidators;
  private UriInfo uriInfo;

  public StartIdValidator(@Context Application application, @Context UriInfo uriInfo) {
    customValidators = SCIMApplication.from(application).getCustomConstraintValidators();
    this.uriInfo = uriInfo;
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
        || isValid(startId);
    // @formatter:on
  }

  private boolean isValid(Object resourceId) {
    if (uriInfo.getPath().startsWith(API.GROUPS) && customValidators.containsKey(GROUP_ID)) {
      return  customValidators.get(GROUP_ID).isValid(resourceId);
    } else if (uriInfo.getPath().startsWith(API.USERS) && customValidators.containsKey(USER_ID)) {
      return customValidators.get(USER_ID).isValid(resourceId);
    } else if (customValidators.containsKey(RESOURCE_ID)) {
      return customValidators.get(RESOURCE_ID).isValid(resourceId);
    }

    return true;
  }
}
