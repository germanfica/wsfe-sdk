package com.germanfica.wsfe;

/**
 * WSAA (Web Services de Autenticación y Autorización): El WSAA valida que se tengan las credenciales correctas para acceder a los servicios de ARCA (AFIP).
 */
public abstract class Wsaa {

    public static final String VERSION = "1.0.0";

    public static final String TEST_API_BASE = "https://wsaahomo.afip.gov.ar";
    public static final String PROD_API_BASE = "https://wsaa.afip.gov.ar";

    private Wsaa() {} // Previene instanciación
}
