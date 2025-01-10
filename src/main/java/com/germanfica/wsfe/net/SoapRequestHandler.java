package com.germanfica.wsfe.net;


public interface SoapRequestHandler {
    <T> T handleRequest(ApiRequest apiRequest, Class<T> responseType) throws Exception;
}
