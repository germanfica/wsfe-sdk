package com.germanfica;

import com.germanfica.wsfe.dto.ErrorDto;
import com.germanfica.wsfe.exception.WsfeException;
import com.germanfica.wsfe.net.HttpStatus;
import fev1.dif.afip.gov.ar.ArrayOfErr;
import fev1.dif.afip.gov.ar.Err;
import org.junit.jupiter.api.Test;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;


public class WsfeExceptionTest {

    @Test
    void whenMultipleErrors_thenWsfeExceptionContainsAllAndAggregateIsCorrect() {
        // preparar ArrayOfErr con 5 Err reales (Code es int)
        ArrayOfErr arr = new ArrayOfErr();

        for (int i = 1; i <= 5; i++) {
            Err e = new Err();
            e.setCode(i);                     // int
            e.setMsg("mensaje " + i);         // String
            arr.getErr().add(e);              // lista JAXB mutable
        }

        // lanzar la excepción usando el constructor público
        WsfeException thrown = assertThrows(WsfeException.class, () -> {
            throw new WsfeException(arr, HttpStatus.BAD_REQUEST);
        });

        // validaciones
        assertNotNull(thrown.getErrors(), "La lista de errores no debe ser null");
        assertEquals(5, thrown.getErrors().size(), "Cantidad de errores debe ser 5");

        // aggregate esperado: codes separados por ", " y mensajes por " | "
        String expectedCodes = IntStream.rangeClosed(1, 5)
            .mapToObj(String::valueOf)
            .collect(Collectors.joining(", "));
        String expectedMsgs = IntStream.rangeClosed(1, 5)
            .mapToObj(i -> "mensaje " + i)
            .collect(Collectors.joining(" | "));

        ErrorDto agg = thrown.getErrorDto();
        assertNotNull(agg, "Aggregate ErrorDto no debe ser null");
        assertEquals(expectedCodes, agg.getFaultCode(), "Aggregate faultCode mal construido");
        assertEquals(expectedMsgs, agg.getFaultString(), "Aggregate faultString mal construido");

        // errorsAsString debe concatenar mensajes con '; '
        String expectedConcatForLog = IntStream.rangeClosed(1, 5)
            .mapToObj(i -> "mensaje " + i)
            .collect(Collectors.joining("; "));
        assertEquals(expectedConcatForLog, thrown.errorsAsString(), "errorsAsString() incorrecto");
    }

    @Test
    void whenSingleError_thenWsfeExceptionHasSingleAndAggregateMatches() {
        // 1 Err real
        ArrayOfErr arr = new ArrayOfErr();
        Err e = new Err();
        e.setCode(100);               // ejemplo con int
        e.setMsg("un solo mensaje");
        arr.getErr().add(e);

        WsfeException thrown = assertThrows(WsfeException.class, () -> {
            throw new WsfeException(arr, HttpStatus.BAD_REQUEST);
        });

        assertNotNull(thrown.getErrors());
        assertEquals(1, thrown.getErrors().size());

        ErrorDto agg = thrown.getErrorDto();
        assertNotNull(agg);
        assertEquals("100", agg.getFaultCode());           // el aggregate guarda String.valueOf(code)
        assertEquals("un solo mensaje", agg.getFaultString());

        assertEquals("un solo mensaje", thrown.errorsAsString());
    }
}
