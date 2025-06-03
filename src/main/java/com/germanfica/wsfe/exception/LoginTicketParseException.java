package com.germanfica.wsfe.exception;

/** Señala problemas de interpretación de un Login Ticket (request/response). */
public class LoginTicketParseException extends RuntimeException {
  public LoginTicketParseException(String message)                   { super(message); }
  public LoginTicketParseException(String message, Throwable cause)  { super(message, cause); }
}
