package com.germanfica.wsfe;

import com.germanfica.wsfe.exception.ApiException;
import com.germanfica.wsfe.net.ApiEnvironment;
import com.germanfica.wsfe.net.SoapResponseGetterOptions;
import com.germanfica.wsfe.net.DefaultSoapRequestHandler;
import com.germanfica.wsfe.net.SoapRequestHandler;
import com.germanfica.wsfe.service.WsfeService;
import fev1.dif.afip.gov.ar.FEAuthRequest;
import fev1.dif.afip.gov.ar.FECAERequest;
import fev1.dif.afip.gov.ar.FECAEResponse;
import fev1.dif.afip.gov.ar.FERecuperaLastCbteResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.net.Proxy;

/**
 * This is the primary entrypoint to make requests against WSFE's API. It provides a means of
 * accessing all the methods on the WSFE API, and the ability to set configuration such as apiKey
 * and connection timeouts.
 */
public class WsfeClient {
    private final SoapRequestHandler soapRequestHandler;

    /**
     * Creates a WsfeClient using a custom SoapRequestHandler.
     *
     * <p>This is intended for testing or advanced scenarios where you need full control
     * over how requests are handled by the WsfeClient.
     */
    public WsfeClient(SoapRequestHandler requestHandler) {
        this.soapRequestHandler = requestHandler;
    }

    public FECAEResponse fecaeSolicitar(FEAuthRequest auth, FECAERequest feCAEReq) throws ApiException {
        return new WsfeService(soapRequestHandler).fecaeSolicitar(auth, feCAEReq);
    }

    public FERecuperaLastCbteResponse feCompUltimoAutorizado(FEAuthRequest auth, int ptoVta, int cbteTipo) throws ApiException {
        return new com.germanfica.wsfe.service.WsfeService(soapRequestHandler).feCompUltimoAutorizado(auth, ptoVta, cbteTipo);
    }

    static class ClientWsfeResponseGetterOptions extends SoapResponseGetterOptions {
        @Getter(onMethod_ = {@Override})
        private final String urlBase;
        @Getter(onMethod_ = {@Override})
        private final ApiEnvironment apiEnvironment;
        @Getter(onMethod_ = {@Override})
        private final Proxy proxy;

        ClientWsfeResponseGetterOptions(String token, String sign, Long cuit, String urlBase, ApiEnvironment apiEnvironment, Proxy proxy) {
            this.urlBase = urlBase;
            this.apiEnvironment = apiEnvironment;
            this.proxy = proxy;
        }
    }

    /**
     * Builder class for creating a {@link WsfeClient} instance. Allows you to specify settings like
     * the API key, connect and read timeouts, and proxy settings.
     */
    public static WsfeClient.WsfeClientBuilder builder() {
        return new WsfeClient.WsfeClientBuilder();
    }

    @Setter
    @Accessors(chain = true)
    public static final class WsfeClientBuilder {
        private String token;
        private String sign;
        private Long cuit;
        private String apiBase;
        ApiEnvironment apiEnvironment;
        Proxy proxy;

        public WsfeClient build() {
            return new WsfeClient(new DefaultSoapRequestHandler(buildOptions()));
        }

        private SoapResponseGetterOptions buildOptions() {
            return new ClientWsfeResponseGetterOptions(
                    this.token,
                    this.sign,
                    this.cuit,
                    this.apiBase,
                    this.apiEnvironment,
                    this.proxy
            );
        }
    }
}
