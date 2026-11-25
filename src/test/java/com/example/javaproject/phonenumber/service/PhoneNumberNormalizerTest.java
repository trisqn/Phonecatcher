package com.example.javaproject.phonenumber.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhoneNumberNormalizerTest {

    private final PhoneNumberNormalizer normalizer = new PhoneNumberNormalizer();

    @Test
    void normalizesInternationalNumberToE164() {
        assertThat(normalizer.normalize("+372 5123 4567")).isEqualTo("+37251234567");
    }

    @Test
    void rejectsNumbersWithoutInternationalCountryCallingCode() {
        assertThatThrownBy(() -> normalizer.normalize("51234567"))
                .isInstanceOf(InvalidPhoneNumberException.class);
    }

    @Test
    void rejectsInvalidInternationalNumbers() {
        assertThatThrownBy(() -> normalizer.normalize("+999 123"))
                .isInstanceOf(InvalidPhoneNumberException.class);
    }
}
