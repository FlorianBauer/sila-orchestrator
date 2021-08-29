package de.fau.clients.orchestrator.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class providing functions to put/extract basic data-types to and from JSON. This class
 * provides basically the same functionality as the classes in the package
 * <code>sila_java.library.core.sila.types</code> only with the usage of the Jackson
 * <code>JsonNode</code> type, since the Protobuf complaint <code>SiLAFramework</code>-types are
 * internally harder to handle.
 */
@Slf4j
public final class SilaBasicTypeUtils {

    public static final String FIELD_VALUE = "value";
    public static final String FIELD_SECOND = "second";
    public static final String FIELD_MINUTE = "minute";
    public static final String FIELD_HOUR = "hour";
    public static final String FIELD_DAY = "day";
    public static final String FIELD_MONTH = "month";
    public static final String FIELD_YEAR = "year";
    public static final String FIELD_TIMEZONE = "timezone";
    public static final String FIELD_TZ_HOURS = "hours";
    public static final String FIELD_TZ_MINUTES = "minutes";

    /**
     * The mapper used to create <code>JsonNode</code>s.
     */
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private SilaBasicTypeUtils() {
        throw new UnsupportedOperationException("Instantiation not allowed.");
    }

    public static JsonNode binaryAsJsonNode(final byte[] val) {
        final ObjectNode binaryNode = JSON_MAPPER.createObjectNode();
        binaryNode.put(FIELD_VALUE, val);
        return binaryNode;
    }

    public static JsonNode boolAsJsonNode(final boolean val) {
        final ObjectNode boolNode = JSON_MAPPER.createObjectNode();
        boolNode.put(FIELD_VALUE, val);
        return boolNode;
    }

    public static JsonNode dateAsJsonNode(final OffsetDateTime date) {
        final ObjectNode dateNode = JSON_MAPPER.createObjectNode();
        dateNode.put(FIELD_DAY, date.getDayOfMonth());
        dateNode.put(FIELD_MONTH, date.getMonthValue());
        dateNode.put(FIELD_YEAR, date.getYear());
        dateNode.set(FIELD_TIMEZONE, timezoneAsJsonNode(date.getOffset()));
        return dateNode;
    }

    public static JsonNode integerAsJsonNode(final long val) {
        final ObjectNode intNode = JSON_MAPPER.createObjectNode();
        intNode.put(FIELD_VALUE, val);
        return intNode;
    }

    public static JsonNode realAsJsonNode(final double val) {
        final ObjectNode realNode = JSON_MAPPER.createObjectNode();
        realNode.put(FIELD_VALUE, val);
        return realNode;
    }

    public static JsonNode stringAsJsonNode(final String str) {
        final ObjectNode strNode = JSON_MAPPER.createObjectNode();
        strNode.put(FIELD_VALUE, str);
        return strNode;
    }

    public static JsonNode timeAsJsonNode(final OffsetTime time) {
        final ObjectNode timeNode = JSON_MAPPER.createObjectNode();
        timeNode.put(FIELD_SECOND, time.getSecond());
        timeNode.put(FIELD_MINUTE, time.getMinute());
        timeNode.put(FIELD_HOUR, time.getHour());
        timeNode.set(FIELD_TIMEZONE, timezoneAsJsonNode(time.getOffset()));
        return timeNode;
    }

    public static JsonNode timestampAsJsonNode(final OffsetDateTime ts) {
        final ObjectNode tsNode = JSON_MAPPER.createObjectNode();
        tsNode.put(FIELD_SECOND, ts.getSecond());
        tsNode.put(FIELD_MINUTE, ts.getMinute());
        tsNode.put(FIELD_HOUR, ts.getHour());
        tsNode.put(FIELD_DAY, ts.getDayOfMonth());
        tsNode.put(FIELD_MONTH, ts.getMonthValue());
        tsNode.put(FIELD_YEAR, ts.getYear());
        tsNode.set(FIELD_TIMEZONE, timezoneAsJsonNode(ts.getOffset()));
        return tsNode;
    }

    public static JsonNode timezoneAsJsonNode(final ZoneOffset tz) {
        final ObjectNode tzNode = JSON_MAPPER.createObjectNode();
        final int totalSeconds = tz.getTotalSeconds();
        tzNode.put(FIELD_TZ_HOURS, totalSeconds / 3600);
        final int offsetMinutes = totalSeconds / 60 % 60;
        if (offsetMinutes != 0) {
            tzNode.put(FIELD_TZ_MINUTES, offsetMinutes);
        }
        return tzNode;
    }

