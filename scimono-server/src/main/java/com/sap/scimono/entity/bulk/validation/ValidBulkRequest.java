package com.sap.scimono.entity.bulk.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;


@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BulkRequestValidator.class)
@Documented
public @interface ValidBulkRequest {
  String message() default "Invalid bulk request";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
