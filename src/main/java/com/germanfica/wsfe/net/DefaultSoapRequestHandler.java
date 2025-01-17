package com.germanfica.wsfe.net;

import jakarta.xml.soap.SOAPMessage;

public class DefaultSoapRequestHandler implements SoapRequestHandler {

    private final SoapClient soapClient;

    public DefaultSoapRequestHandler(SoapClient soapClient) {
        this.soapClient = (soapClient != null) ? soapClient : buildDefaultSoapClient();
    }

    /**
     * Método para construir un cliente SOAP predeterminado.
     *
     * @return Una instancia de SoapClient predeterminada.
     */
    private static SoapClient buildDefaultSoapClient() {
        // Configuración predeterminada para SoapClient.
        // Este es un ejemplo genérico; ajusta según las necesidades específicas de tu aplicación.
        return new DefaultSoapClient();
    }

    @Override
    public <T> T handleRequest(ApiRequest apiRequest, Class<T> responseType) throws Exception {
        SOAPMessage soapMessage = soapClient.createSoapMessage(
                apiRequest.getSoapAction(),
                apiRequest.getPayload(),
                apiRequest.getNamespace(),
                apiRequest.getOperation(),
                apiRequest.getBodyElements()
        );

        System.out.println(soapMessage);

        SOAPMessage soapResponse = soapClient.sendSoapRequest(soapMessage, apiRequest.getEndpoint());

        System.out.println(apiRequest.getEndpoint());

        String xmlResponse = soapClient.extractResponse(soapResponse);

        System.out.println(xmlResponse);



        return ApiResource.mapToDto(xmlResponse, responseType);
    }
}
