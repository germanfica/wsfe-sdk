package com.germanfica.wsfe.examples;

import com.germanfica.wsfe.util.ConfigLoader;
import fev1.dif.afip.gov.ar.FECAEResponse;

import com.germanfica.wsfe.Wsfe;
import com.germanfica.wsfe.WsfeClient;

public class FECAESolicitarExample {

        public static void main(String[] args) {
            // TODO: el siguiente paso sería revisar esto. Porque Ahora existe la clase Cms. Además sería mejor que FECAE y los servicios WSFE solo requiran token y NO el Cms... ya que el Cms es parte del Wsaa
            try {
                // 1) Endpoint de WSAA (homologación)
                Wsfe.overrideApiBase(Wsfe.TEST_API_BASE);

                // 2) Crear el WsfeClient
                WsfeClient client = new WsfeClient(null);

                // 3) Invocar WSFE (FECAESolicitar) con las credenciales
                FECAEResponse feResp = client.fecaeSolicitarService().invokeWsfev1(ConfigLoader.TOKEN, ConfigLoader.SIGN, ConfigLoader.CUIT);

                // 4) Procesar la respuesta
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
