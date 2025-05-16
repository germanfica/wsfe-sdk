package com.germanfica.wsfe.util;

import java.io.*;
import java.util.Properties;
//TODO 1: Reemplazar este ConfigManager por SystemSetting, SystemSettingUtils y SystemSettingUtilsTestBackdoor. Ya que permite, System.getenv() y System.getProperty() <3
// https://github.com/aws/aws-sdk-java-v2/blob/master/utils/src/main/java/software/amazon/awssdk/utils/SystemSetting.java
// https://github.com/aws/aws-sdk-java-v2/blob/master/utils/src/main/java/software/amazon/awssdk/utils/internal/SystemSettingUtils.java
// https://github.com/aws/aws-sdk-java-v2/blob/master/utils/src/main/java/software/amazon/awssdk/utils/internal/SystemSettingUtilsTestBackdoor.java

//TODO 2: Agregar el Provider Chain <3 (DefaultCredentialsProvider, algo similar a ProfileCredentialsProvider...
// https://github.com/aws/aws-sdk-java-v2/blob/2.31.30/core/auth/src/main/java/software/amazon/awssdk/auth/credentials/DefaultCredentialsProvider.java
// https://github.com/aws/aws-sdk-java-v2/blob/2.31.30/core/auth/src/main/java/software/amazon/awssdk/auth/credentials/ProfileCredentialsProvider.java
// https://github.com/aws/aws-sdk-java-v2/blob/2.31.30/core/profiles/src/main/java/software/amazon/awssdk/profiles/ProfileFile.java // No hace falta. No es necesario tener perfiles, De hecho, debería ser un "único perfil"... como el SDK de stripe con su apiKey

//TODO 3: la idea es permitir ingresar un signedCmsBase64 y también firmar un cms desde dado el certificado como archivo.
// Nota: recordar que
// - signedCmsBase64: tiene un vencimiento de 2 años (dado que este, se genera a partir del certificado generado en ARCA). Tiene que ser generado manualmente (no lo porporciona ARCA).
// - Token y sign (TA, Ticket de Acceso): tiene un periodo de vencimiento más corto de 12 horas desde la fecha de emisión (generado por WsfeClient y usado por WsfeClient)
// Docs: https://www.afip.gob.ar/ws/WSAA/Especificacion_Tecnica_WSAA_1.2.2.pdf

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
