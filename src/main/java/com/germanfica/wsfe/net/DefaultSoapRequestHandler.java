package com.germanfica.wsfe.net;

import com.germanfica.wsfe.exception.SoapProcessingException;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import com.germanfica.wsfe.dto.ErrorDto;
import com.germanfica.wsfe.exception.ApiException;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.soap.SOAPFaultException;
import org.w3c.dom.Node;

public class DefaultSoapRequestHandler implements SoapRequestHandler {

    @Override
    public <T> T handleRequest(ApiRequest apiRequest, RequestExecutor<T> executor) {
        try {
            return executor.execute();
        } catch (SOAPFaultException e) {
            handleSoapFault(e);
        } catch (WebServiceException e) {
            handleWebServiceError(e);
        } catch (Exception e) {
            handleUnexpectedError(e);
        }
        return null; // Este return nunca se alcanzará debido a los throws
    }

    private void handleSoapFault(SOAPFaultException e) {
        System.err.println("SOAP Fault: " + e.getFault().getFaultString());

        throw new ApiException(
                new ErrorDto("soap_fault", e.getFault().getFaultString(), null),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    private void handleWebServiceError(WebServiceException e) {
        System.err.println("Web Service Error: " + e.getMessage());

        throw new ApiException(
                new ErrorDto("webservice_error", "Error de comunicación con AFIP", null),
                HttpStatus.BAD_GATEWAY
        );
    }

    private void handleUnexpectedError(Exception e) {
        System.err.println("Unexpected error occurred: " + e.getMessage());
        e.printStackTrace();

        throw new ApiException(
                new ErrorDto("unexpected_error", "Unexpected error occurred", null),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
