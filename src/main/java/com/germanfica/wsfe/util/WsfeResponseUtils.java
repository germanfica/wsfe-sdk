package com.germanfica.wsfe.util;

import fev1.dif.afip.gov.ar.ArrayOfErr;
import fev1.dif.afip.gov.ar.ArrayOfEvt;
import fev1.dif.afip.gov.ar.Err;
import fev1.dif.afip.gov.ar.Evt;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility methods for reading and transforming WSFE JAXB-generated response
 * containers (ArrayOfErr and ArrayOfEvt).
 */
public final class WsfeResponseUtils {

    private WsfeResponseUtils() {}

    /* ----------------------- predicates / checks ----------------------- */

    /**
     * Returns {@code true} when {@code arr} contains at least one {@code Err}.
     *
     * @param arr the JAXB {@link ArrayOfErr} instance, may be {@code null}
     * @return {@code true} if {@code arr} is not {@code null} and contains
     *         a non-empty list of {@code Err}; {@code false} otherwise
     */
    public static boolean hasErrors(ArrayOfErr arr) {
        return arr != null && arr.getErr() != null && !arr.getErr().isEmpty();
    }

    /**
     * Returns {@code true} when {@code arr} contains at least one {@code Evt}.
     *
     * @param arr the JAXB {@link ArrayOfEvt} instance, may be {@code null}
     * @return {@code true} if {@code arr} is not {@code null} and contains
     *         a non-empty list of {@code Evt}; {@code false} otherwise
     */
    public static boolean hasEvents(ArrayOfEvt arr) {
        return arr != null && arr.getEvt() != null && !arr.getEvt().isEmpty();
    }

    /**
     * Returns {@code true} if any {@link Err} element in {@code arr} satisfies
     * the given {@code predicate}.
     *
     * <p>If {@code arr} is {@code null} or contains no errors, this method
     * returns {@code false}.
     *
     * @param arr the JAXB {@link ArrayOfErr} to inspect, may be {@code null}
     * @param predicate predicate to apply to each {@link Err}, must not be {@code null}
     * @return {@code true} if at least one {@link Err} matches the predicate;
     *         {@code false} otherwise
     */
    public static boolean anyErrorMatch(ArrayOfErr arr, Predicate<Err> predicate) {
        if (!hasErrors(arr)) return false;
        return arr.getErr().stream().anyMatch(predicate);
    }

    /**
     * Returns {@code true} if any {@link Evt} element in {@code arr} satisfies
     * the given {@code predicate}.
     *
     * <p>If {@code arr} is {@code null} or contains no events, this method
     * returns {@code false}.
     *
     * @param arr the JAXB {@link ArrayOfEvt} to inspect, may be {@code null}
     * @param predicate predicate to apply to each {@link Evt}, must not be {@code null}
     * @return {@code true} if at least one {@link Evt} matches the predicate;
     *         {@code false} otherwise
     */
    public static boolean anyEventMatch(ArrayOfEvt arr, Predicate<Evt> predicate) {
        if (!hasEvents(arr)) return false;
        return arr.getEvt().stream().anyMatch(predicate);
    }

    /* ----------------------- getters / lists ----------------------- */

    /**
     * Returns an unmodifiable view of the error list contained in {@code arr}.
     *
     * <p>If {@code arr} is {@code null} or has no errors, an empty list is
     * returned (never {@code null}).
     *
     * @param arr the JAXB {@link ArrayOfErr}, may be {@code null}
     * @return an unmodifiable {@link List} of {@link Err} instances; never {@code null}
     */
    public static List<Err> errorsList(ArrayOfErr arr) {
        if (!hasErrors(arr)) return Collections.emptyList();
        return Collections.unmodifiableList(arr.getErr());
    }

    /**
     * Returns an unmodifiable view of the event list contained in {@code arr}.
     *
     * <p>If {@code arr} is {@code null} or has no events, an empty list is
     * returned (never {@code null}).
     *
     * @param arr the JAXB {@link ArrayOfEvt}, may be {@code null}
     * @return an unmodifiable {@link List} of {@link Evt} instances; never {@code null}
     */
    public static List<Evt> eventsList(ArrayOfEvt arr) {
        if (!hasEvents(arr)) return Collections.emptyList();
        return Collections.unmodifiableList(arr.getEvt());
    }

    /* ----------------------- formatting / string helpers ----------------------- */

    /**
     * Joins error entries using the default delimiter " | ".
     *
     * <p>If {@code arr} has no errors, this method returns the literal string
     * '(sin errores)'.
     *
     * @param arr the JAXB {@link ArrayOfErr}, may be {@code null}
     * @return a single string representing all errors joined by " | ", or
     *         '(sin errores)' when there are none
     */
    public static String joinErrors(ArrayOfErr arr) {
        return joinErrors(arr, " | ");
    }

    /**
     * Joins error entries using the provided {@code delimiter}.
     *
     * <p>If {@code arr} has no errors, this method returns the literal string
     * '(sin errores)'.
     *
     * @param arr the JAXB {@link ArrayOfErr}, may be {@code null}
     * @param delimiter string used to separate each error entry; must not be {@code null}
     * @return a single string representing all errors joined by {@code delimiter},
     *         or '(sin errores)' when there are none
     */
    public static String joinErrors(ArrayOfErr arr, String delimiter) {
        if (!hasErrors(arr)) return "(sin errores)";
        return arr.getErr().stream()
            .map(e -> safeCode(e) + ": " + safeMsg(e))
            .collect(Collectors.joining(delimiter));
    }

    /**
     * Joins event entries using the default delimiter " | ".
     *
     * <p>If {@code arr} has no events, this method returns the literal string
     * '(sin eventos)'.
     *
     * @param arr the JAXB {@link ArrayOfEvt}, may be {@code null}
     * @return a single string representing all events joined by " | ", or
     *         '(sin eventos)' when there are none
     */
    public static String joinEvents(ArrayOfEvt arr) {
        return joinEvents(arr, " | ");
    }

    /**
     * Joins event entries using the provided {@code delimiter}.
     *
     * <p>If {@code arr} has no events, this method returns the literal string
     * '(sin eventos)'.
     *
     * @param arr the JAXB {@link ArrayOfEvt}, may be {@code null}
     * @param delimiter string used to separate each event entry; must not be {@code null}
     * @return a single string representing all events joined by {@code delimiter},
     *         or '(sin eventos)' when there are none
     */
    public static String joinEvents(ArrayOfEvt arr, String delimiter) {
        if (!hasEvents(arr)) return "(sin eventos)";
        return arr.getEvt().stream()
            .map(ev -> safeCode(ev) + ": " + safeMsg(ev))
            .collect(Collectors.joining(delimiter));
    }

    /* ----------------------- internals ----------------------- */

    /**
     * Safely extracts the message from an {@link Err} or {@link Evt} instance.
     * If the provided object is neither {@code Err} nor {@code Evt}, or if the
     * message is {@code null}, an empty string is returned.
     *
     * @param obj an {@link Err} or {@link Evt} instance, may be {@code null}
     * @return the message text, or an empty string if not available
     */
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

    /**
     * Safely extracts the numeric code from an {@link Err} or {@link Evt}
     * instance and converts it to {@link String}.
     *
     * <p>If the object is neither {@code Err} nor {@code Evt}, or if the code
     * is {@code null}, this method returns the string "null".
     *
     * @param obj an {@link Err} or {@link Evt} instance, may be {@code null}
     * @return the numeric code as a string, or "null" when not available
     */
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
}
