package com.germanfica.wsfe.wsaa.ticket;

import com.germanfica.wsfe.util.XMLExtractor;

import javax.xml.xpath.XPathExpressionException;

public record LoginTicketResponseData(
    /* credentials */ String token, String sign,
    /* header      */ String source, String destination, String uniqueId,
                      String generationTime, String expirationTime)
    implements LoginTicketData {

    static LoginTicketResponseData from(String xml, XMLExtractor x) throws XPathExpressionException {
        return new LoginTicketResponseData(
            x.extractValue("/loginTicketResponse/credentials/token"),
            x.extractValue("/loginTicketResponse/credentials/sign"),
            x.extractValue("/loginTicketResponse/header/source"),
            x.extractValue("/loginTicketResponse/header/destination"),
            x.extractValue("/loginTicketResponse/header/uniqueId"),
            x.extractValue("/loginTicketResponse/header/generationTime"),
            x.extractValue("/loginTicketResponse/header/expirationTime")
        );
    }
}