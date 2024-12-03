package com.germanfica.wsfe.utils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import javax.xml.datatype.XMLGregorianCalendar;

public class ArcaDateTimeUtils {

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
     * Formats an OffsetDateTime object to a string based on the specified format and offset.
     *
     * @param dateTime the OffsetDateTime object to format
     * @param format   the desired date-time format
     * @param offset   the ZoneOffset to apply (e.g., ZoneOffset.of("-03:00"))
     * @return the formatted date as a string
     */
    public static String formatDateTime(OffsetDateTime dateTime, DateTimeFormat format, ZoneOffset offset) {
        if (dateTime == null || format == null) {
            throw new IllegalArgumentException("DateTime and format cannot be null");
        }
        if (offset != null) {
            dateTime = dateTime.withOffsetSameInstant(offset);
        }
        return dateTime.format(format.getFormatter());
    }

    /**
     * Formats an XMLGregorianCalendar object to a string based on the specified format and offset.
     *
     * @param xmlGregorianCalendar the XMLGregorianCalendar object to format
     * @param format               the desired date-time format
     * @param offset               the ZoneOffset to apply (e.g., ZoneOffset.of("-03:00"))
     * @return the formatted date as a string
     */
    public static String formatDateTime(XMLGregorianCalendar xmlGregorianCalendar, DateTimeFormat format, ZoneOffset offset) {
        if (xmlGregorianCalendar == null || format == null) {
            throw new IllegalArgumentException("XMLGregorianCalendar and format cannot be null");
        }
        OffsetDateTime dateTime = xmlGregorianCalendar.toGregorianCalendar().toZonedDateTime().toOffsetDateTime();
        return formatDateTime(dateTime, format, offset);
    }
}
