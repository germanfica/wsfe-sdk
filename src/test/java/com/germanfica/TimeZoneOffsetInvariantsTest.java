package com.germanfica;

import com.germanfica.wsfe.time.ArcaDateTime;
import com.germanfica.wsfe.util.ArcaDateTimeUtils;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

public class TimeZoneOffsetInvariantsTest {

    @Test
    public void argentina_offset_constant_and_projections_use_it() {
        ZoneOffset argentina = ArcaDateTimeUtils.TimeZoneOffset.ARGENTINA.getZoneOffset();
        assertEquals(ZoneOffset.ofHours(-3), argentina, "Argetina offset debe ser -03:00");

        Instant now = Instant.now();
        ArcaDateTime a = ArcaDateTime.of(now);

        // la vista de salida usa exactamente la constante
        assertEquals(argentina, a.toOffsetDateTime().getOffset(), "toOffsetDateTime debe usar la constante ARGENTINA");
        assertEquals(now, a.toInstant(), "toInstant debe devolver el instant original");
    }
}
