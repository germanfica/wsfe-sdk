package com.germanfica.wsfe.examples;

import com.germanfica.wsfe.time.ArcaDateTime;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

public class ArcaDateTimeExample {
    public static void main(String[] args) {
        ArcaDateTime a = ArcaDateTime.now();

        // 1) ArcaDateTime -> toString() ya usa ISO_8601_FULL
        System.out.println("arcaDateTime.toString():        " + a);

        Clock fixedClock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
        ArcaDateTime b = ArcaDateTime.now(fixedClock);
        System.out.println("arcaDateTime B:                 " + b);
        System.out.println("OffsetDateTime B:               " + b.getValue());

        //OffsetDateTime odt = OffsetDateTime.now(ZoneOffset.ofHours(-2));
        //ArcaDateTime asdw = new ArcaDateTime(odt);

    }
}
