
package com.sap.scimono.entity.schema.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailsValidator.class)
@Documented
public @interface ValidEmails {
  String message() default "Invalid emails definition";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
