package com.germanfica.wsfe.model.soap.envelope;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class SoapFault { // Cambiado a public
    @XmlElement(name = "faultcode")
    @JacksonXmlProperty(localName = "faultcode")
    private String faultCode;

    @XmlElement(name = "faultstring")
    @JacksonXmlProperty(localName = "faultstring")
    private String faultString;

    @XmlElement(name = "detail")
    @JacksonXmlProperty(localName = "detail")
    private SoapFaultDetail detail;

    // Getters y Setters
    public String getFaultCode() {
        return faultCode;
    }

    public void setFaultCode(String faultCode) {
        this.faultCode = faultCode;
    }

    public String getFaultString() {
        return faultString;
    }

    public void setFaultString(String faultString) {
        this.faultString = faultString;
    }

    public SoapFaultDetail getDetail() {
        return detail;
    }

    public void setDetail(SoapFaultDetail detail) {
        this.detail = detail;
    }
}
