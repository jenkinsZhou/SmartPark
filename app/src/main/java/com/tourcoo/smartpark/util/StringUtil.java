package com.tourcoo.smartpark.util;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

/**
 * @author :JenkinsZhou
 * @description : 字符工具类
 * @company :途酷科技
 * @date 2020年11月19日14:36
 * @Email: 971613168@qq.com
 */
public class StringUtil {

    private static final String PHONE_START = "1";
    private static final int PHONE_LENGTH = 11;

    private static final int LENGTH_ID_CARD = 18;
    /**
     * 温度符号
     */
    public static final String SYMBOL_TEMP = "°";
    public static final String LINE_HORIZONTAL = "--";

    public static boolean isPhoneNumber(String number) {
        if (TextUtils.isEmpty(number)) {
            return false;
        }
        return number.length() == PHONE_LENGTH && number.startsWith(PHONE_START);
    }


    public static boolean isIdCard(String number) {
        if (TextUtils.isEmpty(number)) {
            return false;
        }
        return number.length() == LENGTH_ID_CARD;
    }

    public static String getNotNullValue(String number) {
        if (TextUtils.isEmpty(number)) {
            return "";
        }
        return number;
    }


    public static boolean isCarNumberNo(String carNumber) {
        if (TextUtils.isEmpty(carNumber)) {
            return false;
        }
        if (carNumber.length() < 7 || carNumber.length() > 8) {
            return false;
        }
        String str = carNumber.charAt(carNumber.length() - 1) + "";
        boolean checkLast = (!str.equalsIgnoreCase("I")) && (!str.equalsIgnoreCase("O")) && isLetter(str) || isNumeric(str);
        return isChineseChar(carNumber.charAt(0)) && isLetter(carNumber.charAt(1) + "") && checkLast;
    }

    public static boolean isChineseChar(char c) {
        try {
            return String.valueOf(c).getBytes("UTF-8").length > 1;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    /**
     * 判断是否是字母
     *
     * @param str 传入字符串
     * @return 是字母返回true，否则返回false
     */

    public static boolean isLetter(String str) {

        if (str == null) return false;

        return str.matches("[a-zA-Z]+");

    }

    public static String getNotNullValueLine(String value) {
        if (TextUtils.isEmpty(value)) {
            return "-";
        }
        return value;
    }

    public static long parseToLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

}
