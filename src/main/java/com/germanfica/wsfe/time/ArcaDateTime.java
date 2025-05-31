package com.germanfica.wsfe.time;

import com.germanfica.wsfe.util.ArcaDateTimeUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.time.*;
import java.time.format.DateTimeParseException;

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

    @Override
    public String toString() {
        return ArcaDateTimeUtils.formatDateTime(value, ArcaDateTimeUtils.DateTimeFormat.ISO_8601_FULL);
    }
}
