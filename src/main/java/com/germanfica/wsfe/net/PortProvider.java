package com.germanfica.wsfe.net;

public interface PortProvider {
    <T> T getPort(Class<T> portClass, ApiRequest request);
}
