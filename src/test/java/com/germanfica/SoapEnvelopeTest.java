package com.germanfica;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.germanfica.wsfe.model.soap.envelope.FaultDetail;
import com.germanfica.wsfe.model.soap.envelope.SoapBody;
import com.germanfica.wsfe.model.soap.envelope.SoapEnvelope;
import com.germanfica.wsfe.model.soap.envelope.SoapFault;
import com.sun.xml.messaging.saaj.soap.Envelope;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;



import java.io.StringReader;
import java.io.StringWriter;

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

    @Test
    void testPrintSoapEnvelope() {
        // Crear instancia de SoapEnvelope y asignar valores de ejemplo
        SoapEnvelope soapEnvelope = new SoapEnvelope();
        SoapBody soapBody = new SoapBody();
        SoapFault soapFault = new SoapFault();
        FaultDetail faultDetail = new FaultDetail();

        // Configurar los valores del objeto SoapEnvelope
        faultDetail.setExceptionName("gov.afip.desein.dvadac.sua.view.wsaa.LoginFault");
        faultDetail.setHostname("wsaaext0.homo.afip.gov.ar");
        soapFault.setFaultCode("ns1:coe.alreadyAuthenticated");
        soapFault.setFaultString("El CEE ya posee un TA valido para el acceso al WSN solicitado");
        soapFault.setDetail(faultDetail);
        soapBody.setFault(soapFault);
        soapEnvelope.setBody(soapBody);

        // Crear contexto JAXB y configurar el marshaller
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(SoapEnvelope.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            // Convertir el objeto a XML
            StringWriter xmlWriter = new StringWriter();
            marshaller.marshal(soapEnvelope, xmlWriter);

            // Verificar el XML generado
            String generatedXml = xmlWriter.toString();
            System.out.println("XML generado: \n" + generatedXml);

            // Verificar algunos fragmentos clave del XML
            Assertions.assertTrue(generatedXml.contains("<ns3:Envelope"), "El XML generado no contiene el nodo Envelope");
            Assertions.assertTrue(generatedXml.contains("<ns3:Body"), "El XML generado no contiene el nodo Body");
            Assertions.assertTrue(generatedXml.contains("ns1:coe.alreadyAuthenticated"), "El XML generado no contiene el faultcode esperado");
            Assertions.assertTrue(generatedXml.contains("El CEE ya posee un TA valido para el acceso al WSN solicitado"), "El XML generado no contiene el faultstring esperado");
        } catch (JAXBException e) {
            Assertions.fail("Error durante la serialización: " + e.getMessage());
        }
    }
}
