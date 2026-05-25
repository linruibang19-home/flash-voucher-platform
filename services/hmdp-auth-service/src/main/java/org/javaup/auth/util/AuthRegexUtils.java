package org.javaup.auth.util;

import org.springframework.util.StringUtils;

public final class AuthRegexUtils {

    private static final String PHONE_REGEX = "^1([38][0-9]|4[579]|5[0-3,5-9]|6[6]|7[0135678]|9[89])\\d{8}$";

    private AuthRegexUtils() {
    }

    public static boolean isPhoneInvalid(String phone) {
        return !StringUtils.hasText(phone) || !phone.matches(PHONE_REGEX);
    }
}
