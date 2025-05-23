package com.germanfica.wsfe.net;

import jakarta.xml.soap.*;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.Map;

/** Base abstract class for SOAP clients used to send requests to Wsfe's API. */
@Deprecated
public abstract class SoapClient {

    @Getter(AccessLevel.PROTECTED)
    private final SOAPConnectionFactory soapConnectionFactory;

    protected SoapClient() {
        try {
            // FIXME: corregir se está creando potencialmente una nueva instancia de soapConnectionFactory por cada SoapClient creado
            System.out.println("################### INICIALIZADO FIRST TIME: soapConnectionFactory ####################");
            this.soapConnectionFactory = SOAPConnectionFactory.newInstance();
        } catch (SOAPException e) {
            throw new RuntimeException("Error al inicializar SOAPConnectionFactory", e);
        }
    }

    protected SOAPMessage createSoapMessage(String soapAction, byte[] payload, String namespace, String operation, Map<String, String> bodyElements) throws SOAPException {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();

        System.out.println("************");
        System.out.println(soapMessage.getSOAPBody().getTextContent());
        System.out.println("************");

        // Crear cuerpo del mensaje
        SOAPEnvelope envelope = soapMessage.getSOAPPart().getEnvelope();
        SOAPBody body = envelope.getBody();

        SOAPElement operationElement = body.addChildElement(operation, "ns1", namespace);

        // Agregar elementos al cuerpo
        for (Map.Entry<String, String> entry : bodyElements.entrySet()) {
            operationElement.addChildElement(entry.getKey()).addTextNode(entry.getValue());
        }

        // Agregar encabezados
        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", soapAction);

        soapMessage.saveChanges();
        return soapMessage;
    }

    protected SOAPMessage sendSoapRequest(SOAPMessage soapMessage, String endpoint) throws SOAPException {
        try (SOAPConnection connection = soapConnectionFactory.createConnection()) {
            return connection.call(soapMessage, endpoint);
        }
    }

//    /**
//     * Método que combina la creación, envío y mapeo de una respuesta SOAP.
//     *
//     * @param soapAction La acción SOAP.
//     * @param payload El contenido del mensaje SOAP.
//     * @param namespace El namespace del mensaje SOAP.
//     * @param operation La operación SOAP.
//     * @param bodyElements Elementos del cuerpo del mensaje SOAP.
//     * @param endpoint URL del servicio SOAP.
//     * @param responseType Clase del tipo objetivo para mapear la respuesta.
//     * @param <T> Tipo genérico del DTO de respuesta.
//     * @return Instancia del DTO mapeada desde la respuesta SOAP.
//     * @throws Exception Si ocurre algún error.
//     */
//    protected <T> T request(String soapAction, byte[] payload, String namespace, String operation,
//                         Map<String, String> bodyElements, String endpoint, Class<T> responseType) throws Exception {
//        // Crear mensaje SOAP
//        SOAPMessage soapMessage = createSoapMessage(soapAction, payload, namespace, operation, bodyElements);
//
//        // Enviar solicitud
//        SOAPMessage soapResponse = sendSoapRequest(soapMessage, endpoint);
//
//        // Procesar respuesta
//        return SoapProcessor.processResponse(soapResponse, responseType);
//    }
//
//    /**
//     * Envía una solicitud SOAP utilizando la información de la instancia de ApiRequest
//     * y mapea la respuesta al tipo de dato especificado.
//     *
//     * @param apiRequest Instancia de ApiRequest que contiene los detalles de la solicitud SOAP.
//     * @param responseType Clase del tipo objetivo para mapear la respuesta.
//     * @param <T> Tipo genérico que define el tipo esperado en la respuesta.
//     * @return Una instancia del tipo especificado mapeada desde la respuesta SOAP.
//     * @throws Exception Si ocurre algún error al crear, enviar o procesar la solicitud SOAP.
//     */
//    public <T> T request(ApiRequest apiRequest, Class<T> responseType) throws Exception {
//        // Crear mensaje SOAP utilizando los valores de ApiRequest
//        SOAPMessage soapMessage = createSoapMessage(
//                apiRequest.getSoapAction(),
//                apiRequest.getPayload(),
//                apiRequest.getNamespace(),
//                apiRequest.getOperation(),
//                apiRequest.getBodyElements()
//        );
//
//        // Enviar solicitud
//        SOAPMessage soapResponse = sendSoapRequest(soapMessage, apiRequest.getEndpoint());
//
//        // Procesar respuesta
//        return SoapProcessor.processResponse(soapResponse, responseType);
//    }
}
