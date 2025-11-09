// src/test/java/com/germanfica/ArcaDateTimeUnsafeTest.java
package com.germanfica;

import com.germanfica.wsfe.time.ArcaDateTime;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

public class ArcaDateTimeUnsafeTest {

    private static Unsafe getUnsafe() throws Exception {
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        return (Unsafe) f.get(null);
    }

    @Test
    public void unsafe_allocateInstance_and_field_set_should_not_change_public_view_offset() throws Exception {
        OffsetDateTime evil = OffsetDateTime.of(LocalDateTime.of(2025, 6, 2, 15, 30), ZoneOffset.ofHours(2));
        Instant evilInstant = evil.toInstant();

        Unsafe unsafe = getUnsafe();
        Object hackedObj = unsafe.allocateInstance(ArcaDateTime.class);

        Field valueField = ArcaDateTime.class.getDeclaredField("value");
        valueField.setAccessible(true);
        valueField.set(hackedObj, evilInstant); // inyectar Instant

        ArcaDateTime hacked = (ArcaDateTime) hackedObj;

        // 1) la vista pública debe normalizar a -03:00
        assertEquals(ZoneOffset.ofHours(-3), hacked.toOffsetDateTime().getOffset(),
            "La vista pública debe usar -03:00");

        // toString debe contener -03:00
        String s = hacked.toString();
        assertTrue(s.contains("-03:00"), "toString() debe contener -03:00: " + s);

        // 2) toInstant debe reflejar el instant inyectado
        assertEquals(evilInstant, hacked.toInstant(), "toInstant debe coincidir con el instant inyectado");

        // 3) equals por instant
        ArcaDateTime expected = ArcaDateTime.of(evilInstant);
        assertEquals(expected, hacked, "equals por instant debe ser true");

        // 4) isAfter/isBefore según instantes
        ArcaDateTime earlier = ArcaDateTime.of(evilInstant.minusSeconds(3600));
        ArcaDateTime later = ArcaDateTime.of(evilInstant.plusSeconds(3600));

        assertTrue(hacked.isAfter(earlier), "hacked debe ser after de 1h antes");
        assertTrue(hacked.isBefore(later), "hacked debe ser before de 1h después");
    }

    @Test
    public void reflection_constructor_should_normalize_offset() throws Exception {
        // sigue existiendo el ctor privado (OffsetDateTime) y convierte a Instant internamente
        OffsetDateTime evil = OffsetDateTime.of(LocalDateTime.of(2025, 6, 2, 15, 30), ZoneOffset.ofHours(2));
        Instant evilInstant = evil.toInstant();

        Constructor<ArcaDateTime> ctor = ArcaDateTime.class.getDeclaredConstructor(OffsetDateTime.class);
        ctor.setAccessible(true);
        ArcaDateTime viaCtor = ctor.newInstance(evil);

        // vista pública -03:00
        assertEquals(ZoneOffset.ofHours(-3), viaCtor.toOffsetDateTime().getOffset(),
            "Constructor reflection: vista pública usa -03:00");

        // instant preservado
        assertEquals(evilInstant, viaCtor.toInstant(), "Constructor reflection: instant debe coincidir");

        // equals por instant
        ArcaDateTime expected = ArcaDateTime.of(evilInstant);
        assertEquals(expected, viaCtor, "Constructor reflection: equals por instant");
    }

    @Test
    public void unsafe_allocateInstance_then_serialize_deserialize_should_normalize_via_readResolve() throws Exception {
        OffsetDateTime evil = OffsetDateTime.of(LocalDateTime.of(2025, 6, 2, 15, 30), ZoneOffset.ofHours(2));
        Instant evilInstant = evil.toInstant();

        Unsafe unsafe = getUnsafe();
        Object hacked = unsafe.allocateInstance(ArcaDateTime.class);

        // inyectar Instant directamente
        Field valueField = ArcaDateTime.class.getDeclaredField("value");
        valueField.setAccessible(true);
        valueField.set(hacked, evilInstant);

        // serializar
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bout)) {
            oos.writeObject(hacked);
        }

        // deserializar (readResolve crea instancia consistente por instant)
        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
        Object deserialized;
        try (ObjectInputStream ois = new ObjectInputStream(bin)) {
            deserialized = ois.readObject();
        }

        assertTrue(deserialized instanceof ArcaDateTime, "Debe deserializar como ArcaDateTime");
        ArcaDateTime des = (ArcaDateTime) deserialized;

        // vista pública normalizada
        assertEquals(ZoneOffset.ofHours(-3), des.toOffsetDateTime().getOffset(),
            "Deserializado: vista pública -03:00");

        // instant preservado
        assertEquals(evilInstant, des.toInstant(), "Deserializado: instant debe coincidir");

        // equals y comparaciones
        ArcaDateTime expected = ArcaDateTime.of(evilInstant);
        assertEquals(expected, des, "Deserializado: equals por instant");
        assertTrue(des.isAfter(ArcaDateTime.of(evilInstant.minusSeconds(1))));
        assertTrue(des.isBefore(ArcaDateTime.of(evilInstant.plusSeconds(1))));
    }

    @Test
    public void unsafe_allocateInstance_and_field_set_public_api_consistency() throws Exception {
        OffsetDateTime evil = OffsetDateTime.of(LocalDateTime.of(2025, 6, 2, 15, 30), ZoneOffset.ofHours(2));
        Instant evilInstant = evil.toInstant();

        Unsafe unsafe = getUnsafe();
        Object o = unsafe.allocateInstance(ArcaDateTime.class);

        Field valueField = ArcaDateTime.class.getDeclaredField("value");
        valueField.setAccessible(true);
        valueField.set(o, evilInstant); // Instant

        ArcaDateTime hacked = (ArcaDateTime) o;

        // public view debe estar normalizada
        assertEquals(ZoneOffset.ofHours(-3), hacked.toOffsetDateTime().getOffset(), "Public view usa -03:00");

        // toInstant refleja el instant inyectado
        assertEquals(evilInstant, hacked.toInstant(), "toInstant refleja el instant inyectado");

        // equals por instant
        ArcaDateTime expected = ArcaDateTime.of(evilInstant);
        assertEquals(expected, hacked, "equals por instant debe ser true");
    }
}
