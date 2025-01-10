package com.germanfica.wsfe.service;

import jakarta.xml.soap.*;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.Map;

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
}
