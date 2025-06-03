package com.germanfica;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.germanfica.wsfe.exception.XmlMappingException;
import com.germanfica.wsfe.util.XmlExtractor;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.Test;
import java.io.StringReader;

public class SoapEnvelopeTest {

    @Test
    void testXMLExtractor() throws Exception {
        String xmlResponse =
                """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <loginTicketResponse version="1.0">
                    <header>
                        <source>CN=wsaahomo, O=AFIP, C=AR, SERIALNUMBER=CUIT 33693450239</source>
                        <destination>SERIALNUMBER=CUIT xxxxxxxxxxxx, CN=test</destination>
                        <uniqueId>xxxxxxxxx</uniqueId>
                        <generationTime>2025-02-08T15:13:46.507-03:00</generationTime>
                        <expirationTime>2025-02-09T03:13:46.507-03:00</expirationTime>
                    </header>
                    <credentials>
                        <token>asdasdasd==</token>
                        <sign>asdasd%%$==</sign>
                    </credentials>
                </loginTicketResponse>
                """;

        XmlExtractor extractor = new XmlExtractor(xmlResponse);
        String token = extractor.extractValue("/ loginTicketResponse/ credentials/ token");
        XmlExtractor. LoginTicketData data = extractor. extractLoginTicketData();

        // Imprimir resultados
        System.out.println("Respuesta de autenticación xml: \n" + xmlResponse);
        System.out.println("Respuesta de autenticación json: \n" + data);
        System.out.println("\nRespuesta de token json: \n" + data.token);
        System.out.println("\nToken: \n" + token);
    }

    /**
     * Converts an XML string to an object of the specified type using Unmarshaller.
     *
     * @param xmlString the XML string to convert
     * @param clazz     the class type of the object
     * @param <T>       the type parameter
     * @return the deserialized object of type T
     * @throws XmlMappingException if an error occurs during deserialization
     */
    private static <T> T convertXmlToObjectWithUnmarshaller(String xmlString, Class<T> clazz) throws XmlMappingException {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            var unmarshaller = jaxbContext.createUnmarshaller();
            return (T) unmarshaller.unmarshal(new StringReader(xmlString));
        } catch (JAXBException e) {
            throw new XmlMappingException("Error mapping XML to object with Unmarshaller", xmlString, e);
        }
    }

     /**
     * Converts an XML string to an object of the specified type.
     *
     * @param xmlString the XML string to convert
     * @param clazz     the class type of the object
     * @param <T>       the type parameter
     * @return the deserialized object of type T
     * @throws Exception if an error occurs during deserialization
     */
     private static <T> T convertXmlToObject(String xmlString, Class<T> clazz) throws XmlMappingException {
        XmlMapper xmlMapper = new XmlMapper();
        try {
            return xmlMapper.readValue(xmlString, clazz);
        } catch (Exception e) {
            throw new XmlMappingException("Error mapping XML to object", xmlString, e);
        }
    }
}
