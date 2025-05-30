package com.germanfica.wsfe.net;

import com.germanfica.wsfe.dto.ErrorDto;
import com.germanfica.wsfe.exception.ApiException;
import com.germanfica.wsfe.exception.UnsupportedProxyAuthException;
import fev1.dif.afip.gov.ar.Service;
import fev1.dif.afip.gov.ar.ServiceSoap;
import https.wsaa_afip_gov_ar.ws.services.logincms.LoginCMS;
import https.wsaa_afip_gov_ar.ws.services.logincms.LoginCMSService;
import https.wsaa_afip_gov_ar.ws.services.logincms.LoginFault;
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
        threadBus.setProperty(AsyncHTTPConduit.USE_ASYNC, Boolean.TRUE);
        threadBus.setProperty(AsyncHTTPConduit.ENABLE_HTTP2, Boolean.TRUE);
        BusFactory.setThreadDefaultBus(threadBus);

        try {
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

        throw new ApiException(
            new ErrorDto("soap_fault", e.getFault().getFaultString(), null),
            HttpStatus.INTERNAL_SERVER_ERROR
        );
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

    @Deprecated
    private void validateUnsupportedFeatures() throws ApiException {
        if (options.getProxyOptions() != null && options.getProxyOptions().hasCredentials()) {
            throw new UnsupportedProxyAuthException();
        }
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
        RequestOptions mergedOptions = RequestOptions.merge(this.options, request != null ? request.getOptions() : null);

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
}
