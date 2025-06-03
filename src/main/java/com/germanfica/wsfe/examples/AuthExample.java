package com.germanfica.wsfe.examples;

import com.germanfica.wsfe.WsaaClient;
import com.germanfica.wsfe.cms.Cms;
import com.germanfica.wsfe.model.LoginTicketResponseData;
import com.germanfica.wsfe.net.ApiEnvironment;
import com.germanfica.wsfe.param.CmsParams;
import com.germanfica.wsfe.provider.ProviderChain;
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

            // 2) Crear el WsfeClient
            //WsaaClient client = WsaaClient.builder().build(); // (1)
            // Endpoint de WSAA (homologación)
            //WsaaClient client = WsaaClient.builder().setUrlBase("https://wsaa.afip.gov.ar").build();
            //WsaaClient client = WsaaClient.builder().setApiEnvironment(ApiEnvironment.HOMO).build(); // (2)
            WsaaClient client = WsaaClient.builder().setApiEnvironment(ApiEnvironment.PROD).build();

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
