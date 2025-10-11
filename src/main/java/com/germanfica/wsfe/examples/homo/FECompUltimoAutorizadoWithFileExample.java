package com.germanfica.wsfe.examples.homo;

import com.germanfica.wsfe.WsfeClient;
import com.germanfica.wsfe.exception.ApiException;
import com.germanfica.wsfe.net.ApiEnvironment;
import com.germanfica.wsfe.param.FEAuthParams;
import com.germanfica.wsfe.provider.ProviderChain;
import com.germanfica.wsfe.provider.feauth.FileFEAuthParamsProvider;
import fev1.dif.afip.gov.ar.FERecuperaLastCbteResponse;

/**
 * Ejemplo que demuestra cómo leer las credenciales (token, sign, cuit, etc.)
 * desde ~/.wsfe/feauth.ini usando {@link FileFEAuthParamsProvider} y la
 * cadena de proveedores {@link ProviderChain}.
 *
 * <p>Este ejemplo no requiere llamar a WSAA, ya que reutiliza un TA válido
 * previamente guardado.</p>
 *
 * <pre>
 * [default]
 * token = eyJhbGciOi...
 * sign = MIIG...
 * cuit = 20111111112
 * generationTime = 2025-10-01T00:00:00.000-03:00
 * expirationTime = 2025-10-02T00:00:00.000-03:00
 * </pre>
 */
public class FECompUltimoAutorizadoWithFileExample {

    public static void main(String[] args) throws ApiException {
        // 1) Cargar credenciales desde ~/.wsfe/feauth.ini
        FEAuthParams cache = ProviderChain.<FEAuthParams>builder()
            .addProvider(new FileFEAuthParamsProvider())
            .build()
            .resolve()
            .orElseThrow(() -> new IllegalStateException("No se encontró feauth.ini o está incompleto"));

        // 2) Parámetros de la consulta
        int ptoVta = 1; // Punto de venta
        int cbteTipo = 11; // Factura C

        // 3) Crear el WsfeClient con las credenciales cargadas
        WsfeClient client = WsfeClient.builder()
            .setApiEnvironment(ApiEnvironment.HOMO)
            .setFEAuthParams(cache)
            .build();

        // 4) Consultar el último comprobante autorizado
        FERecuperaLastCbteResponse response = client.feCompUltimoAutorizado(ptoVta, cbteTipo);

        // 5) Imprimir resultado
        System.out.printf("Último comprobante autorizado: %d%n", response.getCbteNro());
    }
}
