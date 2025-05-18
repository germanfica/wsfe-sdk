package com.germanfica.wsfe.net;

import com.germanfica.wsfe.Wsaa;
import com.germanfica.wsfe.Wsfe;
import fev1.dif.afip.gov.ar.Service;
import fev1.dif.afip.gov.ar.ServiceSoap;
import https.wsaa_afip_gov_ar.ws.services.logincms.LoginCMS;
import https.wsaa_afip_gov_ar.ws.services.logincms.LoginCMSService;
import jakarta.xml.ws.BindingProvider;

import java.util.Map;
import java.util.function.Supplier;

public class DefaultSoapPortResolver implements PortProvider {
    @Override
    public <T> T getPort(Class<T> portClass, ApiRequest request) {
        String endpoint = request != null && request.getApiBase() != null
            ? request.getApiBase()
            : apiBaseResolvers.getOrDefault(portClass, () -> {
            throw new IllegalArgumentException("No apiBase configured for: " + portClass);
        }).get();

        if (portClass.equals(ServiceSoap.class)) {
            Service service = new Service();
            ServiceSoap port = service.getServiceSoap();
            ((BindingProvider) port).getRequestContext()
                .put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint + "/wsfev1/service.asmx");
            return portClass.cast(port);
        }

        if (portClass.equals(LoginCMS.class)) {
            LoginCMSService service = new LoginCMSService();
            LoginCMS port = service.getLoginCms();
            ((BindingProvider) port).getRequestContext()
                .put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint + "/ws/services/LoginCms");
            return portClass.cast(port);
        }

        throw new IllegalArgumentException("Unsupported port type: " + portClass);
    }

    private static final Map<Class<?>, Supplier<String>> apiBaseResolvers = Map.of(
        ServiceSoap.class, Wsfe::getApiBase,
        LoginCMS.class, Wsaa::getApiBase
    );
}
