package com.germanfica.wsfe.service;

import com.germanfica.wsfe.cms.Cms;
import com.germanfica.wsfe.exception.ApiException;
import com.germanfica.wsfe.net.ApiService;
import com.germanfica.wsfe.net.SoapRequestHandler;
import https.wsaa_afip_gov_ar.ws.services.logincms.LoginCMS;
import https.wsaa_afip_gov_ar.ws.services.logincms.LoginCMSService;
import jakarta.xml.ws.BindingProvider;

public class AuthService extends ApiService {
    // URL del WSDL de AFIP para LoginCms en Homologación
    private static final String URL = "https://wsaahomo.afip.gov.ar/ws/services/LoginCms";

    private final LoginCMS port;

    public AuthService(SoapRequestHandler soapRequestHandler) throws ApiException {
        super(soapRequestHandler);
        // Inicializar el servicio SOAP
        LoginCMSService service = new LoginCMSService(); // usa el WSDL embebido (de producción)
        port = service.getLoginCms();

        // Sobrescribir el endpoint para usar homologación (aunque la clase es de producción)
        ((BindingProvider) this.port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, URL);
    }

    /**
     * Envía el CMS firmado a AFIP para obtener el Ticket de Autorización (TA).
     *
     * @param cmsFirmado CMS firmado digitalmente con la clave privada de la empresa.
     * @return Token de Autorización (TA) en formato XML.
     */
    public String autenticar(String cmsFirmado) throws ApiException {
        //return port.loginCms(cmsFirmado);
        return this.request(null, () -> port.loginCms(cmsFirmado));
    }

    public String autenticar(Cms cms) throws ApiException {
        //return port.loginCms(cmsFirmado);
        return this.request(null, () -> port.loginCms(cms.getSignedValue()));
    }
}
