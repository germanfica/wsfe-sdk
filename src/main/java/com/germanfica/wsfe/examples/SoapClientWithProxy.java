package com.germanfica.wsfe.examples;

import https.wsaa_afip_gov_ar.ws.services.logincms.LoginCMS;
import https.wsaa_afip_gov_ar.ws.services.logincms.LoginCMSService;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.configuration.jsse.TLSClientParameters;

import java.security.cert.X509Certificate;
import javax.net.ssl.*;

public class SoapClientWithProxy {

    public static void main(String[] args) {
        // (Obligatorio) Configurar truststore
        System.setProperty("javax.net.ssl.trustStore", "certs/private/merged-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");

        // Crear el cliente desde el WSDL
        LoginCMSService service = new LoginCMSService();
        LoginCMS port = service.getLoginCms();

        // Obtener cliente de Apache CXF
        Client client = ClientProxy.getClient(port);
        HTTPConduit http = (HTTPConduit) client.getConduit();

        // Configurar proxy
        http.getClient().setProxyServer("127.0.0.1");
        http.getClient().setProxyServerPort(8080);

        // (Opcional) Autenticación en el proxy
//        ProxyAuthorizationPolicy proxyAuth = new ProxyAuthorizationPolicy();
//        proxyAuth.setUserName("usuario");
//        proxyAuth.setPassword("contraseña");
//        http.setProxyAuthorization(proxyAuth);

        // ⚠️ Solo para desarrollo: desactivar validación SSL si estás usando mitmproxy u otro proxy con CA propia
        //disableSSLValidation(http);

        // Ejemplo: llamada real al WS
        try {
            String signedCms = "...tu CMS base64 firmado...";
            String token = port.loginCms(signedCms);
            System.out.println("Token obtenido: " + token);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void disableSSLValidation(HTTPConduit conduit) {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            TLSClientParameters tlsParams = new TLSClientParameters();
            tlsParams.setSSLSocketFactory(sslContext.getSocketFactory());
            tlsParams.setDisableCNCheck(true); // Desactiva verificación del CN

            conduit.setTlsClientParameters(tlsParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
