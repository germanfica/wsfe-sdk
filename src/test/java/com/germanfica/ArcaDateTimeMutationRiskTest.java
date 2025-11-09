// src/test/java/com/germanfica/ArcaDateTimeMutationRiskTest.java
package com.germanfica;

import com.germanfica.wsfe.time.ArcaDateTime;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

public class ArcaDateTimeMutationRiskTest {

    private static Unsafe getUnsafe() throws Exception {
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        return (Unsafe) f.get(null);
    }

    @Test
    public void unsafe_can_change_instant_and_public_semantics_change() throws Exception {
        // 1) baseline
        OffsetDateTime original = OffsetDateTime.of(LocalDateTime.of(2025, 6, 2, 15, 30), ZoneOffset.ofHours(-3));
        ArcaDateTime normal = ArcaDateTime.of(original.toInstant());

        // 2) "evil" con distinto instante
        OffsetDateTime evil = OffsetDateTime.of(LocalDateTime.of(2025, 6, 2, 17, 30), ZoneOffset.ofHours(2));
        Instant evilInstant = evil.toInstant();
        assertNotEquals(normal.toInstant(), evilInstant, "instantes distintos");

        // 3) allocateInstance e inyectar Instant
        Unsafe unsafe = getUnsafe();
        Object hackedObj = unsafe.allocateInstance(ArcaDateTime.class);

        Field valueField = ArcaDateTime.class.getDeclaredField("value");
        valueField.setAccessible(true);
        valueField.set(hackedObj, evilInstant); // ahora el campo es Instant

        ArcaDateTime hacked = (ArcaDateTime) hackedObj;

        // 4) la vista pública normaliza a -03:00 y el instant cambió
        assertEquals(ZoneOffset.ofHours(-3), hacked.toOffsetDateTime().getOffset(),
            "la vista pública debe usar -03:00");
        assertEquals(evilInstant, hacked.toInstant(), "toInstant debe reflejar el instant inyectado");

        // 5) comparaciones por instant
        assertNotEquals(normal, hacked, "objetos con distinto instant NO son iguales");
        assertTrue(hacked.isAfter(normal) || hacked.isBefore(normal),
            "la comparación debe reflejar el cambio de instant");
    }
}
