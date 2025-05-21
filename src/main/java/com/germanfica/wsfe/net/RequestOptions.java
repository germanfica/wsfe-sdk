package com.germanfica.wsfe.net;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = false)
@Getter
public class RequestOptions {
    private final String token;
    private final String sign;
    private final Long cuit;
    private final String urlBase;
    private final ApiEnvironment apiEnvironment;

    private RequestOptions(String token, String sign, Long cuit, String urlBase, ApiEnvironment apiEnvironment) {
        this.token = normalizeToken(token);
        this.sign = normalizeSign(sign);
        this.cuit = cuit;
        this.urlBase = normalizeUrlBase(urlBase);
        this.apiEnvironment = apiEnvironment;
    }

    public static RequestOptionsBuilder builder() {
        return new RequestOptionsBuilder();
    }

    public RequestOptionsBuilder toBuilder() {
        return new RequestOptionsBuilder()
            .setToken(this.token)
            .setSign(this.sign)
            .setCuit(this.cuit)
            .setUrlBase(this.urlBase);
    }

    private static String normalizeToken(String token) {
        if (token == null) return null;
        String trimmed = token.trim();
        if (trimmed.isEmpty()) throw new InvalidRequestOptionsException("Empty token specified!");
        return trimmed;
    }

    private static String normalizeSign(String sign) {
        if (sign == null) return null;
        String trimmed = sign.trim();
        if (trimmed.isEmpty()) throw new InvalidRequestOptionsException("Empty sign specified!");
        return trimmed;
    }

    private static String normalizeUrlBase(String urlBase) {
        if (urlBase == null) return null;
        String trimmed = urlBase.trim();
        if (trimmed.isEmpty()) throw new InvalidRequestOptionsException("Empty urlBase specified!");
        return trimmed;
    }

    public static class RequestOptionsBuilder {
        private String token;
        private String sign;
        private Long cuit;
        private String urlBase;
        private ApiEnvironment apiEnvironment;

        public RequestOptionsBuilder setToken(String token) {
            this.token = token;
            return this;
        }

        public RequestOptionsBuilder setSign(String sign) {
            this.sign = sign;
            return this;
        }

        public RequestOptionsBuilder setCuit(Long cuit) {
            this.cuit = cuit;
            return this;
        }

        public RequestOptionsBuilder setUrlBase(String urlBase) {
            this.urlBase = urlBase;
            return this;
        }
        public RequestOptionsBuilder setBaseAddress(ApiEnvironment apiEnvironment) {
            this.apiEnvironment = apiEnvironment;
            return this;
        }

        public RequestOptions build() {
            return new RequestOptions(token, sign, cuit, urlBase, apiEnvironment);
        }
    }

    public static RequestOptions merge(SoapResponseGetterOptions globalOptions, RequestOptions localOptions) {
        if (globalOptions == null && localOptions == null) {
            return null;
        }
        if (globalOptions == null) return localOptions;
        if (localOptions == null) {
            return new RequestOptionsBuilder()
//                .setToken(globalOptions.getToken())
//                .setSign(globalOptions.getSign())
//                .setCuit(globalOptions.getCuit())
                .setUrlBase(globalOptions.getUrlBase())
                .setBaseAddress(globalOptions.getApiEnvironment())
                .build();
        }

        return new RequestOptionsBuilder()
//            .setToken(localOptions.getToken() != null ? localOptions.getToken() : globalOptions.getToken())
//            .setSign(localOptions.getSign() != null ? localOptions.getSign() : globalOptions.getSign())
//            .setCuit(localOptions.getCuit() != null ? localOptions.getCuit() : globalOptions.getCuit())
            .setUrlBase(localOptions.getUrlBase() != null ? localOptions.getUrlBase() : globalOptions.getUrlBase())
            .setBaseAddress(localOptions.getApiEnvironment() != null ? localOptions.getApiEnvironment() : globalOptions.getApiEnvironment())
            .build();
    }

    public static class InvalidRequestOptionsException extends RuntimeException {
        public InvalidRequestOptionsException(String message) {
            super(message);
        }
    }
}
