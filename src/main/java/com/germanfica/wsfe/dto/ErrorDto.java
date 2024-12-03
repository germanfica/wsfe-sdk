package com.germanfica.wsfe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class ErrorDto {

    @JsonProperty("fault_code")
    String faultCode;

    @JsonProperty("fault_string")
    String faultString;

    @JsonProperty("details")
    ErrorDetailsDto details;

    @Value
    public static class ErrorDetailsDto {
        @JsonProperty("exception_name")
        String exceptionName;

        @JsonProperty("hostname")
        String hostname;
    }
}
