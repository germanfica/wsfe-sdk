package com.germanfica.wsfe.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.Optional;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.util.Optional;

@Value
public class ErrorDto {
    @JsonProperty("type")
    @NotNull
    ErrorType type; // required

    @JsonProperty("fault_code")
    String faultCode;

    @JsonProperty("fault_string")
    String faultString;

    @JsonProperty("details")
    ErrorDetailsDto details;

    @JsonProperty("doc_url")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    Optional<String> docUrl;

    // Constructor privado para forzar el uso del Builder
    private ErrorDto(Builder builder) {
        this.type = builder.type;
        this.faultCode = builder.faultCode;
        this.faultString = builder.faultString;
        this.details = builder.details;
        this.docUrl = builder.docUrl;
    }

    // Método estático para crear el Builder
    public static Builder builder(ErrorType type) {
        return new Builder(type);
    }

    // Builder para flexibilidad
    public static class Builder {
        private final ErrorType type; // Obligatorio
        private String faultCode;
        private String faultString;
        private ErrorDetailsDto details;
        private Optional<String> docUrl = Optional.empty();

        // Constructor para inicializar el tipo (obligatorio)
        private Builder(ErrorType type) {
            if (type == null) {
                throw new IllegalArgumentException("Type cannot be null");
            }
            this.type = type;
        }

        public Builder faultCode(String faultCode) {
            this.faultCode = faultCode;
            return this;
        }

        public Builder faultString(String faultString) {
            this.faultString = faultString;
            return this;
        }

        public Builder details(ErrorDetailsDto details) {
            this.details = details;
            return this;
        }

        public Builder docUrl(Optional<String> docUrl) {
            this.docUrl = docUrl != null ? docUrl : Optional.empty();
            return this;
        }

        public ErrorDto build() {
            return new ErrorDto(this);
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
