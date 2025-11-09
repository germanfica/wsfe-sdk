// src/test/java/com/germanfica/UnsafeArcaDateTimeTest.java
package com.germanfica;

import com.germanfica.wsfe.time.ArcaDateTime;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.time.*;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assertions.*;

@Tag("unsafe")
public class UnsafeArcaDateTimeTest {

    private static Unsafe getUnsafeOrNull() {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return (Unsafe) f.get(null);
        } catch (Throwable t) {
            return null;
        }
    }

    @Test
    public void unsafe_allocateInstance_and_field_set_should_keep_public_view_normalized() throws Exception {
        Unsafe unsafe = getUnsafeOrNull();
        assumeTrue(unsafe != null, "Unsafe no disponible en esta JVM - test skipeado");

        OffsetDateTime evil = OffsetDateTime.of(LocalDateTime.of(2025, 6, 2, 15, 30), ZoneOffset.ofHours(2));
        Instant evilInstant = evil.toInstant();

        Object hackedObj = unsafe.allocateInstance(ArcaDateTime.class);

        Field valueField = ArcaDateTime.class.getDeclaredField("value");
        valueField.setAccessible(true);
        valueField.set(hackedObj, evilInstant); // inyectar Instant

        ArcaDateTime hacked = (ArcaDateTime) hackedObj;

        // vista pública normalizada
        assertEquals(ZoneOffset.ofHours(-3), hacked.toOffsetDateTime().getOffset(),
            "vista pública debe normalizar a -03:00");

        // el instant reflejará el instant inyectado
        assertEquals(evilInstant, hacked.toInstant());

        // equals basada en instant
        ArcaDateTime expected = ArcaDateTime.of(evilInstant);
        assertEquals(expected, hacked);
    }
}
