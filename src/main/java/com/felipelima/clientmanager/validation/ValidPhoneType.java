package com.felipelima.clientmanager.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Validates that the annotated string is a valid phone type.
 * Accepted values: RESIDENTIAL, COMMERCIAL, MOBILE (case-insensitive).
 */
@Documented
@Constraint(validatedBy = PhoneTypeValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhoneType {

    String message() default "Phone type must be one of: RESIDENTIAL, COMMERCIAL, MOBILE";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
