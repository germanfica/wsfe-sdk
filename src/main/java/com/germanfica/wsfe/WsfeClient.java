package com.germanfica.wsfe;

import com.germanfica.wsfe.exception.ApiException;
import com.germanfica.wsfe.net.*;
import com.germanfica.wsfe.param.FEAuthParams;
import com.germanfica.wsfe.service.WsfeService;
import com.germanfica.wsfe.provider.feauth.FEAuthProvider;
import com.germanfica.wsfe.provider.ProviderChain;
import com.germanfica.wsfe.provider.feauth.StaticAuthProvider;
import fev1.dif.afip.gov.ar.FEActividadesResponse;
import fev1.dif.afip.gov.ar.FECAERequest;
import fev1.dif.afip.gov.ar.FECAEResponse;
import fev1.dif.afip.gov.ar.FERecuperaLastCbteResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is the primary entrypoint to make requests against WSFE's API. It provides a means of
 * accessing all the methods on the WSFE API, and the ability to set configuration such as apiKey
 * and connection timeouts.
 */
public class WsfeClient {
    private final SoapRequestHandler soapRequestHandler;
    private final FEAuthProvider authProvider;

    /**
     * Creates a WsfeClient using a custom SoapRequestHandler.
     *
     * <p>This is intended for testing or advanced scenarios where you need full control
     * over how requests are handled by the WsfeClient.
     */
    public WsfeClient(SoapRequestHandler requestHandler, FEAuthProvider feAuthProvider) {
        this.soapRequestHandler = requestHandler;
        this.authProvider = feAuthProvider;
    }

    public FECAEResponse fecaeSolicitar(FECAERequest feCAEReq) throws ApiException {
        return new WsfeService(soapRequestHandler, authProvider).fecaeSolicitar(feCAEReq);
    }

    public FERecuperaLastCbteResponse feCompUltimoAutorizado(int ptoVta, int cbteTipo) throws ApiException {
        return new com.germanfica.wsfe.service.WsfeService(soapRequestHandler, authProvider).feCompUltimoAutorizado(ptoVta, cbteTipo);
    }

    public FEActividadesResponse feParamGetActividades() throws ApiException {
        return new com.germanfica.wsfe.service.WsfeService(soapRequestHandler, authProvider).feParamGetActividades();
    }

    static class ClientWsfeResponseGetterOptions extends SoapResponseGetterOptions {
        @Getter(onMethod_ = {@Override})
        private final String urlBase;
        @Getter(onMethod_ = {@Override})
        private final ApiEnvironment apiEnvironment;
        @Getter(onMethod_ = {@Override})
        private final ProxyOptions proxyOptions;
        @Getter(onMethod_ = {@Override})
        private final HttpTransportMode httpTransportMode;

        ClientWsfeResponseGetterOptions(String token, String sign, Long cuit, String urlBase, ApiEnvironment apiEnvironment, ProxyOptions proxyOptions, HttpTransportMode httpTransportMode) {
            this.urlBase = urlBase;
            this.apiEnvironment = apiEnvironment;
            this.proxyOptions = proxyOptions;
            this.httpTransportMode = httpTransportMode;
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
        @Setter(AccessLevel.NONE)
        private FEAuthProvider feAuthProvider;
        private String token;
        private String sign;
        private Long cuit;
        private String urlBase;
        private ApiEnvironment apiEnvironment;
        private ProxyOptions proxyOptions;
        private HttpTransportMode httpTransportMode;

        public WsfeClientBuilder setFEAuthProvider(FEAuthProvider feAuthProvider) {
            this.feAuthProvider = feAuthProvider;
            return this;
        }

        public WsfeClientBuilder setFEAuthParams(FEAuthParams params) {
            this.feAuthProvider = new StaticAuthProvider(params);
            return this;
        }

        public WsfeClient build() {
            return new WsfeClient(
                //new DefaultSoapRequestHandler(buildOptions()),
                new DefaultWsfeRequestHandler(buildOptions()),
                this.feAuthProvider != null ? this.feAuthProvider : defaultProviderChain()
            );
        }

        private FEAuthProvider defaultProviderChain() {
            return new StaticAuthProvider(
                ProviderChain.<FEAuthParams>builder()
                    //.addProvider(new ApplicationPropertiesFeAuthParamsProvider())
                    //.addProvider(new EnvironmentFEAuthParamsProvider())
                    .build()
                    .resolve()
                    .orElseThrow(() -> new IllegalStateException("No FEAuthParams found"))
            );
        }

        private SoapResponseGetterOptions buildOptions() {
            return new ClientWsfeResponseGetterOptions(
                    this.token,
                    this.sign,
                    this.cuit,
                    this.urlBase,
                    this.apiEnvironment,
                    this.proxyOptions,
                    this.httpTransportMode != null ? this.httpTransportMode : HttpTransportMode.HTTP
            );
        }
    }
}
