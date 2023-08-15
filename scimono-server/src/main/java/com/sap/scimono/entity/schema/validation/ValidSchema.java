
package com.sap.scimono.entity.schema.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SchemaValidator.class)
@Documented
public @interface ValidSchema {
  String message() default "Invalid schema definition";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
