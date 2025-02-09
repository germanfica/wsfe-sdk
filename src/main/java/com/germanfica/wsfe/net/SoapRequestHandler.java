package com.germanfica.wsfe.net;


public interface SoapRequestHandler {
    <T> T handleRequest(ApiRequest apiRequest, RequestExecutor<T> executor) throws Exception;
}
