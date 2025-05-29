package com.germanfica.wsfe.exception;

import com.germanfica.wsfe.dto.ErrorDto;
import com.germanfica.wsfe.net.HttpStatus;

import java.net.InetAddress;

public class UnsupportedProxyAuthException extends ApiException {
    public UnsupportedProxyAuthException() {
        super(new ErrorDto(
            "proxy_auth_not_supported",
            "Proxy authentication is not supported with HTTPS due to limitations in HttpURLConnection.",
            new ErrorDto.ErrorDetailsDto(
                "UnsupportedProxyAuthException",
                getLocalHostname()
            )
        ), HttpStatus.BAD_REQUEST);
    }

    private static String getLocalHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown-host";
        }
    }
}
