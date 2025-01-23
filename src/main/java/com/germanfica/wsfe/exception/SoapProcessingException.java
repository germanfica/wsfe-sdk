package com.germanfica.wsfe.exception;

import jakarta.xml.soap.SOAPMessage;

public class SoapProcessingException extends RuntimeException {
    private final SOAPMessage soapMessage;

    public SoapProcessingException(String message) {
        super(message);
        this.soapMessage = null;
    }

    public SoapProcessingException(String message, Throwable cause) {
        super(message, cause);
        this.soapMessage = null;
    }

    public SoapProcessingException(String message, Throwable cause, SOAPMessage soapMessage) {
        super(message, cause);
        this.soapMessage = soapMessage;
    }

    public SOAPMessage getSoapMessage() {
        return soapMessage;
    }
}
