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

    /**
     * -- GETTER --
     *  Gets the value of the token property.
     *
     *
     * -- SETTER --
     *  Sets the value of the token property.
     *
     @return
     *     possible object is
     *     {@link String }
      * @param value
      *     allowed object is
      *     {@link String }
     */
    @XmlElement(required = true)
    protected String token;
    /**
     * -- GETTER --
     *  Gets the value of the sign property.
     *
     *
     * -- SETTER --
     *  Sets the value of the sign property.
     *
     @return
     *     possible object is
     *     {@link String }
      * @param value
      *     allowed object is
      *     {@link String }
     */
    @XmlElement(required = true)
    protected String sign;

}
