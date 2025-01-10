package com.germanfica.wsfe.config;

import com.germanfica.wsfe.net.BaseApiRequest;
import com.germanfica.wsfe.net.DefaultSoapRequestHandler;
import com.germanfica.wsfe.net.SoapRequestHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;

@Configuration
public class SoapClientConfig {
    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        // Set the package where your generated JAXB classes are located
        marshaller.setContextPath("gov.afip.desein.dvadac.sua.view.wsaa");
        return marshaller;
    }

    @Bean
    public WebServiceTemplate webServiceTemplate(Jaxb2Marshaller marshaller) {
        WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
        webServiceTemplate.setDefaultUri("https://wsaahomo.afip.gov.ar/ws/services/LoginCms");
        webServiceTemplate.setMarshaller(marshaller);
        webServiceTemplate.setUnmarshaller(marshaller);
        return webServiceTemplate;
    }

//    @Bean
//    public BaseApiRequest baseApiRequest() {
//        // Crear e inicializar la instancia de BaseApiRequest
//        return new BaseApiRequest();
//    }
//
//    @Bean
//    public SoapRequestHandler soapRequestHandler(BaseApiRequest baseApiRequest) {
//        // Pasar baseApiRequest al constructor de DefaultSoapRequestHandler
//        return new DefaultSoapRequestHandler(baseApiRequest);
//    }
}