package com.germanfica.wsfe.net;

public enum HttpTransportMode {
    HTTP,        // Default HTTPConduit (HttpURLConnection)
    //HTTP_HC,   // HttpClient v4
    HTTP_HC5,    // HttpClient v5 (async, soporta proxy con auth RFC 7235)
    //HTTP_NETTY,
    //HTTP_JETTY,
    //HTTP_UNDERTOW,
    //WEBSOCKET,
    //JMS,
    //UDP
}
