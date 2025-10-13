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

    public CbteTipoResponse feParamGetTiposCbte() throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feParamGetTiposCbte(authProvider.getAuth()));
    }

    public ConceptoTipoResponse feParamGetTiposConcepto() throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feParamGetTiposConcepto(authProvider.getAuth()));
    }

    public CondicionIvaReceptorResponse feParamGetCondicionIvaReceptor(String claseCmp) throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feParamGetCondicionIvaReceptor(authProvider.getAuth(), claseCmp));
    }

    public DocTipoResponse feParamGetTiposDoc() throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feParamGetTiposDoc(authProvider.getAuth()));
    }

    public FEActividadesResponse feParamGetActividades() throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feParamGetActividades(authProvider.getAuth()));
    }

    public FECAEAGetResponse fecaeaConsultar(int periodo, short orden) throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.fecaeaConsultar(authProvider.getAuth(), periodo, orden));
    }

    public DummyResponse feDummy() throws ApiException {
        return invoke(null, ServiceSoap.class, ServiceSoap::feDummy);
    }

    public FECAEAGetResponse fecaeaSolicitar(int periodo, short orden) throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.fecaeaSolicitar(authProvider.getAuth(), periodo, orden));
    }

    public FECAEAResponse fecaeaRegInformativo(FECAEARequest feCAEARegInfReq) throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.fecaeaRegInformativo(authProvider.getAuth(), feCAEARegInfReq));
    }

    public FECAEASinMovConsResponse fecaeaSinMovimientoConsultar(String caea, int ptoVta) throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.fecaeaSinMovimientoConsultar(authProvider.getAuth(), caea, ptoVta));
    }

    public FECAEASinMovResponse fecaeaSinMovimientoInformar(int ptoVta, String caea) throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.fecaeaSinMovimientoInformar(authProvider.getAuth(), ptoVta, caea));
    }

    public FECAEResponse fecaeSolicitar(FECAERequest feCAEReq) throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.fecaeSolicitar(authProvider.getAuth(), feCAEReq));
    }

    public FECompConsultaResponse feCompConsultar(FECompConsultaReq feCompConsReq) throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feCompConsultar(authProvider.getAuth(), feCompConsReq));
    }

    public FECotizacionResponse feParamGetCotizacion(String monId, String fchCotiz) throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feParamGetCotizacion(authProvider.getAuth(), monId, fchCotiz));
    }

    public FEPaisResponse feParamGetTiposPaises() throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feParamGetTiposPaises(authProvider.getAuth()));
    }

    public FEPtoVentaResponse feParamGetPtosVenta() throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feParamGetPtosVenta(authProvider.getAuth()));
    }

    public FERecuperaLastCbteResponse feCompUltimoAutorizado(int ptoVta, int cbteTipo) throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feCompUltimoAutorizado(authProvider.getAuth(), ptoVta, cbteTipo));
    }

    public FERegXReqResponse feCompTotXRequest() throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feCompTotXRequest(authProvider.getAuth()));
    }

    public FETributoResponse feParamGetTiposTributos() throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feParamGetTiposTributos(authProvider.getAuth()));
    }

    public IvaTipoResponse feParamGetTiposIva() throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feParamGetTiposIva(authProvider.getAuth()));
    }

    public MonedaResponse feParamGetTiposMonedas() throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feParamGetTiposMonedas(authProvider.getAuth()));
    }

    public OpcionalTipoResponse feParamGetTiposOpcional() throws ApiException {
        return invoke(null, ServiceSoap.class, port -> port.feParamGetTiposOpcional(authProvider.getAuth()));
    }
}