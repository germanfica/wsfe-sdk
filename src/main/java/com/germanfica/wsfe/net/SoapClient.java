package com.germanfica.wsfe.net;

import jakarta.xml.soap.*;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class SoapClient {

    private final SOAPConnectionFactory soapConnectionFactory;

    public SoapClient() {
        try {
            this.soapConnectionFactory = SOAPConnectionFactory.newInstance();
        } catch (SOAPException e) {
            throw new RuntimeException("Error al inicializar SOAPConnectionFactory", e);
        }
    }

    public SOAPMessage createSoapMessage(String soapAction, byte[] payload, String namespace, String operation, Map<String, String> bodyElements) throws SOAPException {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();

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

    public SOAPMessage sendSoapRequest(SOAPMessage soapMessage, String endpoint) throws SOAPException {
        try (SOAPConnection connection = soapConnectionFactory.createConnection()) {
            return connection.call(soapMessage, endpoint);
        }
    }

    public String extractResponse(SOAPMessage soapMessage) throws SOAPException {
        try {
            return soapMessage.getSOAPBody().getTextContent();
        } catch (Exception e) {
            throw new SOAPException("Error al extraer contenido del cuerpo SOAP", e);
        }
    }
}
