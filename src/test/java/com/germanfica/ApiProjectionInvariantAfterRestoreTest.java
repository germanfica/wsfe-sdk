package com.germanfica;

import com.germanfica.wsfe.time.ArcaDateTime;
import com.germanfica.wsfe.util.ArcaDateTimeUtils;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprueba que, en condiciones normales (sin mutaciones), la API proyecta siempre -03:00
 * y que toInstant/equals siguen comportandose correctamente.
 */
public class ApiProjectionInvariantAfterRestoreTest {

    @Test
    public void normal_usage_projects_argentina_offset_and_instant_is_preserved() {
        ZoneOffset argentina = ArcaDateTimeUtils.TimeZoneOffset.ARGENTINA.getZoneOffset();
        assertEquals(ZoneOffset.ofHours(-3), argentina, "Constante ARGENTINA debe ser -03:00");

        Instant now = Instant.parse("2025-06-02T18:30:00Z");
        ArcaDateTime a = ArcaDateTime.of(now);

        assertEquals(argentina, a.toOffsetDateTime().getOffset(), "toOffsetDateTime debe usar ARGENTINA");
        assertEquals(now, a.toInstant(), "toInstant debe devolver el instant original");

        // equals/hashes
        ArcaDateTime same = ArcaDateTime.of(now);
        assertEquals(same, a);
        assertEquals(same.hashCode(), a.hashCode());
    }
}
