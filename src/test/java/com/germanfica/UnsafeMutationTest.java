package com.germanfica;

import com.germanfica.wsfe.time.ArcaDateTime;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.*;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assertions.*;

@Tag("unsafe")
public class UnsafeMutationTest {

    private static sun.misc.Unsafe getUnsafeOrNull() {
        try {
            Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return (sun.misc.Unsafe) f.get(null);
        } catch (Throwable t) {
            return null;
        }
    }

    @Test
    public void unsafe_can_mutate_instant_but_public_view_remains_argentina() throws Exception {
        sun.misc.Unsafe unsafe = getUnsafeOrNull();
        assumeTrue(unsafe != null, "Unsafe no disponible en esta JVM - test skipeado");

        ArcaDateTime normal = ArcaDateTime.of(Instant.parse("2025-06-02T18:30:00Z"));

        OffsetDateTime evil = OffsetDateTime.of(LocalDateTime.of(2015,1,1,12,0), ZoneOffset.ofHours(9));
        Instant evilInstant = evil.toInstant();

        Object o = unsafe.allocateInstance(ArcaDateTime.class);
        Field valueField = ArcaDateTime.class.getDeclaredField("value");
        valueField.setAccessible(true);
        valueField.set(o, evilInstant);

        ArcaDateTime hacked = (ArcaDateTime) o;

        // instant cambió
        assertEquals(evilInstant, hacked.toInstant(), "toInstant refleja el instant inyectado");

        // vista pública sigue -03:00
        assertEquals(ZoneOffset.ofHours(-3), hacked.toOffsetDateTime().getOffset(), "Public view sigue normalizandose a -03:00");

        // comparaciones por instant funcionan
        assertNotEquals(normal, hacked, "Objetos con distinto instant no son iguales");
    }
}
