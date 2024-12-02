package com.germanfica.wsfe.service;

import gov.afip.desein.dvadac.sua.view.wsaa.LoginCms;
import gov.afip.desein.dvadac.sua.view.wsaa.LoginCmsResponse;
import gov.afip.desein.dvadac.sua.view.wsaa.ObjectFactory;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import org.apache.commons.codec.binary.Base64;
import java.io.ByteArrayOutputStream;

@Service
public class SoapClientService {

    private final WebServiceTemplate webServiceTemplate;

    public SoapClientService(WebServiceTemplate webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
    }

    public String invokeWsaa(byte[] loginTicketRequestXmlCms, String endpoint) throws Exception {
        try {
            // Configurar el marshaller y unmarshaller
            Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
            marshaller.setPackagesToScan("gov.afip.desein.dvadac.sua.view.wsaa"); // Cambia por tu paquete generado
            webServiceTemplate.setMarshaller(marshaller);
            webServiceTemplate.setUnmarshaller(marshaller);

            // Crear la solicitud utilizando las clases generadas por JAXB
            ObjectFactory factory = new ObjectFactory(); // Cambia por la fábrica generada
            LoginCms request = factory.createLoginCms();

            String encodedRequest = Base64.encodeBase64String(loginTicketRequestXmlCms);
            request.setIn0(encodedRequest);

            // Enviar la solicitud y obtener la respuesta
            LoginCmsResponse response = (LoginCmsResponse) webServiceTemplate
                    .marshalSendAndReceive(endpoint, request, new SoapActionCallback("urn:loginCms"));

            return response.getLoginCmsReturn();

        } catch (SoapFaultClientException e) {
            // Manejo específico de errores SOAP
            return generateErrorXml("Error SOAP: " + e.getMessage());
        } catch (Exception e) {
            // Manejo genérico de otros errores
            return generateErrorXml("Error inesperado: " + e.getMessage());
        }
    }

    // Método para generar un XML con el mensaje de error
    private String generateErrorXml(String errorMessage) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<error>" +
                "<message>" + errorMessage + "</message>" +
                "</error>";
    }

    // Método auxiliar para convertir objetos a XML (opcional, para depuración)
    private String marshallObjectToXml(Object obj) throws JAXBException {
        // Usa el contexto de JAXB para la clase específica
        JAXBContext context = JAXBContext.newInstance(obj.getClass());
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        // Usa un OutputStream para capturar el XML
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            marshaller.marshal(obj, outputStream);
        } catch (JAXBException ex) {
            throw new JAXBException("Error al marshallizar el objeto a XML", ex);
        }
        return outputStream.toString(); // Convierte el contenido a String
    }
}
