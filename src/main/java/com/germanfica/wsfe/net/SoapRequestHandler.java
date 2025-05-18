package com.germanfica.wsfe.net;


import com.germanfica.wsfe.exception.ApiException;

public interface SoapRequestHandler {
    <T> T handleRequest(ApiRequest apiRequest, RequestExecutor<T> executor) throws ApiException;
    <P, R> R invoke(ApiRequest apiRequest, Class<P> portClass, PortInvoker<P, R> invoker) throws ApiException;
}
