package com.germanfica.wsfe.net;

import lombok.Getter;

/** Clase base para realizar solicitudes API */
@Getter
public class ApiRequest extends BaseApiRequest {
    private final String token;
    private final String sign;
    private final Long cuit;
    private final String apiBase;

    public ApiRequest(String token, String sign, Long cuit, String apiBase) {
        super(token, sign, cuit, apiBase);
        this.token = token;
        this.sign = sign;
        this.cuit = cuit;
        this.apiBase = apiBase;
    }
}
