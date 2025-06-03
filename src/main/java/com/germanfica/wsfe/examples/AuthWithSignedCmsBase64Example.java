package com.germanfica.wsfe.examples;

import com.germanfica.wsfe.WsaaClient;
import com.germanfica.wsfe.cms.Cms;
import com.germanfica.wsfe.net.ApiEnvironment;
import com.germanfica.wsfe.util.XMLExtractor;

public class AuthWithSignedCmsBase64Example {
    public static void main(String[] args) {
        try {
            // 1) Armar CMS para WSAA
            Cms cms = Cms.create("your-signed-cms-base64");

            // 2) Crear el WsfeClient
            //WsaaClient client = WsaaClient.builder().build(); // (1)
            // Endpoint de WSAA (homologación)
            //WsaaClient client = WsaaClient.builder().setUrlBase("https://wsaa.afip.gov.ar").build();
            //WsaaClient client = WsaaClient.builder().setApiEnvironment(ApiEnvironment.HOMO).build(); // (2)
            WsaaClient client = WsaaClient.builder().setApiEnvironment(ApiEnvironment.PROD).build();

            // 3) Invocar autenticación en WSAA
            String authResponse = client.authService().autenticar(cms);

            XMLExtractor extractor = new XMLExtractor(authResponse);
            String token = extractor.extractToken();
            String sign = extractor.extractSign();
            String generationTime = extractor.extractLoginTicketData().generationTime;
            String expirationTime = extractor.extractLoginTicketData().expirationTime;
            XMLExtractor.LoginTicketData data = extractor.extractLoginTicketData();

            // 4) Imprimir resultado
            System.out.println("Respuesta de autenticación xml: \n" + authResponse);
            System.out.println("Respuesta de autenticación json: \n" + data);
            System.out.println("Token: \n" + token);
            System.out.println("Sign: \n" + sign);
            System.out.println("generationTime: \n" + generationTime);
            System.out.println("expirationTime: \n" + expirationTime);
        } catch (Exception e) {
            System.err.println("Error al invocar autenticación WSAA: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
