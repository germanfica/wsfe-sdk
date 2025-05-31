package com.germanfica.wsfe.net;

import fev1.dif.afip.gov.ar.ServiceSoap;
import https.wsaa_afip_gov_ar.ws.services.logincms.LoginCMS;

/** The base address to use for the request. */
public enum ApiEnvironment {
    PROD("https://wsaa.afip.gov.ar", "https://servicios1.afip.gov.ar"),
    HOMO("https://wsaahomo.afip.gov.ar", "https://wswhomo.afip.gov.ar");

    private final String wsaaUrl;
    private final String wsfeUrl;

    ApiEnvironment(String wsaaUrl, String wsfeUrl) {
        this.wsaaUrl = wsaaUrl;
        this.wsfeUrl = wsfeUrl;
    }

    public String getUrlFor(Class<?> portClass) {
        //if (portClass.getSimpleName().equals("LoginCMS")) return wsaaUrl;
        //if (portClass.getSimpleName().equals("ServiceSoap")) return wsfeUrl;
        if (portClass.equals(ServiceSoap.class)) return wsfeUrl;
        if (portClass.equals(LoginCMS.class)) return wsaaUrl;
        throw new IllegalArgumentException("No known service for port: " + portClass);
    }
}
