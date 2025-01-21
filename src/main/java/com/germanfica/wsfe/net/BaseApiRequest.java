package com.germanfica.wsfe.net;

import jakarta.xml.soap.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.io.ByteArrayOutputStream;


import java.util.Map;

import static com.germanfica.wsfe.utils.ArcaWSAAUtils.convertXmlToObject;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseApiRequest {
    private final String soapAction;
    private final byte[] payload;
    private final String namespace;
    private final String operation;
    private final Map<String, String> bodyElements;
    private final String endpoint;
    private final Class<?> responseType;

    @Getter(AccessLevel.PROTECTED)
    private final SOAPConnectionFactory soapConnectionFactory;

    protected BaseApiRequest(String soapAction, byte[] payload, String namespace, String operation, Map<String, String> bodyElements, String endpoint, Class<?> responseType) {
        this.soapAction = soapAction;
        this.payload = payload;
        this.namespace = namespace;
        this.operation = operation;
        this.bodyElements = bodyElements;
        this.endpoint = endpoint;
        this.responseType = responseType;
        try {
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
    protected <T> T request(String soapAction, byte[] payload, String namespace, String operation,
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
     * Envía una solicitud SOAP utilizando la información de la instancia de ApiRequest
     * y mapea la respuesta al tipo de dato especificado.
     *
     * @param apiRequest Instancia de ApiRequest que contiene los detalles de la solicitud SOAP.
     * @param responseType Clase del tipo objetivo para mapear la respuesta.
     * @param <T> Tipo genérico que define el tipo esperado en la respuesta.
     * @return Una instancia del tipo especificado mapeada desde la respuesta SOAP.
     * @throws Exception Si ocurre algún error al crear, enviar o procesar la solicitud SOAP.
     */
    public <T> T request(ApiRequest apiRequest, Class<T> responseType) throws Exception {
        // Crear mensaje SOAP utilizando los valores de ApiRequest
        SOAPMessage soapMessage = createSoapMessage(
                apiRequest.getSoapAction(),
                apiRequest.getPayload(),
                apiRequest.getNamespace(),
                apiRequest.getOperation(),
                apiRequest.getBodyElements()
        );

        // Enviar solicitud
        SOAPMessage soapResponse = sendSoapRequest(soapMessage, apiRequest.getEndpoint());

        // Procesar respuesta
        String xmlResponse = extractResponse(soapResponse);

        // Mapear la respuesta al tipo solicitado
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

    /**
     * Extraer la respuesta del mensaje SOAP como cadena XML completa.
     *
     * @param soapMessage El mensaje SOAP de respuesta.
     * @return Respuesta como cadena XML.
     * @throws SOAPException Si ocurre un error al procesar el mensaje.
     */
    protected String extractResponseXml(SOAPMessage soapMessage) throws SOAPException {
        try {
            // Convertir el cuerpo del mensaje SOAP a un XML completo como cadena
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            soapMessage.writeTo(outputStream);
            return outputStream.toString("UTF-8");
        } catch (Exception e) {
            throw new SOAPException("Error al extraer contenido XML del cuerpo SOAP", e);
        }
    }
}
