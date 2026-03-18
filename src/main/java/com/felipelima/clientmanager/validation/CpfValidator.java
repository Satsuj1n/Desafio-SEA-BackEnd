package com.felipelima.clientmanager.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates Brazilian CPF numbers.
 *
 * Validation rules:
 * 1. After removing non-digit characters, must have exactly 11 digits
 * 2. Cannot be a sequence of identical digits (e.g., 111.111.111-11)
 * 3. Check digits (last two) must be mathematically valid
 */
public class CpfValidator implements ConstraintValidator<CPF, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            // Let @NotBlank handle null/empty — this validator only checks format
            return true;
        }

        String digits = value.replaceAll("\\D", "");

        if (digits.length() != 11) {
            return false;
        }

        // Reject sequences of identical digits (e.g., 000.000.000-00)
        if (digits.chars().distinct().count() == 1) {
            return false;
        }

        // Validate first check digit
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += Character.getNumericValue(digits.charAt(i)) * (10 - i);
        }
        int firstCheck = 11 - (sum % 11);
        if (firstCheck >= 10) {
            firstCheck = 0;
        }
        if (Character.getNumericValue(digits.charAt(9)) != firstCheck) {
            return false;
        }

        // Validate second check digit
        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += Character.getNumericValue(digits.charAt(i)) * (11 - i);
        }
        int secondCheck = 11 - (sum % 11);
        if (secondCheck >= 10) {
            secondCheck = 0;
        }

        return Character.getNumericValue(digits.charAt(10)) == secondCheck;
    }
}
