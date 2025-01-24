package com.germanfica.wsfe.model.soap.loginticket;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LoginTicketResponseType", propOrder = {
    "header",
    "credentials"
})
@XmlRootElement(name = "loginTicketResponse")
@JacksonXmlRootElement(localName = "loginTicketResponse")
//@XmlRootElement(name = "loginTicketResponse", namespace = "http://wsaa.view.sua.dvadac.desein.afip.gov.ar")
//@JacksonXmlRootElement(localName = "loginTicketResponse", namespace = "http://wsaa.view.sua.dvadac.desein.afip.gov.ar")
public class LoginTicketResponseType {
    @XmlElement(required = true)
    @JacksonXmlProperty(localName = "header")
    protected HeaderType header;

    @XmlElement(required = true)
    @JacksonXmlProperty(localName = "credentials")
    protected CredentialsType credentials;

    @XmlAttribute(name = "version", required = true)
    @JacksonXmlProperty(localName = "version")
    protected String version;
}
