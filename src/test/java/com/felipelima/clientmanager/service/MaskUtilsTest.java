package com.felipelima.clientmanager.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MaskUtilsTest {

    // ==========================================
    // removeNonDigits
    // ==========================================

    @Nested
    @DisplayName("removeNonDigits")
    class RemoveNonDigitsTests {

        @Test
        @DisplayName("Should remove dots and dash from CPF")
        void removeCpfMask() {
            assertEquals("12345678900", MaskUtils.removeNonDigits("123.456.789-00"));
        }

        @Test
        @DisplayName("Should remove dash from zip code")
        void removeZipCodeMask() {
            assertEquals("70040010", MaskUtils.removeNonDigits("70040-010"));
        }

        @Test
        @DisplayName("Should remove parentheses, space and dash from phone")
        void removePhoneMask() {
            assertEquals("61999998888", MaskUtils.removeNonDigits("(61) 99999-8888"));
        }

        @Test
        @DisplayName("Should return same string when no non-digits")
        void noMask() {
            assertEquals("12345678900", MaskUtils.removeNonDigits("12345678900"));
        }

        @Test
        @DisplayName("Should return null for null input")
        void nullInput() {
            assertNull(MaskUtils.removeNonDigits(null));
        }
    }

    // ==========================================
    // maskCpf
    // ==========================================

    @Nested
    @DisplayName("maskCpf")
    class MaskCpfTests {

        @Test
        @DisplayName("Should apply CPF mask correctly")
        void maskValid() {
            assertEquals("123.456.789-00", MaskUtils.maskCpf("12345678900"));
        }

        @Test
        @DisplayName("Should return original when length is not 11")
        void maskInvalidLength() {
            assertEquals("12345", MaskUtils.maskCpf("12345"));
        }

        @Test
        @DisplayName("Should return null for null input")
        void maskNull() {
            assertNull(MaskUtils.maskCpf(null));
        }
    }

    // ==========================================
    // maskZipCode
    // ==========================================

    @Nested
    @DisplayName("maskZipCode")
    class MaskZipCodeTests {

        @Test
        @DisplayName("Should apply zip code mask correctly")
        void maskValid() {
            assertEquals("70040-010", MaskUtils.maskZipCode("70040010"));
        }

        @Test
        @DisplayName("Should return original when length is not 8")
        void maskInvalidLength() {
            assertEquals("7004", MaskUtils.maskZipCode("7004"));
        }

        @Test
        @DisplayName("Should return null for null input")
        void maskNull() {
            assertNull(MaskUtils.maskZipCode(null));
        }
    }

    // ==========================================
    // maskPhone
    // ==========================================

    @Nested
    @DisplayName("maskPhone")
    class MaskPhoneTests {

        @Test
        @DisplayName("Should mask 11-digit mobile phone")
        void maskMobile() {
            assertEquals("(61) 99999-8888", MaskUtils.maskPhone("61999998888"));
        }

        @Test
        @DisplayName("Should mask 10-digit landline phone")
        void maskLandline() {
            assertEquals("(61) 3333-4444", MaskUtils.maskPhone("6133334444"));
        }

        @Test
        @DisplayName("Should return original for other lengths")
        void maskOtherLength() {
            assertEquals("12345", MaskUtils.maskPhone("12345"));
        }

        @Test
        @DisplayName("Should return null for null input")
        void maskNull() {
            assertNull(MaskUtils.maskPhone(null));
        }
    }
}
