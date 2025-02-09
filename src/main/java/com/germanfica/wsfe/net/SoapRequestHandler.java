package com.germanfica.wsfe.net;


import com.germanfica.wsfe.exception.ApiException;

public interface SoapRequestHandler {
    <T> T handleRequest(ApiRequest apiRequest, RequestExecutor<T> executor) throws ApiException;
}
