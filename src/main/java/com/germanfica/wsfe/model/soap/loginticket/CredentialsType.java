package com.germanfica.wsfe.model.soap.loginticket;

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
    protected String token;

    @XmlElement(required = true)
    protected String sign;
}
