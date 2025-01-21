package com.germanfica.wsfe.model.soap.loginticket;

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
public class LoginTicketResponseType {

    /**
     * -- GETTER --
     *  Gets the value of the header property.
     *
     *
     * -- SETTER --
     *  Sets the value of the header property.
     *
     @return
     *     possible object is
     *     {@link HeaderType }
      * @param value
      *     allowed object is
      *     {@link HeaderType }
     */
    @XmlElement(required = true)
    protected HeaderType header;
    /**
     * -- GETTER --
     *  Gets the value of the credentials property.
     *
     *
     * -- SETTER --
     *  Sets the value of the credentials property.
     *
     @return
     *     possible object is
     *     {@link CredentialsType }
      * @param value
      *     allowed object is
      *     {@link CredentialsType }
     */
    @XmlElement(required = true)
    protected CredentialsType credentials;
    /**
     * -- GETTER --
     *  Gets the value of the version property.
     *
     *
     * -- SETTER --
     *  Sets the value of the version property.
     *
     @return
     *     possible object is
     *     {@link String }
      * @param value
      *     allowed object is
      *     {@link String }
     */
    @XmlAttribute(name = "version", required = true)
    protected String version;

}
