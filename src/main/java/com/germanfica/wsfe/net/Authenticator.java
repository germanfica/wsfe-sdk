package com.germanfica.wsfe.net;

import com.germanfica.wsfe.annotation.Experimental;
import com.germanfica.wsfe.exception.ApiException;

@Experimental("This class is a placeholder and subject to change.")
@Deprecated
/** * Represents a request authentication mechanism. */
public interface Authenticator {
    /**
     * * Authenticate the request
     *
     * @param request the request that need authentication.
     * @return the request with authentication headers applied.
     * @throws ApiException on authentication errors.
     */
    WsfeRequest authenticate(WsfeRequest request) throws ApiException;
}
