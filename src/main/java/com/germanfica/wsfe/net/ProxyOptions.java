package com.germanfica.wsfe.net;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder(setterPrefix = "set")
@EqualsAndHashCode(callSuper = false)
public class ProxyOptions {
    private final String host;
    private final int port;
    private final String username;
    private final String password;

    /**
     * Indicates whether the proxy configuration is considered valid.
     * A valid proxy must have a non-empty host and a port greater than 0.
     */
    public boolean isValid() {
        return host != null && !host.trim().isEmpty() && port > 0;
    }

    public boolean hasCredentials() {
        return username != null && !username.trim().isEmpty();
    }
}
