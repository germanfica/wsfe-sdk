package com.germanfica.wsfe.model.soap.envelope;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class FaultDetail { // Cambiado a public
    @XmlElement(name = "exceptionName", namespace = "http://xml.apache.org/axis/")
    @JacksonXmlProperty(localName = "exceptionName", namespace = "http://xml.apache.org/axis/")
    private String exceptionName;

    @XmlElement(name = "hostname", namespace = "http://xml.apache.org/axis/")
    @JacksonXmlProperty(localName = "hostname", namespace = "http://xml.apache.org/axis/")
    private String hostname;

    // Getters y Setters
    public String getExceptionName() {
        return exceptionName;
    }

    public void setExceptionName(String exceptionName) {
        this.exceptionName = exceptionName;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
}
