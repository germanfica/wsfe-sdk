package com.germanfica.wsfe.net;

import java.net.Proxy;

public abstract class SoapResponseGetterOptions {
    public abstract String getUrlBase();
    public abstract ApiEnvironment getApiEnvironment();
    public abstract ProxyOptions getProxyOptions();
}
