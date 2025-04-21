package com.germanfica.wsfe.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    private static final Properties properties = new Properties();

    public static final String KEYSTORE_PATH;
    public static final String KEYSTORE_PASSWORD;
    public static final String KEYSTORE_SIGNER;
    public static final String DSTDN;
    public static final Long TICKET_TIME;
    public static final String SERVICE;
    public static final String TOKEN;
    public static final String SIGN;
    /** Tu CUIT real/homo. */
    public static final Long CUIT;

    static {
        try (FileInputStream input = new FileInputStream("src/main/resources/application.properties")) {
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar el archivo de configuración", e);
        }

        KEYSTORE_PATH = properties.getProperty("keystore");
        KEYSTORE_PASSWORD = properties.getProperty("keystore-password");
        KEYSTORE_SIGNER = properties.getProperty("keystore-signer");
        DSTDN = properties.getProperty("dstdn", "cn=wsaahomo,o=afip,c=ar,serialNumber=CUIT 33693450239");
        TICKET_TIME = Long.parseLong(properties.getProperty("TicketTime", "36000"));
        SERVICE = properties.getProperty("service");
        CUIT = Long.parseLong(properties.getProperty("cuit"));
        TOKEN = properties.getProperty("token");
        SIGN = properties.getProperty("sign");
    }
}
