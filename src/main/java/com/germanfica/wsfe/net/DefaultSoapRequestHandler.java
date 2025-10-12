package com.germanfica.wsfe.net;

import com.germanfica.wsfe.dto.ErrorDto;
import com.germanfica.wsfe.exception.ApiException;
import com.germanfica.wsfe.exception.MissingHttpTransportSupportException;
import com.germanfica.wsfe.exception.UnsupportedProxyAuthException;
import fev1.dif.afip.gov.ar.Service;
import fev1.dif.afip.gov.ar.ServiceSoap;
import https.wsaa_afip_gov_ar.ws.services.logincms.LoginCMS;
import https.wsaa_afip_gov_ar.ws.services.logincms.LoginCMSService;
import https.wsaa_afip_gov_ar.ws.services.logincms.LoginFault;
import jakarta.xml.soap.SOAPFault;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.soap.SOAPFaultException;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.configuration.security.ProxyAuthorizationPolicy;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transport.http.asyncclient.hc5.AsyncHTTPConduit;

import java.net.MalformedURLException;

/**
 * Similar a lo que Stripe denomina LiveStripeResponseGetter.
 *
 * Esta clase actúa como punto central para enviar solicitudes SOAP y manejar los errores resultantes.
 * Se dejó el nombre DefaultSoapRequestHandler por claridad en el dominio ARCA, pero su rol funcional
 * es equivalente al ResponseGetter de SDKs como Stripe.
 */
public class DefaultSoapRequestHandler implements SoapRequestHandler {
    private final SoapResponseGetterOptions options;

    public DefaultSoapRequestHandler(SoapResponseGetterOptions options) {
        this.options = options;
    }

    @Override
    public <T> T handleRequest(ApiRequest apiRequest, RequestExecutor<T> executor) throws ApiException {
        Bus previousBus = BusFactory.getThreadDefaultBus(false);
        Bus threadBus = BusFactory.newInstance().createBus();
        threadBus.setProperty(AsyncHTTPConduit.USE_ASYNC, resolveUseAsync(apiRequest));
        threadBus.setProperty(AsyncHTTPConduit.ENABLE_HTTP2, Boolean.TRUE);
        BusFactory.setThreadDefaultBus(threadBus);

        try {
            validateUnsupportedFeatures(apiRequest);
            return executor.execute();
        } catch (LoginFault e) {
            handleLoginFault(e);
        } catch (SOAPFaultException e) {
            handleSoapFault(e);
        } catch (WebServiceException e) {
            handleWebServiceError(e);
        } catch (MalformedURLException e) {
            handleMalformedUrlError(e);
        } catch (Exception e) {
            handleUnexpectedError(e);
        } finally {
            BusFactory.setThreadDefaultBus(previousBus);
            threadBus.shutdown(true); // cerramos recursos correctamente
        }

        return null; // Este return nunca se alcanzará debido a los throws
    }

    public <P, R> R invoke(ApiRequest apiRequest, Class<P> portClass, PortInvoker<P, R> invoker) throws ApiException {
        return handleRequest(apiRequest, () -> {
            P port = resolveConfiguredPort(apiRequest, portClass);
            return invoker.invoke(port);
        });
    }

    private void handleLoginFault(LoginFault e) throws ApiException {
        System.err.println("Login Fault error occurred: " + e.getMessage());
        e.printStackTrace();

        throw new ApiException(
            new ErrorDto("login_fault", "Error de autenticación con AFIP: " + e.getMessage(), null),
            HttpStatus.UNAUTHORIZED
        );
    }

