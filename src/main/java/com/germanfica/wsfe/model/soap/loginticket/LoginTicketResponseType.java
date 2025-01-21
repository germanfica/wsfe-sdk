package com.germanfica.wsfe.model.soap.loginticket;

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
@XmlRootElement(name = "loginTicketResponse", namespace = "http://wsaa.view.sua.dvadac.desein.afip.gov.ar")
@JacksonXmlRootElement(localName = "loginTicketResponse", namespace = "http://wsaa.view.sua.dvadac.desein.afip.gov.ar")
public class LoginTicketResponseType {
    @XmlElement(required = true)
    protected HeaderType header;

    @XmlElement(required = true)
    protected CredentialsType credentials;

    @XmlAttribute(name = "version", required = true)
    protected String version;
}
