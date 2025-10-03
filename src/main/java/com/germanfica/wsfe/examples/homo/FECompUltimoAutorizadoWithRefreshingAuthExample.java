package com.germanfica.wsfe.examples.homo;

import com.germanfica.wsfe.WsaaClient;
import com.germanfica.wsfe.WsfeClient;
import com.germanfica.wsfe.exception.ApiException;
import com.germanfica.wsfe.net.ApiEnvironment;
import com.germanfica.wsfe.provider.feauth.RefreshingAuthProvider;
import fev1.dif.afip.gov.ar.FERecuperaLastCbteResponse;

public class FECompUltimoAutorizadoWithRefreshingAuthExample {

    public static void main(String[] args) throws ApiException {
        int ptoVta = 1;
        int cbteTipo = 11; // Factura C

        // 1) Crear el WsaaClient
        WsaaClient wsaaClient = WsaaClient.builder()
            .setApiEnvironment(ApiEnvironment.HOMO)
            .build();

        // 2) Crear el WsfeClient
        WsfeClient client = WsfeClient.builder()
            .setApiEnvironment(ApiEnvironment.HOMO)
            .setFEAuthProvider(new RefreshingAuthProvider(wsaaClient))
            .build();

        // 3) Consultar comprobante
        FERecuperaLastCbteResponse response = client.feCompUltimoAutorizado(ptoVta, cbteTipo);
        System.out.println(response.getCbteNro());
    }
}
