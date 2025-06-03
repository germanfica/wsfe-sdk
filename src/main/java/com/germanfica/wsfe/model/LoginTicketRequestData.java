package com.germanfica.wsfe.model;

/**
 * DTO inmutable para <loginTicketRequest>.
 */
public record LoginTicketRequestData(
    /* header */ String source,
                 String destination,
                 String uniqueId,
                 String generationTime,
                 String expirationTime,
    /* root   */ String service) implements LoginTicketData {
}
