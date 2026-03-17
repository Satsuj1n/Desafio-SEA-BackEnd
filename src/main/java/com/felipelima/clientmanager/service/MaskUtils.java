package com.felipelima.clientmanager.service;

/**
 * Utility class for applying and removing masks.
 */
public final class MaskUtils {

    private MaskUtils() {
        // Prevent instantiation — utility class with static methods only
    }

    /**
     * Removes all non-digit characters from a string.
     * "123.456.789-00" → "12345678900"
     * "(61) 99999-8888" → "61999998888"
     */
    public static String removeNonDigits(String value) {
        if (value == null) return null;
        return value.replaceAll("\\D", "");
    }

    /**
     * Applies CPF mask: "12345678900" → "123.456.789-00"
     */
    public static String maskCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) return cpf;
        return String.format("%s.%s.%s-%s",
                cpf.substring(0, 3),
                cpf.substring(3, 6),
                cpf.substring(6, 9),
                cpf.substring(9, 11));
    }

    /**
     * Applies CEP mask: "70000000" → "70000-000"
     */
    public static String maskZipCode(String zipCode) {
        if (zipCode == null || zipCode.length() != 8) return zipCode;
        return String.format("%s-%s",
                zipCode.substring(0, 5),
                zipCode.substring(5, 8));
    }

    /**
     * Applies phone mask based on length:
     * 10 digits (landline): "6133334444" → "(61) 3333-4444"
     * 11 digits (mobile):   "61999998888" → "(61) 99999-8888"
     */
    public static String maskPhone(String phone) {
        if (phone == null) return null;

        if (phone.length() == 11) {
            return String.format("(%s) %s-%s",
                    phone.substring(0, 2),
                    phone.substring(2, 7),
                    phone.substring(7, 11));
        }

        if (phone.length() == 10) {
            return String.format("(%s) %s-%s",
                    phone.substring(0, 2),
                    phone.substring(2, 6),
                    phone.substring(6, 10));
        }

        return phone;
    }
}