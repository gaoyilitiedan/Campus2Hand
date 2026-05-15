package com.campus2hand.common.util;

import java.util.regex.Pattern;

public final class MaskUtil {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$");

    private MaskUtil() {
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    public static String maskEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            return email;
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return email;
        }
        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (username.length() <= 2) {
            return username.charAt(0) + "*" + domain;
        }
        return username.charAt(0) + "****" + username.charAt(username.length() - 1) + domain;
    }

    public static String maskStudentId(String studentId) {
        if (studentId == null || studentId.length() < 4) {
            return studentId;
        }
        int len = studentId.length();
        return studentId.substring(0, 2) + "*".repeat(Math.max(0, len - 4)) + studentId.substring(len - 2);
    }

    public static String maskIdCard(String idCard) {
        if (idCard == null || !ID_CARD_PATTERN.matcher(idCard).matches()) {
            return idCard;
        }
        return idCard.substring(0, 4) + "**********" + idCard.substring(14);
    }

    public static String maskName(String name) {
        if (name == null || name.length() <= 1) {
            return name;
        }
        return name.charAt(0) + "*".repeat(name.length() - 1);
    }

    public static String maskBankCard(String bankCard) {
        if (bankCard == null || bankCard.length() < 8) {
            return bankCard;
        }
        int len = bankCard.length();
        return bankCard.substring(0, 4) + "****".repeat(4) + bankCard.substring(len - 4);
    }
}