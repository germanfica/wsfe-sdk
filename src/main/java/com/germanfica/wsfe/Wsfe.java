package com.germanfica.wsfe;

/**
 *  WSFE (Web Services de Factura Electrónica).
 * */
public abstract class Wsfe {

    // Default timeouts
    public static final int DEFAULT_CONNECT_TIMEOUT = 30 * 1000;
    public static final int DEFAULT_READ_TIMEOUT = 80 * 1000;

    // API Bases
    public static final String TEST_API_BASE = "https://wswhomo.afip.gov.ar";
    public static final String PROD_API_BASE = "https://servicios1.afip.gov.ar";

    // Version
    public static final String VERSION = "1.0.0";

    private Wsfe() {} // Previene instanciación
}
