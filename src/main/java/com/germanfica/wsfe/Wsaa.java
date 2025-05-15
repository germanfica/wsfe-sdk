package com.germanfica.wsfe;

public abstract class Wsaa {

    public static final String VERSION = "1.0.0";

    public static final String TEST_API_BASE = "https://wsaahomo.afip.gov.ar";
    public static final String PROD_API_BASE = "https://wsaa.afip.gov.ar";

    private static volatile String apiBase = PROD_API_BASE;

    /**
     * Override the WSAA API base URL (for testing/mocking).
     */
    public static void overrideApiBase(final String newBase) {
        apiBase = newBase;
    }

    public static String getApiBase() {
        return apiBase;
    }
}
