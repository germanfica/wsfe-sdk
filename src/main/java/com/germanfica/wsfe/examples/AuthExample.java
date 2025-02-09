package com.germanfica.wsfe.examples;

import com.germanfica.wsfe.Wsfe;
import com.germanfica.wsfe.WsfeClient;
import com.germanfica.wsfe.utils.ArcaWSAAUtils;
import com.germanfica.wsfe.utils.ConfigLoader;
import com.germanfica.wsfe.utils.XMLExtractor;

public class AuthExample {

    public static void main(String[] args) {
        // 1) Cargar configuración

        try {
            // 2) Armar CMS para WSAA
            byte[] loginTicketRequestXmlCms = ArcaWSAAUtils.create_cms(
                    ConfigLoader.KEYSTORE_PATH,
                    ConfigLoader.KEYSTORE_PASSWORD,
                    ConfigLoader.KEYSTORE_SIGNER,
                    ConfigLoader.DSTDN,
                    ConfigLoader.SERVICE,
                    ConfigLoader.TICKET_TIME
            );

            // 3) Endpoint de WSAA (homologación)
            Wsfe.overrideWsaaBase(Wsfe.TEST_WSAA_API_BASE);
            String endpointWsaa = Wsfe.getWsaaBase() + "/ws/services/LoginCms";

            // 4) Crear el WsfeClient
            WsfeClient client = new WsfeClient(loginTicketRequestXmlCms);

            String cmsFirmado = ArcaWSAAUtils.createSignedCmsBase64(loginTicketRequestXmlCms);

            System.out.println("cmsFirmado: " + cmsFirmado);

            // 5) Invocar autenticación en WSAA
            String authResponse = client.authService().autenticar(cmsFirmado);

            XMLExtractor extractor = new XMLExtractor(authResponse);
            String token = extractor. extractValue("/ loginTicketResponse/ credentials/ token");
            XMLExtractor. LoginTicketData data = extractor. extractLoginTicketData();

            // 6) Imprimir resultado
            System.out.println("Respuesta de autenticación xml: \n" + authResponse);
            System.out.println("Respuesta de autenticación json: \n" + data);
            System.out.println("Token: \n" + token);

        } catch (Exception e) {
            System.err.println("Error al invocar autenticación WSAA: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
