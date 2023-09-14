
package com.sap.scimono.entity.validation;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidatorContext;

public class ValidationUtil {
  private static final Pattern EXPRESSION_LANGUAGE_CHARACTERS = Pattern.compile("([${}])");

  public static void interpolateErrorMessage(ConstraintValidatorContext context, String errorMessage) {
    context.disableDefaultConstraintViolation();
    context.buildConstraintViolationWithTemplate(escapeExpressionLanguage(errorMessage)).addConstraintViolation();
  }

  private static String escapeExpressionLanguage(String text) {
    return EXPRESSION_LANGUAGE_CHARACTERS.matcher(text).replaceAll( "\\\\$1" );
  }

}
