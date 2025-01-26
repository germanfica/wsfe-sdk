package com.germanfica.wsfe.examples;

import com.germanfica.wsfe.Wsfe;
import com.germanfica.wsfe.WsfeClient;
import com.germanfica.wsfe.dto.LoginCmsResponseDto;
import com.germanfica.wsfe.utils.ArcaWSAAUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class WsfeExample {

    public static void main(String[] args) {
        // Cargar las propiedades desde el archivo application.properties
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("src/main/resources/application.properties")) {
            properties.load(input);
        } catch (IOException e) {
            System.err.println("Error al cargar el archivo application.properties: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Leer las propiedades necesarias
        String keystorePath = properties.getProperty("keystore");
        String keystorePassword = properties.getProperty("keystore-password");
        String keystoreSigner = properties.getProperty("keystore-signer");
        String dstdn = properties.getProperty("dstdn", "cn=wsaahomo,o=afip,c=ar,serialNumber=CUIT 33693450239");
        Long ticketTime = Long.parseLong(properties.getProperty("TicketTime", "36000"));
        String service = properties.getProperty("service");

        try {
            // Crear el CMS
            byte[] loginTicketRequestXmlCms = ArcaWSAAUtils.create_cms(
                    keystorePath,
                    keystorePassword,
                    keystoreSigner,
                    dstdn,
                    service,
                    ticketTime
            );

            // Obtener el endpoint base dinámico de WSAA
            Wsfe.overrideWsaaBase(Wsfe.TEST_WSAA_API_BASE); // Usar TEST_WSAA_API_BASE para pruebas
            String endpoint = Wsfe.getWsaaBase() + "/ws/services/LoginCms"; // Concatenar la ruta específica

            // Invocar el WSAA
            WsfeClient client = new WsfeClient(loginTicketRequestXmlCms);
            LoginCmsResponseDto response = client.loginService().invokeWsaa(loginTicketRequestXmlCms, endpoint);

            // Imprimir la respuesta recibida
            System.out.println("Header:");
            System.out.println("Unique ID: " + response.getHeader().getUniqueId());
            System.out.println("Generation Time: " + response.getHeader().getGenerationTime());
            System.out.println("Expiration Time: " + response.getHeader().getExpirationTime());

            System.out.println("Credentials:");
            System.out.println("Token: " + response.getCredentials().getToken());
            System.out.println("Sign: " + response.getCredentials().getSign());

        } catch (Exception e) {
            // Manejo de errores en caso de fallo
            System.err.println("Error al invocar el servicio WSAA: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
