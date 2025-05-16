package com.germanfica.wsfe.service;

import ar.gov.afip.wsfe.test.Service;
import ar.gov.afip.wsfe.test.ServiceSoap;
import com.germanfica.wsfe.exception.ApiException;
import com.germanfica.wsfe.net.ApiService;
import com.germanfica.wsfe.net.SoapRequestHandler;

public class WsfeService extends ApiService {
    private final ServiceSoap port;

    public WsfeService(SoapRequestHandler soapRequestHandler) {
        super(soapRequestHandler);
        // Inicializar el servicio SOAP
        Service service = new Service();
        this.port = service.getServiceSoap();
    }

    public ar.gov.afip.wsfe.test.FECAEResponse fecaeSolicitar(ar.gov.afip.wsfe.test.FEAuthRequest auth, ar.gov.afip.wsfe.test.FECAERequest feCAEReq) throws ApiException {
        return this.request(null, () -> port.fecaeSolicitar(auth, feCAEReq));
    }

    public ar.gov.afip.wsfe.test.FERecuperaLastCbteResponse feCompUltimoAutorizado(ar.gov.afip.wsfe.test.FEAuthRequest auth, int ptoVta, int cbteTipo) throws ApiException {
        return this.request(null, () -> port.feCompUltimoAutorizado(auth, ptoVta, cbteTipo));
    }

    /**
     * Obtiene el último comprobante autorizado para un punto de venta y tipo de comprobante específicos.
     * @param auth Objeto de autenticación que contiene el token, sign y CUIT
     * @param ptoVta Punto de venta
     * @param cbteTipo Tipo de comprobante (Ej: 11 para Factura C)
     * @return Número del último comprobante autorizado
     */
    public int obtenerUltimoComprobante(ar.gov.afip.wsfe.test.FEAuthRequest auth, int ptoVta, int cbteTipo) {
        return port.feCompUltimoAutorizado(auth, ptoVta, cbteTipo).getCbteNro();
    }
}
