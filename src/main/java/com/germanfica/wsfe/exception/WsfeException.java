package com.germanfica.wsfe.exception;

import com.germanfica.wsfe.dto.ErrorDto;
import com.germanfica.wsfe.net.HttpStatus;
import fev1.dif.afip.gov.ar.ArrayOfErr;
import fev1.dif.afip.gov.ar.Err;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.germanfica.wsfe.util.WsfeResponseUtils.*;

/**
 * Excepción agregadora para errores devueltos por WSFE (ArrayOfErr).
 *
 * <p>Esta excepción sirve para representar uno o más errores retornados por WSFE
 * de forma agregada. Internamente contiene:</p>
 *
 * <ul>
 *   <li>un {@link ErrorDto} "aggregate" que resume los códigos y mensajes (accesible vía {@link #getErrorDto()})</li>
 *   <li>una {@link List} inmutable de {@link ErrorDto} con cada error individual (accesible vía {@link #getErrors()})</li>
 * </ul>
 *
 * <p>BUENA PRÁCTICA: el consumidor debe usar {@link #getErrors()} para el manejo
 * programático. {@link #getErrorDto()} ofrece sólo
 * un resumen útil para logs o respuestas rápidas.</p>
 *
 * <p>Para diagnóstico la excepción también preserva trazas individuales:
 * el primer error se asigna como 'cause' y los errores restantes se añaden como
 * 'suppressed' (ver {@link Throwable#getSuppressed()}).</p>
 *
 * <p>No se expone el objeto JAXB {@code ArrayOfErr} en la API pública: la clase
 * devuelve {@link ErrorDto} desacoplados del binding.</p>
 */
@Getter
public class WsfeException extends ApiException {
  private final List<ErrorDto> errors;    // lista con cada error individual
  private final ErrorDto aggregate;       // resumen agregado (no null)

  /**
   * Constructor principal: construye una WsfeException a partir de {@code ArrayOfErr}.
   *
   * <p>Comportamiento:
   * <ul>
   *   <li>Si {@code arr} no contiene errores, lanza {@link IllegalArgumentException}.</li>
   *   <li>Construye {@link #errors} (lista de {@link ErrorDto}) y {@code aggregate} (summary).</li>
   *   <li>Establece el primer error como cause y añade los demás como suppressed para preservar trazas.</li>
   * </ul>
   *
   * @param arr ArrayOfErr retornado por WSFE (debe contener al menos 1 {@code Err})
   * @param status HttpStatus asociado a la respuesta (si es {@code null} usa {@code HttpStatus.BAD_REQUEST})
   * @throws IllegalArgumentException si {@code arr} no contiene errores
   */
  public WsfeException(ArrayOfErr arr, HttpStatus status) {
    super(requireAggregate(arr, "wsfe_errors"), resolveStatus(status));
    this.aggregate = getErrorDto(); // ApiException almacena el ErrorDto; lo reutilizamos
    this.errors = List.copyOf(buildErrorList(arr));

    // --- Añadimos cause + suppressed para preservar cada error individual en la traza ---
    if (!this.errors.isEmpty()) {
      Throwable primary = createThrowableFromError(this.errors.get(0));
      try {
        initCause(primary);
      } catch (IllegalStateException ignored) { /* si ya tiene cause */ }

      for (int i = 1; i < this.errors.size(); i++) {
        Throwable t = createThrowableFromError(this.errors.get(i));
        addSuppressed(t);
      }
    }
  }

  // -----------------------
  // Helpers privados
  // -----------------------

  private static HttpStatus resolveStatus(HttpStatus status) {
    return status != null ? status : HttpStatus.BAD_REQUEST;
  }

