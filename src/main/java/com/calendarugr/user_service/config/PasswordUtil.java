package com.calendarugr.user_service.config;

import org.apache.commons.codec.digest.DigestUtils;

public class PasswordUtil {

    public static String encryptPassword(String password) {
        return DigestUtils.sha256Hex(password);
    }

    public static boolean matches(String rawPassword, String hashedPassword) {
        return DigestUtils.sha256Hex(rawPassword).equals(hashedPassword);
    }
}
