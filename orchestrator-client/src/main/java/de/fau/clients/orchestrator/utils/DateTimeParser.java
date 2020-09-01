package de.fau.clients.orchestrator.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.List;

/**
 * A utility class which provides functions to parse ISO-8601 represented Date- and Time-Strings to
 * native data types using the Time API introduced in Java 8.
 */
public final class DateTimeParser {

    public static final ZoneOffset LOCAL_OFFSET = ZoneId.systemDefault().getRules().getOffset(Instant.now());
    public static final int LOCAL_OFFSET_IN_SEC = LOCAL_OFFSET.getTotalSeconds();

    private DateTimeParser() {
        throw new UnsupportedOperationException("Instantiation not allowed.");
    }

    /**
     * Parses ISO-8601 date-Strings of the form <code>yyyy-MM-dd</code>. Additional
     * time-zone-offsets are going to be ignored.
     *
     * @param isoDateStr a ISO-8601 conform date-String.
     * @return A LocalDate-date or <code>null</code> on error.
     */
    public static LocalDate parseIsoDate(String isoDateStr) {
        final List<DateTimeFormatter> formatters = Arrays.asList(
                DateTimeFormatter.ISO_DATE,
                DateTimeFormatter.ofPattern("uuuu-MM-ddX"),
                new DateTimeFormatterBuilder()
                        .appendPattern("uuuuMMdd[X]")
                        .toFormatter());

        for (final DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(isoDateStr, formatter);
            } catch (DateTimeParseException ex) {
            }
        }
        return null;
    }

    /**
     * Parses ISO-8601 time-Strings of the form <code>HH:mm:ss</code>.
     *
     * @param isoTimeStr A ISO-8601 conform time-String.
     * @return A OffsetTime-timestamp adjusted to the offset of the current system or
     * <code>null</code> on error.
     */
    public static OffsetTime parseIsoTime(String isoTimeStr) {
        final List<DateTimeFormatter> formatters = Arrays.asList(
                DateTimeFormatter.ISO_OFFSET_TIME,
                new DateTimeFormatterBuilder()
                        .appendPattern("HH:mm:ss")
                        .optionalStart()
                        .appendLiteral(".")
                        .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, false)
                        .optionalEnd()
                        .optionalStart().appendPattern("[X]").optionalEnd()
                        .parseDefaulting(ChronoField.OFFSET_SECONDS, LOCAL_OFFSET_IN_SEC)
                        .toFormatter(),
                new DateTimeFormatterBuilder()
                        .appendPattern("HHmmss")
                        .optionalStart()
                        .appendLiteral(".")
                        .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, false)
                        .optionalEnd()
                        .optionalStart().appendPattern("[X]").optionalEnd()
                        .parseDefaulting(ChronoField.OFFSET_SECONDS, LOCAL_OFFSET_IN_SEC)
                        .toFormatter());

        for (final DateTimeFormatter formatter : formatters) {
            try {
                return OffsetTime.parse(isoTimeStr, formatter).withOffsetSameInstant(LOCAL_OFFSET);
            } catch (DateTimeParseException ex) {
            }
        }
        return null;
    }

    /**
     * Parses ISO-8601 timestamps of the form <code>yyyy-MM-ddTHH:mm:ss</code>.
     *
     * @param isoDateTimeStr A ISO-8601 conform date-time-String.
     * @return A OffsetDateTime-timestamp adjusted to UTC or <code>null</code> on error.
     */
    public static OffsetDateTime parseIsoDateTime(String isoDateTimeStr) {
        final List<DateTimeFormatter> formatters = Arrays.asList(
                DateTimeFormatter.ISO_OFFSET_DATE_TIME,
                new DateTimeFormatterBuilder()
                        .appendPattern("uuuu-MM-dd'T'HH:mm:ss")
                        .optionalStart()
                        .appendLiteral(".")
                        .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, false)
                        .optionalEnd()
                        .optionalStart().appendPattern("[X]").optionalEnd()
                        .parseDefaulting(ChronoField.OFFSET_SECONDS, LOCAL_OFFSET_IN_SEC)
                        .toFormatter(),
                new DateTimeFormatterBuilder()
                        .appendPattern("uuuuMMdd'T'HHmmss")
                        .optionalStart()
                        .appendLiteral(".")
                        .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, false)
                        .optionalEnd()
                        .optionalStart().appendPattern("[X]").optionalEnd()
                        .parseDefaulting(ChronoField.OFFSET_SECONDS, LOCAL_OFFSET_IN_SEC)
                        .toFormatter());

        for (final DateTimeFormatter fmt : formatters) {
            try {
                return OffsetDateTime.parse(isoDateTimeStr, fmt).withOffsetSameInstant(ZoneOffset.UTC);
            } catch (DateTimeParseException ex) {
            }
        }
        return null;
    }
}
