package com.germanfica.wsfe.service;

import com.germanfica.wsfe.net.ApiService;
import com.germanfica.wsfe.net.SoapRequestHandler;
import https.wsaahomo_afip_gov_ar.ws.services.logincms.LoginCMS;
import https.wsaahomo_afip_gov_ar.ws.services.logincms.LoginCMSService;
import https.wsaahomo_afip_gov_ar.ws.services.logincms.LoginFault;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URL;

public class AuthService extends ApiService {
    // URL del WSDL de AFIP para LoginCms en Homologación
    private static final String WSDL_URL = "https://wsaahomo.afip.gov.ar/ws/services/LoginCms?WSDL";
    private static final QName SERVICE_NAME = new QName("https://wsaahomo.afip.gov.ar/ws/services/LoginCms", "LoginCMSService");

    private final LoginCMS port;

    public AuthService(SoapRequestHandler soapRequestHandler) throws MalformedURLException {
        super(soapRequestHandler);
        // Inicializar el servicio SOAP
        LoginCMSService service = new LoginCMSService(new URL(WSDL_URL), SERVICE_NAME);
        this.port = service.getLoginCms();
    }

    /**
     * Envía el CMS firmado a AFIP para obtener el Ticket de Autorización (TA).
     *
     * @param cmsFirmado CMS firmado digitalmente con la clave privada de la empresa.
     * @return Token de Autorización (TA) en formato XML.
     */
    public String autenticar(String cmsFirmado) throws LoginFault {
        return port.loginCms(cmsFirmado);
    }
}
