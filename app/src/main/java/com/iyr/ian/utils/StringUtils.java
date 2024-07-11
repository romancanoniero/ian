package com.iyr.ian.utils;

import android.content.Context;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

public class StringUtils {

    public static boolean isDigitsOrLetters(String s) {
        if (s == null) // checks if the String is null {
            return false;

        int len = s.length();
        for (
                int i = 0;
                i < len; i++) {
            // checks whether the character is neither a letter nor a digit
            // if it is neither a letter nor a digit then it will return false
            if ((!Character.isLetterOrDigit(s.charAt(i)))) {
                return false;
            }
        }
        return true;
    }


    public static boolean isDigitsAndLetters(String s) {
        if (s == null) // checks if the String is null {
            return false;

        boolean hasDigits = false;
        boolean hasLetters = false;
        int len = s.length();
        for (
                int i = 0;
                i < len; i++) {
            // checks whether the character is neither a letter nor a digit
            // if it is neither a letter nor a digit then it will return false
            if ((!Character.isLetterOrDigit(s.charAt(i)))) {
                return false;
            }
            if (Character.isLetter(s.charAt(i)))
                hasLetters = true;

            if (Character.isDigit(s.charAt(i)))
                hasDigits = true;
        }
        return hasLetters && hasDigits ;
    }


    public static boolean areOnlyDigits(String s)
    {
        if (s == null) // checks if the String is null {
            return false;

        int len = s.length();
        for (
                int i = 0;
                i < len; i++) {
            if ((!Character.isDigit(s.charAt(i)))) {
                return false;
            }
        }
        return true;
    }

    public static boolean emailAtSymbolPresent(String s)
    {
        if (s == null) // checks if the String is null {
            return false;

        return s.contains("@");
    }


    public static String getStringResourceByName(Context context, String aString) {
        String packageName = context.getPackageName();
        int resId = context.getResources().getIdentifier(aString, "string", packageName);
        return context.getString(resId);
    }


    public static String getDurationString(int seconds) {
        Date date = new Date(seconds * 1000L);
        SimpleDateFormat formatter = new SimpleDateFormat(seconds >= 3600 ? "HH:mm:ss" : "mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        return formatter.format(date);
    }


    public static String generateRandomString() {
        String uuid = UUID.randomUUID().toString();
        return uuid;
    }


}

