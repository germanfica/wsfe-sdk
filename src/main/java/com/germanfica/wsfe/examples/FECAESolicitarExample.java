package com.germanfica.wsfe.examples;

import com.germanfica.wsfe.WsfeClient;
import com.germanfica.wsfe.exception.ApiException;
import com.germanfica.wsfe.util.ConfigLoader;
import fev1.dif.afip.gov.ar.*;

public class FECAESolicitarExample {

    public static void main(String[] args) throws ApiException {
        // Cargar token y sign desde ConfigLoader
        final String token = ConfigLoader.TOKEN;
        final String sign = ConfigLoader.SIGN;
        final Long cuit = ConfigLoader.CUIT;
        int ptoVta = 1;
        int cbteTipo = 11;

        // 1) Crear el WsfeClient
        WsfeClient client = WsfeClient.builder().build();

        // 2) Armar el objeto FEAuthRequest con las credenciales
        FEAuthRequest auth = new FEAuthRequest();
        auth.setToken(token);
        auth.setSign(sign);
        auth.setCuit(cuit);

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

        // 5) Contenedor para uno o más detalles
        ArrayOfFECAEDetRequest detalles = new ArrayOfFECAEDetRequest();
        detalles.getFECAEDetRequest().add(det);

        // 6) Construir el FECAERequest
        FECAERequest feCaeReq = new FECAERequest();
        feCaeReq.setFeCabReq(cab);
        feCaeReq.setFeDetReq(detalles);

        // 6) Invocar a AFIP
        FECAEResponse response = client.fecaeSolicitar(feCaeReq);
    }
}
