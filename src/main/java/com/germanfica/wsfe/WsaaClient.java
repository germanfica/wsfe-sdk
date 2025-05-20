package com.germanfica.wsfe;

import com.germanfica.wsfe.exception.ApiException;
import com.germanfica.wsfe.net.DefaultSoapRequestHandler;
import com.germanfica.wsfe.net.SoapRequestHandler;
import com.germanfica.wsfe.net.SoapResponseGetterOptions;
import lombok.Getter;

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
        this.soapRequestHandler = new DefaultSoapRequestHandler(builder().buildOptions());
    }

    public WsaaClient(SoapRequestHandler requestHandler) {
        this.soapRequestHandler = requestHandler;
    }

    public com.germanfica.wsfe.service.AuthService authService() throws ApiException {
        return new com.germanfica.wsfe.service.AuthService(soapRequestHandler);
    }

    static class ClientWsaaResponseGetterOptions extends SoapResponseGetterOptions {
        @Getter(onMethod_ = {@Override})
        private final String urlBase;

        ClientWsaaResponseGetterOptions(String urlBase) {
            this.urlBase = urlBase;
        }
    }

    public static WsaaClientBuilder builder() {
        return new WsaaClientBuilder();
    }

    public static final class WsaaClientBuilder {
        private String apiBase = Wsaa.PROD_API_BASE; // default value

        public WsaaClientBuilder setApiBase(String apiBase) {
            this.apiBase = apiBase;
            return this;
        }

        public WsaaClient build() {
            return new WsaaClient(new DefaultSoapRequestHandler(buildOptions()));
        }

        private SoapResponseGetterOptions buildOptions() {
            return new ClientWsaaResponseGetterOptions(this.apiBase);
        }
    }
}
