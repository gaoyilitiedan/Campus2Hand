package com.campus2hand.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PriceUtil {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private PriceUtil() {
    }

    public static long yuanToCent(BigDecimal yuan) {
        if (yuan == null) {
            return 0;
        }
        return yuan.multiply(BigDecimal.valueOf(100))
                .setScale(0, ROUNDING_MODE)
                .longValue();
    }

    public static long yuanToCent(double yuan) {
        return yuanToCent(BigDecimal.valueOf(yuan));
    }

    public static long yuanToCent(String yuan) {
        if (yuan == null || yuan.trim().isEmpty()) {
            return 0;
        }
        return yuanToCent(new BigDecimal(yuan));
    }

    public static BigDecimal centToYuan(long cent) {
        return BigDecimal.valueOf(cent)
                .divide(BigDecimal.valueOf(100), SCALE, ROUNDING_MODE);
    }

    public static double centToYuanDouble(long cent) {
        return centToYuan(cent).doubleValue();
    }

    public static String centToYuanString(long cent) {
        return centToYuan(cent).toPlainString();
    }

    public static long add(long cent1, long cent2) {
        return cent1 + cent2;
    }

    public static long subtract(long cent1, long cent2) {
        return cent1 - cent2;
    }

    public static long multiply(long cent, double multiplier) {
        return yuanToCent(centToYuan(cent).multiply(BigDecimal.valueOf(multiplier)));
    }

    public static long divide(long cent, double divisor) {
        if (divisor == 0) {
            throw new IllegalArgumentException("Divisor cannot be zero");
        }
        return yuanToCent(centToYuan(cent).divide(BigDecimal.valueOf(divisor), SCALE, ROUNDING_MODE));
    }

    public static long calculateDiscount(long cent, double discount) {
        if (discount < 0 || discount > 1) {
            throw new IllegalArgumentException("Discount must be between 0 and 1");
        }
        return multiply(cent, discount);
    }

    public static long calculateTax(long cent, double taxRate) {
        if (taxRate < 0) {
            throw new IllegalArgumentException("Tax rate cannot be negative");
        }
        return multiply(cent, taxRate);
    }

    public static boolean equals(long cent1, long cent2) {
        return cent1 == cent2;
    }

    public static boolean greaterThan(long cent1, long cent2) {
        return cent1 > cent2;
    }

    public static boolean greaterThanOrEqual(long cent1, long cent2) {
        return cent1 >= cent2;
    }

    public static boolean lessThan(long cent1, long cent2) {
        return cent1 < cent2;
    }

    public static boolean lessThanOrEqual(long cent1, long cent2) {
        return cent1 <= cent2;
    }

    public static boolean isPositive(long cent) {
        return cent > 0;
    }

    public static boolean isNonNegative(long cent) {
        return cent >= 0;
    }

    public static boolean isValidPrice(long cent) {
        return cent >= 0 && cent <= 99999999999L;
    }
}