    public static LocalDate dateFromJsonNode(final JsonNode jsonNode) {
        final LocalDate date;
        if (jsonNode.has(FIELD_VALUE)) {
            /* This is not conform with SiLA v1.0, so we nag about it in the logs and handle the
               wrong 'value'-field anyway.*/
            log.warn("The 'value'-field in Date-types is not conform with the SiLA standard. "
                    + "Please use the defined data fields instead.");
            date = DateTimeParser.parseIsoDate(jsonNode.get(FIELD_VALUE).asText());
        } else {
            try {
                date = LocalDate.of(
                        jsonNode.get(FIELD_YEAR).asInt(),
                        jsonNode.get(FIELD_MONTH).asInt(),
                        jsonNode.get(FIELD_DAY).asInt());
            } catch (final Exception ex) {
                log.warn(ex.getMessage());
                return null;
            }
        }
        return date;
    }

    public static OffsetTime timeFromJsonNode(final JsonNode jsonNode) {
        final OffsetTime date;
        if (jsonNode.has(FIELD_VALUE)) {
            /* This is not conform with SiLA v1.0, so we nag about it in the logs and handle the
               wrong 'value'-field anyway.*/
            log.warn("The 'value'-field in Date-types is not conform with the SiLA standard. "
                    + "Please use the defined data fields instead.");
            date = DateTimeParser.parseIsoTime(jsonNode.get(FIELD_VALUE).asText());
        } else {
            final JsonNode zoneNode = jsonNode.get(FIELD_TIMEZONE);
            final ZoneOffset zoneOffset = (zoneNode != null)
                    ? zoneOffsetFromJsonNode(zoneNode)
                    : ZoneOffset.UTC;
            try {
                date = OffsetTime.of(
                        jsonNode.get(FIELD_HOUR).asInt(),
                        jsonNode.get(FIELD_MINUTE).asInt(),
                        jsonNode.get(FIELD_SECOND).asInt(),
                        0,
                        zoneOffset);
            } catch (final Exception ex) {
                log.warn(ex.getMessage());
                return null;
            }
        }
        return date;
    }

    public static OffsetDateTime timestampFromJsonNode(final JsonNode jsonNode) {
        final OffsetDateTime timestamp;
        if (jsonNode.has(FIELD_VALUE)) {
            /* This is not conform with SiLA v1.0, so we nag about it in the logs and handle the
               wrong 'value'-field anyway.*/
            log.warn("The 'value'-field in Date-types is not conform with the SiLA standard. "
                    + "Please use the defined data fields instead.");
            timestamp = DateTimeParser.parseIsoDateTime(jsonNode.get(FIELD_VALUE).asText());
        } else {
            final JsonNode zoneNode = jsonNode.get(FIELD_TIMEZONE);
            final ZoneOffset zoneOffset = (zoneNode != null)
                    ? zoneOffsetFromJsonNode(zoneNode)
                    : ZoneOffset.UTC;
            try {
                timestamp = OffsetDateTime.of(
                        jsonNode.get(FIELD_YEAR).asInt(),
                        jsonNode.get(FIELD_MONTH).asInt(),
                        jsonNode.get(FIELD_DAY).asInt(),
                        jsonNode.get(FIELD_HOUR).asInt(),
                        jsonNode.get(FIELD_MINUTE).asInt(),
                        jsonNode.get(FIELD_SECOND).asInt(),
                        0,
                        zoneOffset);
            } catch (final Exception ex) {
                log.warn(ex.getMessage());
                return null;
            }
        }
        return timestamp;
    }

    public static ZoneOffset zoneOffsetFromJsonNode(final JsonNode jsonNode) {
        final JsonNode hours;
        final JsonNode minutes;
        try {
            hours = jsonNode.get(FIELD_TZ_HOURS);
            minutes = jsonNode.get(FIELD_TZ_MINUTES);
        } catch (final Exception ex) {
            log.warn("Invalid ZoneOffset. Zone is set to UTC.");
            return ZoneOffset.UTC;
        }
        return ZoneOffset.ofHoursMinutes(
                hours != null ? hours.asInt(0) : 0,
                minutes != null ? minutes.asInt(0) : 0);
    }

    /**
     * Converts hexadecimal byte values to a string.
     *
     * @param hexValues The hex values to represent as string.
     * @return A the hex values as String.
     */
    public static String toHexString(byte[] hexValues) {
        final BigInteger number = new BigInteger(1, hexValues);
        final StringBuilder hexString = new StringBuilder(number.toString(16));
        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }
        return hexString.toString();
    }
}
