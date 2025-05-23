package com.germanfica.wsfe.util;

import java.net.Proxy;
import java.util.function.Supplier;

public class ProxyUtils {

    public static <T> T withTemporaryProxy(Proxy proxy, Supplier<T> supplier) {
        if (proxy == null || proxy.type() == Proxy.Type.DIRECT) {
            return supplier.get(); // Sin proxy, ejecutamos directamente
        }

        String host = ((java.net.InetSocketAddress) proxy.address()).getHostString();
        int port = ((java.net.InetSocketAddress) proxy.address()).getPort();

        String previousHost = System.getProperty("http.proxyHost");
        String previousPort = System.getProperty("http.proxyPort");

        try {
            System.setProperty("http.proxyHost", host);
            System.setProperty("http.proxyPort", String.valueOf(port));
            return supplier.get();
        } finally {
            if (previousHost != null) {
                System.setProperty("http.proxyHost", previousHost);
            } else {
                System.clearProperty("http.proxyHost");
            }

            if (previousPort != null) {
                System.setProperty("http.proxyPort", previousPort);
            } else {
                System.clearProperty("http.proxyPort");
            }
        }
    }
}
