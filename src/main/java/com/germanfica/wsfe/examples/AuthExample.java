package com.germanfica.wsfe.examples;

import com.germanfica.wsfe.Wsaa;
import com.germanfica.wsfe.WsaaClient;

import com.germanfica.wsfe.cms.Cms;
import com.germanfica.wsfe.param.CmsParams;
import com.germanfica.wsfe.util.*;

public class AuthExample {

    public static void main(String[] args) {
        try {
            // 1) Armar CMS para WSAA
            // Crear ProviderChain que obtiene CmsParams desde múltiples fuentes
            ProviderChain<CmsParams> providerChain = ProviderChain.<CmsParams>builder()
                    .addProvider(new EnvironmentCmsParamsProvider())    // Usa variables de entorno
                    .addProvider(new SystemPropertyCmsParamsProvider()) // Usa propiedades del sistema
                    .addProvider(new ApplicationPropertiesCmsParamsProvider())
                    .build();

            CmsParams cmsParams = providerChain.resolve()
                    .orElseThrow(() -> new IllegalStateException("No se pudieron obtener los CmsParams."));

            Cms cms = Cms.create(cmsParams);

            // 2) Endpoint de WSAA (homologación)
            Wsaa.overrideApiBase(Wsaa.TEST_API_BASE);

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