  /**
   * Requiere que arr tenga errores y devuelve el ErrorDto agregado (summary).
   * Lanza IllegalArgumentException si no tiene errores.
   *
   * IMPORTANTE: static porque se usa en la llamada a super(...) al inicio del constructor.
   */
  private static ErrorDto requireAggregate(ArrayOfErr arr, String source) {
    if (!hasErrors(arr)) {
      throw new IllegalArgumentException("ArrayOfErr no contiene errores; no se puede construir WsfeException.");
    }
    ErrorDto agg = toAggregateErrorDto(arr, source);
    // por seguridad, si el helper devolviera null (no debería), crear un summary synthetic
    if (agg == null) {
      String syntheticMsg = "WSFE returned errors but aggregate could not be built";
      agg = new ErrorDto(null, syntheticMsg, new ErrorDto.ErrorDetailsDto(source, getSafeHostname()));
    }
    return agg;
  }

  /**
   * Construye la lista de ErrorDto a partir de ArrayOfErr.
   */
  private List<ErrorDto> buildErrorList(ArrayOfErr arr) {
    if (arr == null || arr.getErr() == null || arr.getErr().isEmpty()) {
      return Collections.emptyList();
    }

    return arr.getErr().stream()
        .filter(Objects::nonNull)
        .map(this::toErrorDtoFromErr)
        .collect(Collectors.toUnmodifiableList());
  }

  /**
   * Convierte un Err JAXB en ErrorDto. Ajustá los getters si tu binding usa otros nombres.
   */
  private ErrorDto toErrorDtoFromErr(Err e) {
    String code = null;
    try {
      Object maybeCode = e.getCode(); // getCode() devuelve int en tu binding
      code = maybeCode != null ? String.valueOf(maybeCode) : null;
    } catch (Exception ignored) { /* defensivo */ }

    String msg = null;
    try {
      msg = e.getMsg();
    } catch (Exception ignored) { /* defensivo */ }

    return new ErrorDto(code, msg, new ErrorDto.ErrorDetailsDto("WsfeError", getSafeHostname()));
  }

  /**
   * Combina los elementos de ArrayOfErr en un único ErrorDto resumen (aggregate).
   * Devuelve null solo si no hay errores o no se puede construir.
   */
  private static ErrorDto toAggregateErrorDto(ArrayOfErr arr, String source) {
    if (arr == null || arr.getErr() == null || arr.getErr().isEmpty()) return null;

    String codes = arr.getErr().stream()
        .map(err -> {
          try {
            Object maybeCode = err.getCode();
            return maybeCode != null ? String.valueOf(maybeCode) : null;
          } catch (Exception ex) {
            return null;
          }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.joining(", "));

    String msgs = arr.getErr().stream()
        .map(err -> {
          try {
            return err.getMsg();
          } catch (Exception ex) {
            return null;
          }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.joining(" | "));

    if ((codes == null || codes.isEmpty()) && (msgs == null || msgs.isEmpty())) return null;

    return new ErrorDto(
        codes.isEmpty() ? null : codes,
        msgs.isEmpty() ? null : msgs,
        new ErrorDto.ErrorDetailsDto(source, getSafeHostname())
    );
  }

  /**
   * Crea un Throwable sintético a partir de un ErrorDto para poder adjuntarlo como cause/suppressed.
   */
  private Throwable createThrowableFromError(ErrorDto e) {
    if (e == null) return new RuntimeException("WSFE error (no details)");
    StringBuilder sb = new StringBuilder("WSFE error");
    if (e.getFaultCode() != null) sb.append(" code=").append(e.getFaultCode());
    if (e.getFaultString() != null) sb.append(" message=").append(e.getFaultString());
    return new RuntimeException(sb.toString());
  }

  // -----------------------
  // Utilidades públicas
  // -----------------------

  @Override
  public String toString() {
    String summary = getErrorDto() != null ? getErrorDto().getFaultString() : "no-summary";
    return "WsfeException{status=" + getHttpStatus()
        + ", errorsCount=" + errors.size()
        + ", summary='" + summary + "'}";
  }

  /**
   * Mensajes concatenados (útil para logs rápidos).
   */
  public String errorsAsString() {
    return errors.stream()
        .map(ErrorDto::getFaultString)
        .filter(s -> s != null && !s.isEmpty())
        .collect(Collectors.joining("; "));
  }
}
