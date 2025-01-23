package com.germanfica.wsfe.net;

import com.germanfica.wsfe.exception.SoapProcessingException;
import jakarta.xml.soap.SOAPMessage;
import com.germanfica.wsfe.dto.ErrorDto;
import com.germanfica.wsfe.exception.ApiException;
import org.w3c.dom.Node;


public class DefaultSoapRequestHandler implements SoapRequestHandler {

    private final BaseApiRequest baseApiRequest;

    public DefaultSoapRequestHandler(BaseApiRequest baseApiRequest) {
        this.baseApiRequest = baseApiRequest;
    }

    @Override
    public <T> T handleRequest(ApiRequest apiRequest, Class<T> responseType) throws Exception {
        try {
            // Crear y enviar el mensaje SOAP
            SOAPMessage soapMessage = baseApiRequest.createSoapMessage(
                    apiRequest.getSoapAction(),
                    apiRequest.getPayload(),
                    apiRequest.getNamespace(),
                    apiRequest.getOperation(),
                    apiRequest.getBodyElements()
            );

            SOAPMessage soapResponse = baseApiRequest.sendSoapRequest(soapMessage, apiRequest.getEndpoint());

            // Verificar si el cuerpo SOAP es nulo
            if (soapResponse == null || soapResponse.getSOAPBody() == null) {
                throw new ApiException(
                        new ErrorDto("soap_body_null", "El cuerpo de la respuesta SOAP es nulo", null),
                        HttpStatus.INTERNAL_SERVER_ERROR
                );
            }

            boolean hasFault = soapResponse.getSOAPBody().hasFault();

            System.err.println("Soap hubo falla?: " + hasFault);
            if (hasFault) {
                System.out.println("AAAAAAAAAAAAAAAAAAA");
                // Manejo del fault si existe
                jakarta.xml.soap.SOAPFault fault = soapResponse.getSOAPBody().getFault();
                String faultCode = fault != null ? fault.getFaultCode() : "unknown";
                String faultString = fault != null ? fault.getFaultString() : "unknown";
                String exceptionName = null;
                String hostname = null;

                // Intentar obtener detalles del fallo
                if (fault.getDetail() != null) {
                    System.out.println("Parece que hay contenido en getDetails...");
                    System.out.println("Contenido de getDetail: " + fault.getDetail().getTextContent());


                    // Iterar sobre los nodos hijos y obtener sus nombres
//                    Node detailNode = fault.getDetail();
//                    System.out.println("Nombres de los nodos hijos de getDetail:");
//                    for (int i = 0; i < detailNode.getChildNodes().getLength(); i++) {
//                        Node childNode = detailNode.getChildNodes().item(i);
//                        System.out.println("- Nodo: " + childNode.getNodeName() + ", Valor: " + childNode.getTextContent());
//                    }


                    Node exceptionNode = fault.getDetail().getElementsByTagNameNS("http://xml.apache.org/axis/", "exceptionName").item(0);
                    Node hostnameNode = fault.getDetail().getElementsByTagNameNS("http://xml.apache.org/axis/", "hostname").item(0);

                    // Verificar y extraer el contenido de los nodos
                    if (exceptionNode != null) {
                        exceptionName = exceptionNode.getTextContent();
                        System.out.println("Exception Name: " + exceptionName);
                    } else {
                        System.err.println("No se encontró el nodo exceptionName.");
                    }

                    if (hostnameNode != null) {
                        hostname = hostnameNode.getTextContent();
                        System.out.println("Hostname: " + hostname);
                    } else {
                        System.err.println("No se encontró el nodo hostname.");
                    }
                }

                throw new ApiException(
                        new ErrorDto(
                                faultCode,
                                faultString,
                                new ErrorDto.ErrorDetailsDto(exceptionName, hostname)
                        ),
                        HttpStatus.INTERNAL_SERVER_ERROR
                );
            }

            // Procesar la respuesta y mapear al DTO
            return SoapProcessor.processResponse(soapResponse, responseType);
        } catch (SoapProcessingException soapProcessingException) {
            System.err.println("Error mapping XML to DTO: " + soapProcessingException.getMessage());
            soapProcessingException.getCause().printStackTrace();
            throw new ApiException(
                    new ErrorDto("xml_mapping_error", "Error mapping XML response to DTO", null),
                    HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            // Si ya es una exception conocida, simplemente la relanza
            if (e instanceof ApiException) throw e;
            if (e instanceof SoapProcessingException) throw e;

            // Manejo de excepciones genéricas
            System.err.println("Unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
            throw new ApiException(
                    new ErrorDto("unexpected_error", "Unexpected error occurred", null),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
