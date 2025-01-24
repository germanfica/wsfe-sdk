package com.germanfica;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.germanfica.wsfe.exception.XmlMappingException;
import com.germanfica.wsfe.model.soap.loginticket.CredentialsType;
import com.germanfica.wsfe.model.soap.loginticket.HeaderType;
import com.germanfica.wsfe.model.soap.loginticket.LoginTicketResponseType;
import com.germanfica.wsfe.utils.ArcaDateTimeUtils;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import javax.xml.datatype.DatatypeConfigurationException;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SoapEnvelopeTest {

    @Test
    void testConvertXmlToObjectLoginTicketResponseType() throws Exception {
        String xmlResponse =
                """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <loginTicketResponse version="1.0">
                    <header>
                        <source>CN=wsaahomo, O=AFIP, C=AR, SERIALNUMBER=CUIT 33693450239</source>
                        <destination>SERIALNUMBER=CUIT xxxxxxxxxxx, CN=test</destination>
                        <uniqueId>2610407892</uniqueId>
                        <generationTime>2025-01-21T18:36:40.770-03:00</generationTime>
                        <expirationTime>2025-01-22T06:36:40.770-03:00</expirationTime>
                    </header>
                    <credentials>
                        <token>xxxxxxxxxxxxxxxxxxxxxxxxx</token>
                        <sign>xxxxxxxxxxxxxxxxxxxxxxxxx</sign>
                    </credentials>
                </loginTicketResponse>
                """;

        LoginTicketResponseType loginTicketResponse = convertXmlToObjectWithUnmarshaller(xmlResponse, LoginTicketResponseType.class);

        assertEquals("CN=wsaahomo, O=AFIP, C=AR, SERIALNUMBER=CUIT 33693450239", loginTicketResponse.getHeader().getSource());
        assertEquals("SERIALNUMBER=CUIT xxxxxxxxxxx, CN=test", loginTicketResponse.getHeader().getDestination());
        assertEquals("2610407892", loginTicketResponse.getHeader().getUniqueId());
        assertEquals("2025-01-21T18:36:40.770-03:00", ArcaDateTimeUtils.formatDateTime(loginTicketResponse.getHeader().getGenerationTime(), ArcaDateTimeUtils.DateTimeFormat.ISO_8601_FULL, ArcaDateTimeUtils.TimeZoneOffset.ARGENTINA));
        assertEquals("2025-01-22T06:36:40.770-03:00", ArcaDateTimeUtils.formatDateTime(loginTicketResponse.getHeader().getExpirationTime(), ArcaDateTimeUtils.DateTimeFormat.ISO_8601_FULL, ArcaDateTimeUtils.TimeZoneOffset.ARGENTINA));
        assertEquals("xxxxxxxxxxxxxxxxxxxxxxxxx", loginTicketResponse.getCredentials().getToken());
        assertEquals("xxxxxxxxxxxxxxxxxxxxxxxxx", loginTicketResponse.getCredentials().getSign());
    }

    @Test
    void testConvertXmlToObjectLoginTicketResponseTypeWithSoapEnvelope() throws Exception {
        String xmlResponse =
                """
                <?xml version="1.0" encoding="utf-8"?><soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"><soapenv:Body><loginCmsResponse xmlns="http://wsaa.view.sua.dvadac.desein.afip.gov.ar/"><loginCmsReturn><?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <loginTicketResponse version="1.0">
                    <header>
                        <source>CN=wsaahomo, O=AFIP, C=AR, SERIALNUMBER=CUIT 33693450239</source>
                        <destination>SERIALNUMBER=CUIT xxxxxxxxxxx, CN=test</destination>
                        <uniqueId>283060288</uniqueId>
                        <generationTime>2025-01-22T00:23:57.202-03:00</generationTime>
                        <expirationTime>2025-01-22T12:23:57.202-03:00</expirationTime>
                    </header>
                    <credentials>
                        <token>xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx</token>
                        <sign>xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx</sign>
                    </credentials>
                </loginTicketResponse>
                </loginCmsReturn></loginCmsResponse></soapenv:Body></soapenv:Envelope>
                """;

        LoginTicketResponseType loginTicketResponse = convertXmlToObject(xmlResponse, LoginTicketResponseType.class);

        assertEquals("CN=wsaahomo, O=AFIP, C=AR, SERIALNUMBER=CUIT 33693450239", loginTicketResponse.getHeader().getSource());
        assertEquals("SERIALNUMBER=CUIT xxxxxxxxxxx, CN=test", loginTicketResponse.getHeader().getDestination());
        assertEquals("2610407892", loginTicketResponse.getHeader().getUniqueId());
        assertEquals("2025-01-21T18:36:40.770-03:00", ArcaDateTimeUtils.formatDateTime(loginTicketResponse.getHeader().getGenerationTime(), ArcaDateTimeUtils.DateTimeFormat.ISO_8601_FULL, ArcaDateTimeUtils.TimeZoneOffset.ARGENTINA));
        assertEquals("2025-01-22T06:36:40.770-03:00", ArcaDateTimeUtils.formatDateTime(loginTicketResponse.getHeader().getExpirationTime(), ArcaDateTimeUtils.DateTimeFormat.ISO_8601_FULL, ArcaDateTimeUtils.TimeZoneOffset.ARGENTINA));
        assertEquals("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", loginTicketResponse.getCredentials().getToken());
        assertEquals("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", loginTicketResponse.getCredentials().getSign());
    }

    @Test
    void testConvertXmlToObjectLoginTicketResponseTypeWithNoSoapEnvelope() throws Exception {
        String xmlResponse =
                """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <loginTicketResponse version="1.0">
                    <header>
                        <source>CN=wsaahomo, O=AFIP, C=AR, SERIALNUMBER=CUIT 33693450239</source>
                        <destination>SERIALNUMBER=CUIT xxxxxxxxxxx, CN=test</destination>
                        <uniqueId>283060288</uniqueId>
                        <generationTime>2025-01-22T00:23:57.202-03:00</generationTime>
                        <expirationTime>2025-01-22T12:23:57.202-03:00</expirationTime>
                    </header>
                    <credentials>
                        <token>xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx</token>
                        <sign>xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx</sign>
                    </credentials>
                </loginTicketResponse>
                </loginCmsReturn>
                """;

        LoginTicketResponseType loginTicketResponse = convertXmlToObject(xmlResponse, LoginTicketResponseType.class);

        assertEquals("CN=wsaahomo, O=AFIP, C=AR, SERIALNUMBER=CUIT 33693450239", loginTicketResponse.getHeader().getSource());
        assertEquals("SERIALNUMBER=CUIT xxxxxxxxxxx, CN=test", loginTicketResponse.getHeader().getDestination());
        assertEquals("283060288", loginTicketResponse.getHeader().getUniqueId());
        assertEquals("2025-01-22T00:23:57.202-03:00", ArcaDateTimeUtils.formatDateTime(loginTicketResponse.getHeader().getGenerationTime(), ArcaDateTimeUtils.DateTimeFormat.ISO_8601_FULL, ArcaDateTimeUtils.TimeZoneOffset.ARGENTINA));
        assertEquals("2025-01-22T12:23:57.202-03:00", ArcaDateTimeUtils.formatDateTime(loginTicketResponse.getHeader().getExpirationTime(), ArcaDateTimeUtils.DateTimeFormat.ISO_8601_FULL, ArcaDateTimeUtils.TimeZoneOffset.ARGENTINA));
        assertEquals("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", loginTicketResponse.getCredentials().getToken());
        assertEquals("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", loginTicketResponse.getCredentials().getSign());
    }



    @Test
    void testUnmarshalLoginTicketResponseType() throws JAXBException {
        // XML de prueba
        String xmlResponse = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <loginTicketResponse version="1.0">
                <header>
                    <source>CN=wsaahomo, O=AFIP, C=AR, SERIALNUMBER=CUIT 33693450239</source>
                    <destination>SERIALNUMBER=CUIT xxxxxxxxxxx, CN=test</destination>
                    <uniqueId>2610407892</uniqueId>
                    <generationTime>2025-01-21T18:36:40.770-03:00</generationTime>
                    <expirationTime>2025-01-22T06:36:40.770-03:00</expirationTime>
                </header>
                <credentials>
                    <token>xxxxxxxxxxxxxxxxxxxxxxxxx</token>
                    <sign>xxxxxxxxxxxxxxxxxxxxxxxxx</sign>
                </credentials>
            </loginTicketResponse>
                """;

        // Configuración del contexto JAXB
        JAXBContext jaxbContext = JAXBContext.newInstance(LoginTicketResponseType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        // Parseo del XML
        StringReader reader = new StringReader(xmlResponse);

        LoginTicketResponseType loginTicketResponse = (LoginTicketResponseType) unmarshaller.unmarshal(reader);

        // Verificación de resultados
        assertEquals("CN=wsaahomo, O=AFIP, C=AR, SERIALNUMBER=CUIT 33693450239", loginTicketResponse.getHeader().getSource());
        assertEquals("SERIALNUMBER=CUIT xxxxxxxxxxx, CN=test", loginTicketResponse.getHeader().getDestination());
        assertEquals("2610407892", loginTicketResponse.getHeader().getUniqueId());
        assertEquals("2025-01-21T18:36:40.770-03:00", loginTicketResponse.getHeader().getGenerationTime().toString());
        assertEquals("2025-01-22T06:36:40.770-03:00", loginTicketResponse.getHeader().getExpirationTime().toString());

        assertEquals("xxxxxxxxxxxxxxxxxxxxxxxxx", loginTicketResponse.getCredentials().getToken());
        assertEquals("xxxxxxxxxxxxxxxxxxxxxxxxx", loginTicketResponse.getCredentials().getSign());
    }

    @Test
    void testPrintLoginTicketResponseType() throws DatatypeConfigurationException {
        // Crear instancia de LoginTicketResponseType y asignar valores de ejemplo
        var loginTicketResponse = new LoginTicketResponseType();
        var header = new HeaderType();
        var credentials = new CredentialsType();

        // Configurar los valores del objeto LoginTicketResponseType
        header.setSource("TestSource");
        header.setDestination("TestDestination");
        header.setUniqueId("12345");
        header.setGenerationTime(javax.xml.datatype.DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-01-01T12:00:00Z"));
        header.setExpirationTime(javax.xml.datatype.DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-01-02T12:00:00Z"));

        credentials.setToken("TestToken");
        credentials.setSign("TestSign");

        loginTicketResponse.setHeader(header);
        loginTicketResponse.setCredentials(credentials);
        loginTicketResponse.setVersion("1.0");

        // Crear contexto JAXB y configurar el marshaller
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(LoginTicketResponseType.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            // Convertir el objeto a XML
            StringWriter xmlWriter = new StringWriter();
            marshaller.marshal(loginTicketResponse, xmlWriter);

            // Verificar el XML generado
            String generatedXml = xmlWriter.toString();
            System.out.println("XML generado: \n" + generatedXml);

            // Verificar algunos fragmentos clave del XML
            Assertions.assertTrue(generatedXml.contains("<header>"), "El XML generado no contiene el nodo header");
            Assertions.assertTrue(generatedXml.contains("<source>TestSource</source>"), "El XML generado no contiene el source esperado");
            Assertions.assertTrue(generatedXml.contains("<destination>TestDestination</destination>"), "El XML generado no contiene el destination esperado");
            Assertions.assertTrue(generatedXml.contains("<credentials>"), "El XML generado no contiene el nodo credentials");
            Assertions.assertTrue(generatedXml.contains("<token>TestToken</token>"), "El XML generado no contiene el token esperado");
        } catch (JAXBException e) {
            Assertions.fail("Error durante la serialización: " + e.getMessage());
        }
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
