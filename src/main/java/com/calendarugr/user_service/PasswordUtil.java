package com.calendarugr.user_service;

import org.apache.commons.codec.digest.DigestUtils;

public class PasswordUtil {

    public static String encryptPassword(String password) {
        return DigestUtils.sha256Hex(password);
    }
}
