package com.germanfica.wsfe.service;

import generated.LoginTicketResponseType;
import jakarta.xml.soap.*;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.Map;

import static com.germanfica.wsfe.utils.ArcaWSAAUtils.convertXmlToObject;

/** Clase base para todos los servicios SOAP */
public abstract class SoapService {

    @Getter(AccessLevel.PROTECTED)
    private final SOAPConnectionFactory soapConnectionFactory;

    protected SoapService() {
        try {
            this.soapConnectionFactory = SOAPConnectionFactory.newInstance();
        } catch (SOAPException e) {
            throw new RuntimeException("Error al inicializar SOAPConnectionFactory", e);
        }
    }

    protected SOAPMessage createSoapMessage(String soapAction, byte[] payload, String namespace, String operation, Map<String, String> bodyElements) throws SOAPException {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();

        // Crear cuerpo del mensaje
        SOAPEnvelope envelope = soapMessage.getSOAPPart().getEnvelope();
        SOAPBody body = envelope.getBody();
        SOAPElement operationElement = body.addChildElement(operation, "ns1", namespace);

        // Agregar elementos al cuerpo
        bodyElements.forEach((key, value) -> {
            try {
                operationElement.addChildElement(key).addTextNode(value);
            } catch (SOAPException e) {
                throw new RuntimeException("Error al agregar elementos al cuerpo SOAP", e);
            }
        });

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

    /**
     * Método genérico para mapear un XML a un DTO.
     *
     * @param xml El XML como cadena.
     * @param clazz La clase del tipo objetivo.
     * @param <T> Tipo genérico.
     * @return Instancia del DTO mapeada desde el XML.
     * @throws Exception Si ocurre un error durante el mapeo.
     */
    protected <T> T mapToDto(String xml, Class<T> clazz) throws Exception {
        try {
            return convertXmlToObject(xml, clazz);
        } catch (Exception e) {
            throw new Exception("Error al mapear XML a DTO: " + e.getMessage(), e);
        }
    }

    /**
     * Método que combina la creación, envío y mapeo de una respuesta SOAP.
     *
     * @param soapAction La acción SOAP.
     * @param payload El contenido del mensaje SOAP.
     * @param namespace El namespace del mensaje SOAP.
     * @param operation La operación SOAP.
     * @param bodyElements Elementos del cuerpo del mensaje SOAP.
     * @param endpoint URL del servicio SOAP.
     * @param responseType Clase del tipo objetivo para mapear la respuesta.
     * @param <T> Tipo genérico del DTO de respuesta.
     * @return Instancia del DTO mapeada desde la respuesta SOAP.
     * @throws Exception Si ocurre algún error.
     */
    public <T> T request(String soapAction, byte[] payload, String namespace, String operation,
                         Map<String, String> bodyElements, String endpoint, Class<T> responseType) throws Exception {
        // Crear mensaje SOAP
        SOAPMessage soapMessage = createSoapMessage(soapAction, payload, namespace, operation, bodyElements);

        // Enviar solicitud
        SOAPMessage soapResponse = sendSoapRequest(soapMessage, endpoint);

        // Procesar respuesta
        String xmlResponse = extractResponse(soapResponse);

        // Mapear la respuesta a un DTO
        return mapToDto(xmlResponse, responseType);
    }

    /**
     * Extraer la respuesta del mensaje SOAP como cadena XML.
     *
     * @param soapMessage El mensaje SOAP de respuesta.
     * @return Respuesta como cadena XML.
     * @throws SOAPException Si ocurre un error al procesar el mensaje.
     */
    protected String extractResponse(SOAPMessage soapMessage) throws SOAPException {
        try {
            return soapMessage.getSOAPBody().getTextContent();
        } catch (Exception e) {
            throw new SOAPException("Error al extraer contenido del cuerpo SOAP", e);
        }
    }
}
