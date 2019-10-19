package com.kritsin.preferenceview;

import android.content.Context;

public class PreferenceUtils {


    protected static String getResourceEntryName(Context context, int id) {
        try {
            if (id == -1) {
                return "";
            } else {
                return context.getResources().getResourceEntryName(id);
            }
        } catch (Throwable e) {
            return "";
        }
    }

    public static String toSnakeCase(String text) {
        return text.replaceAll("([A-Z]+)", "\\_$1").toLowerCase();
    }

    public static String toCamelCase(String text) {
        StringBuffer sb = new StringBuffer();
        for (String s : text.split("_")) {
            sb.append(Character.toUpperCase(s.charAt(0)));
            sb.append(s.substring(1).toLowerCase());
        }
        return sb.toString();
    }
}
