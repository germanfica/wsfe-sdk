package com.germanfica.wsfe;

import com.germanfica.wsfe.net.DefaultSoapRequestHandler;
import com.germanfica.wsfe.net.SoapRequestHandler;

import java.net.MalformedURLException;

/**
 * This is the primary entrypoint to make requests against WSAA's API. It provides a means of
 * accessing all the methods on the WSAA API, and the ability to set configuration such as apiKey
 * and connection timeouts.
 */
public class WsaaClient {
    private final SoapRequestHandler soapRequestHandler;

    /**
     * Constructor que recibe parámetros para inicializar un BaseApiRequest.
     */
    public WsaaClient() {
        this.soapRequestHandler = new DefaultSoapRequestHandler();
    }

    public com.germanfica.wsfe.service.AuthService authService() throws MalformedURLException {
        return new com.germanfica.wsfe.service.AuthService(soapRequestHandler);
    }
}
