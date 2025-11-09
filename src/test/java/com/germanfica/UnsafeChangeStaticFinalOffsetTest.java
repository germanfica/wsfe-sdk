package com.germanfica;

import com.germanfica.wsfe.time.ArcaDateTime;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.time.*;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Usa Unsafe para mutar la referencia static final ARCA_OFFSET.
 * Requiere que Unsafe esté disponible en la JVM; si no, el test se saltea.
 */
@Tag("unsafe")
public class UnsafeChangeStaticFinalOffsetTest {

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
    public void unsafe_can_overwrite_static_final_ARCA_OFFSET_and_projection_changes() throws Exception {
        Unsafe unsafe = getUnsafeOrNull();
        assumeTrue(unsafe != null, "Unsafe no disponible en esta JVM - test skipeado");

        Field staticField = com.germanfica.wsfe.time.ArcaDateTime.class.getDeclaredField("ARCA_OFFSET");
        staticField.setAccessible(true);

        // valor original para restaurar
        ZoneOffset original = (ZoneOffset) staticField.get(null);

        Object staticBase = unsafe.staticFieldBase(staticField);
        long staticOffset = unsafe.staticFieldOffset(staticField);

        try {
            ZoneOffset nuevo = ZoneOffset.ofHours(1); // +01:00
            unsafe.putObject(staticBase, staticOffset, nuevo);

            Instant now = Instant.parse("2025-06-02T18:30:00Z");
            ArcaDateTime a = ArcaDateTime.of(now);

            assertEquals(nuevo, a.toOffsetDateTime().getOffset(),
                "La proyeccion debe reflejar el nuevo offset escrito por Unsafe");

        } finally {
            // restaurar
            try {
                unsafe.putObject(staticBase, staticOffset, original);
            } catch (Throwable ignore) {
                // si no se puede restaurar, al menos intentamos
            }
        }
    }
}
