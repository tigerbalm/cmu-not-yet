package com.lge.notyet.driver.util;

import java.math.BigInteger;

public class NumberUtils {

    public static boolean isNegativeIntegerNumber(String intString) {

        try {
            BigInteger integer = new BigInteger(intString);
            return integer.signum() == -1;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public static boolean isPositiveIntegerNumber(String intString) {

        try {
            BigInteger integer = new BigInteger(intString);
            return integer.signum() == 1;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public static int toInt(String intString) {

        try {
            BigInteger integer = new BigInteger(intString);
            return integer.intValue();
        } catch (NumberFormatException nfe) {
            return Integer.MIN_VALUE;
        }
    }
}
