package com.germanfica.wsfe.net;

import jakarta.xml.soap.*;
import java.io.ByteArrayOutputStream;
import java.util.Map;

public class DefaultSoapClient extends SoapClient {

    public DefaultSoapClient() {
        super(); // Inicializa SoapClient con la configuración predeterminada
    }

    @Override
    public SOAPMessage createSoapMessage(String soapAction, byte[] payload, String namespace, String operation, Map<String, String> bodyElements) throws SOAPException {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();

        // Crear el cuerpo del mensaje SOAP
        SOAPEnvelope envelope = soapMessage.getSOAPPart().getEnvelope();
        SOAPBody body = envelope.getBody();
        SOAPElement operationElement = body.addChildElement(operation, "ns1", namespace);

        // Agregar elementos al cuerpo del mensaje
        for (Map.Entry<String, String> entry : bodyElements.entrySet()) {
            operationElement.addChildElement(entry.getKey()).addTextNode(entry.getValue());
        }

        // Configurar encabezados del mensaje
        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", soapAction);

        // Guardar cambios en el mensaje
        soapMessage.saveChanges();
        return soapMessage;
    }

    @Override
    public SOAPMessage sendSoapRequest(SOAPMessage soapMessage, String endpoint) throws SOAPException {
        try (SOAPConnection connection = SOAPConnectionFactory.newInstance().createConnection()) {
            return connection.call(soapMessage, endpoint);
        }
    }

    @Override
    public String extractResponse(SOAPMessage soapResponse) throws SOAPException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            soapResponse.writeTo(outputStream);
            return outputStream.toString("UTF-8");
        } catch (Exception e) {
            throw new SOAPException("Error al extraer la respuesta SOAP", e);
        }
    }
}
