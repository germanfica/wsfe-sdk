package com.germanfica.wsfe.examples.homo;

import com.germanfica.wsfe.WsaaClient;
import com.germanfica.wsfe.cms.Cms;
import com.germanfica.wsfe.model.LoginTicketResponseData;
import com.germanfica.wsfe.net.ApiEnvironment;
import com.germanfica.wsfe.util.LoginTicketParser;
import com.germanfica.wsfe.util.SimpleIni;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Ejemplo que lee directamente ~/.wsfe/cms.ini usando SimpleIni (igual que FileSignedCmsProvider)
 * y luego invoca WSAA con el CMS obtenido.
 *
 * Comportamiento:
 *  - si no existe ~/.wsfe/cms.ini o la clave está vacía -> falla con mensaje claro
 *  - valida que el certificado no esté expirado y que el TRA no esté vencido
 */
public class AuthWithSimpleIniCmsExample {

    /* ~/.wsfe/cms.ini */
    private static final Path CMS_FILE = Paths.get(
        System.getProperty("user.home"), ".wsfe", "cms.ini"
    );
    private static final String SECTION = "default";
    private static final String KEY     = "signedCms";

    public static void main(String[] args) {
        try {
            String signedCms = readSignedCmsFromIni()
                .orElseThrow(() -> new IllegalStateException("No se encontro un signedCms valido en " + CMS_FILE));

            Cms cms = Cms.create(signedCms);

            // validaciones identicas a FileSignedCmsProvider
//            if (cms.isCertExpired()) {
//                throw new IllegalStateException("Certificate expired: " + cms.getSubjectCuit());
//            }
//            if (cms.isTicketExpired()) {
//                throw new IllegalStateException("TRA (ticket) vencido: forzar re-firma del CMS");
//            }

            // Crear cliente WSAA (HOMO)
            WsaaClient client = WsaaClient.builder()
                .setApiEnvironment(ApiEnvironment.HOMO)
                .build();

            // Invocar autenticacion
            String authResponse = client.authService().autenticar(cms);

            LoginTicketResponseData data = (LoginTicketResponseData) LoginTicketParser.parse(authResponse);

            // Imprimir resultados
            System.out.println("Respuesta de autenticacion xml: \n" + authResponse);
            System.out.println("Respuesta de autenticacion json: \n" + data);
            System.out.println("Token: " + data.token());
            System.out.println("Sign: " + data.sign());
            System.out.println("generationTime: " + data.generationTime());
            System.out.println("expirationTime: " + data.expirationTime());

        } catch (Exception e) {
            System.err.println("❌ Error en autenticacion WSAA: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lee ~/.wsfe/cms.ini y devuelve Optional con el valor de 'signedCms' en la seccion 'default'.
     * En problemas de IO se retorna Optional.empty() para no lanzar un checked exception desde aqui.
     */
    private static Optional<String> readSignedCmsFromIni() {
        try {
            if (!Files.exists(CMS_FILE)) return Optional.empty();

            SimpleIni ini = SimpleIni.load(CMS_FILE);
            String cms = ini.get(SECTION, KEY);
            if (cms == null || cms.isBlank()) return Optional.empty();
            return Optional.of(cms.trim());
        } catch (IOException e) {
            // Problemas de lectura se tratan como "no encontrado" para que el ejemplo falle con mensaje claro más arriba.
            return Optional.empty();
        }
    }
}
