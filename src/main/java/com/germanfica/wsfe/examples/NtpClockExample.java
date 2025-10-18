package com.germanfica.wsfe.examples;

import com.germanfica.wsfe.time.NtpClock;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

public class NtpClockExample {
    public static void main(String[] args) {
        // Siempre consulta al servidor NTP (sin cache)
        NtpClock ntp = new NtpClock("time.afip.gov.ar", Clock.system(ZoneOffset.of("-03")));

        Instant instant = ntp.instant();
        System.out.println("Instant corregido: " + instant);

        // Mostrar como ZonedDateTime con offset -03
        System.out.println("Zoned (UTC-3): " + instant.atZone(ZoneOffset.of("-03")));
    }
}
