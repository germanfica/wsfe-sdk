package com.germanfica.wsfe.examples;

import com.germanfica.wsfe.Wsfe;
import com.germanfica.wsfe.WsfeClient;
import com.germanfica.wsfe.utils.ArcaWSAAUtils;
import org.apache.commons.codec.binary.Base64;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AuthExample {

    public static void main(String[] args) {
        // 1) Cargar configuración
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("src/main/resources/application.properties")) {
            properties.load(input);
        } catch (IOException e) {
            System.err.println("Error al cargar application.properties: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        String keystorePath = properties.getProperty("keystore");
        String keystorePassword = properties.getProperty("keystore-password");
        String keystoreSigner = properties.getProperty("keystore-signer");
        String dstdn = properties.getProperty("dstdn", "cn=wsaahomo,o=afip,c=ar,serialNumber=CUIT 33693450239");
        Long ticketTime = Long.parseLong(properties.getProperty("TicketTime", "36000"));
        String service = properties.getProperty("service");

        try {
            // 2) Armar CMS para WSAA
            byte[] loginTicketRequestXmlCms = ArcaWSAAUtils.create_cms(
                    keystorePath, keystorePassword, keystoreSigner, dstdn, service, ticketTime);

            // 3) Endpoint de WSAA (homologación)
            Wsfe.overrideWsaaBase(Wsfe.TEST_WSAA_API_BASE);
            String endpointWsaa = Wsfe.getWsaaBase() + "/ws/services/LoginCms";

            // 4) Crear el WsfeClient
            WsfeClient client = new WsfeClient(loginTicketRequestXmlCms);

            byte[] decodedCms = Base64.decodeBase64(Base64.encodeBase64String(loginTicketRequestXmlCms));

            //String cmsFirmado = new String(loginTicketRequestXmlCms);
            String cmsFirmado = Base64.encodeBase64String(loginTicketRequestXmlCms);

            System.out.println("cmsFirmado: " + cmsFirmado);

            // 5) Invocar autenticación en WSAA
            String authResponse = client.authService().autenticar(cmsFirmado);

            // 6) Imprimir resultado
            System.out.println("Respuesta de autenticación: \n" + authResponse);

        } catch (Exception e) {
            System.err.println("Error al invocar autenticación WSAA: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
