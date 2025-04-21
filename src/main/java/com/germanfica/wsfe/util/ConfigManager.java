package com.germanfica.wsfe.util;

import java.io.*;
import java.util.Properties;

/**
 * Maneja la lectura y escritura del archivo ~/.wsfe/config.properties,
 * solamente permitido en entornos de desarrollo.
 */
public class ConfigManager {
    private static final String CONFIG_PATH = System.getProperty("user.home") + "/.wsfe/config.properties";
    private static final Properties properties = new Properties();
    private static boolean loaded = false;

    public static void load() {
        if (loaded) return;

        try (FileInputStream fis = new FileInputStream(CONFIG_PATH)) {
            properties.load(fis);
            loaded = true;

        } catch (FileNotFoundException e) {
            // Primera vez: se crea vacío
            System.out.println("Archivo de configuración no encontrado, se usará uno nuevo.");
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar config.properties", e);
        }
    }

    public static String get(String key) {
        load();
        return properties.getProperty(key);
    }

    public static void set(String key, String value) {
        load();
        properties.setProperty(key, value);
        save();
    }

    public static void save() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_PATH)) {
            properties.store(fos, "WSFE config - ¡NO USAR EN PRODUCCIÓN!");
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar config.properties", e);
        }
    }
}
