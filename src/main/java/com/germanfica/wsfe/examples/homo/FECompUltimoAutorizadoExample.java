package com.germanfica.wsfe.examples.homo;

import com.germanfica.wsfe.WsfeClient;
import com.germanfica.wsfe.exception.ApiException;
import com.germanfica.wsfe.net.ApiEnvironment;
import com.germanfica.wsfe.param.FEAuthParams;
import com.germanfica.wsfe.provider.CredentialsProvider;
import com.germanfica.wsfe.provider.feauth.ApplicationPropertiesFeAuthParamsProvider;
import fev1.dif.afip.gov.ar.FERecuperaLastCbteResponse;

public class FECompUltimoAutorizadoExample {

    public static void main(String[] args) throws ApiException {
        // 1) Cargar credenciales
        CredentialsProvider<FEAuthParams> authProvider = new ApplicationPropertiesFeAuthParamsProvider();
        FEAuthParams auth = authProvider.resolve().orElseThrow(() -> new IllegalStateException("Credentials not found in application.properties"));

        // 2) Parámetros de la consulta
        int ptoVta = 1; // Punto de venta
        int cbteTipo = 11; // Factura C

        // 3) Crear el WsfeClient
        WsfeClient client = WsfeClient.builder()
            .setApiEnvironment(ApiEnvironment.HOMO)
            .setFEAuthParams(auth)
            .build();

        // 4) Consultar comprobante
        FERecuperaLastCbteResponse response = client.feCompUltimoAutorizado(ptoVta, cbteTipo);

        // 5) Imprimir resultado
        System.out.println(response.getCbteNro());
    }
}
