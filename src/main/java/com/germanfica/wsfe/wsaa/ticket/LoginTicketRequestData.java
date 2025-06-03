package com.germanfica.wsfe.wsaa.ticket;

import com.germanfica.wsfe.util.XMLExtractor;

import javax.xml.xpath.XPathExpressionException;

public record LoginTicketRequestData(
    /* header */ String source, String destination, String uniqueId,
                 String generationTime, String expirationTime,
    /* root  */ String service) implements LoginTicketData {

    static LoginTicketRequestData from(String xml, XMLExtractor x) throws XPathExpressionException {
        return new LoginTicketRequestData(
            x.extractValue("/loginTicketRequest/header/source"),
            x.extractValue("/loginTicketRequest/header/destination"),
            x.extractValue("/loginTicketRequest/header/uniqueId"),
            x.extractValue("/loginTicketRequest/header/generationTime"),
            x.extractValue("/loginTicketRequest/header/expirationTime"),
            x.extractValue("/loginTicketRequest/service")
        );
    }
}