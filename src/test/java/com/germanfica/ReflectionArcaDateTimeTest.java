// src/test/java/com/germanfica/ReflectionArcaDateTimeTest.java
package com.germanfica;

import com.germanfica.wsfe.time.ArcaDateTime;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

public class ReflectionArcaDateTimeTest {

    @Test
    public void privateConstructor_viaReflection_shouldNormalizeOffsetAndPreserveInstant() throws Exception {
        OffsetDateTime evil = OffsetDateTime.of(LocalDateTime.of(2025, 6, 2, 15, 30), ZoneOffset.ofHours(2));
        Instant evilInstant = evil.toInstant();

        Constructor<ArcaDateTime> ctor = ArcaDateTime.class.getDeclaredConstructor(OffsetDateTime.class);
        ctor.setAccessible(true);
        ArcaDateTime viaCtor = ctor.newInstance(evil);

        // la vista pública siempre normaliza a -03:00
        assertEquals(ZoneOffset.ofHours(-3), viaCtor.toOffsetDateTime().getOffset());

        // instant preservado
        assertEquals(evilInstant, viaCtor.toInstant());

        // equals por instant
        ArcaDateTime expected = ArcaDateTime.of(evilInstant);
        assertEquals(expected, viaCtor);
    }
}
