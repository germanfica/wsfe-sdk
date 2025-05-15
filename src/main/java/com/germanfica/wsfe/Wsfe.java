package com.germanfica.wsfe;

import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 *  WSAA (Web Services de Autenticación y Autorización): El WSAA valida que se tengan las credenciales correctas para acceder a los servicios de ARCA (AFIP).
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

    // Configurations
    public static volatile String apiKey;
    public static volatile boolean enableTelemetry = true;

    private static volatile int connectTimeout = -1;
    private static volatile int readTimeout = -1;

    private static volatile Proxy connectionProxy = null;
    private static volatile PasswordAuthentication proxyCredential = null;
    private static volatile Map<String, String> appInfo = null;

    // API Base Overrides
    private static volatile String wsaaBase = TEST_API_BASE;
    private static volatile String wsfeBase = PROD_API_BASE;

    /**
     * (FOR TESTING ONLY) If you'd like your WSAA requests to hit your own (mocked) server, you can set
     * this up here by overriding the base WSAA URL.
     *
     * @param overriddenWsaaBase the new base URL for WSAA requests.
     */
    public static void overrideApiBase(final String overriddenWsaaBase) {
        wsaaBase = overriddenWsaaBase;
    }

    /**
     * Returns the base URL for WSAA requests.
     *
     * @return the base URL for WSAA.
     */
    public static String getWsaaBase() {
        return wsaaBase;
    }

    /**
     * (FOR TESTING ONLY) If you'd like your WSFE requests to hit your own (mocked) server, you can set
     * this up here by overriding the base WSFE URL.
     *
     * @param overriddenWsfeBase the new base URL for WSFE requests.
     */
    public static void overrideWsfeBase(final String overriddenWsfeBase) {
        wsfeBase = overriddenWsfeBase;
    }

    /**
     * Returns the base URL for WSFE requests.
     *
     * @return the base URL for WSFE.
     */
    public static String getWsfeBase() {
        return wsfeBase;
    }

    /**
     * Set proxy to tunnel all WSFE connections.
     *
     * @param proxy proxy host and port setting.
     */
    public static void setConnectionProxy(final Proxy proxy) {
        connectionProxy = proxy;
    }

    /**
     * Returns the proxy settings for WSFE connections.
     *
     * @return the proxy settings.
     */
    public static Proxy getConnectionProxy() {
        return connectionProxy;
    }

    /**
     * Provide credentials for proxy authorization if required.
     *
     * @param auth proxy required username and password.
     */
    public static void setProxyCredential(final PasswordAuthentication auth) {
        proxyCredential = auth;
    }

    /**
     * Returns the credentials for proxy authorization.
     *
     * @return the proxy credentials.
     */
    public static PasswordAuthentication getProxyCredential() {
        return proxyCredential;
    }

    /**
     * Returns the connection timeout.
     *
     * @return timeout value in milliseconds.
     */
    public static int getConnectTimeout() {
        return connectTimeout == -1 ? DEFAULT_CONNECT_TIMEOUT : connectTimeout;
    }

    /**
     * Sets the timeout value that will be used for making new connections to the WSFE API (in milliseconds).
     *
     * @param timeout timeout value in milliseconds.
     */
    public static void setConnectTimeout(final int timeout) {
        connectTimeout = timeout;
    }

    /**
     * Returns the read timeout.
     *
     * @return timeout value in milliseconds.
     */
    public static int getReadTimeout() {
        return readTimeout == -1 ? DEFAULT_READ_TIMEOUT : readTimeout;
    }

    /**
     * Sets the timeout value that will be used when reading data from an established connection to
     * the WSFE API (in milliseconds).
     *
     * <p>Note that this value should be set conservatively because some API requests can take time
     * and a short timeout increases the likelihood of causing a problem in the backend.
     *
     * @param timeout timeout value in milliseconds.
     */
    public static void setReadTimeout(final int timeout) {
        readTimeout = timeout;
    }

    /**
     * Sets information about your application. The information is passed along to WSFE.
     *
     * @param name Name of your application (e.g. "MyAwesomeApp").
     * @param version Version of your application (e.g. "1.2.34").
     * @param url Website for your application (e.g. "https://myawesomeapp.info").
     * @param partnerId Your Partner ID (if applicable).
     */
    public static void setAppInfo(String name, String version, String url, String partnerId) {
        if (appInfo == null) {
            appInfo = new HashMap<>();
        }
        appInfo.put("name", name);
        appInfo.put("version", version);
        appInfo.put("url", url);
        appInfo.put("partner_id", partnerId);
    }

    /**
     * Returns information about your application.
     *
     * @return a map containing application details such as name, version, and URL.
     */
    public static Map<String, String> getAppInfo() {
        return appInfo;
    }
}
