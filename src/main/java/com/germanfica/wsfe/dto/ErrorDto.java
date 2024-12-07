package com.germanfica.wsfe.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;


import lombok.Value;

import java.util.Optional;




@Value
public class ErrorDto {
    @JsonProperty("type")
    ErrorType type; // Obligatorio

    @JsonProperty("fault_code")
    String faultCode;

    @JsonProperty("fault_string")
    String faultString;

    @JsonProperty("details")
    ErrorDetailsDto details;

    @JsonProperty("doc_url")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    Optional<String> docUrl;

    private ErrorDto(ErrorType type, String faultCode, String faultString, ErrorDetailsDto details, Optional<String> docUrl) {
        this.type = type;
        this.faultCode = faultCode;
        this.faultString = faultString;
        this.details = details;
        this.docUrl = docUrl;
    }

    // Constructor sin parámetro, solo se hace obligatorio a través de .withType()
    public static BuilderTypeStep builder() {
        return new Builder();
    }

    // Interface para el paso obligatorio de `type`
    public interface BuilderTypeStep {
        BuilderOptionalStep withType(ErrorType type);
    }

    // Interface para los pasos opcionales
    public interface BuilderOptionalStep {
        BuilderOptionalStep faultCode(String faultCode);

        BuilderOptionalStep faultString(String faultString);

        BuilderOptionalStep details(ErrorDetailsDto details);

        BuilderFinalStep docUrl(Optional<String> docUrl);

        ErrorDto build();
    }

    // Interface final que deshabilita volver a definir docUrl
    public interface BuilderFinalStep {
        ErrorDto build();
    }

    private static class Builder implements BuilderTypeStep, BuilderOptionalStep, BuilderFinalStep {
        private ErrorType type;
        private String faultCode;
        private String faultString;
        private ErrorDetailsDto details;
        private Optional<String> docUrl = Optional.empty();
        private boolean docUrlSet = false; // Controla si `docUrl` ya fue asignado

        private Builder() {
        }

        @Override
        public BuilderOptionalStep withType(ErrorType type) {
            if (type == null) {
                throw new IllegalArgumentException("Type cannot be null");
            }
            this.type = type;
            return this;
        }

        @Override
        public BuilderOptionalStep faultCode(String faultCode) {
            this.faultCode = faultCode;
            return this;
        }

        @Override
        public BuilderOptionalStep faultString(String faultString) {
            this.faultString = faultString;
            return this;
        }

        @Override
        public BuilderOptionalStep details(ErrorDetailsDto details) {
            this.details = details;
            return this;
        }

        @Override
        public BuilderFinalStep docUrl(Optional<String> docUrl) {
            if (docUrlSet) {
                throw new IllegalStateException("docUrl can only be set once.");
            }
            this.docUrl = docUrl != null ? docUrl : Optional.empty();
            docUrlSet = true; // Marca como establecido
            return this;
        }

        @Override
        public ErrorDto build() {
            return new ErrorDto(type, faultCode, faultString, details, docUrl);
        }
    }

    @Value
    public static class ErrorDetailsDto {
        @JsonProperty("exception_name")
        String exceptionName;

        @JsonProperty("hostname")
        String hostname;
    }

    public enum ErrorType {
        API_ERROR("api_error"),
        EXTERNAL_ERROR("external_error"),
        INVALID_REQUEST_ERROR("invalid_request_error");

        private final String type;

        ErrorType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return type;
        }
    }
}
