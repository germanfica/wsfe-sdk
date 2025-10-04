package com.germanfica.wsfe.examples.homo;

import com.germanfica.wsfe.WsaaClient;
import com.germanfica.wsfe.WsfeClient;
import com.germanfica.wsfe.exception.ApiException;
import com.germanfica.wsfe.net.ApiEnvironment;
import com.germanfica.wsfe.provider.feauth.RefreshingAuthProvider;
import fev1.dif.afip.gov.ar.*;

public class FECAESolicitarWithRefreshingAuthExample {

    public static void main(String[] args) throws ApiException {
        // 1) Parámetros del comprobante
        int ptoVta = 1;
        int cbteTipo = 11;

        // 2) Crear el WsfeClient
        WsaaClient wsaaClient = WsaaClient.builder()
            .setApiEnvironment(ApiEnvironment.HOMO)
            .build();

        WsfeClient client = WsfeClient.builder()
            .setApiEnvironment(ApiEnvironment.HOMO)
            .setFEAuthProvider(new RefreshingAuthProvider(wsaaClient))
            .build();

        // 3) Cabecera FECAECabRequest
        FECAECabRequest cab = new FECAECabRequest();
        cab.setCantReg(1);  // Cantidad de comprobantes a enviar
        cab.setPtoVta(ptoVta);   // Punto de venta
        cab.setCbteTipo(cbteTipo);// Factura C

        // 4) Detalle FECAEDetRequest (datos de la factura)
        FECAEDetRequest det = new FECAEDetRequest();
        det.setConcepto(2);      // 2 = Servicios
        det.setDocTipo(99);      // 99 = Consumidor final / Doc. no informado
        det.setDocNro(0);        // 0 (anónimo)
        FERecuperaLastCbteResponse feRecuperaLastCbteResponse = client.feCompUltimoAutorizado(ptoVta, cbteTipo);
        det.setCbteDesde(feRecuperaLastCbteResponse.getCbteNro() + 1);     // Número de comprobante desde - FECompUltimoAutorizado + 1
        det.setCbteHasta(feRecuperaLastCbteResponse.getCbteNro() + 1);     // Número de comprobante hasta - FECompUltimoAutorizado + 1
        det.setCbteFch("20250131");  // Fecha de emisión (AAAAMMDD)
        det.setImpTotal(100.0);  // Importe total
        det.setImpTotConc(0.0);
        det.setImpNeto(100.0);   // Neto
        det.setImpOpEx(0.0);
        det.setImpTrib(0.0);
        det.setImpIVA(0.0);      // Para Factura C no corresponde IVA
        // Fechas servicio
        det.setFchServDesde("20250131");
        det.setFchServHasta("20250131");
        det.setFchVtoPago("20250131");
        // Moneda
        det.setMonId("PES");     // Pesos
        det.setMonCotiz(1.0);    // Cotización

        // 6) Contenedor para uno o más detalles
        ArrayOfFECAEDetRequest detalles = new ArrayOfFECAEDetRequest();
        detalles.getFECAEDetRequest().add(det);

        // 7) Construir el FECAERequest
        FECAERequest feCaeReq = new FECAERequest();
        feCaeReq.setFeCabReq(cab);
        feCaeReq.setFeDetReq(detalles);

        // 8) Invocar a AFIP
        FECAEResponse response = client.fecaeSolicitar(feCaeReq);

        // === Mostrar la respuesta en consola ===
        System.out.println("CAE Response: " + response);

        // Mostrar CAE si vino
        if (response.getFeDetResp() != null
            && !response.getFeDetResp().getFECAEDetResponse().isEmpty()) {
            System.out.println("CAE: " +
                response.getFeDetResp().getFECAEDetResponse().get(0).getCAE());
        }

        // Mostrar errores si existen
        if (response.getErrors() != null
            && !response.getErrors().getErr().isEmpty()) {
            response.getErrors().getErr().forEach(err -> {
                System.out.println("Error " + err.getCode() + ": " + err.getMsg());
            });
        }
    }
}
