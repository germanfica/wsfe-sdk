package com.germanfica.wsfe.examples;

import com.germanfica.wsfe.Wsfe;
import com.germanfica.wsfe.WsfeClient;
import com.germanfica.wsfe.util.ArcaWSAAUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class FECompUltimoAutorizadoExample {

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
        long cuit = Long.parseLong(properties.getProperty("cuit")); // CUIT real/homo

        try {
            // 2) Armar CMS para WSAA
            byte[] loginTicketRequestXmlCms = ArcaWSAAUtils.create_cms(
                    keystorePath, keystorePassword, keystoreSigner, dstdn, service, ticketTime);

            // 3) Endpoint de WSAA (homologación)
            Wsfe.overrideApiBase(Wsfe.TEST_API_BASE);
            String endpointWsaa = Wsfe.getApiBase() + "/ws/services/LoginCms";

            // 4) Crear el WsfeClient
            WsfeClient client = new WsfeClient(loginTicketRequestXmlCms);

            // 5) Invocar login WSAA para obtener Token y Sign
//            LoginCmsResponseDto loginResp = client.loginService().invokeWsaa(loginTicketRequestXmlCms, endpointWsaa);
//            String token = loginResp.getCredentials().getToken();
//            String sign  = loginResp.getCredentials().getSign();

            // Simulación de valores para pruebas
            String token = properties.getProperty("token");
            String sign  = properties.getProperty("sign");

            // 6) Invocar FECompUltimoAutorizadoService
            int puntoVenta = Integer.parseInt(properties.getProperty("puntoVenta", "1"));
            int tipoComprobante = Integer.parseInt(properties.getProperty("tipoComprobante", "11")); // Factura C

            int ultimoComprobante = client.feCompUltimoAutorizadoService()
                    .obtenerUltimoComprobante(token, sign, cuit, puntoVenta, tipoComprobante);

            // 7) Imprimir resultado
            System.out.println("Último Comprobante Autorizado: " + ultimoComprobante);

        } catch (Exception e) {
            System.err.println("Error al invocar WSFE: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
