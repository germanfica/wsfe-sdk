package com.germanfica.wsfe.service;

import com.germanfica.wsfe.exception.ApiException;
import com.germanfica.wsfe.net.ApiService;
import com.germanfica.wsfe.net.SoapRequestHandler;
import fev1.dif.afip.gov.ar.*;
import jakarta.xml.ws.BindingProvider;

public class WsfeService extends ApiService {
    private static final String URL = "https://wswhomo.afip.gov.ar/wsfev1/service.asmx";

    private final ServiceSoap port;

    public WsfeService(SoapRequestHandler soapRequestHandler) throws ApiException {
        super(soapRequestHandler);
        // Inicializar el servicio SOAP
        Service service = new Service();
        this.port = service.getServiceSoap();

        // Sobrescribir el endpoint para usar homologación (aunque la clase es de producción)
        ((BindingProvider) this.port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, URL);
    }

    public FECAEResponse fecaeSolicitar(FEAuthRequest auth, FECAERequest feCAEReq) throws ApiException {
        return this.request(null, () -> port.fecaeSolicitar(auth, feCAEReq));
    }

    public FERecuperaLastCbteResponse feCompUltimoAutorizado(FEAuthRequest auth, int ptoVta, int cbteTipo) throws ApiException {
        return this.request(null, () -> port.feCompUltimoAutorizado(auth, ptoVta, cbteTipo));
    }

    /**
     * Obtiene el último comprobante autorizado para un punto de venta y tipo de comprobante específicos.
     * @param auth Objeto de autenticación que contiene el token, sign y CUIT
     * @param ptoVta Punto de venta
     * @param cbteTipo Tipo de comprobante (Ej: 11 para Factura C)
     * @return Número del último comprobante autorizado
     */
    public int obtenerUltimoComprobante(FEAuthRequest auth, int ptoVta, int cbteTipo) {
        return port.feCompUltimoAutorizado(auth, ptoVta, cbteTipo).getCbteNro();
    }
}
