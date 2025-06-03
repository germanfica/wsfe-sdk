package com.germanfica.wsfe.util;

import com.germanfica.wsfe.exception.LoginTicketParseException;
import com.germanfica.wsfe.model.LoginTicketData;
import com.germanfica.wsfe.model.LoginTicketRequestData;
import com.germanfica.wsfe.model.LoginTicketResponseData;

/**
 * Parser único para LoginTicketRequest / Response.
 */
public final class LoginTicketParser {

    /** Parsea el XML y devuelve la representación tipada correspondiente. */
    public static LoginTicketData parse(String xml) throws LoginTicketParseException {
        try {
            XMLExtractor x   = new XMLExtractor(xml);
            String       tag = x.extractValue("local-name(/*)");

            return switch (tag) {
                case "loginTicketRequest"  -> new LoginTicketRequestData(
                    x.extractValue("/loginTicketRequest/header/source"),
                    x.extractValue("/loginTicketRequest/header/destination"),
                    x.extractValue("/loginTicketRequest/header/uniqueId"),
                    x.extractValue("/loginTicketRequest/header/generationTime"),
                    x.extractValue("/loginTicketRequest/header/expirationTime"),
                    x.extractValue("/loginTicketRequest/service")
                );
                case "loginTicketResponse" -> new LoginTicketResponseData(
                    x.extractValue("/loginTicketResponse/credentials/token"),
                    x.extractValue("/loginTicketResponse/credentials/sign"),
                    x.extractValue("/loginTicketResponse/header/source"),
                    x.extractValue("/loginTicketResponse/header/destination"),
                    x.extractValue("/loginTicketResponse/header/uniqueId"),
                    x.extractValue("/loginTicketResponse/header/generationTime"),
                    x.extractValue("/loginTicketResponse/header/expirationTime")
                );
                default -> throw new LoginTicketParseException(
                    "Documento desconocido: raíz <" + tag + ">"
                );
            };
        } catch (LoginTicketParseException e) {
            throw e;                                // excepciones de negocio
        } catch (Exception e) {                     // XPath, parser, etc.
            throw new LoginTicketParseException(
                "Error procesando LoginTicket XML", e
            );
        }
    }

    private LoginTicketParser() {/* util */}
}
