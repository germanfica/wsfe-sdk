package com.germanfica.wsfe.examples;

import com.germanfica.wsfe.time.ArcaNtpClock;
import com.germanfica.wsfe.time.ArcaDateTime;

import java.time.Clock;
import java.time.ZoneOffset;

/**
 * Ejemplo de uso de ArcaNtpClock y ArcaDateTime.
 */
public class ArcaNtpClockExample {

    public static void main(String[] args) {
        // 1) ArcaNtpClock por defecto (server = time.afip.gov.ar, fallback = UTC-3)
        ArcaNtpClock defaultClock = new ArcaNtpClock();
        printNow(defaultClock, "ARCA (NTP por defecto)");

        // 2) ArcaNtpClock con fallback explícito (por ejemplo, usar system UTC si queres)
        ArcaNtpClock fallbackUtc = new ArcaNtpClock(Clock.systemUTC());
        printNow(fallbackUtc, "ARCA (NTP + fallback UTC)");

        // 3) ArcaNtpClock con servidor personalizado y fallback en zona -03:00
        ArcaNtpClock custom = new ArcaNtpClock("pool.ntp.org", Clock.system(ZoneOffset.ofHours(-3)));
        printNow(custom, "ARCA (server pool.ntp.org, fallback -03:00)");
    }

    private static void printNow(Clock clock, String label) {
        try {
            ArcaDateTime now = ArcaDateTime.now(clock);
            System.out.println(label + ": " + now); // toString() ya devuelve ISO_8601_FULL
            // representaciones adicionales
            System.out.println("  instant: " + now.toInstant());
            System.out.println("  offsetDateTime: " + now.toOffsetDateTime());
        } catch (Exception e) {
            // Si algo falla (NTP no responde), mostrar error y usar hora local de la JVM
            System.err.println(label + " - error obteniendo hora desde el clock: " + e.getMessage());
            System.out.println("  fallback (hora local JVM): " + ArcaDateTime.now());
        }
    }
}
