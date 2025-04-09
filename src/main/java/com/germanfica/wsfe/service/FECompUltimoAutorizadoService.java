package com.germanfica.wsfe.service;

import com.germanfica.wsfe.net.ApiService;
import com.germanfica.wsfe.net.SoapRequestHandler;
import fev1.dif.afip.gov.ar.*;

import javax.xml.namespace.QName;

public class FECompUltimoAutorizadoService extends ApiService {
    // URL al WSDL (en Homologación)
    private static final String WSDL_URL = "https://wswhomo.afip.gov.ar/wsfev1/service.asmx?WSDL";
    QName qname = new QName("http://ar.gov.afip.dif.FEV1/", "Service");

    private final ServiceSoap port;

    public FECompUltimoAutorizadoService(SoapRequestHandler soapRequestHandler) {
        super(soapRequestHandler);
        // Inicialización del servicio SOAP
        Service service = new Service();
        this.port = service.getServiceSoap();
    }

    /**
     * Obtiene el último comprobante autorizado para un punto de venta y tipo de comprobante específicos.
     * @param token Token WSAA
     * @param sign Sign WSAA
     * @param cuit CUIT utilizado en el WS
     * @param ptoVta Punto de venta
     * @param cbteTipo Tipo de comprobante (Ej: 11 para Factura C)
     * @return Número del último comprobante autorizado
     */
    public int obtenerUltimoComprobante(String token, String sign, long cuit, int ptoVta, int cbteTipo) {
        FEAuthRequest auth = new FEAuthRequest();
        auth.setToken(token);
        auth.setSign(sign);
        auth.setCuit(cuit);

        FERecuperaLastCbteResponse response = port.feCompUltimoAutorizado(auth, ptoVta, cbteTipo);
        return response.getCbteNro();
    }
}
