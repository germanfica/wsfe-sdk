package com.germanfica.wsfe.model;

/**
 * Contrato mínimo que comparten Request y Response.
 */
public sealed interface LoginTicketData
    permits LoginTicketRequestData, LoginTicketResponseData {

    /* --- atributos comunes --- */
    String source();
    String destination();
    String uniqueId();
    String generationTime();
    String expirationTime();
}
