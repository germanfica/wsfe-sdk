package com.germanfica.wsfe.service;

import com.germanfica.wsfe.exception.ApiException;
import com.germanfica.wsfe.net.ApiService;
import com.germanfica.wsfe.net.SoapRequestHandler;
import com.germanfica.wsfe.provider.feauth.FEAuthProvider;
import fev1.dif.afip.gov.ar.*;

public class WsfeService extends ApiService {
    private final FEAuthProvider authProvider;

    public WsfeService(SoapRequestHandler soapRequestHandler, FEAuthProvider feAuthProvider) throws ApiException {
        super(soapRequestHandler);
        this.authProvider = feAuthProvider;
    }

    public FECAEResponse fecaeSolicitar(FECAERequest feCAEReq) throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.fecaeSolicitar(authProvider.getAuth(), feCAEReq));
    }

    public FERecuperaLastCbteResponse feCompUltimoAutorizado(int ptoVta, int cbteTipo) throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feCompUltimoAutorizado(authProvider.getAuth(), ptoVta, cbteTipo));
    }

    /**
     * Obtiene el último comprobante autorizado para un punto de venta y tipo de comprobante específicos.
     * @param ptoVta Punto de venta
     * @param cbteTipo Tipo de comprobante (Ej: 11 para Factura C)
     * @return Número del último comprobante autorizado
     */
    public int obtenerUltimoComprobante(int ptoVta, int cbteTipo) throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feCompUltimoAutorizado(authProvider.getAuth(), ptoVta, cbteTipo).getCbteNro());
    }
}
