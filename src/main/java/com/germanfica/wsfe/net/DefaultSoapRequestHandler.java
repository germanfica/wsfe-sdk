package com.germanfica.wsfe.net;

import https.wsaahomo_afip_gov_ar.ws.services.logincms.LoginFault;
import com.germanfica.wsfe.dto.ErrorDto;
import com.germanfica.wsfe.exception.ApiException;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.soap.SOAPFaultException;

/**
 * Similar a lo que Stripe denomina LiveStripeResponseGetter.
 *
 * Esta clase actúa como punto central para enviar solicitudes SOAP y manejar los errores resultantes.
 * Se dejó el nombre DefaultSoapRequestHandler por claridad en el dominio ARCA, pero su rol funcional
 * es equivalente al ResponseGetter de SDKs como Stripe.
 */
public class DefaultSoapRequestHandler implements SoapRequestHandler {

    @Override
    public <T> T handleRequest(ApiRequest apiRequest, RequestExecutor<T> executor) throws ApiException {
        try {
            return executor.execute();
        } catch (LoginFault e) {
            handleLoginFault(e);
        } catch (SOAPFaultException e) {
            handleSoapFault(e);
        } catch (WebServiceException e) {
            handleWebServiceError(e);
        } catch (Exception e) {
            handleUnexpectedError(e);
        }
        return null; // Este return nunca se alcanzará debido a los throws
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

    private void handleUnexpectedError(Exception e) throws ApiException {
        System.err.println("Unexpected error occurred: " + e.getMessage());
        e.printStackTrace();

        throw new ApiException(
                new ErrorDto("unexpected_error", "Unexpected error occurred", null),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
