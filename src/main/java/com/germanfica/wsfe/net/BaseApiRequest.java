package com.germanfica.wsfe.net;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Map;

/**
 * Clase base para representar una solicitud SOAP.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseApiRequest {
    private final String soapAction;
    private final byte[] payload;
    private final String namespace;
    private final String operation;
    private final Map<String, String> bodyElements;
    private final String endpoint;
    private final Class<?> responseType;
}
