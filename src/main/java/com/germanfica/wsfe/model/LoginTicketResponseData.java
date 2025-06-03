package com.germanfica.wsfe.model;

/**
 * DTO inmutable para <loginTicketResponse>.
 */
public record LoginTicketResponseData(
    /* credentials */ String token,
                      String sign,
    /* header      */ String source,
                      String destination,
                      String uniqueId,
                      String generationTime,
                      String expirationTime) implements LoginTicketData {
}
