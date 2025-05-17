package com.germanfica.wsfe.examples;

import com.germanfica.wsfe.WsfeClient;
import com.germanfica.wsfe.exception.ApiException;
import com.germanfica.wsfe.util.ConfigLoader;
import fev1.dif.afip.gov.ar.FEAuthRequest;
import fev1.dif.afip.gov.ar.FERecuperaLastCbteResponse;

public class FECompUltimoAutorizadoExample {

    public static void main(String[] args) throws ApiException {
        // Cargar token y sign desde ConfigLoader
        final String token = ConfigLoader.TOKEN;
        final String sign = ConfigLoader.SIGN;
        final Long cuit = ConfigLoader.CUIT;
        int ptoVta = 1;
        int cbteTipo = 11; // Factura C

        // 1) Crear el WsfeClient
        WsfeClient client = new WsfeClient();

        // 2) Armar el objeto FEAuthRequest con las credenciales
        FEAuthRequest auth = new FEAuthRequest();
        auth.setToken(token);
        auth.setSign(sign);
        auth.setCuit(cuit);

        // 3) Consultar comprobante
        FERecuperaLastCbteResponse response = client.feCompUltimoAutorizado(auth, ptoVta, cbteTipo);
        System.out.println(response.getCbteNro());
    }
}
