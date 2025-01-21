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

    /**
     * -- GETTER --
     *  Gets the value of the source property.
     *
     *
     * -- SETTER --
     *  Sets the value of the source property.
     *
     @return
     *     possible object is
     *     {@link String }
      * @param value
      *     allowed object is
      *     {@link String }
     */
    @XmlElement(required = true)
    protected String source;
    /**
     * -- GETTER --
     *  Gets the value of the destination property.
     *
     *
     * -- SETTER --
     *  Sets the value of the destination property.
     *
     @return
     *     possible object is
     *     {@link String }
      * @param value
      *     allowed object is
      *     {@link String }
     */
    @XmlElement(required = true)
    protected String destination;
    /**
     * -- GETTER --
     *  Gets the value of the uniqueId property.
     *
     *
     * -- SETTER --
     *  Sets the value of the uniqueId property.
     *
     @return
     *     possible object is
     *     {@link String }
      * @param value
      *     allowed object is
      *     {@link String }
     */
    @XmlElement(required = true)
    protected String uniqueId;
    /**
     * -- GETTER --
     *  Gets the value of the generationTime property.
     *
     *
     * -- SETTER --
     *  Sets the value of the generationTime property.
     *
     @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
      * @param value
      *     allowed object is
      *     {@link XMLGregorianCalendar }
     */
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar generationTime;
    /**
     * -- GETTER --
     *  Gets the value of the expirationTime property.
     *
     *
     * -- SETTER --
     *  Sets the value of the expirationTime property.
     *
     @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
      * @param value
      *     allowed object is
      *     {@link XMLGregorianCalendar }
     */
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar expirationTime;

}
