package com.germanfica.wsfe.exception;

public class XmlMappingException extends Exception {
    private final String xmlResponse;

    public XmlMappingException(String message, String xmlResponse, Throwable cause) {
        super(message, cause);
        this.xmlResponse = xmlResponse;
    }

    public String getXmlResponse() {
        return xmlResponse;
    }
}
