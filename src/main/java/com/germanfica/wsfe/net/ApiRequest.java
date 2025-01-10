package com.germanfica.wsfe.net;

import lombok.Getter;


import java.util.Map;

/** Clase base para realizar solicitudes API */
public class ApiRequest extends BaseApiRequest {
    @Getter
    private final String soapAction;
    private final byte[] payload;
    private final String namespace;
    private final String operation;
    private final Map<String, String> bodyElements;
    private final String endpoint;
    private final Class<?> responseType;

    public ApiRequest(String soapAction, byte[] payload, String namespace, String operation, Map<String, String> bodyElements, String endpoint, Class<?> responseType) {
        super(soapAction, payload, namespace, operation, bodyElements, endpoint, responseType);
        this.soapAction = soapAction;
        this.payload = payload;
        this.namespace = namespace;
        this.operation = operation;
        this.bodyElements = bodyElements;
        this.endpoint = endpoint;
        this.responseType = responseType;
    }
}
