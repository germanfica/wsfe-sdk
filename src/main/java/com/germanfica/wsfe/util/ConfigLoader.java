package com.germanfica.wsfe.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    private static final Properties properties = new Properties();

    // WSAA (CMS)
    public static final String KEYSTORE_PATH;
    public static final String KEYSTORE_PASSWORD;
    public static final String KEYSTORE_SIGNER;
    public static final String DSTDN;
    public static final Long TICKET_TIME;
    public static final String SERVICE;

    // WSFE
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

        // WSAA - CMS
        KEYSTORE_PATH = properties.getProperty("wsaa.cms.keystore-path");
        KEYSTORE_PASSWORD = properties.getProperty("wsaa.cms.keystore-password");
        KEYSTORE_SIGNER = properties.getProperty("wsaa.cms.keystore-signer");
        DSTDN = properties.getProperty("wsaa.cms.dstdn", "cn=wsaahomo,o=afip,c=ar,serialNumber=CUIT 33693450239");
        TICKET_TIME = Long.parseLong(properties.getProperty("wsaa.cms.ticket-time", "36000"));
        SERVICE = properties.getProperty("wsaa.cms.service");

        // WSFE
        CUIT = Long.parseLong(properties.getProperty("wsfe.cuit"));
        TOKEN = properties.getProperty("wsfe.token");
        SIGN = properties.getProperty("wsfe.sign");
    }
}
