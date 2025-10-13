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

    /**
     * Recupera el listado  de Tipos de Comprobantes utilizables en servicio de autorización.
     */
    public CbteTipoResponse feParamGetTiposCbte() throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feParamGetTiposCbte(authProvider.getAuth()));
    }

    /**
     * Recupera el listado  de identificadores para el campo Concepto.
     */
    public ConceptoTipoResponse feParamGetTiposConcepto() throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feParamGetTiposConcepto(authProvider.getAuth()));
    }

    /**
     * Recupera la condicion frente al IVA del receptor (para una clase de comprobante determinada o para todos si no se especifica).
     */
    public CondicionIvaReceptorResponse feParamGetCondicionIvaReceptor(String claseCmp) throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feParamGetCondicionIvaReceptor(authProvider.getAuth(), claseCmp));
    }

    /**
     * Recupera el listado  de Tipos de Documentos utilizables en servicio de autorización.
     */
    public DocTipoResponse feParamGetTiposDoc() throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feParamGetTiposDoc(authProvider.getAuth()));
    }

    /**
     * Recupera el listado de las diferentes actividades habilitadas para el emisor
     */
    public FEActividadesResponse feParamGetActividades() throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feParamGetActividades(authProvider.getAuth()));
    }

    /**
     * Consultar CAEA emitidos.
     */
    public FECAEAGetResponse fecaeaConsultar(int periodo, short orden) throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.fecaeaConsultar(authProvider.getAuth(), periodo, orden));
    }

    /**
     * Metodo dummy para verificacion de funcionamiento
     */
    public DummyResponse feDummy() throws ApiException {
        return invoke(null, ServiceSoap.class, ServiceSoap::feDummy);
    }

    /**
     * Solicitud de Código de Autorización Electrónico (CAE)
     */
    public FECAEAGetResponse fecaeaSolicitar(int periodo, short orden) throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.fecaeaSolicitar(authProvider.getAuth(), periodo, orden));
    }

    /**
     * Rendición de comprobantes asociados a un CAEA.
     */
    public FECAEAResponse fecaeaRegInformativo(FECAEARequest feCAEARegInfReq) throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.fecaeaRegInformativo(authProvider.getAuth(), feCAEARegInfReq));
    }

    /**
     * Consulta CAEA informado como sin movimientos.
     */
    public FECAEASinMovConsResponse fecaeaSinMovimientoConsultar(String caea, int ptoVta) throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.fecaeaSinMovimientoConsultar(authProvider.getAuth(), caea, ptoVta));
    }

    /**
     * Informa CAEA sin movimientos.
     */
    public FECAEASinMovResponse fecaeaSinMovimientoInformar(int ptoVta, String caea) throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.fecaeaSinMovimientoInformar(authProvider.getAuth(), ptoVta, caea));
    }

    /**
     * Solicitud de Código de Autorización Electrónico (CAE)
     */
    public FECAEResponse fecaeSolicitar(FECAERequest feCAEReq) throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.fecaeSolicitar(authProvider.getAuth(), feCAEReq));
    }

    /**
     * Consulta Comprobante emitido y su código.
     */
    public FECompConsultaResponse feCompConsultar(FECompConsultaReq feCompConsReq) throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feCompConsultar(authProvider.getAuth(), feCompConsReq));
    }

    /**
     * Recupera la cotizacion de la moneda consultada y su  fecha
     */
    public FECotizacionResponse feParamGetCotizacion(String monId, String fchCotiz) throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feParamGetCotizacion(authProvider.getAuth(), monId, fchCotiz));
    }

    /**
     * Recupera el listado de los diferente paises que pueden ser utilizados  en el servicio de autorizacion
     */
    public FEPaisResponse feParamGetTiposPaises() throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feParamGetTiposPaises(authProvider.getAuth()));
    }

    /**
     * Recupera el listado de puntos de venta registrados y su estado
     */
    public FEPtoVentaResponse feParamGetPtosVenta() throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feParamGetPtosVenta(authProvider.getAuth()));
    }

    /**
     * Retorna el ultimo comprobante autorizado para el tipo de comprobante / cuit / punto de venta ingresado / Tipo de Emisión
     */
    public FERecuperaLastCbteResponse feCompUltimoAutorizado(int ptoVta, int cbteTipo) throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feCompUltimoAutorizado(authProvider.getAuth(), ptoVta, cbteTipo));
    }

    /**
     * Retorna la cantidad maxima de registros que puede tener una invocacion al metodo FECAESolicitar / FECAEARegInformativo
     */
    public FERegXReqResponse feCompTotXRequest() throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feCompTotXRequest(authProvider.getAuth()));
    }

    /**
     * Recupera el listado  de los diferente tributos que pueden ser utilizados  en el servicio de autorizacion
     */
    public FETributoResponse feParamGetTiposTributos() throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feParamGetTiposTributos(authProvider.getAuth()));
    }

    /**
     * Recupera el listado  de Tipos de Iva utilizables en servicio de autorización.
     */
    public IvaTipoResponse feParamGetTiposIva() throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feParamGetTiposIva(authProvider.getAuth()));
    }

    /**
     * Recupera el listado de monedas utilizables en servicio de autorización
     */
    public MonedaResponse feParamGetTiposMonedas() throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feParamGetTiposMonedas(authProvider.getAuth()));
    }

    /**
     * Recupera el listado de identificadores para los campos Opcionales
     */
    public OpcionalTipoResponse feParamGetTiposOpcional() throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feParamGetTiposOpcional(authProvider.getAuth()));
    }
}