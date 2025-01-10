package com.germanfica.wsfe.service;

import com.germanfica.wsfe.exception.ApiException;
import com.germanfica.wsfe.net.ApiObjectInterface;
import com.germanfica.wsfe.net.ApiRequest;
import com.germanfica.wsfe.net.ApiResponseGetter;
import lombok.AccessLevel;
import lombok.Getter;

import java.lang.reflect.Type;

/** Clase base para todos los servicios SOAP */
public abstract class SoapService {
//    @Getter(AccessLevel.PROTECTED)
//    private final ApiResponseGetter responseGetter;
//
//    protected SoapService(ApiResponseGetter responseGetter) {
//        this.responseGetter = responseGetter;
//    }

//    @SuppressWarnings("TypeParameterUnusedInFormals")
//    protected <T extends ApiObjectInterface> T request(ApiRequest request, Type typeToken)
//            throws ApiException {
//        return this.getResponseGetter().request(request, typeToken);
//    }

    protected <T> T request(ApiRequest request, Class<T> responseType)
            throws Exception {
        return request.request(request, responseType);
    }
}
