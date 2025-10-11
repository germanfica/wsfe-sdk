package com.germanfica.wsfe.net;

import com.germanfica.wsfe.exception.ApiException;

import com.germanfica.wsfe.exception.WsfeException;
import fev1.dif.afip.gov.ar.*;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.germanfica.wsfe.util.WsfeResponseUtils.hasErrors;
import static com.germanfica.wsfe.util.WsfeResponseUtils.hasEvents;

public class DefaultWsfeRequestHandler implements SoapRequestHandler {
    private final SoapRequestHandler delegate;
    private final Consumer<ArrayOfEvt> eventsListener;
    private final HttpStatus defaultErrorStatus;

    public DefaultWsfeRequestHandler(SoapResponseGetterOptions soapOptions) {
        this.delegate = Objects.requireNonNull(new DefaultSoapRequestHandler(soapOptions), "delegate is required");
        this.eventsListener = null;
        this.defaultErrorStatus = HttpStatus.BAD_REQUEST;
    }

    /**
     * Pasa directo al handler base (transporte/infra). No inspecciona el resultado.
     */
    @Override
    public <T> T handleRequest(ApiRequest apiRequest, RequestExecutor<T> executor) throws ApiException {
        return delegate.handleRequest(apiRequest, executor);
    }

    /**
     * Ejecuta la invocación, intercepta el resultado y:
     *  - Si hay ArrayOfErr -> lanza WsfeException.
     *  - Si hay ArrayOfEvt -> notifica a eventsListener (si existe).
     */
    @Override
    public <P, R> R invoke(ApiRequest apiRequest, Class<P> portClass, PortInvoker<P, R> invoker) throws ApiException {
        R result = delegate.invoke(apiRequest, portClass, invoker);
        handleWsfeResponse(result);
        return result;
    }

    /* --------------------------- internals --------------------------- */

    private void handleWsfeResponse(Object payload) throws ApiException {
        if (payload == null) return;

        ArrayOfErr errors = extractErrors(payload);
        if (hasErrors(errors)) {
            handleWsfeErrors(errors);
            return;
        }

        ArrayOfEvt events = extractEvents(payload);
        if (hasEvents(events)) handleWsfeEvents(events);
    }

    private void handleWsfeErrors(ArrayOfErr errors) throws WsfeException {
        throw new WsfeException(errors, defaultErrorStatus);
    }

    private void handleWsfeEvents(ArrayOfEvt events) {
        if (eventsListener != null) {
            try {
                //assert eventsListener != null; // && eventsListener != null
                eventsListener.accept(events);
            } catch (Exception ignored) { /* no romper el flujo */ }
        }
    }

    private static ArrayOfErr extractErrors(Object payload) {
        if (payload == null) return null;
        if (payload instanceof ArrayOfErr) return (ArrayOfErr) payload;

        Function<Object, ArrayOfErr> f = ERR_EXTRACTORS.get(payload.getClass());
        return f == null ? null : f.apply(payload);
    }

    private static ArrayOfEvt extractEvents(Object payload) {
        if (payload == null) return null;
        if (payload instanceof ArrayOfEvt) return (ArrayOfEvt) payload;

        Function<Object, ArrayOfEvt> f = EVT_EXTRACTORS.get(payload.getClass());
        return f == null ? null : f.apply(payload);
    }

    // registry policy: only exact registered classes are handled
    private static final Map<Class<?>, Function<Object, ArrayOfErr>> ERR_EXTRACTORS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Function<Object, ArrayOfEvt>> EVT_EXTRACTORS = new ConcurrentHashMap<>();

