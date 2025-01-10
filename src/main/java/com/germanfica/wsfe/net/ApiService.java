package com.germanfica.wsfe.net;

import lombok.AccessLevel;
import lombok.Getter;

/** The base class for all services. */
public abstract class ApiService {
    @Getter(AccessLevel.PROTECTED)
    private final SoapRequestHandler soapRequestHandler;

    protected ApiService(SoapRequestHandler soapRequestHandler) {
        this.soapRequestHandler = soapRequestHandler;
    }

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
        //return request.request(request, responseType);
        return soapRequestHandler.handleRequest(request, responseType);
    }
}
