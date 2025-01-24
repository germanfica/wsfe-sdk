package com.germanfica.wsfe.net;

import jakarta.xml.soap.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.io.ByteArrayOutputStream;


import java.util.Map;

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
