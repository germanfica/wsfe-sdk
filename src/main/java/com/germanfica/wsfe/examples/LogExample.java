package com.germanfica.wsfe.examples;

import com.germanfica.wsfe.util.Logger;

public class LogExample {
    private static final Logger log = Logger.loggerFor(LogExample.class);

    public static void main(String[] args) {
        log.info(() -> "Inicio del programa 🚀");

        log.debug(() -> "Esto es un mensaje en DEBUG, útil para desarrollo 👨‍💻");

        try {
            int result = 10 / 0; // Forzar excepción
        } catch (ArithmeticException e) {
            log.error(() -> "Ocurrió un error al dividir", e);
        }

        log.warn(() -> "Este es un mensaje de advertencia ⚠️");

        log.trace(() -> "Este trace solo se verá si activás el nivel TRACE");

        log.info(() -> "Fin del programa ✅");
    }
}
