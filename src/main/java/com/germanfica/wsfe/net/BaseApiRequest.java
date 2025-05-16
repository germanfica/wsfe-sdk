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
    private final String token;
    private final String sign;
    private final Long cuit;
    private final String apiBase;
}
