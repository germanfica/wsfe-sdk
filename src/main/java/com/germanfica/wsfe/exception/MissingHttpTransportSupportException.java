package com.germanfica.wsfe.exception;

import com.germanfica.wsfe.dto.ErrorDto;
import com.germanfica.wsfe.net.HttpStatus;

import java.net.InetAddress;

public class MissingHttpTransportSupportException extends ApiException {
  public MissingHttpTransportSupportException() {
    super(new ErrorDto(
        "invalid_transport_mode",
        "Transport 'HTTP_HC5' is required when using proxy with authentication (RFC 7235 support)",
        new ErrorDto.ErrorDetailsDto(
            "MissingHttpTransportSupportException",
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
