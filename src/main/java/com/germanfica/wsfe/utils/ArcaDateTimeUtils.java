package com.germanfica.wsfe.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import javax.xml.datatype.XMLGregorianCalendar;

public class ArcaDateTimeUtils {

    /**
     * Enum to represent available Offsets.
     */
    @Getter
    @RequiredArgsConstructor
    public enum TimeZoneOffset {
        UTC(ZoneOffset.UTC),
        ARGENTINA(ZoneOffset.of("-03:00"));

        private final ZoneOffset zoneOffset;
    }

    /**
     * Enum to represent the date-time formats used in Arca.
     */
    public enum DateTimeFormat {
        ISO_8601_BASIC("yyyy-MM-dd'T'HH:mm:ss"),
        ISO_8601_FULL("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        private final String pattern;

        DateTimeFormat(String pattern) {
            this.pattern = pattern;
        }

        public DateTimeFormatter getFormatter() {
            return DateTimeFormatter.ofPattern(pattern);
        }
    }

    /**
     * Formats an OffsetDateTime object to a string based on the specified format.
     *
     * @param dateTime the OffsetDateTime object to format
     * @param format   the desired date-time format
     * @return the formatted date as a string
     */
    public static String formatDateTime(OffsetDateTime dateTime, DateTimeFormat format) {
        if (dateTime == null || format == null) {
            throw new IllegalArgumentException("DateTime and format cannot be null");
        }
        return dateTime.format(format.getFormatter());
    }

    /**
     * Formats an XMLGregorianCalendar object to a string based on the specified format.
     *
     * @param xmlGregorianCalendar the XMLGregorianCalendar object to format
     * @param format               the desired date-time format
     * @return the formatted date as a string
     */
    public static String formatDateTime(XMLGregorianCalendar xmlGregorianCalendar, DateTimeFormat format) {
        if (xmlGregorianCalendar == null || format == null) {
            throw new IllegalArgumentException("XMLGregorianCalendar and format cannot be null");
        }
        OffsetDateTime dateTime = xmlGregorianCalendar.toGregorianCalendar().toZonedDateTime().toOffsetDateTime();
        return formatDateTime(dateTime, format);
    }

    /**
     * Formats an {@link XMLGregorianCalendar} object to a string based on the specified format and timezone offset.
     *
     * <p>This method converts an {@code XMLGregorianCalendar} instance into an {@link OffsetDateTime},
     * adjusts it to the specified {@link TimeZoneOffset}, and then formats it into a string
     * representation using the specified {@link DateTimeFormat}. It ensures that the input
     * XMLGregorianCalendar object, the format, and the offset are not null, throwing an
     * {@link IllegalArgumentException} if any are null.
     *
     * <p>Usage example:
     * <pre>
     *     XMLGregorianCalendar xmlCalendar = ...;
     *     String formattedDate = ArcaDateTimeUtils.formatDateTime(xmlCalendar, DateTimeFormat.ISO_8601_FULL, TimeZoneOffset.ARGENTINA);
     *     System.out.println(formattedDate);
     * </pre>
     *
     * @param xmlGregorianCalendar the {@code XMLGregorianCalendar} object to be formatted. Must not be null.
     * @param format               the {@link DateTimeFormat} enum specifying the desired date-time format. Must not be null.
     * @param offset               the {@link TimeZoneOffset} enum specifying the timezone offset to apply. Must not be null.
     * @return the formatted date as a string in the specified format with the specified timezone offset.
     * @throws IllegalArgumentException if {@code xmlGregorianCalendar}, {@code format}, or {@code offset} is null.
     */
    public static String formatDateTime(XMLGregorianCalendar xmlGregorianCalendar, DateTimeFormat format, TimeZoneOffset offset) {
        if (xmlGregorianCalendar == null || format == null || offset == null) {
            throw new IllegalArgumentException("XMLGregorianCalendar and format cannot be null");
        }

        OffsetDateTime dateTime = xmlGregorianCalendar
                .toGregorianCalendar()
                .toZonedDateTime()
                .withZoneSameInstant(offset.getZoneOffset()) // Ajuste explícito del offset
                .toOffsetDateTime();
        return formatDateTime(dateTime, format);
    }
}
