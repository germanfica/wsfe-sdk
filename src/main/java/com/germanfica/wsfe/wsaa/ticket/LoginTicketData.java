package com.germanfica.wsfe.wsaa.ticket;

import com.germanfica.wsfe.util.XMLExtractor;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

public sealed interface LoginTicketData permits
    LoginTicketRequestData, LoginTicketResponseData {

    String generationTime();
    String expirationTime();

    /* Factory: detecta la raíz y crea el subtipo adecuado -------------- */
    static LoginTicketData parse(String xml) throws IllegalArgumentException, ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        XMLExtractor x = new XMLExtractor(xml);
        String root = x.extractValue("local-name(/*)");
        return switch (root) {
            case "loginTicketRequest"  -> LoginTicketRequestData.from(xml, x);
            case "loginTicketResponse" -> LoginTicketResponseData.from(xml, x);
            default -> throw new IllegalArgumentException("Tipo de LoginTicket desconocido: " + root);
        };
    }

    static LoginTicketRequestData  parseRequest (String xml) throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        return ((LoginTicketRequestData)  parse(xml));   // el `parse` común ya valida la raíz
    }
    static LoginTicketResponseData parseResponse(String xml) throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        return ((LoginTicketResponseData) parse(xml));
    }
}