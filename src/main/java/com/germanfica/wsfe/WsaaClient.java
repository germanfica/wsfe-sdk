package com.germanfica.wsfe;

import com.germanfica.wsfe.exception.ApiException;
import com.germanfica.wsfe.net.*;
import lombok.Getter;

import java.net.Proxy;

/**
 * This is the primary entrypoint to make requests against WSAA's API. It provides a means of
 * accessing all the methods on the WSAA API, and the ability to set configuration such as apiKey
 * and connection timeouts.
 */
public class WsaaClient {
    private final SoapRequestHandler soapRequestHandler;

    /**
     * Creates a WsaaClient using a custom SoapRequestHandler.
     *
     * <p>This is intended for testing or advanced scenarios where you need full control
     * over how requests are handled by the WsaaClient.
     */
    public WsaaClient(SoapRequestHandler requestHandler) {
        this.soapRequestHandler = requestHandler;
    }

    public com.germanfica.wsfe.service.AuthService authService() throws ApiException {
        return new com.germanfica.wsfe.service.AuthService(soapRequestHandler);
    }

    static class ClientWsaaResponseGetterOptions extends SoapResponseGetterOptions {
        @Getter(onMethod_ = {@Override})
        private final String urlBase;
        @Getter(onMethod_ = {@Override})
        private final ApiEnvironment apiEnvironment;
        @Getter(onMethod_ = {@Override})
        private final ProxyOptions proxyOptions;

        ClientWsaaResponseGetterOptions(String urlBase, ApiEnvironment apiEnvironment, ProxyOptions proxyOptions) {
            this.urlBase = urlBase;
            this.apiEnvironment = apiEnvironment;
            this.proxyOptions = proxyOptions;
        }
    }

    public static WsaaClientBuilder builder() {
        return new WsaaClientBuilder();
    }

    public static final class WsaaClientBuilder {
        private String urlBase;
        private ApiEnvironment apiEnvironment;
        private ProxyOptions proxyOptions;

        public WsaaClientBuilder setUrlBase(String urlBase) {
            this.urlBase = urlBase;
            return this;
        }

        public WsaaClientBuilder setApiEnvironment(ApiEnvironment apiEnvironment) {
            this.apiEnvironment = apiEnvironment;
            return this;
        }

        public WsaaClientBuilder setProxyOptions(ProxyOptions proxyOptions) {
            this.proxyOptions = proxyOptions;
            return this;
        }

        public WsaaClient build() {
            return new WsaaClient(new DefaultSoapRequestHandler(buildOptions()));
        }

        private SoapResponseGetterOptions buildOptions() {
            return new ClientWsaaResponseGetterOptions(this.urlBase, this.apiEnvironment, this.proxyOptions);
        }
    }
}
