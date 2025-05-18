package com.germanfica.wsfe.net;

@FunctionalInterface
public interface PortInvoker<P, R> {
    R invoke(P port) throws Exception;
}