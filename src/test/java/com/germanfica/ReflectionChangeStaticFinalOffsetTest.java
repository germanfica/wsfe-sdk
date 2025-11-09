package com.germanfica;

import com.germanfica.wsfe.time.ArcaDateTime;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.*;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JVM 17
 */
public class ReflectionChangeStaticFinalOffsetTest {
    @Test
    public void reflection_can_replace_static_final_ARCA_OFFSET_and_projection_changes() throws Exception {
        Field f = ArcaDateTime.class.getDeclaredField("ARCA_OFFSET");
        f.setAccessible(true);

        ZoneOffset original = (ZoneOffset) f.get(null);

        boolean changed = false;

        // 1) intento clásico (puede fallar en Java 9+)
        try {
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);

            ZoneOffset nuevo = ZoneOffset.ofHours(0);
            f.set(null, nuevo);
            changed = true;
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            // no disponible -> seguir a siguiente técnica
        }

        // 2) intento con VarHandle (Java 9+)
        if (!changed) {
            try {
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                MethodHandles.Lookup privateLookup = MethodHandles.privateLookupIn(ArcaDateTime.class, lookup);
                VarHandle vh = privateLookup.findStaticVarHandle(ArcaDateTime.class, "ARCA_OFFSET", ZoneOffset.class);
                vh.set(ZoneOffset.ofHours(0));
                changed = true;
            } catch (Throwable t) {
                // puede fallar por accesos de módulo o permisos -> fallback
            }
        }

        // 3) intento con Unsafe (fallback)
        if (!changed) {
            try {
                sun.misc.Unsafe unsafe = getUnsafe();
                long offset = unsafe.staticFieldOffset(f);
                Object base = unsafe.staticFieldBase(f);
                unsafe.putObject(base, offset, ZoneOffset.ofHours(0));
                changed = true;
            } catch (Throwable t) {
                // fallback no disponible
            }
        }

        // si no pudimos cambiar, mejor skipear test en lugar de fallar por incompatibilidad de JVM
        assumeTrue(changed, "No se pudo cambiar ARCA_OFFSET en esta JVM con las estrategias conocidas - test skipeado");

        try {
            // comprobar proyeccion publica
            Instant now = Instant.parse("2025-06-02T18:30:00Z");
            ArcaDateTime a = ArcaDateTime.of(now);
            assertEquals(ZoneOffset.ofHours(0), a.toOffsetDateTime().getOffset(),
                "Proyeccion debe usar el offset modificado por reflexion");
        } finally {
            // restaurar
            try { f.set(null, original); } catch (Throwable ignore) {}
        }
    }

    private static sun.misc.Unsafe getUnsafe() throws Exception {
        Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        return (sun.misc.Unsafe) f.get(null);
    }
}
