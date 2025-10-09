package com.germanfica.wsfe.examples.homo;

import com.germanfica.wsfe.WsaaClient;
import com.germanfica.wsfe.cms.Cms;
import com.germanfica.wsfe.model.LoginTicketResponseData;
import com.germanfica.wsfe.net.ApiEnvironment;
import com.germanfica.wsfe.provider.ProviderChain;
import com.germanfica.wsfe.provider.cms.FileSignedCmsProvider;
import com.germanfica.wsfe.util.LoginTicketParser;

/**
 * Ejemplo que reutiliza el CMS firmado desde ~/.wsfe/cms.ini
 *
 * <p>Si no existe el CMS o tiene o TRA vencido, lanza error directamente.</p>
 */
public class AuthWithFileCmsExample {

    public static void main(String[] args) {
        try {
            // 1) Resolver el CMS firmado desde archivo
            ProviderChain<String> cmsChain = ProviderChain.<String>builder()
                .addProvider(new FileSignedCmsProvider())
                .build();

            String cmsParams = cmsChain.resolve()
                .orElseThrow(() -> new IllegalStateException("No se pudo obtener un CMS firmado: archivo inexistente o TRA vencido."));

            Cms cms = Cms.create(cmsParams);

            // 2) Crear el WsfeClient
            WsaaClient client = WsaaClient.builder().setApiEnvironment(ApiEnvironment.HOMO).build(); // (2)

            // 3) Invocar autenticación en WSAA
            String authResponse = client.authService().autenticar(cms);

            LoginTicketResponseData data = (LoginTicketResponseData) LoginTicketParser.parse(authResponse);

            // Guardar TA en disco
//            FEAuthParams params = FEAuthParams.builder()
//                .setToken(data.token())
//                .setSign(data.sign())
//                .setCuit(cms.getSubjectCuit())
//                .setGenerationTime(ArcaDateTime.parse(data.generationTime()))
//                .setExpirationTime(ArcaDateTime.parse(data.generationTime()))
//                .build();
//
//            FileFEAuthParamsProvider.save(params);

            // 4) Imprimir resultados
            //System.out.println("Nuevo TA obtenido y guardado ✅");
            System.out.println("Respuesta de autenticación xml: \n" + authResponse);
            System.out.println("Respuesta de autenticación json: \n" + data);
            System.out.println("Token: " + data.token());
            System.out.println("Sign: " + data.sign());
            System.out.println("generationTime: " + data.generationTime());
            System.out.println("expirationTime: " + data.expirationTime());

        } catch (Exception e) {
            System.err.println("❌ Error en autenticación WSAA: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
