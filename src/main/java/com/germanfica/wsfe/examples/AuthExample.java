package com.germanfica.wsfe.examples;

import com.germanfica.wsfe.Wsfe;
import com.germanfica.wsfe.WsfeClient;
import com.germanfica.wsfe.cms.Cms;
import com.germanfica.wsfe.utils.ConfigLoader;
import com.germanfica.wsfe.utils.XMLExtractor;

public class AuthExample {

    public static void main(String[] args) {
        try {
            // 1) Armar CMS para WSAA
            Cms cms = new Cms(
                    ConfigLoader.KEYSTORE_PATH,
                    ConfigLoader.KEYSTORE_PASSWORD,
                    ConfigLoader.KEYSTORE_SIGNER,
                    ConfigLoader.DSTDN,
                    ConfigLoader.SERVICE,
                    ConfigLoader.TICKET_TIME
            );

            // 2) Endpoint de WSAA (homologación)
            Wsfe.overrideWsaaBase(Wsfe.TEST_WSAA_API_BASE);
            String endpointWsaa = Wsfe.getWsaaBase() + "/ws/services/LoginCms";

            // 3) Crear el WsfeClient
            WsfeClient client = new WsfeClient(null);

            // 4) Invocar autenticación en WSAA
            String authResponse = client.authService().autenticar(cms);

            XMLExtractor extractor = new XMLExtractor(authResponse);
            String token = extractor.extractToken();
            XMLExtractor.LoginTicketData data = extractor.extractLoginTicketData();

            // 5) Imprimir resultado
            System.out.println("Respuesta de autenticación xml: \n" + authResponse);
            System.out.println("Respuesta de autenticación json: \n" + data);
            System.out.println("Token: \n" + token);
        } catch (Exception e) {
            System.err.println("Error al invocar autenticación WSAA: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
