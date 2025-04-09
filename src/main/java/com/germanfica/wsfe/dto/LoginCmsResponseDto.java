package com.germanfica.wsfe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class LoginCmsResponseDto {

    @JsonProperty("header")
    private HeaderDto header;

    @JsonProperty("credentials")
    private CredentialsDto credentials;

    @Value
    public static class HeaderDto {
        @JsonProperty("source")
        private String source;

        @JsonProperty("destination")
        private String destination;

        @JsonProperty("unique_id")
        private String uniqueId;

        @JsonProperty("generation_time")
        private String generationTime;

        @JsonProperty("expiration_time")
        private String expirationTime;
    }

    @Value
    public static class CredentialsDto {
        @JsonProperty("token")
        private String token;

        @JsonProperty("sign")
        private String sign;
    }
}
