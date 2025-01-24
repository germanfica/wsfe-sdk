package com.germanfica.wsfe.net;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.germanfica.wsfe.exception.SoapProcessingException;
import com.germanfica.wsfe.exception.XmlMappingException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

public class SoapProcessor {

    /**
     * @deprecated This method is deprecated because it uses a direct string representation of the SOAP response.
     * Consider using {@link #processResponse(SOAPMessage, Class)} instead for better handling of SOAP messages.
     */
    @Deprecated
    public static <T> T processResponse(String xmlResponse, Class<T> targetClass) throws SoapProcessingException {
        try {
            // Detectar si es un SOAP Envelope
            if (xmlResponse.contains("<soapenv:Envelope")) {
                // Extraer el contenido relevante del Body
                String extractedContent = extractBodyContent(xmlResponse);
                return deserializeXml(extractedContent, targetClass);
            }
            // Si no es SOAP, deserializar directamente
            return deserializeXml(xmlResponse, targetClass);
        } catch (Exception e) {
            throw new SoapProcessingException("Error processing SOAP response", e);
        }
    }

    public static <T> T processResponse(SOAPMessage soapMessage, Class<T> targetClass) throws SoapProcessingException, XmlMappingException {
        try {
            String xmlResponse = extractResponse(soapMessage);
            return processResponse(xmlResponse, targetClass);
        } catch (Exception e) {
            throw new SoapProcessingException("Error processing SOAPMessage", e, soapMessage);
        }
    }

    private static String extractBodyContent(String xmlResponse) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xmlResponse)));

        Node bodyNode = document.getElementsByTagNameNS("http://schemas.xmlsoap.org/soap/envelope/", "Body").item(0);

        if (bodyNode == null) {
            throw new SoapProcessingException("SOAP Body not found in the response");
        }

        return bodyNode.getTextContent().trim();
    }

    private static <T> T deserializeXmlJAXB(String xml, Class<T> targetClass) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(targetClass);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return targetClass.cast(unmarshaller.unmarshal(new StringReader(xml)));
    }

    private static <T> T deserializeXml(String xml, Class<T> clazz) throws XmlMappingException {
        XmlMapper xmlMapper = new XmlMapper();
        try {
            return xmlMapper.readValue(xml, clazz);
        } catch (Exception e) {
            throw new XmlMappingException("Error mapping XML to object", xml, e);
        }
    }

    /**
     * Extraer la respuesta del mensaje SOAP como cadena XML completa.
     *
     * @param soapMessage El mensaje SOAP de respuesta.
     * @return Respuesta como cadena XML.
     * @throws SOAPException Si ocurre un error al procesar el mensaje.
     */
    private static String extractResponse(SOAPMessage soapMessage) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            soapMessage.writeTo(outputStream);
            return outputStream.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error extracting response from SOAPMessage", e);
        }
    }
}
