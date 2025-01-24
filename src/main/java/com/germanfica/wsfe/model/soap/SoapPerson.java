package com.germanfica.wsfe.model.soap;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "Person", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Person", propOrder = {"firstName", "lastName"})
public class SoapPerson {
    @XmlElement(name = "firstname", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
    @JacksonXmlProperty(localName = "firstname", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
    private String firstName;

    @XmlElement(name = "lastname", namespace = "http://xml.apache.org/axis/")
    @JacksonXmlProperty(localName = "lastname", namespace = "http://xml.apache.org/axis/")
    private String lastName;

    // Getters y Setters
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
