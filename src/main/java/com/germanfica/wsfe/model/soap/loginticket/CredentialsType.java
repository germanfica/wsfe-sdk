package com.germanfica.wsfe.model.soap.loginticket;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CredentialsType", propOrder = {
    "token",
    "sign"
})

public class CredentialsType {
    @XmlElement(required = true)
    @JacksonXmlProperty(localName = "token")
    protected String token;

    @XmlElement(required = true)
    @JacksonXmlProperty(localName = "sign")
    protected String sign;
}
