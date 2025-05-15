package com.germanfica.wsfe;

import com.germanfica.wsfe.net.BaseApiRequest;
import com.germanfica.wsfe.net.DefaultSoapRequestHandler;
import com.germanfica.wsfe.net.SoapRequestHandler;

import java.net.MalformedURLException;
import java.util.Map;

/**
 * This is the primary entrypoint to make requests against WSFE's API. It provides a means of
 * accessing all the methods on the WSFE API, and the ability to set configuration such as apiKey
 * and connection timeouts.
 */
public class WsfeClient {
    private final SoapRequestHandler soapRequestHandler;

    /**
     * Constructor que recibe parámetros para inicializar un BaseApiRequest.
     *
     * @param payload Contenido del mensaje SOAP en bytes.
     */
    public WsfeClient(byte[] payload) {
        this.soapRequestHandler = new DefaultSoapRequestHandler();
    }

    public com.germanfica.wsfe.service.FECAESolicitarService fecaeSolicitarService() {
        return new com.germanfica.wsfe.service.FECAESolicitarService(soapRequestHandler);
    }

    public com.germanfica.wsfe.service.FECompUltimoAutorizadoService feCompUltimoAutorizadoService() {
        return new com.germanfica.wsfe.service.FECompUltimoAutorizadoService(soapRequestHandler);
    }

    public com.germanfica.wsfe.service.AuthService authService() throws MalformedURLException {
        return new com.germanfica.wsfe.service.AuthService(soapRequestHandler);
    }

    static class ClientWsfeResponseGetterOptions extends BaseApiRequest {
        ClientWsfeResponseGetterOptions(
                String soapAction,
                byte[] payload,
                String namespace,
                String operation,
                Map<String, String> bodyElements,
                String endpoint,
                Class<?> responseType
        ) {
            super(soapAction, payload, namespace, operation, bodyElements, endpoint, responseType);
        }
    }

    /**
     * Builder class for creating a {@link WsfeClient} instance. Allows you to specify settings like
     * the API key, connect and read timeouts, and proxy settings.
     */
    public static WsfeClientBuilder builder() {
        return new WsfeClientBuilder();
    }

    public static final class WsfeClientBuilder {
        private String soapAction;
        private byte[] payload;
        private String namespace;
        private String operation;
        private Map<String, String> bodyElements;
        private String endpoint;
        private Class<?> responseType;

        public WsfeClientBuilder setSoapAction(String soapAction) {
            this.soapAction = soapAction;
            return this;
        }

        public WsfeClientBuilder setPayload(byte[] payload) {
            this.payload = payload;
            return this;
        }

        public WsfeClientBuilder setNamespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        public WsfeClientBuilder setOperation(String operation) {
            this.operation = operation;
            return this;
        }

        public WsfeClientBuilder setBodyElements(Map<String, String> bodyElements) {
            this.bodyElements = bodyElements;
            return this;
        }

        public WsfeClientBuilder setEndpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public WsfeClientBuilder setResponseType(Class<?> responseType) {
            this.responseType = responseType;
            return this;
        }

        /** Constructs a {@link BaseApiRequest} with the specified values. */
        public WsfeClient build() {
            return new WsfeClient(buildOptions().getPayload());
        }

        /**
         * Constructs a request options builder with the global parameters (API key and client ID) as
         * default values.
         */
        public WsfeClientBuilder() {}

        BaseApiRequest buildOptions() {
            return new ClientWsfeResponseGetterOptions(
                    this.soapAction,
                    this.payload,
                    this.namespace,
                    this.operation,
                    this.bodyElements,
                    this.endpoint,
                    this.responseType
            );
        }
    }
}
