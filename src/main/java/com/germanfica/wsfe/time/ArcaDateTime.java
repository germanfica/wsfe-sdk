package com.germanfica.wsfe.time;

import com.germanfica.wsfe.util.ArcaDateTimeUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Represents a date‐time with an offset, intended to simplify working with ARCA/AFIP date‐time values.
 *
 * <p>Internally, this class wraps a {@link OffsetDateTime}, but provides
 * a more convenient API for ARCA/AFIP usage, including:
 * <ul>
 *     <li>Parsing from ISO‐8601 full format (with milliseconds and offset).</li>
 *     <li>Adding or subtracting minutes via {@link #plusMinutes(long)} and {@link #minusMinutes(long)}.</li>
 *     <li>Overriding {@link #toString()} so that the output always adheres to
 *         <strong>ISO_8601_FULL</strong> (<code>yyyy-MM-dd'T'HH:mm:ss.SSSXXX</code>), which is required by ARCA/AFIP.</li>
 * </ul>
 *
 * <p>Example of the string representation: <code>2025-06-02T15:30:00.123-03:00</code>.</p>
 *
 * @see OffsetDateTime
 */
@Getter
@EqualsAndHashCode
public final class ArcaDateTime implements Serializable {
    private final Instant value;
    private static final ZoneOffset ARCA_OFFSET = ArcaDateTimeUtils.TimeZoneOffset.ARGENTINA.getZoneOffset();

    private ArcaDateTime(Instant value) {
        this.value = value;
    }

    private ArcaDateTime(OffsetDateTime value) {
        this.value = value.toInstant();
    }

    private OffsetDateTime normalizedValue() {
        return value.atOffset(ARCA_OFFSET);
    }

    @Serial
    private Object readResolve() {
        return ArcaDateTime.of(this.toInstant());
    }

    public static ArcaDateTime now() {
        return of(Instant.now());
    }

    public static ArcaDateTime now(Clock clock) {
        return of(clock.instant());
    }

    public static ArcaDateTime of(OffsetDateTime value) {
        return new ArcaDateTime(value.toInstant());
    }

    /**
     * Creates an {@code ArcaDateTime} from a given {@link Instant}.
     *
     * <p>The provided {@code Instant} is interpreted as an instant on the UTC time-line,
     * and is then converted to an {@link OffsetDateTime} using the Argentina time-zone offset (-03:00).
     * Consequently, if one supplies an {@code Instant} representing {@code "2025-06-02T18:30:00Z"},
     * the resulting {@code ArcaDateTime} will represent {@code "2025-06-02T15:30:00-03:00"}.
     *
     * <p>Typical ways to obtain an {@code Instant} include:
     * <ul>
     *     <li>{@code Instant.now()} for the current moment in UTC.</li>
     *     <li>{@code Instant.parse("2025-06-02T18:30:00Z")} to parse an ISO-8601 string in UTC.</li>
     * </ul>
     *
     * @param instant the {@code Instant} to convert; must not be null.
     * @return an {@code ArcaDateTime} representing the same instant in the Argentina offset (-03:00).
     * @throws IllegalArgumentException if {@code instant} is null.
     */
    public static ArcaDateTime of(Instant instant) {
        return new ArcaDateTime(instant);
    }

    /**
     * Parses an ISO‐8601 string (e.g. "2025-06-02T15:30:00-03:00") into ArcaDateTime.
     * @throws DateTimeParseException if the text cannot be parsed.
     */
    public static ArcaDateTime parse(String isoString) throws DateTimeParseException {
        return of(OffsetDateTime.parse(isoString));
    }

    public boolean isAfterNow() {
        return value.isAfter(ArcaDateTime.now().value);
    }

    public boolean isBeforeNow() {
        return value.isBefore(ArcaDateTime.now().value);
    }

    public boolean isAfter(ArcaDateTime other) {
        return this.value.isAfter(other.value);
    }

    public boolean isBefore(ArcaDateTime other) {
        return this.value.isBefore(other.value);
    }

    public boolean isEqual(ArcaDateTime other) {
        return this.value.equals(other.value);
    }

    public boolean isNotEqual(ArcaDateTime other) {
        return !isEqual(other);
    }

    public int compareTo(ArcaDateTime other) {
        return this.value.compareTo(other.value);
    }

    public boolean isBetween(ArcaDateTime startInclusive, ArcaDateTime endExclusive) {
        return !this.isBefore(startInclusive) && this.isBefore(endExclusive);
    }

    public OffsetDateTime toOffsetDateTime() {
        return normalizedValue();
    }

    public LocalDate toLocalDate() {
        return toOffsetDateTime().toLocalDate();
    }

    public LocalTime toLocalTime() {
        return toOffsetDateTime().toLocalTime();
    }

    public LocalDateTime toLocalDateTime() {
        return toOffsetDateTime().toLocalDateTime();
    }

    public Instant toInstant() {
        return value;
    }

    /**
     * Returns a new ArcaDateTime that is <code>minutes</code> minutes earlier than this one.
     */
    public ArcaDateTime minusMinutes(long minutes) {
        //return new ArcaDateTime(value.minusMinutes(minutes));
        Instant newInstant = toInstant().minus(minutes, ChronoUnit.MINUTES);
        return ArcaDateTime.of(newInstant);
    }

    /**
     * Returns a new ArcaDateTime that is <code>minutes</code> minutes later than this one.
     */
    public ArcaDateTime plusMinutes(long minutes) {
        //return new ArcaDateTime(value.plusMinutes(minutes));
        Instant newInstant = toInstant().plus(minutes, ChronoUnit.MINUTES);
        return ArcaDateTime.of(newInstant);
    }

    /**
     * Returns a new ArcaDateTime that is <code>millis</code> milliseconds later than this one.
     */
    public ArcaDateTime plusMillis(long millis) {
        return new ArcaDateTime(value.plus(java.time.Duration.ofMillis(millis)));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArcaDateTime)) return false;
        ArcaDateTime other = (ArcaDateTime) o;
        return this.toInstant().equals(other.toInstant());
    }

    @Override
    public int hashCode() {
        return this.toInstant().hashCode();
    }

    /**
     * Returns a string representation of this date‐time, formatted in the ISO‐8601 full pattern,
     * including milliseconds and offset. The exact pattern used is:
     *
     * <pre>
     *     yyyy-MM-dd'T'HH:mm:ss.SSSXXX
     * </pre>
     *
     * <p>Examples of valid output:
     * <ul>
     *     <li><code>2025-06-02T15:30:00.000-03:00</code></li>
     * </ul>
     *
     * @return a formatted {@code String} in ISO‐8601 full format.
     * @see ArcaDateTimeUtils.DateTimeFormat#ISO_8601_FULL
     */
    @Override
    public String toString() {
        return ArcaDateTimeUtils.formatDateTime(normalizedValue(), ArcaDateTimeUtils.DateTimeFormat.ISO_8601_FULL);
    }
}
