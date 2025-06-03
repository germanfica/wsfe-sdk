package com.germanfica.wsfe.time;

import com.germanfica.wsfe.util.ArcaDateTimeUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.time.*;
import java.time.format.DateTimeParseException;

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
    private final OffsetDateTime value;

    private ArcaDateTime(OffsetDateTime value) {
        this.value = value;
    }

    public static ArcaDateTime now() {
        return new ArcaDateTime(OffsetDateTime.now());
    }

    public static ArcaDateTime of(OffsetDateTime value) {
        return new ArcaDateTime(value);
    }

    /**
     * Parses an ISO‐8601 string (e.g. "2025-06-02T15:30:00-03:00") into ArcaDateTime.
     * @throws DateTimeParseException if the text cannot be parsed.
     */
    public static ArcaDateTime parse(String isoString) throws DateTimeParseException {
        return new ArcaDateTime(OffsetDateTime.parse(isoString));
    }

    public boolean isAfterNow() {
        return value.isAfter(OffsetDateTime.now());
    }

    public boolean isBeforeNow() {
        return value.isBefore(OffsetDateTime.now());
    }

    public boolean isAfter(ArcaDateTime other) {
        return this.value.isAfter(other.value);
    }

    public boolean isBefore(ArcaDateTime other) {
        return this.value.isBefore(other.value);
    }

    public boolean isEqual(ArcaDateTime other) {
        return this.value.isEqual(other.value);
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
        return value;
    }

    public LocalDate toLocalDate() {
        return value.toLocalDate();
    }

    public LocalTime toLocalTime() {
        return value.toLocalTime();
    }

    public LocalDateTime toLocalDateTime() {
        return value.toLocalDateTime();
    }

    public Instant toInstant() {
        return value.toInstant();
    }

    /**
     * Returns a new ArcaDateTime that is <code>minutes</code> minutes earlier than this one.
     */
    public ArcaDateTime minusMinutes(long minutes) {
        return new ArcaDateTime(value.minusMinutes(minutes));
    }

    /**
     * Returns a new ArcaDateTime that is <code>minutes</code> minutes later than this one.
     */
    public ArcaDateTime plusMinutes(long minutes) {
        return new ArcaDateTime(value.plusMinutes(minutes));
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
        return ArcaDateTimeUtils.formatDateTime(value, ArcaDateTimeUtils.DateTimeFormat.ISO_8601_FULL);
    }
}
