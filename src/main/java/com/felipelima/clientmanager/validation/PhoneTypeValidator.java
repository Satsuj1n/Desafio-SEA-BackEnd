package com.felipelima.clientmanager.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.felipelima.clientmanager.entity.enums.PhoneTypeEnum;

/**
 * Validates that a string matches one of the accepted PhoneTypeEnum values.
 * Comparison is case-insensitive to provide a better user experience.
 */
public class PhoneTypeValidator implements ConstraintValidator<ValidPhoneType, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            // Let @NotNull handle null — this validator only checks the value
            return true;
        }

        try {
            PhoneTypeEnum.valueOf(value.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
