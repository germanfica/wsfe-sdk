package com.germanfica.wsfe.exception;

public class SoapProcessingException extends RuntimeException {
    public SoapProcessingException(String message) {
        super(message);
    }

    public SoapProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