    private void handleSoapFault(SOAPFaultException e) throws ApiException {
        System.err.println("SOAP Fault: " + e.getFault().getFaultString());
        String faultCode = handleSoapFaultCode(e.getFault());
        System.err.println("SOAP Fault Code: " + faultCode);

        throw new ApiException(
            new ErrorDto(faultCode, e.getFault().getFaultString(), null),
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    /**
     * Safely extracts the fault code from a {@link SOAPFault}.
     * <p>
     * This method attempts to obtain the local part of the fault code from
     * {@link SOAPFault#getFaultCodeAsQName()}. If unavailable, it falls back to
     * {@link SOAPFault#getFaultCode()} and removes any namespace prefix (e.g., "ns1:").
     * <p>
     * If no valid fault code is found or an exception occurs while reading it,
     * the method returns the default value {@code "soap_fault"}.
     *
     * @param fault the {@link SOAPFault} instance, may be {@code null}
     * @return the extracted fault code, or {@code "soap_fault"} if unknown or unavailable
     */
    private String handleSoapFaultCode(SOAPFault fault) {
        if (fault == null) return "soap_fault";

        try {
            javax.xml.namespace.QName q = fault.getFaultCodeAsQName();
            if (q != null) {
                String local = q.getLocalPart();
                if (local != null && !local.isBlank()) {
                    return local;
                }
            }
        } catch (Throwable ignored) {
        }

        try {
            String raw = fault.getFaultCode();
            if (raw != null && !raw.isBlank()) {
                int idx = raw.indexOf(':');
                return (idx >= 0 && idx + 1 < raw.length()) ? raw.substring(idx + 1) : raw;
            }
        } catch (Throwable ignored) {
        }

        return "soap_fault";
    }

    private void handleWebServiceError(WebServiceException e) throws ApiException {
        System.err.println("Web Service Error: " + e.getMessage());

        throw new ApiException(
            new ErrorDto("webservice_error", "Error de comunicación con AFIP", null),
            HttpStatus.BAD_GATEWAY
        );
    }

    private void handleMalformedUrlError(MalformedURLException e) throws ApiException {
        System.err.println("Malformed URL: " + e.getMessage());

        throw new ApiException(
            new ErrorDto("malformed_url", "La URL del WSDL es inválida o está mal formada: " + e.getMessage(), null),
            HttpStatus.BAD_REQUEST
        );
    }

    private void handleUnexpectedError(Exception e) throws ApiException {
        System.err.println("Unexpected error occurred: " + e.getMessage());
        e.printStackTrace();

        throw new ApiException(
            new ErrorDto("unexpected_error", "Unexpected error occurred", null),
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    /**
     * Devuelve TRUE si el modo de transporte requiere AsyncHTTPConduit.
     */
    private Boolean resolveUseAsync(BaseApiRequest request) {
        RequestOptions mergedOptions = mergeRequestOptions(request);
        return mergedOptions.getHttpTransportMode() == HttpTransportMode.HTTP_HC5
            ? Boolean.TRUE
            : Boolean.FALSE;
    }

    /**
     * Determina si se debe utilizar AsyncHTTPConduit en función de si se ha configurado un proxy con credenciales.
     * Esto activa el transporte asíncrono en Apache CXF.
     *
     * @param request La solicitud actual con posibles opciones específicas.
     * @return Boolean.TRUE si se requiere AsyncHTTPConduit, de lo contrario Boolean.FALSE.
     */
    @Deprecated
    private Boolean asyncConduitRequired(BaseApiRequest request) {
        RequestOptions mergedOptions = mergeRequestOptions(request);;
        return (mergedOptions.hasProxy() && mergedOptions.getProxyOptions().hasCredentials()) ? Boolean.TRUE : Boolean.FALSE;
    }

    private void validateUnsupportedFeatures(BaseApiRequest request) throws ApiException {
        //validateProxyAuthSupport(request);
        validateAuthenticatedProxyRequiresHttpHc5(request);
    }

    private void validateAuthenticatedProxyRequiresHttpHc5(BaseApiRequest request) throws MissingHttpTransportSupportException {
        RequestOptions mergedOptions = mergeRequestOptions(request);
        if (mergedOptions.hasProxy() && mergedOptions.getProxyOptions().hasCredentials()) {
            HttpTransportMode mode = mergedOptions.getHttpTransportMode();
            if (mode != HttpTransportMode.HTTP_HC5) throw new MissingHttpTransportSupportException();
        }
    }

    @Deprecated
    private void validateProxyAuthSupport(BaseApiRequest request) throws UnsupportedProxyAuthException {
        RequestOptions mergedOptions = mergeRequestOptions(request);
        if (mergedOptions.getProxyOptions() != null && mergedOptions.getProxyOptions().hasCredentials()) throw new UnsupportedProxyAuthException();
    }

    private <T> T resolvePort(Class<T> portClass, String endpoint) {
        if (portClass.equals(ServiceSoap.class)) {
            ServiceSoap port = new Service().getServiceSoap();
            ((BindingProvider) port).getRequestContext()
                .put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint + "/wsfev1/service.asmx");
            return portClass.cast(port);
        }

        if (portClass.equals(LoginCMS.class)) {
            LoginCMS port = new LoginCMSService().getLoginCms();
            ((BindingProvider) port).getRequestContext()
                .put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint + "/ws/services/LoginCms");
            return portClass.cast(port);
        }

        throw new IllegalArgumentException("Unsupported port class: " + portClass);
    }

    private void resolveCxfClient(Object port, RequestOptions options) {
        Client client = ClientProxy.getClient(port);
        HTTPConduit conduit = (HTTPConduit) client.getConduit();
        ProxyOptions proxyOptions = options.getProxyOptions();

        if (options.hasProxy()) {
            conduit.getClient().setProxyServer(proxyOptions.getHost());
            conduit.getClient().setProxyServerPort(proxyOptions.getPort());
        }

        if (options.hasProxy() && proxyOptions.hasCredentials()) {
            ProxyAuthorizationPolicy proxyAuth = new ProxyAuthorizationPolicy();
            proxyAuth.setUserName(proxyOptions.getUsername());
            proxyAuth.setPassword(proxyOptions.getPassword());
            conduit.setProxyAuthorization(proxyAuth);
        }
    }

    private <T> T resolveConfiguredPort(BaseApiRequest request, Class<T> portClass) {
        RequestOptions mergedOptions = mergeRequestOptions(request);;

        String endpoint = mergedOptions.getUrlBase() != null
            ? mergedOptions.getUrlBase()
            : resolveDefaultApiBase(portClass, mergedOptions.getApiEnvironment());

        T port = resolvePort(portClass, endpoint);
        resolveCxfClient(port, mergedOptions);
        return port;
    }

    private String resolveDefaultApiBase(Class<?> portClass, ApiEnvironment env) {
        if (env != null) return env.getUrlFor(portClass);
        throw new IllegalArgumentException("No default API base configured for port: " + portClass);
    }

    private RequestOptions mergeRequestOptions(BaseApiRequest request) {
        return RequestOptions.merge(this.options, request != null ? request.getOptions() : null);
    }
}
