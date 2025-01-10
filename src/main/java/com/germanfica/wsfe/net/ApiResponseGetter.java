package com.germanfica.wsfe.net;

import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;

public interface ApiResponseGetter {
    /**
     * Envía una solicitud basada en una instancia de BaseApiRequest y devuelve la respuesta procesada.
     *
     * @param request La solicitud que hereda de BaseApiRequest.
     * @param responseType La clase del tipo esperado de respuesta.
     * @param <T> Tipo genérico para la respuesta.
     * @return Una instancia del tipo de respuesta mapeada.
     * @throws Exception Si ocurre un error al procesar la solicitud.
     */
    <T> T request(BaseApiRequest request, Class<T> responseType) throws Exception;

    /**
     * Envía una solicitud basada en una instancia de ApiRequest y devuelve la respuesta procesada.
     *
     * @param request La solicitud de tipo ApiRequest.
     * @param <T> Tipo genérico para la respuesta.
     * @return Una instancia del tipo de respuesta mapeada.
     * @throws Exception Si ocurre un error al procesar la solicitud.
     */
    <T> T request(ApiRequest request) throws Exception;

    /**
     * Envía un mensaje SOAP directamente y devuelve la respuesta.
     *
     * @param soapMessage El mensaje SOAP a enviar.
     * @param endpoint El endpoint del servicio SOAP.
     * @return La respuesta SOAP.
     * @throws SOAPException Si ocurre un error durante la comunicación SOAP.
     */
    SOAPMessage sendRequest(SOAPMessage soapMessage, String endpoint) throws SOAPException;
}
