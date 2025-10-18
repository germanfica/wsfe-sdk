package com.germanfica.wsfe.time;

import com.germanfica.wsfe.util.ArcaDateTimeUtils;

import java.time.Clock;
import java.time.ZoneOffset;

/**
 * Clock preconfigurado para ARCA / AFIP.
 *
 * - servidor NTP por defecto: time.afip.gov.ar
 * - zona fallback por defecto: UTC-3 (ZoneOffset.of("-03"))
 * - TTL por defecto: 60_000 ms (1 minuto)
 *
 * Esta clase solo delega en NtpClock pasando los valores de ARCA.
 * Puede usarse directamente en ArcaDateTime.now(clock).
 */
public final class ArcaNtpClock extends NtpClock {

    /** Servidor NTP oficial recomendado para ARCA/AFIP */
    public static final String DEFAULT_ARCA_NTP_SERVER = "time.afip.gov.ar";

    /** Zona fallback por defecto (UTC-3) */
    public static final ZoneOffset DEFAULT_ARCA_ZONE_OFFSET = ArcaDateTimeUtils.TimeZoneOffset.ARGENTINA.getZoneOffset();

    /** Por defecto: server=time.afip.gov.ar, fallback=Clock.system(UTC-3) */
    public ArcaNtpClock() {
        super(DEFAULT_ARCA_NTP_SERVER, Clock.system(DEFAULT_ARCA_ZONE_OFFSET));
    }

    /** Permite especificar solo fallback (usa server por defecto). */
    public ArcaNtpClock(Clock fallbackClock) {
        super(DEFAULT_ARCA_NTP_SERVER,
            fallbackClock == null ? Clock.system(DEFAULT_ARCA_ZONE_OFFSET) : fallbackClock);
    }

    /** Permite especificar servidor y fallback (por ejemplo, para un pool interno). */
    public ArcaNtpClock(String server, Clock fallbackClock) {
        super(server == null ? DEFAULT_ARCA_NTP_SERVER : server,
            fallbackClock == null ? Clock.system(DEFAULT_ARCA_ZONE_OFFSET) : fallbackClock);
    }
}
