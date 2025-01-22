package com.germanfica.wsfe.model.soap.loginticket;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

import javax.xml.datatype.XMLGregorianCalendar;

@Setter
@Getter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HeaderType", propOrder = {
    "source",
    "destination",
    "uniqueId",
    "generationTime",
    "expirationTime"
})
public class HeaderType {
    @XmlElement(required = true)
    @JacksonXmlProperty(localName = "source")
    protected String source;

    @XmlElement(required = true)
    @JacksonXmlProperty(localName = "destination")
    protected String destination;

    @XmlElement(required = true)
    @JacksonXmlProperty(localName = "uniqueId")
    protected String uniqueId;

    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    @JacksonXmlProperty(localName = "generationTime")
    protected XMLGregorianCalendar generationTime;

    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    @JacksonXmlProperty(localName = "expirationTime")
    protected XMLGregorianCalendar expirationTime;
}
