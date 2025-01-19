package com.germanfica;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.germanfica.wsfe.model.soap.envelope.SoapEnvelope;
import com.sun.xml.messaging.saaj.soap.Envelope;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.junit.jupiter.api.Test;


import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SoapEnvelopeTest {

    @Test
    void testUnmarshalSoapEnvelope() throws JAXBException {
        // XML de prueba
        String xmlResponse = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                   <soapenv:Body>
                      <soapenv:Fault>
                         <faultcode xmlns:ns1="http://xml.apache.org/axis/">ns1:coe.alreadyAuthenticated</faultcode>
                         <faultstring>El CEE ya posee un TA valido para el acceso al WSN solicitado</faultstring>
                         <detail>
                            <ns2:exceptionName xmlns:ns2="http://xml.apache.org/axis/">gov.afip.desein.dvadac.sua.view.wsaa.LoginFault</ns2:exceptionName>
                            <ns3:hostname xmlns:ns3="http://xml.apache.org/axis/">wsaaext0.homo.afip.gov.ar</ns3:hostname>
                         </detail>
                      </soapenv:Fault>
                   </soapenv:Body>
                </soapenv:Envelope>
                """;

        // Configuración del contexto JAXB
        JAXBContext jaxbContext = JAXBContext.newInstance(SoapEnvelope.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        // Parseo del XML
        StringReader reader = new StringReader(xmlResponse);
        SoapEnvelope envelope = (SoapEnvelope) unmarshaller.unmarshal(reader);

        // Verificación de resultados
        assertEquals("ns1:coe.alreadyAuthenticated", envelope.getBody().getFault().getFaultCode());
        assertEquals("El CEE ya posee un TA valido para el acceso al WSN solicitado", envelope.getBody().getFault().getFaultString());
        assertEquals("gov.afip.desein.dvadac.sua.view.wsaa.LoginFault", envelope.getBody().getFault().getDetail().getExceptionName());
        assertEquals("wsaaext0.homo.afip.gov.ar", envelope.getBody().getFault().getDetail().getHostname());
    }


    @Test
    void testConvertXmlToObject() throws Exception  {
        // XML de prueba
        String xmlResponse = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                   <soapenv:Body>
                      <soapenv:Fault>
                         <faultcode xmlns:ns1="http://xml.apache.org/axis/">ns1:coe.alreadyAuthenticated</faultcode>
                         <faultstring>El CEE ya posee un TA valido para el acceso al WSN solicitado</faultstring>
                         <detail>
                            <ns2:exceptionName xmlns:ns2="http://xml.apache.org/axis/">gov.afip.desein.dvadac.sua.view.wsaa.LoginFault</ns2:exceptionName>
                            <ns3:hostname xmlns:ns3="http://xml.apache.org/axis/">wsaaext0.homo.afip.gov.ar</ns3:hostname>
                         </detail>
                      </soapenv:Fault>
                   </soapenv:Body>
                </soapenv:Envelope>
                """;

        // Conversión directa del XML a un objeto
        XmlMapper xmlMapper = new XmlMapper();
        SoapEnvelope envelope = xmlMapper.readValue(xmlResponse, SoapEnvelope.class);

        // Verificación de resultados
        assertEquals("ns1:coe.alreadyAuthenticated", envelope.getBody().getFault().getFaultCode());
        assertEquals("El CEE ya posee un TA valido para el acceso al WSN solicitado", envelope.getBody().getFault().getFaultString());
        assertEquals("gov.afip.desein.dvadac.sua.view.wsaa.LoginFault", envelope.getBody().getFault().getDetail().getExceptionName());
        assertEquals("wsaaext0.homo.afip.gov.ar", envelope.getBody().getFault().getDetail().getHostname());
    }
}
