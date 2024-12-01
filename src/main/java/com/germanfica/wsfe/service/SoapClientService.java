package com.germanfica.wsfe.service;

import gov.afip.desein.dvadac.sua.view.wsaa.LoginCms;
import gov.afip.desein.dvadac.sua.view.wsaa.LoginCmsResponse;
import gov.afip.desein.dvadac.sua.view.wsaa.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import java.util.Base64;

@Service
public class SoapClientService {

    private static final Logger logger = LoggerFactory.getLogger(SoapClientService.class);

    private final WebServiceTemplate webServiceTemplate;

    public SoapClientService(WebServiceTemplate webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
    }

    public String invokeWsaa(byte[] loginTicketRequestXmlCms, String endpoint) {
        try {
            // Configurar el marshaller y unmarshaller
            Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
            marshaller.setPackagesToScan("gov.afip.desein.dvadac.sua.view.wsaa");
            marshaller.afterPropertiesSet();
            webServiceTemplate.setMarshaller(marshaller);
            webServiceTemplate.setUnmarshaller(marshaller);

            // Crear la solicitud utilizando las clases generadas por JAXB
            ObjectFactory factory = new ObjectFactory();
            LoginCms request = factory.createLoginCms();
            String encodedRequest = Base64.getEncoder().encodeToString(loginTicketRequestXmlCms);
            request.setIn0(encodedRequest);

            logger.info("Preparando solicitud SOAP para el endpoint: {}", endpoint);
            logger.debug("Contenido Base64 de la solicitud: {}", encodedRequest);

            // Enviar la solicitud y obtener la respuesta
            LoginCmsResponse response = (LoginCmsResponse) webServiceTemplate
                    .marshalSendAndReceive(endpoint, request, new SoapActionCallback("urn:loginCms"));

            logger.info("Respuesta recibida correctamente del WSAA.");
            return response.getLoginCmsReturn();

        } catch (SoapFaultClientException e) {
            logger.error("Error SOAP: {}", e.getFaultStringOrReason(), e);
            throw new RuntimeException("Error al invocar el WSAA: " + e.getFaultStringOrReason(), e);
        } catch (Exception e) {
            logger.error("Error inesperado al invocar el WSAA", e);
            throw new RuntimeException("Error inesperado al invocar el WSAA", e);
        }
    }
}
