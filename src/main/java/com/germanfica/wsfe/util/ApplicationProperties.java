package com.germanfica.wsfe.util;

import java.util.Properties;
import java.io.InputStream;

public final class ApplicationProperties {

    // The shared Properties instance; loaded on first access
    private static Properties props;

    /**
     * Ensures that the properties file is loaded exactly once
     * in a thread-safe manner.
     */
    private static void ensureLoaded() {
        if (props == null) {
            synchronized (ApplicationProperties.class) {
                if (props == null) {
                    Properties p = new Properties();
                    try (InputStream in = Thread.currentThread()
                        .getContextClassLoader()
                        .getResourceAsStream("application.properties")) {
                        if (in != null) {
                            p.load(in);
                        }
                        // if the resource stream is null, leave properties empty
                    } catch (Exception e) {
                        System.err.println("Warning: Unable to load 'application.properties': " + e.getMessage());
                    }
                    props = p;
                }
            }
        }
    }

    private ApplicationProperties() {}

    /**
     * Retrieves the value of the specified property key.
     *
     * @param key the property name to look up
     * @return the property value, or null if not found
     */
    public static String getProperty(String key) {
        ensureLoaded();
        return props.getProperty(key);
    }

    /**
     * Retrieves the value of the specified property key,
     * returning the given default if the key is not present.
     *
     * @param key the property name to look up
     * @param defaultValue the value to return if the property is absent
     * @return the property value, or defaultValue if not found
     */
    public static String getProperty(String key, String defaultValue) {
        ensureLoaded();
        return props.getProperty(key, defaultValue);
    }
}
