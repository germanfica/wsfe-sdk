package com.germanfica.wsfe.model.soap.envelope;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class SoapBody { // Cambiado a public
    @XmlElement(name = "Fault", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
    @JacksonXmlProperty(localName = "Fault", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
    private SoapFault fault;

    // Getters y Setters
    public SoapFault getFault() {
        return fault;
    }

    public void setFault(SoapFault fault) {
        this.fault = fault;
    }
}
