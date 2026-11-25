package com.example.javaproject.phonenumber.service;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import org.springframework.stereotype.Component;

/**
 * Validates international phone numbers and normalizes them to E.164.
 */
@Component
public class PhoneNumberNormalizer {

    private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    /**
     * Creates a normalizer using the shared libphonenumber metadata.
     */
    public PhoneNumberNormalizer() {
    }

    /**
     * Validates and converts an international phone number to E.164 format.
     *
     * @param input user-supplied phone number
     * @return normalized number including the country calling code
     * @throws InvalidPhoneNumberException if the input is not a valid international number
     */
    public String normalize(String input) {
        if (input == null || !input.strip().startsWith("+")) {
            throw new InvalidPhoneNumberException();
        }

        try {
            var parsedNumber = phoneNumberUtil.parse(input, null);
            if (!phoneNumberUtil.isValidNumber(parsedNumber)) {
                throw new InvalidPhoneNumberException();
            }
            return phoneNumberUtil.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException exception) {
            throw new InvalidPhoneNumberException();
        }
    }
}
