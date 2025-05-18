package com.germanfica.wsfe.service;

import com.germanfica.wsfe.exception.ApiException;
import com.germanfica.wsfe.net.ApiService;
import com.germanfica.wsfe.net.SoapRequestHandler;
import fev1.dif.afip.gov.ar.*;

public class WsfeService extends ApiService {
    public WsfeService(SoapRequestHandler soapRequestHandler) throws ApiException {
        super(soapRequestHandler);
    }

    public FECAEResponse fecaeSolicitar(FEAuthRequest auth, FECAERequest feCAEReq) throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.fecaeSolicitar(auth, feCAEReq));
    }

    public FERecuperaLastCbteResponse feCompUltimoAutorizado(FEAuthRequest auth, int ptoVta, int cbteTipo) throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feCompUltimoAutorizado(auth, ptoVta, cbteTipo));
        //return this.request(null, () -> port.feCompUltimoAutorizado(auth, ptoVta, cbteTipo));
    }

    /**
     * Obtiene el último comprobante autorizado para un punto de venta y tipo de comprobante específicos.
     * @param auth Objeto de autenticación que contiene el token, sign y CUIT
     * @param ptoVta Punto de venta
     * @param cbteTipo Tipo de comprobante (Ej: 11 para Factura C)
     * @return Número del último comprobante autorizado
     */
    public int obtenerUltimoComprobante(FEAuthRequest auth, int ptoVta, int cbteTipo) throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feCompUltimoAutorizado(auth, ptoVta, cbteTipo).getCbteNro());
        //return port.feCompUltimoAutorizado(auth, ptoVta, cbteTipo).getCbteNro();
    }
}
