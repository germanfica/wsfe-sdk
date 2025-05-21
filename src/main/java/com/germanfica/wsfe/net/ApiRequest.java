package com.germanfica.wsfe.net;

import lombok.Getter;

/** Clase base para realizar solicitudes API */
@Getter
public class ApiRequest extends BaseApiRequest {

    protected ApiRequest(RequestOptions options) {
        super(options);
    }
}
