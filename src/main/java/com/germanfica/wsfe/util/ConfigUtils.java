package com.germanfica.wsfe.util;

public class ConfigUtils {

    // --- ENV VARS ---

    public static String getenv(String key) {
        return System.getenv(key);
    }

    public static String getenv(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }

    public static Long getenv(String key, Long defaultValue) {
        try {
            String value = System.getenv(key);
            return value != null ? Long.parseLong(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static Integer getenv(String key, Integer defaultValue) {
        try {
            String value = System.getenv(key);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static Boolean getenv(String key, Boolean defaultValue) {
        String value = System.getenv(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    // --- SYSTEM PROPERTIES ---

    public static String getProperty(String key) {
        return System.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return System.getProperty(key, defaultValue);
    }

    public static Long getProperty(String key, Long defaultValue) {
        try {
            String value = System.getProperty(key);
            return value != null ? Long.parseLong(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static Integer getProperty(String key, Integer defaultValue) {
        try {
            String value = System.getProperty(key);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static Boolean getProperty(String key, Boolean defaultValue) {
        String value = System.getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }
}
