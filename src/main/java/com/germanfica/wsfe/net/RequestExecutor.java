package com.germanfica.wsfe.net;

/**
 * Interfaz funcional para encapsular la ejecución de una operación que devuelve un resultado y puede lanzar excepciones.
 * <p>
 * Diseñada específicamente para manejar llamadas a servicios SOAP en JAX-WS, permitiendo un control centralizado
 * de excepciones como {@code SOAPFaultException} y {@code WebServiceException}.
 * </p>
 * <h3>¿Por qué `RequestExecutor<T>` en lugar de `Callable<T>` o `Supplier<T>`?</h3>
 * <ul>
 *     <li><b>✔ Manejo de excepciones:</b> A diferencia de `Supplier<T>`, esta interfaz permite lanzar excepciones.</li>
 *     <li><b>✔ Llamadas síncronas:</b> A diferencia de `Callable<T>`, esta interfaz no está diseñada para ejecución asíncrona con `ExecutorService`.</li>
 *     <li><b>✔ Enfoque en servicios SOAP:</b> Pensada para encapsular invocaciones a servicios web que pueden fallar.</li>
 * </ul>
 * <p>
 * <b>Desde qué versión de Java funciona:</b>
 * - Las <b>expresiones lambda</b> y <b>interfaces funcionales</b> fueron introducidas en **Java 8**
 * - Este código es totalmente compatible con **Java 8 y versiones superiores** (incluyendo Java 17 y 21).
 * </p>
 *
 * @param <T> Tipo de retorno de la operación encapsulada.
 * @since Java 8+
 * @author German Fica
 */
@FunctionalInterface
public interface RequestExecutor<T> {

    /**
     * Ejecuta una operación que devuelve un resultado y puede lanzar excepciones.
     *
     * @return el resultado de la operación.
     * @throws Exception si ocurre un error durante la ejecución.
     */
    T execute() throws Exception;
}
