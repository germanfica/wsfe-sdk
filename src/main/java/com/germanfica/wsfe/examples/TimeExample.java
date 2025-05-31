package com.germanfica.wsfe.examples;

import com.germanfica.wsfe.time.ArcaDateTime;

import java.time.OffsetDateTime;

public class TimeExample {
    public static void main(String[] args) {
        // Paso 1: Strings originales en formato ISO_8601_FULL
        String generationTimeStr = "2025-05-31T14:41:09.476-03:00";
        String expirationTimeStr = "2025-06-01T02:41:09.476-03:00";

        // Paso 2: Crear ArcaDateTime a partir de los strings
        ArcaDateTime generationTime = ArcaDateTime.parse(generationTimeStr);
        ArcaDateTime expirationTime = ArcaDateTime.parse(expirationTimeStr);

        // Paso 3: Crear OffsetDateTime directamente desde los mismos strings
        OffsetDateTime generationOffset = OffsetDateTime.parse(generationTimeStr);
        OffsetDateTime expirationOffset = OffsetDateTime.parse(expirationTimeStr);

        // Paso 4: Imprimir comparaciones
        System.out.println("Original generationTime string: " + generationTimeStr);
        System.out.println("ArcaDateTime generationTime:    " + generationTime.toString());
        System.out.println("OffsetDateTime generationTime:  " + generationOffset.toString());
        System.out.println("¿Arca == Offset?                " + generationTime.getValue().equals(generationOffset));
        System.out.println("¿String == Arca.toString()?     " + generationTimeStr.equals(generationTime.toString()));
        System.out.println();

        System.out.println("Original expirationTime string: " + expirationTimeStr);
        System.out.println("ArcaDateTime expirationTime:    " + expirationTime.toString());
        System.out.println("OffsetDateTime expirationTime:  " + expirationOffset.toString());
        System.out.println("¿Arca == Offset?                " + expirationTime.getValue().equals(expirationOffset));
        System.out.println("¿String == Arca.toString()?     " + expirationTimeStr.equals(expirationTime.toString()));
    }
}
