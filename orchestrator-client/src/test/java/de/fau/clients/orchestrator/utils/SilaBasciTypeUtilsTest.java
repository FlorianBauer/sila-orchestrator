package de.fau.clients.orchestrator.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import static de.fau.clients.orchestrator.utils.SilaBasicTypeUtils.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import sila_java.library.core.sila.types.SiLABinary;
import sila_java.library.core.sila.types.SiLAString;
import sila_java.library.core.sila.types.SiLATime;
import sila_java.library.core.sila.types.SiLATimeZone;
import sila_java.library.core.sila.types.SiLATimestamp;

public class SilaBasciTypeUtilsTest {

    private final ObjectMapper jsonMapper = new ObjectMapper();

    @Test
    public void binaryAsJsonNode() {
        final byte[] data = {(byte) 0x01, (byte) 0x02, (byte) 0x03};
        JsonNode actNode = SilaBasicTypeUtils.binaryAsJsonNode(data);
        try {
            assertArrayEquals(data, actNode.get(FIELD_VALUE).binaryValue());
        } catch (final IOException ex) {
            fail(ex.getMessage());
        }

        try {
            String exp = JsonFormat.printer().print(SiLABinary.fromBytes(data)).replaceAll("\\s+", "");
            String act = actNode.toString().replaceAll("\\s+", "");
            assertEquals(exp, act);
        } catch (final InvalidProtocolBufferException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void boolAsJsonNode() {
        JsonNode actNode = SilaBasicTypeUtils.boolAsJsonNode(true);
        assertEquals(true, actNode.get(FIELD_VALUE).booleanValue());
        actNode = SilaBasicTypeUtils.boolAsJsonNode(false);
        assertEquals(false, actNode.get(FIELD_VALUE).booleanValue());
        String act = actNode.toString().replaceAll("\\s+", "");
        assertEquals("{\"value\":false}", act);
    }

    @Test
    public void dateAsJsonNode() {
        final OffsetDateTime date = OffsetDateTime.of(2020, 11, 10, 0, 0, 0, 0, ZoneOffset.ofHours(3));
        JsonNode actNode = SilaBasicTypeUtils.dateAsJsonNode(date);
        assertEquals(date.getDayOfMonth(), actNode.get(FIELD_DAY).intValue());
        assertEquals(date.getMonthValue(), actNode.get(FIELD_MONTH).intValue());
        assertEquals(date.getYear(), actNode.get(FIELD_YEAR).intValue());
        String act = actNode.toString().replaceAll("\\s+", "");
        assertEquals("{\"day\":10,\"month\":11,\"year\":2020,\"timezone\":{\"hours\":3}}", act);
    }

    @Test
    public void integerAsJsonNode() {
        JsonNode actNode = SilaBasicTypeUtils.integerAsJsonNode(42);
        assertEquals(42, actNode.get(FIELD_VALUE).intValue());
        actNode = SilaBasicTypeUtils.integerAsJsonNode(-13);
        assertEquals(-13, actNode.get(FIELD_VALUE).intValue());
        String act = actNode.toString().replaceAll("\\s+", "");
        assertEquals("{\"value\":-13}", act);
    }

    @Test
    public void realAsJsonNode() {
        JsonNode actNode = SilaBasicTypeUtils.realAsJsonNode(3.141593);
        assertEquals(3.141593, actNode.get(FIELD_VALUE).doubleValue());
        actNode = SilaBasicTypeUtils.realAsJsonNode(-13.5);
        assertEquals(-13.5, actNode.get(FIELD_VALUE).doubleValue());
        String act = actNode.toString().replaceAll("\\s+", "");
        assertEquals("{\"value\":-13.5}", act);
    }

    @Test
    public void stringAsJsonNode() {
        JsonNode actNode = SilaBasicTypeUtils.stringAsJsonNode("Lorem ipsum dolor sit amet");
        assertEquals("Lorem ipsum dolor sit amet", actNode.get(FIELD_VALUE).asText());
        final String unwantedStr = "\"äöü\\n\\tß\\\\0xf00bar:\\\";€¶@æ²³\\b01101®testâ€™\\\\u0048{[]}\\u2212\"¯\\_(ツ)_/¯;";
        actNode = SilaBasicTypeUtils.stringAsJsonNode(unwantedStr);
        assertEquals(unwantedStr, actNode.get(FIELD_VALUE).asText());

        try {
            String exp = JsonFormat.printer().print(SiLAString.from(unwantedStr)).replaceAll("\\s+", "");
            String act = actNode.toString().replaceAll("\\s+", "");
            assertEquals(exp, act);
        } catch (final InvalidProtocolBufferException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void timeAsJsonNode() {
        final OffsetTime time = OffsetTime.of(9, 8, 7, 0, ZoneOffset.ofHoursMinutes(3, 45));
        JsonNode actNode = SilaBasicTypeUtils.timeAsJsonNode(time);
        assertEquals(time.getSecond(), actNode.get(FIELD_SECOND).intValue());
        assertEquals(time.getMinute(), actNode.get(FIELD_MINUTE).intValue());
        assertEquals(time.getHour(), actNode.get(FIELD_HOUR).intValue());
        String act = actNode.toString().replaceAll("\\s+", "");
        assertEquals("{\"second\":7,\"minute\":8,\"hour\":9,\"timezone\":{\"hours\":3,\"minutes\":45}}", act);

        try {
            String exp = JsonFormat.printer().print(SiLATime.from(time)).replaceAll("\\s+", "");
            assertEquals(exp, act);
        } catch (final InvalidProtocolBufferException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void timestampAsJsonNode() {
        final OffsetDateTime ts = OffsetDateTime.of(2020, 11, 10, 9, 8, 7, 0, ZoneOffset.ofHoursMinutes(3, 45));
        JsonNode actNode = SilaBasicTypeUtils.timestampAsJsonNode(ts);
        assertEquals(ts.getSecond(), actNode.get(FIELD_SECOND).intValue());
        assertEquals(ts.getMinute(), actNode.get(FIELD_MINUTE).intValue());
        assertEquals(ts.getHour(), actNode.get(FIELD_HOUR).intValue());
        assertEquals(ts.getDayOfMonth(), actNode.get(FIELD_DAY).intValue());
        assertEquals(ts.getMonthValue(), actNode.get(FIELD_MONTH).intValue());
        assertEquals(ts.getYear(), actNode.get(FIELD_YEAR).intValue());
        String act = actNode.toString().replaceAll("\\s+", "");
        assertEquals("{\"second\":7,\"minute\":8,\"hour\":9,\"day\":10,\"month\":11,\"year\":2020,"
                + "\"timezone\":{\"hours\":3,\"minutes\":45}}", act);

        try {
            String exp = JsonFormat.printer().print(SiLATimestamp.from(ts)).replaceAll("\\s+", "");
            assertEquals(exp, act);
        } catch (final InvalidProtocolBufferException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void timezoneAsJsonNode() {
        final ZoneOffset zone = ZoneOffset.ofHoursMinutes(12, 15);
        JsonNode actNode = SilaBasicTypeUtils.timezoneAsJsonNode(zone);
        assertEquals(zone.getTotalSeconds() / 3600, actNode.get(FIELD_TZ_HOURS).intValue());
        assertEquals((zone.getTotalSeconds() / 60) % 60, actNode.get(FIELD_TZ_MINUTES).intValue());
        String act = actNode.toString().replaceAll("\\s+", "");
        assertEquals("{\"hours\":12,\"minutes\":15}", act);
        try {
            String exp = JsonFormat.printer().print(SiLATimeZone.from(zone)).replaceAll("\\s+", "");
            assertEquals(exp, act);
        } catch (final InvalidProtocolBufferException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void dateFromJsonNode() {
        ObjectNode node = jsonMapper.createObjectNode();
        node.put(FIELD_YEAR, 2020);
        node.put(FIELD_MONTH, 11);
        node.put(FIELD_DAY, 10);

        final LocalDate exp = LocalDate.of(2020, 11, 10);
        LocalDate act = SilaBasicTypeUtils.dateFromJsonNode(node);
        assertEquals(exp, act);
    }

    @Test
    public void timeFromJsonNode() throws JsonProcessingException {
        JsonNode node = jsonMapper.readTree("{\"second\":7,\"minute\":8,\"hour\":9,\""
                + "timezone\":{\"hours\":3,\"minutes\":45}}");

        OffsetTime exp = OffsetTime.of(9, 8, 7, 0, ZoneOffset.ofHoursMinutes(3, 45));
        OffsetTime act = SilaBasicTypeUtils.timeFromJsonNode(node);
        assertEquals(exp, act);

        node = jsonMapper.readTree("{\"second\":1,\"minute\":2,\"hour\":3}");
        exp = OffsetTime.of(3, 2, 1, 0, ZoneOffset.UTC);
        act = SilaBasicTypeUtils.timeFromJsonNode(node);
        assertEquals(exp, act);
    }

    @Test
    public void timestampFromJsonNode() throws JsonProcessingException {
        JsonNode node = jsonMapper.readTree("{\"second\":7,\"minute\":8,\"hour\":9,\"day\":10,"
                + "\"month\":11,\"year\":2020,\"timezone\":{\"hours\":3,\"minutes\":45}}");

        final OffsetDateTime exp = OffsetDateTime.of(2020, 11, 10, 9, 8, 7, 0, ZoneOffset.ofHoursMinutes(3, 45));
        OffsetDateTime act = SilaBasicTypeUtils.timestampFromJsonNode(node);
        assertEquals(exp, act);
    }

    @Test
    public void zoneOffsetFromJsonNode() throws JsonProcessingException {
        JsonNode node = jsonMapper.readTree("{\"hours\":12,\"minutes\":45}");
        ZoneOffset exp = ZoneOffset.ofHoursMinutes(12, 45);
        ZoneOffset act = SilaBasicTypeUtils.zoneOffsetFromJsonNode(node);
        assertEquals(exp, act);
        node = jsonMapper.readTree("{\"hours\":2}");
        exp = ZoneOffset.ofHours(2);
        act = SilaBasicTypeUtils.zoneOffsetFromJsonNode(node);
        assertEquals(exp, act);
        node = jsonMapper.readTree("{\"hours\":11,\"minutes\":12,\"seconds\":13}");
        exp = ZoneOffset.ofHoursMinutesSeconds(11, 12, 0);
        act = SilaBasicTypeUtils.zoneOffsetFromJsonNode(node);
        assertEquals(exp, act);
    }
}
