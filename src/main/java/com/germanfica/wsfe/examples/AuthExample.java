package com.germanfica.wsfe.examples;

import com.germanfica.wsfe.WsaaClient;
import com.germanfica.wsfe.Wsfe;
import com.germanfica.wsfe.cms.Cms;
import com.germanfica.wsfe.param.CmsParams;
import com.germanfica.wsfe.util.ConfigLoader;
import com.germanfica.wsfe.util.XMLExtractor;

public class AuthExample {

    public static void main(String[] args) {
        try {
            // 1) Armar CMS para WSAA
            CmsParams cmsParams = CmsParams.builder()
                    .setKeystorePath(ConfigLoader.KEYSTORE_PATH)
                    .setPassword(ConfigLoader.KEYSTORE_PASSWORD)
                    .setSigner(ConfigLoader.KEYSTORE_SIGNER)
                    .setDstDn(ConfigLoader.DSTDN)
                    .setService(ConfigLoader.SERVICE)
                    .setTicketTime(ConfigLoader.TICKET_TIME)
                    .build();

            Cms cms = Cms.create(cmsParams);

            // 2) Endpoint de WSAA (homologación)
            Wsfe.overrideWsaaBase(Wsfe.TEST_WSAA_API_BASE);

            // 3) Crear el WsfeClient
            WsaaClient client = new WsaaClient();

            // 4) Invocar autenticación en WSAA
            String authResponse = client.authService().autenticar(cms);

            XMLExtractor extractor = new XMLExtractor(authResponse);
            String token = extractor.extractToken();
            String sign = extractor.extractSign();
            String generationTime = extractor.extractLoginTicketData().generationTime;
            String expirationTime = extractor.extractLoginTicketData().expirationTime;
            XMLExtractor.LoginTicketData data = extractor.extractLoginTicketData();

            // 5) Imprimir resultado
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
