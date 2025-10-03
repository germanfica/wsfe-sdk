package com.germanfica.wsfe.examples.homo;

import com.germanfica.wsfe.WsaaClient;
import com.germanfica.wsfe.WsfeClient;
import com.germanfica.wsfe.exception.ApiException;
import com.germanfica.wsfe.net.ApiEnvironment;
import com.germanfica.wsfe.provider.feauth.RefreshingAuthProvider;
import fev1.dif.afip.gov.ar.ActividadesTipo;
import fev1.dif.afip.gov.ar.Err;
import fev1.dif.afip.gov.ar.Evt;
import fev1.dif.afip.gov.ar.FEActividadesResponse;

/**
 * Ejemplo de cómo consultar las actividades vigentes del emisor (CUIT)
 * usando el método {@code FEParamGetActividades} del WSFEv1.
 * <p>
 * Según la RG 5259/2022 y RG 5264/2022, este método devuelve
 * las actividades declaradas por el emisor. En homologación
 * puede devolver "Sin resultados" si el CUIT de prueba
 * no tiene actividades cargadas en el ambiente de test.
 * </p>
 */
public class ConsultarActividadesVigentesExample {
    /**
     * Punto de entrada del ejemplo.
     * <ol>
     *   <li>Inicializa el cliente WSAA para autenticación.</li>
     *   <li>Construye el cliente WSFE con autenticación de autorenovación.</li>
     *   <li>Llama a {@code feParamGetActividades} y muestra en consola:</li>
     *   <ul>
     *     <li>Errores (si los hay).</li>
     *     <li>Eventos (si los hay).</li>
     *     <li>Listado de actividades vigentes.</li>
     *   </ul>
     * </ol>
     *
     * @throws ApiException si falla la llamada al Web Service
     */
    public static void main(String[] args) throws ApiException {
        // 1) Crear el WsaaClient
        WsaaClient wsaaClient = WsaaClient.builder()
            .setApiEnvironment(ApiEnvironment.HOMO)
            .build();

        // 2) Crear el WsfeClient
        WsfeClient client = WsfeClient.builder()
            .setApiEnvironment(ApiEnvironment.HOMO)
            .setFEAuthProvider(new RefreshingAuthProvider(wsaaClient))
            .build();

        // 3) Consultar
        FEActividadesResponse response = client.feParamGetActividades();

        // Imprimir errores si existen
        if (response.getErrors() != null && response.getErrors().getErr() != null) {
            System.out.println("Errores:");
            for (Err err : response.getErrors().getErr()) {
                System.out.printf("  Código: %d - Mensaje: %s%n", err.getCode(), err.getMsg());
            }
        }

        // Imprimir eventos si existen
        if (response.getEvents() != null && response.getEvents().getEvt() != null) {
            System.out.println("Eventos:");
            for (Evt evt : response.getEvents().getEvt()) {
                System.out.printf("  Código: %d - Mensaje: %s%n", evt.getCode(), evt.getMsg());
            }
        }

        // Imprimir actividades vigentes
        if (response.getResultGet() != null && response.getResultGet().getActividadesTipo() != null) {
            System.out.println("Actividades vigentes:");
            for (ActividadesTipo actividad : response.getResultGet().getActividadesTipo()) {
                System.out.printf("  ID: %d - Orden: %d - Descripción: %s%n",
                    actividad.getId(), actividad.getOrden(), actividad.getDesc());
            }
        } else {
            System.out.println("No se encontraron actividades vigentes.");
        }
    }
}
