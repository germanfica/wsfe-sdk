package com.germanfica.wsfe.model.soap.loginticket;

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
    protected String source;

    @XmlElement(required = true)
    protected String destination;

    @XmlElement(required = true)
    protected String uniqueId;

    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar generationTime;

    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar expirationTime;
}
