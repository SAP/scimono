
package com.sap.scimono.entity.schema.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SchemaIdValidator.class)
@Documented
public @interface ValidSchemaId {
  String message() default "Invalid schema id";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
