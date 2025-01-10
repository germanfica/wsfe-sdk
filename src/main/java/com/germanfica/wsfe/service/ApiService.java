package com.germanfica.wsfe.service;

import com.germanfica.wsfe.exception.ApiException;
import com.germanfica.wsfe.net.ApiRequest;
import com.germanfica.wsfe.net.ApiResponseGetter;
import lombok.AccessLevel;
import lombok.Getter;

import java.io.InputStream;
import java.lang.reflect.Type;

/** The base class for all services. */
public abstract class ApiService {
    @Getter(AccessLevel.PROTECTED)
    private final ApiResponseGetter responseGetter;

    protected ApiService(ApiResponseGetter responseGetter) {
        this.responseGetter = responseGetter;
    }

//    @SuppressWarnings("TypeParameterUnusedInFormals")
//    protected <T extends ApiObjectInterface> T request(ApiRequest request, Type typeToken)
//            throws ApiException {
//        return this.getResponseGetter().request(request.addUsage("stripe_client"), typeToken);
//    }
//
//    protected InputStream requestStream(ApiRequest request) throws ApiException {
//        return this.getResponseGetter().requestStream(request.addUsage("stripe_client"));
//    }
}
