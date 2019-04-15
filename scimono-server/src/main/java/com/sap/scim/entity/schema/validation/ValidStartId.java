
package com.sap.scim.entity.schema.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StartIdValidator.class)
@Documented
public @interface ValidStartId {
  String message() default "Invalid StartId parameter value!";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
