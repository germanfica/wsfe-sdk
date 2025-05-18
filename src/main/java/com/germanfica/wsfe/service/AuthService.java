package com.germanfica.wsfe.service;

import com.germanfica.wsfe.cms.Cms;
import com.germanfica.wsfe.exception.ApiException;
import com.germanfica.wsfe.net.ApiService;
import com.germanfica.wsfe.net.SoapRequestHandler;
import https.wsaa_afip_gov_ar.ws.services.logincms.LoginCMS;

public class AuthService extends ApiService {
    public AuthService(SoapRequestHandler soapRequestHandler) throws ApiException {
        super(soapRequestHandler);
    }

    /**
     * Envía el CMS firmado a AFIP para obtener el Ticket de Autorización (TA).
     *
     * @param cmsFirmado CMS firmado digitalmente con la clave privada de la empresa.
     * @return Token de Autorización (TA) en formato XML.
     */
    public String autenticar(String cmsFirmado) throws ApiException {
        return this.invoke(null, LoginCMS.class, port -> port.loginCms(cmsFirmado));
    }

    public String autenticar(Cms cms) throws ApiException {
        return this.invoke(null, LoginCMS.class, port -> port.loginCms(cms.getSignedValue()));
    }
}
