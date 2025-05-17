package com.germanfica.wsfe;

import com.germanfica.wsfe.exception.ApiException;
import com.germanfica.wsfe.net.BaseApiRequest;
import com.germanfica.wsfe.net.DefaultSoapRequestHandler;
import com.germanfica.wsfe.net.SoapRequestHandler;
import com.germanfica.wsfe.service.WsfeService;
import fev1.dif.afip.gov.ar.FEAuthRequest;
import fev1.dif.afip.gov.ar.FECAERequest;
import fev1.dif.afip.gov.ar.FECAEResponse;
import fev1.dif.afip.gov.ar.FERecuperaLastCbteResponse;

import java.net.MalformedURLException;

/**
 * This is the primary entrypoint to make requests against WSFE's API. It provides a means of
 * accessing all the methods on the WSFE API, and the ability to set configuration such as apiKey
 * and connection timeouts.
 */
public class WsfeClient {
    private final SoapRequestHandler soapRequestHandler;

    /**
     * Constructor que recibe parámetros para inicializar un BaseApiRequest.
     */
    public WsfeClient() {
        this.soapRequestHandler = new DefaultSoapRequestHandler();
    }

    public FECAEResponse fecaeSolicitar(FEAuthRequest auth, FECAERequest feCAEReq) throws ApiException {
        return new WsfeService(soapRequestHandler).fecaeSolicitar(auth, feCAEReq);
    }

    public FERecuperaLastCbteResponse feCompUltimoAutorizado(FEAuthRequest auth, int ptoVta, int cbteTipo) throws ApiException {
        return new com.germanfica.wsfe.service.WsfeService(soapRequestHandler).feCompUltimoAutorizado(auth, ptoVta, cbteTipo);
    }

    static class ClientWsfeResponseGetterOptions extends BaseApiRequest {
        public ClientWsfeResponseGetterOptions(String token, String sign, Long cuit, String apiBase) {
            super(token, sign, cuit, apiBase);
        }
    }

    /**
     * Builder class for creating a {@link WsfeClient} instance. Allows you to specify settings like
     * the API key, connect and read timeouts, and proxy settings.
     */
    public static WsfeClient.WsfeClientBuilder builder() {
        return new WsfeClient.WsfeClientBuilder();
    }

    public static final class WsfeClientBuilder {
        private String token;
        private String sign;
        private Long cuit;
        private String apiBase;

        public WsfeClientBuilder setToken(String token) {
            this.token = token;
            return this;
        }

        public WsfeClientBuilder setSign(String sign) {
            this.sign = sign;
            return this;
        }

        public WsfeClientBuilder setCuit(Long cuit) {
            this.cuit = cuit;
            return this;
        }

        public WsfeClientBuilder setApiBase(String apiBase) {
            this.apiBase = apiBase;
            return this;
        }

        public WsfeClient build() {
            BaseApiRequest request = buildOptions();
            return new WsfeClient();
        }

        private BaseApiRequest buildOptions() {
            return new ClientWsfeResponseGetterOptions(
                    this.token,
                    this.sign,
                    this.cuit,
                    this.apiBase
            );
        }
    }
}
