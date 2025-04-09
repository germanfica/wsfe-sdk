package com.germanfica.wsfe.net;

import java.net.http.HttpHeaders;

/** A response from Stripe's API, with body represented as a String. */
public class ApiResponse extends AbstractApiResponse<String> {
    /**
     * Initializes a new instance of the {@link ApiResponse} class.
     *
     * @param code the HTTP status code of the response
     * @param headers the HTTP headers of the response
     * @param body the body of the response
     * @throws NullPointerException if {@code headers} or {@code body} is {@code null}
     */
    public ApiResponse(int code, HttpHeaders headers, String body) {
        super(code, headers, body);
    }
}
