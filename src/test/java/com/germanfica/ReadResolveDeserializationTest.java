package com.germanfica;

import com.germanfica.wsfe.time.ArcaDateTime;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.Field;
import java.time.*;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assertions.*;

@Tag("unsafe")
public class ReadResolveDeserializationTest {

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
    public void serialize_deserialize_normal_instance_keeps_invariants() throws Exception {
        ArcaDateTime original = ArcaDateTime.of(Instant.parse("2025-06-02T18:30:00Z"));

        // serializar
        byte[] raw;
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bout)) {
            oos.writeObject(original);
            raw = bout.toByteArray();
        }

        // deserializar
        Object deserialized;
        try (ByteArrayInputStream bin = new ByteArrayInputStream(raw);
             ObjectInputStream ois = new ObjectInputStream(bin)) {
            deserialized = ois.readObject();
        }

        assertTrue(deserialized instanceof ArcaDateTime, "Deserializado debe ser ArcaDateTime");
        ArcaDateTime des = (ArcaDateTime) deserialized;

        // instant preservado y vista normalizada a -03:00
        assertEquals(original.toInstant(), des.toInstant(), "Instant debe conservarse tras serializar/deserializar");
        assertEquals(ZoneOffset.ofHours(-3), des.toOffsetDateTime().getOffset(), "Vista pública debe usar -03:00");
        assertEquals(original, des, "equals por instant debe mantener la igualdad");
    }

    @Test
    public void serialize_deserialize_hacked_instance_should_normalize_via_readResolve_if_present() throws Exception {
        sun.misc.Unsafe unsafe = getUnsafeOrNull();
        assumeTrue(unsafe != null, "Unsafe no disponible en esta JVM - test skipeado");

        OffsetDateTime evil = OffsetDateTime.of(LocalDateTime.of(2025,6,2,15,30), ZoneOffset.ofHours(2));
        Instant evilInstant = evil.toInstant();

        // allocateInstance + inyectar instant en campo privado final
        Object hacked = unsafe.allocateInstance(ArcaDateTime.class);
        Field valueField = ArcaDateTime.class.getDeclaredField("value");
        valueField.setAccessible(true);
        valueField.set(hacked, evilInstant);

        // serializar el objeto 'hackeado'
        byte[] raw;
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bout)) {
            oos.writeObject(hacked);
            raw = bout.toByteArray();
        }

        // deserializar y comprobar que la instancia resultante conserva invariantes
        Object deserialized;
        try (ByteArrayInputStream bin = new ByteArrayInputStream(raw);
             ObjectInputStream ois = new ObjectInputStream(bin)) {
            deserialized = ois.readObject();
        }

        assertTrue(deserialized instanceof ArcaDateTime, "Deserializado debe ser ArcaDateTime");
        ArcaDateTime des = (ArcaDateTime) deserialized;

        // Si implementaste readResolve que normaliza, estas aserciones pasarán.
        // Si NO lo implementaste, seguramente el deserializado contendrá el instant inyectado,
        // pero seguirá proyectando -03:00 en la vista pública.
        assertEquals(evilInstant, des.toInstant(), "toInstant debe coincidir con el instant del objeto serializado");
        assertEquals(ZoneOffset.ofHours(-3), des.toOffsetDateTime().getOffset(), "Vista pública debe usar -03:00");
        assertEquals(ArcaDateTime.of(evilInstant), des, "equals por instant debe ser true");
    }
}
