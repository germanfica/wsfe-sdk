package com.germanfica.wsfe.net;

import lombok.AccessLevel;
import lombok.Getter;

/** The base class for all services. */
public abstract class ApiService {
    @Getter(AccessLevel.PROTECTED)
    private final SoapRequestHandler soapRequestHandler;

    protected ApiService(SoapRequestHandler soapRequestHandler) {
        this.soapRequestHandler = soapRequestHandler;
    }

//    @Getter(AccessLevel.PROTECTED)
//    private final ApiResponseGetter responseGetter;
//
//    protected SoapService(ApiResponseGetter responseGetter) {
//        this.responseGetter = responseGetter;
//    }

//    @SuppressWarnings("TypeParameterUnusedInFormals")
//    protected <T extends ApiObjectInterface> T request(ApiRequest request, Type typeToken)
//            throws ApiException {
//        return this.getResponseGetter().request(request, typeToken);
//    }

    /**
     * Ejecuta una solicitud SOAP encapsulada con manejo de errores.
     *
     * <p>
     * <b>Ejemplo de uso en un servicio SOAP:</b>
     * <pre>{@code
     * public String autenticar(String cmsFirmado) throws Exception {
     *     return this.request(null, () -> port.loginCms(cmsFirmado));
     * }
     * }</pre>
     *
     * <pre>{@code
     * public class AuthService extends ApiService {
     *     private final LoginCMS port;
     *
     *     public AuthService(SoapRequestHandler soapRequestHandler) throws MalformedURLException {
     *         super(soapRequestHandler);
     *         LoginCMSService service = new LoginCMSService(new URL(WSDL_URL), SERVICE_NAME);
     *         this.port = service.getLoginCms();
     *     }
     *
     *     public String autenticar(String cmsFirmado) throws Exception {
     *         return this.request(null, () -> port.loginCms(cmsFirmado));
     *     }
     * }
     * }</pre>
     *
     *
     * @param request  Datos opcionales de la solicitud. Puede ser {@code null} si no se requieren parámetros adicionales.
     * @param executor Expresión lambda o método de referencia que encapsula la operación SOAP.
     * @param <T>      Tipo de retorno de la operación SOAP.
     * @return El resultado de la llamada SOAP.
     * @throws Exception Si ocurre un error en la ejecución de la solicitud SOAP.
     */
    protected <T> T request(ApiRequest request, RequestExecutor<T> executor)
            throws Exception {
        //return request.request(request, responseType);
        return soapRequestHandler.handleRequest(request, executor);
    }
}
