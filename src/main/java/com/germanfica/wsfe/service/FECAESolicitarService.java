package com.germanfica.wsfe.service;

import com.germanfica.wsfe.net.ApiService;
import com.germanfica.wsfe.net.SoapRequestHandler;
import fev1.dif.afip.gov.ar.*;

import javax.xml.namespace.QName;
import java.net.URL;


public class FECAESolicitarService extends ApiService {
    // URL al WSDL (en Homologación)
    private static final String WSDL_URL = "https://wswhomo.afip.gov.ar/wsfev1/service.asmx?WSDL";
    // El targetNamespace y el nombre del servicio suelen coincidir con lo generado
    QName qname = new QName("http://ar.gov.afip.dif.FEV1/", "Service");

    private final ServiceSoap port;

    public FECAESolicitarService(SoapRequestHandler soapRequestHandler) {
        super(soapRequestHandler);

        // Si se generó algo como Service()
        Service service = new Service();
        // Obtiene el "port" que implementa las operaciones del WSDL
        this.port = service.getServiceSoap();
    }

//    public FECAESolicitarService() {
//        // Si se generó algo como Service()
//        Service service = new Service();
//        // Obtiene el "port" que implementa las operaciones del WSDL
//        this.port = service.getServiceSoap();
//    }

    /**
     * Invoca el método FECAESolicitar (fecaeSolicitar) para emitir un comprobante
     * de tipo Factura C (CbteTipo=11) con Concepto=Servicios (2).
     * @param token   Token WSAA
     * @param sign    Sign WSAA
     * @param cuit    CUIT utilizado en el WS
     * @return        Respuesta de AFIP (FECAEResponse)
     */
    public FECAEResponse invokeWsfev1(String token, String sign, long cuit) {
        int ptoVta = 1;
        int cbteTipo = 11;

        // 1) Armar el objeto FEAuthRequest con las credenciales
        FEAuthRequest auth = new FEAuthRequest();
        auth.setToken(token);
        auth.setSign(sign);
        auth.setCuit(cuit);

        // 2) Cabecera FECAECabRequest
        FECAECabRequest cab = new FECAECabRequest();
        cab.setCantReg(1);  // Cantidad de comprobantes a enviar
        cab.setPtoVta(ptoVta);   // Punto de venta
        cab.setCbteTipo(cbteTipo);// Factura C

        // 3) Detalle FECAEDetRequest (datos de la factura)
        FECAEDetRequest det = new FECAEDetRequest();
        det.setConcepto(2);      // 2 = Servicios
        det.setDocTipo(99);      // 99 = Consumidor final / Doc. no informado
        det.setDocNro(0);        // 0 (anónimo)
        FERecuperaLastCbteResponse feRecuperaLastCbteResponse = port.feCompUltimoAutorizado(auth, ptoVta, cbteTipo);
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

        // 4) Contenedor para uno o más detalles
        ArrayOfFECAEDetRequest detalles = new ArrayOfFECAEDetRequest();
        detalles.getFECAEDetRequest().add(det);

        // 5) Construir el FECAERequest
        FECAERequest feCaeReq = new FECAERequest();
        feCaeReq.setFeCabReq(cab);
        feCaeReq.setFeDetReq(detalles);

        // 6) Invocar a AFIP
        FECAEResponse response = port.fecaeSolicitar(auth, feCaeReq);
        return response;
    }
}