    static {
        // Registrar los 20 tipos soportados: errors extractor
        ERR_EXTRACTORS.put(CbteTipoResponse.class, o -> ((CbteTipoResponse) o).getErrors());
        ERR_EXTRACTORS.put(ConceptoTipoResponse.class, o -> ((ConceptoTipoResponse) o).getErrors());
        ERR_EXTRACTORS.put(CondicionIvaReceptorResponse.class, o -> ((CondicionIvaReceptorResponse) o).getErrors());
        ERR_EXTRACTORS.put(DocTipoResponse.class, o -> ((DocTipoResponse) o).getErrors());
        ERR_EXTRACTORS.put(FEActividadesResponse.class, o -> ((FEActividadesResponse) o).getErrors());
        ERR_EXTRACTORS.put(FECAEAGetResponse.class, o -> ((FECAEAGetResponse) o).getErrors());
        ERR_EXTRACTORS.put(FECAEAResponse.class, o -> ((FECAEAResponse) o).getErrors());
        ERR_EXTRACTORS.put(FECAEASinMovConsResponse.class, o -> ((FECAEASinMovConsResponse) o).getErrors());
        ERR_EXTRACTORS.put(FECAEASinMovResponse.class, o -> ((FECAEASinMovResponse) o).getErrors());
        ERR_EXTRACTORS.put(FECAEResponse.class, o -> ((FECAEResponse) o).getErrors());
        ERR_EXTRACTORS.put(FECompConsultaResponse.class, o -> ((FECompConsultaResponse) o).getErrors());
        ERR_EXTRACTORS.put(FECotizacionResponse.class, o -> ((FECotizacionResponse) o).getErrors());
        ERR_EXTRACTORS.put(FEPaisResponse.class, o -> ((FEPaisResponse) o).getErrors());
        ERR_EXTRACTORS.put(FEPtoVentaResponse.class, o -> ((FEPtoVentaResponse) o).getErrors());
        ERR_EXTRACTORS.put(FERecuperaLastCbteResponse.class, o -> ((FERecuperaLastCbteResponse) o).getErrors());
        ERR_EXTRACTORS.put(FERegXReqResponse.class, o -> ((FERegXReqResponse) o).getErrors());
        ERR_EXTRACTORS.put(FETributoResponse.class, o -> ((FETributoResponse) o).getErrors());
        ERR_EXTRACTORS.put(IvaTipoResponse.class, o -> ((IvaTipoResponse) o).getErrors());
        ERR_EXTRACTORS.put(MonedaResponse.class, o -> ((MonedaResponse) o).getErrors());
        ERR_EXTRACTORS.put(OpcionalTipoResponse.class, o -> ((OpcionalTipoResponse) o).getErrors());

        // Registrar los 20 tipos soportados: events extractor
        EVT_EXTRACTORS.put(CbteTipoResponse.class, o -> ((CbteTipoResponse) o).getEvents());
        EVT_EXTRACTORS.put(ConceptoTipoResponse.class, o -> ((ConceptoTipoResponse) o).getEvents());
        EVT_EXTRACTORS.put(CondicionIvaReceptorResponse.class, o -> ((CondicionIvaReceptorResponse) o).getEvents());
        EVT_EXTRACTORS.put(DocTipoResponse.class, o -> ((DocTipoResponse) o).getEvents());
        EVT_EXTRACTORS.put(FEActividadesResponse.class, o -> ((FEActividadesResponse) o).getEvents());
        EVT_EXTRACTORS.put(FECAEAGetResponse.class, o -> ((FECAEAGetResponse) o).getEvents());
        EVT_EXTRACTORS.put(FECAEAResponse.class, o -> ((FECAEAResponse) o).getEvents());
        EVT_EXTRACTORS.put(FECAEASinMovConsResponse.class, o -> ((FECAEASinMovConsResponse) o).getEvents());
        EVT_EXTRACTORS.put(FECAEASinMovResponse.class, o -> ((FECAEASinMovResponse) o).getEvents());
        EVT_EXTRACTORS.put(FECAEResponse.class, o -> ((FECAEResponse) o).getEvents());
        EVT_EXTRACTORS.put(FECompConsultaResponse.class, o -> ((FECompConsultaResponse) o).getEvents());
        EVT_EXTRACTORS.put(FECotizacionResponse.class, o -> ((FECotizacionResponse) o).getEvents());
        EVT_EXTRACTORS.put(FEPaisResponse.class, o -> ((FEPaisResponse) o).getEvents());
        EVT_EXTRACTORS.put(FEPtoVentaResponse.class, o -> ((FEPtoVentaResponse) o).getEvents());
        EVT_EXTRACTORS.put(FERecuperaLastCbteResponse.class, o -> ((FERecuperaLastCbteResponse) o).getEvents());
        EVT_EXTRACTORS.put(FERegXReqResponse.class, o -> ((FERegXReqResponse) o).getEvents());
        EVT_EXTRACTORS.put(FETributoResponse.class, o -> ((FETributoResponse) o).getEvents());
        EVT_EXTRACTORS.put(IvaTipoResponse.class, o -> ((IvaTipoResponse) o).getEvents());
        EVT_EXTRACTORS.put(MonedaResponse.class, o -> ((MonedaResponse) o).getEvents());
        EVT_EXTRACTORS.put(OpcionalTipoResponse.class, o -> ((OpcionalTipoResponse) o).getEvents());
    }
}
