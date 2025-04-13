package com.germanfica.wsfe.examples;

import fev1.dif.afip.gov.ar.FECAEResponse;

import com.germanfica.wsfe.Wsfe;
import com.germanfica.wsfe.WsfeClient;
import com.germanfica.wsfe.util.ArcaWSAAUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class FECAESolicitarExample {

        public static void main(String[] args) {

            // 1) Cargar config
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
            String token = properties.getProperty("token");
            String sign  = properties.getProperty("sign");
            long   cuit  = Long.parseLong(properties.getProperty("cuit"));  // tu CUIT real/homo

            try {
                // 2) Armar CMS para WSAA
                byte[] loginTicketRequestXmlCms = ArcaWSAAUtils.create_cms(
                        keystorePath,
                        keystorePassword,
                        keystoreSigner,
                        dstdn,
                        service,
                        ticketTime
                );

                // 3) Endpoint de WSAA (homologación)
                Wsfe.overrideWsaaBase(Wsfe.TEST_WSAA_API_BASE);
                String endpointWsaa = Wsfe.getWsaaBase() + "/ws/services/LoginCms";

                // 4) Crear el WsfeClient
                WsfeClient client = new WsfeClient(loginTicketRequestXmlCms);

                // 5) Invocar login WSAA para obtener Token y Sign
//                LoginCmsResponseDto loginResp = client.loginService().invokeWsaa(loginTicketRequestXmlCms, endpointWsaa);

//                String token = loginResp.getCredentials().getToken();
//                String sign  = loginResp.getCredentials().getSign();
//                long cuit    = 20223344556L;  // tu CUIT real/homo

//                System.out.println("Token: " + token);
//                System.out.println("Sign:  " + sign);

                // 6) Invocar WSFE (FECAESolicitar) con las credenciales
                FECAEResponse feResp = client.fecaeSolicitarService().invokeWsfev1(token, sign, cuit);

                // 7) Procesar la respuesta
                if (feResp.getFeDetResp() != null && !feResp.getFeDetResp().getFECAEDetResponse().isEmpty()) {
                    var detResp = feResp.getFeDetResp().getFECAEDetResponse().get(0);
                    System.out.println("CAE: " + detResp.getCAE());
                    System.out.println("CAE Vto: " + detResp.getCAEFchVto());
                }

                if (feResp.getErrors() != null && feResp.getErrors().getErr() != null) {
                    feResp.getErrors().getErr().forEach(err -> {
                        System.out.println("Error Code: " + err.getCode());
                        System.out.println("Error Msg : " + err.getMsg());
                    });
                }

            } catch (Exception e) {
                System.err.println("Error al invocar WSFE: " + e.getMessage());
                e.printStackTrace();
            }
        }
}
