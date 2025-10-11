package com.germanfica.wsfe.util;

import com.germanfica.wsfe.dto.ErrorDto;
import com.germanfica.wsfe.exception.ApiException;
import com.germanfica.wsfe.net.HttpStatus;
import fev1.dif.afip.gov.ar.ArrayOfErr;
import fev1.dif.afip.gov.ar.ArrayOfEvt;
import fev1.dif.afip.gov.ar.Err;
import fev1.dif.afip.gov.ar.Evt;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utilidades para leer y transformar ArrayOfErr / ArrayOfEvt generados por JAXB (WSFE).
 */
public final class WsfeResponseUtils {

    private WsfeResponseUtils() {}

    /* ----------------------- predicates / checks ----------------------- */

    public static boolean hasErrors(ArrayOfErr arr) {
        return arr != null && arr.getErr() != null && !arr.getErr().isEmpty();
    }

    public static boolean hasEvents(ArrayOfEvt arr) {
        return arr != null && arr.getEvt() != null && !arr.getEvt().isEmpty();
    }

    /**
     * True si existe al menos un error que cumpla la condicion.
     */
    public static boolean anyErrorMatch(ArrayOfErr arr, Predicate<Err> predicate) {
        if (!hasErrors(arr)) return false;
        return arr.getErr().stream().anyMatch(predicate);
    }

    /**
     * True si existe al menos un evento que cumpla la condicion.
     */
    public static boolean anyEventMatch(ArrayOfEvt arr, Predicate<Evt> predicate) {
        if (!hasEvents(arr)) return false;
        return arr.getEvt().stream().anyMatch(predicate);
    }

    /* ----------------------- getters / lists ----------------------- */

    public static List<Err> errorsList(ArrayOfErr arr) {
        if (!hasErrors(arr)) return Collections.emptyList();
        return Collections.unmodifiableList(arr.getErr());
    }

    public static List<Evt> eventsList(ArrayOfEvt arr) {
        if (!hasEvents(arr)) return Collections.emptyList();
        return Collections.unmodifiableList(arr.getEvt());
    }

    /* ----------------------- formatting / string helpers ----------------------- */

    /**
     * Devuelve "(sin errores)" si no hay errores.
     */
    public static String joinErrors(ArrayOfErr arr) {
        return joinErrors(arr, " | ");
    }

    public static String joinErrors(ArrayOfErr arr, String delimiter) {
        if (!hasErrors(arr)) return "(sin errores)";
        return arr.getErr().stream()
            .map(e -> safeCode(e) + ": " + safeMsg(e))
            .collect(Collectors.joining(delimiter));
    }

    public static String joinEvents(ArrayOfEvt arr) {
        return joinEvents(arr, " | ");
    }

    public static String joinEvents(ArrayOfEvt arr, String delimiter) {
        if (!hasEvents(arr)) return "(sin eventos)";
        return arr.getEvt().stream()
            .map(ev -> safeCode(ev) + ": " + safeMsg(ev))
            .collect(Collectors.joining(delimiter));
    }

    private static String safeMsg(Object obj) {
        if (obj instanceof Err) {
            String m = ((Err) obj).getMsg();
            return m == null ? "" : m;
        }
        if (obj instanceof Evt) {
            String m = ((Evt) obj).getMsg();
            return m == null ? "" : m;
        }
        return "";
    }

    private static String safeCode(Object obj) {
        if (obj instanceof Err) {
            Integer c = ((Err) obj).getCode();
            return c == null ? "null" : String.valueOf(c);
        }
        if (obj instanceof Evt) {
            Integer c = ((Evt) obj).getCode();
            return c == null ? "null" : String.valueOf(c);
        }
        return "null";
    }

    /* ----------------------- conversion to ErrorDto ----------------------- */

    public static List<ErrorDto> toErrorDtoList(ArrayOfErr arr) {
        if (!hasErrors(arr)) return Collections.emptyList();
        return arr.getErr().stream()
            .map(e -> new ErrorDto(
                String.valueOf(safeCode(e)),
                safeMsg(e),
                null
            ))
            .collect(Collectors.toList());
    }

    public static Optional<ErrorDto> toAggregateErrorDto(ArrayOfErr arr, String key) {
        if (!hasErrors(arr)) return Optional.empty();
        String joined = joinErrors(arr);
        return Optional.of(new ErrorDto(key != null ? key : "wsfe_errors", joined, null));
    }

    public static ApiException toApiException(ArrayOfErr arr, HttpStatus status) {
        return toApiException(arr, status, "wsfe_errors");
    }

    public static ApiException toApiException(ArrayOfErr arr, HttpStatus status, String key) {
        if (!hasErrors(arr)) return null;

        ErrorDto errorDto = toAggregateErrorDto(arr, key)
            .map(dto -> new ErrorDto(
                dto.getFaultCode(),
                dto.getFaultString(),
                new ErrorDto.ErrorDetailsDto(
                    "WsfeFaultException",
                    getSafeHostname()
                )
            ))
            .orElseGet(() -> new ErrorDto(
                key != null ? key : "wsfe_errors",
                "(unknown error)",
                new ErrorDto.ErrorDetailsDto("WsfeFaultException", getSafeHostname())
            ));

        return new ApiException(
            errorDto,
            status != null ? status : HttpStatus.BAD_REQUEST
        );
    }

    public static String getSafeHostname() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown-host";
        }
    }

    /* ----------------------- helpers para eventos ----------------------- */

    public static List<ErrorDto> eventsAsErrorDtoList(ArrayOfEvt arr) {
        if (!hasEvents(arr)) return Collections.emptyList();
        return arr.getEvt().stream()
            .map(ev -> new ErrorDto(String.valueOf(safeCode(ev)), safeMsg(ev), null))
            .collect(Collectors.toList());
    }

    public static Optional<ErrorDto> toAggregateEventDto(ArrayOfEvt arr, String key) {
        if (!hasEvents(arr)) return Optional.empty();
        String joined = joinEvents(arr);
        return Optional.of(new ErrorDto(key != null ? key : "wsfe_events", joined, null));
    }
}
