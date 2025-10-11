package com.germanfica.wsfe.examples.homo;

import com.germanfica.wsfe.exception.WsfeException;
import fev1.dif.afip.gov.ar.ArrayOfErr;
import fev1.dif.afip.gov.ar.Err;
import com.germanfica.wsfe.net.HttpStatus;
import com.germanfica.wsfe.dto.ErrorDto;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Clase de prueba para ejecutar manualmente y ver el comportamiento de WsfeException.
 */
public class WsfeExceptionMain {
    public static void main(String[] args) {
        System.out.println("=== Prueba: 5 errores ===");
        try {
            ArrayOfErr arr5 = new ArrayOfErr();
            List<Err> list5 = ensureErrList(arr5);
            for (int i = 1; i <= 5; i++) {
                Err e = new Err();
                e.setCode(i); // Code es int
                e.setMsg("mensaje " + i);
                list5.add(e);
            }

            // lanzar para ver la excepción
            throw new WsfeException(arr5, HttpStatus.BAD_REQUEST);

        } catch (WsfeException ex) {
            printWsfeException(ex);
        }

        System.out.println("\n=== Prueba: 1 error ===");
        try {
            ArrayOfErr arr1 = new ArrayOfErr();
            List<Err> list1 = ensureErrList(arr1);
            Err e = new Err();
            e.setCode(100);
            e.setMsg("un solo mensaje");
            list1.add(e);

            throw new WsfeException(arr1, HttpStatus.BAD_REQUEST);

        } catch (WsfeException ex) {
            printWsfeException(ex);
        }

        System.out.println("\n=== Prueba: comportamiento cuando NO hay errores (ejemplo de IllegalArgumentException) ===");
        try {
            ArrayOfErr empty = new ArrayOfErr();
            // Si getErr() está vacío/null, el constructor de WsfeException lanzará IllegalArgumentException
            new WsfeException(empty, HttpStatus.BAD_REQUEST);
            System.out.println("No se lanzo la excepción (esto no deberia pasar)");
        } catch (IllegalArgumentException iae) {
            System.out.println("Se lanzó IllegalArgumentException como se esperaba: " + iae.getMessage());
        }
    }

    private static void printWsfeException(WsfeException ex) {
        System.out.println("WsfeException atrapada:");
        System.out.println(" HTTP status: " + ex.getHttpStatus());
        ErrorDto agg = ex.getErrorDto();
        if (agg != null) {
            System.out.println(" Aggregate faultCode: " + agg.getFaultCode());
            System.out.println(" Aggregate faultString: " + agg.getFaultString());
            System.out.println(" Aggregate details: " + (agg.getDetails() != null ? agg.getDetails().toString() : "null"));
        } else {
            System.out.println(" Aggregate: null");
        }

        System.out.println(" Errors count: " + ex.getErrors().size());
        for (int i = 0; i < ex.getErrors().size(); i++) {
            ErrorDto ed = ex.getErrors().get(i);
            System.out.println("  - error[" + i + "]: code=" + ed.getFaultCode() + " message=" + ed.getFaultString());
        }

        System.out.println(" errorsAsString: " + ex.errorsAsString());

        System.out.println("\n Stack trace:");
        ex.printStackTrace(System.out);
    }

    /**
     * Asegura que ArrayOfErr.getErr() devuelva una lista no-nula. Si getErr() es null,
     * intenta inicializarla vía setErr(List) o via acceso al campo 'err'.
     */
    @SuppressWarnings("unchecked")
    private static List<Err> ensureErrList(ArrayOfErr arr) {
        try {
            List<Err> list = arr.getErr();
            if (list != null) return list;
        } catch (Exception ignored) {
            // si getErr() lanza, seguimos con la inicializacion defensiva
        }

        try {
            // intentar setErr(List) si existe
            Method set = arr.getClass().getMethod("setErr", List.class);
            List<Err> newList = new java.util.ArrayList<>();
            set.invoke(arr, newList);
            return newList;
        } catch (NoSuchMethodException nsme) {
            // intentar acceder al campo 'err' por reflection
            try {
                Field f = arr.getClass().getDeclaredField("err");
                f.setAccessible(true);
                List<Err> newList = new java.util.ArrayList<>();
                f.set(arr, newList);
                return newList;
            } catch (Exception e) {
                throw new RuntimeException("No se puede inicializar la lista interna de ArrayOfErr", e);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error inicializando ArrayOfErr.err via setErr(...)", e);
        }
    }
}
