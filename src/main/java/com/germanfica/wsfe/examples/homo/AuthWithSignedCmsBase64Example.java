package com.germanfica.wsfe.examples.homo;

import com.germanfica.wsfe.WsaaClient;
import com.germanfica.wsfe.cms.Cms;
import com.germanfica.wsfe.model.LoginTicketResponseData;
import com.germanfica.wsfe.net.ApiEnvironment;
import com.germanfica.wsfe.util.LoginTicketParser;

public class AuthWithSignedCmsBase64Example {
    public static void main(String[] args) {
        try {
            // 1) Armar CMS para WSAA
            Cms cms = Cms.create("your-signed-cms-base64");

            // 2) Crear el WsfeClient
            //WsaaClient client = WsaaClient.builder().build(); // (1)
            // Endpoint de WSAA (homologación)
            //WsaaClient client = WsaaClient.builder().setUrlBase("https://wsaahomo.afip.gov.ar").build();
            WsaaClient client = WsaaClient.builder().setApiEnvironment(ApiEnvironment.HOMO).build();

            // 3) Invocar autenticación en WSAA
            String authResponse = client.authService().autenticar(cms);

            LoginTicketResponseData data = (LoginTicketResponseData) LoginTicketParser.parse(authResponse);

            // 4) Imprimir resultado
            System.out.println("Respuesta de autenticación xml: \n" + authResponse);
            System.out.println("Respuesta de autenticación json: \n" + data);
            System.out.println("Token: \n" + data.token());
            System.out.println("Sign: \n" + data.sign());
            System.out.println("generationTime: \n" + data.generationTime());
            System.out.println("expirationTime: \n" + data.expirationTime());
        } catch (Exception e) {
            System.err.println("Error al invocar autenticación WSAA: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
