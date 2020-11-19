package de.fau.clients.orchestrator.nodes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fau.clients.orchestrator.utils.DateTimeParser;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Base64.Encoder;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JViewport;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import sila_java.library.core.models.BasicType;

public class BasicNodeFactoryTest {

    static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void create() {
        try {
            BasicNodeFactory.create(null, false);
            fail("NullPointerException was expected but not thrown.");
        } catch (NullPointerException ex) {
        } catch (Exception ex) {
            fail("Only a NullPointerException was expected but got:" + ex.getMessage());
        }

        try {
            BasicNodeFactory.create(BasicType.ANY, false);
            fail("IllegalArgumentException was expected but not thrown.");
        } catch (IllegalArgumentException ex) {
        } catch (Exception ex) {
            fail("Only a IllegalArgumentException was expected but got:" + ex.getMessage());
        }

        // All types, except 'ANY'
        final BasicType[] types = {
            BasicType.BINARY,
            BasicType.BOOLEAN,
            BasicType.DATE,
            BasicType.INTEGER,
            BasicType.REAL,
            BasicType.STRING,
            BasicType.TIME,
            BasicType.TIMESTAMP
        };

        for (final BasicType type : types) {
            final BasicNode act = BasicNodeFactory.create(type, true);
            assertNotNull(act);
            assertEquals(type, act.getType());
            assertNotNull(act.getComponent());
            assertNotNull(act.getValue());
            assertTrue(act.toJsonString().matches("^\\{\".*\\}$"));
        }

        // check default values
        BasicNode actual = BasicNodeFactory.create(BasicType.BINARY, false);
        final byte[] emptyBytes = {};
        assertArrayEquals(emptyBytes, (byte[]) actual.getValue());
        actual = BasicNodeFactory.create(BasicType.BOOLEAN, false);
        assertEquals(false, actual.getValue());
        actual = BasicNodeFactory.create(BasicType.DATE, false);
        String actStr = actual.getValue().toString();
        assertTrue(actStr.matches("\\d{4}-\\d{2}-\\d{2}"), actStr);
        actual = BasicNodeFactory.create(BasicType.INTEGER, false);
        assertEquals(0l, actual.getValue());
        actual = BasicNodeFactory.create(BasicType.REAL, false);
        assertEquals(0.0, actual.getValue());
        actual = BasicNodeFactory.create(BasicType.STRING, false);
        assertEquals("", actual.getValue());
        actual = BasicNodeFactory.create(BasicType.TIME, false);
        actStr = actual.getValue().toString();
        assertTrue(actStr.matches("\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z"), actStr);
        actual = BasicNodeFactory.create(BasicType.TIMESTAMP, false);
        actStr = actual.getValue().toString();
        assertTrue(actStr.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z"), actStr);
    }

    @Test
    public void createFromJsonCheckOnNullPtr() {
        for (final BasicType type : BasicType.values()) {
            try {
                BasicNodeFactory.createFromJson(type, null, true);
                fail("NullPointerException was expected but not thrown.");
            } catch (NullPointerException ex) {
            } catch (Exception ex) {
                fail("Only a NullPointerException was expected but got:" + ex.getMessage());
            }
        }
    }

    @Test
    public void createAnyFromJson() throws JsonProcessingException {
        String jsonStr = "{\"Any\":{\"type\":\""
                + "<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\" standalone=\\\"yes\\\"?>\\n"
                + "<dataTypeType>\\n"
                + "    <Basic>String</Basic>\\n"
                + "</dataTypeType>\\n\","
                + "\"payload\":\"CgRTaUxB\"}}";
        JsonNode jsonNode = mapper.readTree(jsonStr);
        boolean isEditable = false;
        BasicNode actual = (BasicNode) BasicNodeFactory.createFromJson(BasicType.ANY, jsonNode.get("Any"), isEditable);
        assertEquals(BasicType.STRING, actual.getType());
        assertEquals(false, actual.isEditable);
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals("SiLA", actual.getValue());
        assertEquals("{\"value\":\"SiLA\"}", actual.toJsonString());

        jsonStr = "{\"Any\":{\"type\":\""
                + "<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\" standalone=\\\"yes\\\"?>\\n"
                + "<DataType>\\n"
                + "    <Basic>String</Basic>\\n"
                + "</DataType>\\n\","
                + "\"payload\":\"CgR0ZXN0\"}}";
        jsonNode = mapper.readTree(jsonStr);
        actual = (BasicNode) BasicNodeFactory.createFromJson(BasicType.ANY, jsonNode.get("Any"), isEditable);
        assertEquals(BasicType.STRING, actual.getType());
        assertEquals(false, actual.isEditable);
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals("test", actual.getValue());
        assertEquals("{\"value\":\"test\"}", actual.toJsonString());

        jsonStr = "{\"Any\":{\"type\":\""
                + "<dataTypeType>\\n"
                + "    <Basic>Integer</Basic>\\n"
                + "</dataTypeType>\\n\","
                + "\"payload\":\"CLkK\"}}";
        jsonNode = mapper.readTree(jsonStr);
        actual = (BasicNode) BasicNodeFactory.createFromJson(BasicType.ANY, jsonNode.get("Any"), isEditable);
        assertEquals(BasicType.INTEGER, actual.getType());
        assertEquals(false, actual.isEditable);
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(1337l, actual.getValue());
        assertEquals("{\"value\":\"1337\"}", actual.toJsonString());

        jsonStr = "{\"Any\":{\"type\":\""
                + "<dataTypeType>\\n"
                + "    <Basic>Integer</Basic>\\n"
                + "</dataTypeType>\\n\","
                + "\"payload\":\"CIkG\"}}";
        jsonNode = mapper.readTree(jsonStr);
        actual = (BasicNode) BasicNodeFactory.createFromJson(BasicType.ANY, jsonNode.get("Any"), isEditable);
        assertEquals(BasicType.INTEGER, actual.getType());
        assertEquals(false, actual.isEditable);
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(777l, actual.getValue());
        assertEquals("{\"value\":\"777\"}", actual.toJsonString());

        isEditable = true;
        actual = (BasicNode) BasicNodeFactory.createFromJson(BasicType.ANY, jsonNode.get("Any"), isEditable);
        assertEquals(BasicType.INTEGER, actual.getType());
        assertEquals(true, actual.isEditable);
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JSpinner) actual.getComponent()).isEnabled());

        jsonStr = "{\"Any\":{\"type\":\""
                + "<dataTypeType>"
                + "  <UnknownTag>Integer</UnknownTag>"
                + "</dataTypeType>\","
                + "\"payload\":\"CIkG\"}}";
        jsonNode = mapper.readTree(jsonStr);

        actual = (BasicNode) BasicNodeFactory.createFromJson(BasicType.ANY, jsonNode.get("Any"), isEditable);
        assertEquals(BasicType.ANY, actual.getType());
        assertEquals(false, actual.isEditable);
        assertEquals(JLabel.class, actual.getComponent().getClass());
        assertTrue(((JLabel) actual.getComponent()).getText().startsWith("Error: Unrecognized"));
        assertEquals("", actual.getValue());
        assertEquals("{\"value\":\"\"}", actual.toJsonString());

        jsonStr = "{\"Any\":{\"type\":\""
                + "<unclosedTag>"
                + "  <Basic>Integer</Basic>"
                + "<unclosedTag>\","
                + "\"payload\":\"CIkG\"}}";
        jsonNode = mapper.readTree(jsonStr);
        actual = (BasicNode) BasicNodeFactory.createFromJson(BasicType.ANY, jsonNode.get("Any"), isEditable);
        assertEquals(BasicType.ANY, actual.getType());
        assertEquals(false, actual.isEditable);
        assertEquals(JLabel.class, actual.getComponent().getClass());
        assertTrue(((JLabel) actual.getComponent()).getText().startsWith("Error: Unexpected EOF"));
        assertEquals("", actual.getValue());
        assertEquals("{\"value\":\"\"}", actual.toJsonString());

        jsonStr = "{\"Any\":{\"type\":\""
                + "<dataTypeType>"
                + "  <Basic>UnknownBasicType</Basic>"
                + "</dataTypeType>\","
                + "\"payload\":\"CIkG\"}}";
        jsonNode = mapper.readTree(jsonStr);
        actual = (BasicNode) BasicNodeFactory.createFromJson(BasicType.ANY, jsonNode.get("Any"), isEditable);
        assertEquals(BasicType.ANY, actual.getType());
        assertEquals(false, actual.isEditable);
        assertEquals(JLabel.class, actual.getComponent().getClass());
        assertTrue(((JLabel) actual.getComponent()).getText().startsWith("Error: Cannot"));
        assertEquals("", actual.getValue());
        assertEquals("{\"value\":\"\"}", actual.toJsonString());
    }

    @Test
    public void createBinaryType() {
        final Encoder enc = Base64.getEncoder();
        String testData = "test";
        byte[] payload = testData.getBytes(StandardCharsets.UTF_8);
        boolean isEditable = false;
        BasicNode actual = BasicNodeFactory.createBinaryType(payload, isEditable);
        assertEquals(BasicType.BINARY, actual.getType());
        assertEquals(JScrollPane.class, actual.getComponent().getClass());
        assertEquals(JEditorPane.class, ((JViewport) actual.getComponent().getComponent(0)).getComponent(0).getClass());
        assertEquals(isEditable, ((JEditorPane) ((JViewport) actual.getComponent().getComponent(0)).getComponent(0)).isEnabled());
        assertEquals(testData, ((JEditorPane) ((JViewport) actual.getComponent().getComponent(0)).getComponent(0)).getText());
        assertArrayEquals(payload, (byte[]) actual.getValue());

        isEditable = true;
        actual = BasicNodeFactory.createBinaryType(payload, isEditable);
        assertEquals(BasicType.BINARY, actual.getType());
        assertEquals(isEditable, ((JEditorPane) ((JViewport) actual.getComponent().getComponent(0)).getComponent(0)).isEnabled());
        assertEquals(testData, ((JEditorPane) ((JViewport) actual.getComponent().getComponent(0)).getComponent(0)).getText());
        assertArrayEquals(payload, (byte[]) actual.getValue());

        testData = "äöü\n\tß\\0xf00bar:<></>\";€¶@æ²³\b01101®testâ€™\\u0048{[]}Test\u2212";
        payload = testData.getBytes();
        isEditable = false;
        actual = BasicNodeFactory.createBinaryType(payload, isEditable);
        assertEquals(BasicType.BINARY, actual.getType());
        assertEquals(JScrollPane.class, actual.getComponent().getClass());
        assertEquals(JEditorPane.class, ((JViewport) actual.getComponent().getComponent(0)).getComponent(0).getClass());
        assertEquals(isEditable, ((JEditorPane) ((JViewport) actual.getComponent().getComponent(0)).getComponent(0)).isEnabled());
        assertEquals(testData, ((JEditorPane) ((JViewport) actual.getComponent().getComponent(0)).getComponent(0)).getText());
        assertArrayEquals(payload, (byte[]) actual.getValue());

        isEditable = true;
        actual = BasicNodeFactory.createBinaryType(payload, isEditable);
        assertEquals(BasicType.BINARY, actual.getType());
        assertEquals(isEditable, ((JEditorPane) ((JViewport) actual.getComponent().getComponent(0)).getComponent(0)).isEnabled());
        assertEquals(testData, ((JEditorPane) ((JViewport) actual.getComponent().getComponent(0)).getComponent(0)).getText());
        assertArrayEquals(payload, (byte[]) actual.getValue());

        payload = new byte[]{(byte) 0x00, (byte) 0xd0, (byte) 0x12, (byte) 0xff, (byte) 0xfd};
        isEditable = false;
        actual = BasicNodeFactory.createBinaryType(payload, isEditable);
        assertEquals(BasicType.BINARY, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEnabled());
        assertEquals("SHA-256: cbf2a37578fc1ec0eb92cf57a406f126514036a67aef4df071e2116876ebfb4a",
                ((JTextField) actual.getComponent()).getText());
        assertEquals(payload, (byte[]) actual.getValue());

        isEditable = true;
        actual = BasicNodeFactory.createBinaryType(payload, isEditable);
        assertEquals(BasicType.BINARY, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(false, ((JTextField) actual.getComponent()).isEnabled());
        assertEquals("SHA-256: cbf2a37578fc1ec0eb92cf57a406f126514036a67aef4df071e2116876ebfb4a",
                ((JTextField) actual.getComponent()).getText());
        assertEquals(payload, (byte[]) actual.getValue());
    }

    @Test
    public void createBinary() {
        boolean isEditable = false;
        BasicNode actual = BasicNodeFactory.create(BasicType.BINARY, isEditable);
        final byte[] expected = {};
        assertEquals(BasicType.BINARY, actual.getType());
        assertEquals(JScrollPane.class, actual.getComponent().getClass());
        assertEquals(JEditorPane.class, ((JViewport) actual.getComponent().getComponent(0)).getComponent(0).getClass());
        assertEquals(isEditable, ((JEditorPane) ((JViewport) actual.getComponent().getComponent(0)).getComponent(0)).isEnabled());
        assertEquals("", ((JEditorPane) ((JViewport) actual.getComponent().getComponent(0)).getComponent(0)).getText());
        assertArrayEquals(expected, (byte[]) actual.getValue());

        isEditable = true;
        actual = BasicNodeFactory.create(BasicType.BINARY, isEditable);
        assertEquals(BasicType.BINARY, actual.getType());
        assertEquals(JScrollPane.class, actual.getComponent().getClass());
        assertEquals(JEditorPane.class, ((JViewport) actual.getComponent().getComponent(0)).getComponent(0).getClass());
        assertEquals(isEditable, ((JEditorPane) ((JViewport) actual.getComponent().getComponent(0)).getComponent(0)).isEnabled());
        assertEquals("", ((JEditorPane) ((JViewport) actual.getComponent().getComponent(0)).getComponent(0)).getText());
        assertArrayEquals(expected, (byte[]) actual.getValue());
    }

    @Test
    public void createBinaryFromJson() throws JsonProcessingException {
        String jsonStr = "{\"value\":\"dGVzdA==\"}";
        JsonNode jsonNode = mapper.readTree(jsonStr);
        boolean isEditable = false;
        final byte[] expected = "test".getBytes(StandardCharsets.UTF_8);
        BasicNode actual = (BasicNode) BasicNodeFactory.createFromJson(BasicType.BINARY, jsonNode, isEditable);
        assertEquals(BasicType.BINARY, actual.getType());
        assertEquals(JScrollPane.class, actual.getComponent().getClass());
        assertEquals(JEditorPane.class, ((JViewport) actual.getComponent().getComponent(0)).getComponent(0).getClass());
        assertEquals(isEditable, ((JEditorPane) ((JViewport) actual.getComponent().getComponent(0)).getComponent(0)).isEnabled());
        assertEquals("test", ((JEditorPane) ((JViewport) actual.getComponent().getComponent(0)).getComponent(0)).getText());
        assertArrayEquals(expected, (byte[]) actual.getValue());
        assertEquals(jsonStr, actual.toJson().toString());

        isEditable = true;
        actual = (BasicNode) BasicNodeFactory.createFromJson(BasicType.BINARY, jsonNode, isEditable);
        assertEquals(BasicType.BINARY, actual.getType());
        assertEquals(JScrollPane.class, actual.getComponent().getClass());
        assertEquals(JEditorPane.class, ((JViewport) actual.getComponent().getComponent(0)).getComponent(0).getClass());
        assertEquals(isEditable, ((JEditorPane) ((JViewport) actual.getComponent().getComponent(0)).getComponent(0)).isEnabled());
        assertEquals("test", ((JEditorPane) ((JViewport) actual.getComponent().getComponent(0)).getComponent(0)).getText());
    }

    @Test
    public void createBooleanType() {
        boolean isEditable = false;
        BasicNode actual = BasicNodeFactory.createBooleanType(false, isEditable);
        assertEquals(BasicType.BOOLEAN, actual.getType());
        assertEquals(JCheckBox.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JCheckBox) actual.getComponent()).isEnabled());
        assertEquals(false, actual.getValue());
        assertEquals("{\"value\":\"false\"}", actual.toJsonString());
        actual = BasicNodeFactory.createBooleanType(true, isEditable);
        assertEquals(BasicType.BOOLEAN, actual.getType());
        assertEquals(JCheckBox.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JCheckBox) actual.getComponent()).isEnabled());
        assertEquals(true, actual.getValue());
        assertEquals("{\"value\":\"true\"}", actual.toJsonString());

        isEditable = true;
        actual = BasicNodeFactory.createBooleanType(false, isEditable);
        assertEquals(BasicType.BOOLEAN, actual.getType());
        assertEquals(JCheckBox.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JCheckBox) actual.getComponent()).isEnabled());
        assertEquals(false, actual.getValue());
        actual = BasicNodeFactory.createBooleanType(true, isEditable);
        assertEquals(BasicType.BOOLEAN, actual.getType());
        assertEquals(JCheckBox.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JCheckBox) actual.getComponent()).isEnabled());
        assertEquals(true, actual.getValue());
    }

    @Test
    public void createBoolean() {
        String jsonStr = "{\"value\":\"false\"}";
        boolean isEditable = false;
        BasicNode actual = BasicNodeFactory.create(BasicType.BOOLEAN, isEditable);
        assertEquals(BasicType.BOOLEAN, actual.getType());
        assertEquals(JCheckBox.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JCheckBox) actual.getComponent()).isEnabled());
        assertEquals(false, actual.getValue());
        assertEquals(jsonStr, actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.create(BasicType.BOOLEAN, isEditable);
        assertEquals(BasicType.BOOLEAN, actual.getType());
        assertEquals(JCheckBox.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JCheckBox) actual.getComponent()).isEnabled());
        assertEquals(false, actual.getValue());
        assertEquals(jsonStr, actual.toJsonString());
    }

    @Test
    public void createBooleaFromJson() throws JsonProcessingException {
        String jsonStr = "{\"value\":\"false\"}";
        JsonNode jsonNode = mapper.readTree(jsonStr);
        boolean isEditable = false;
        BasicNode actual = (BasicNode) BasicNodeFactory.createFromJson(BasicType.BOOLEAN, jsonNode, isEditable);
        assertEquals(BasicType.BOOLEAN, actual.getType());
        assertEquals(JCheckBox.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JCheckBox) actual.getComponent()).isEnabled());
        assertEquals(false, actual.getValue());
        assertEquals(jsonStr, actual.toJsonString());
        isEditable = true;
        actual = (BasicNode) BasicNodeFactory.createFromJson(BasicType.BOOLEAN, jsonNode, isEditable);
        assertEquals(BasicType.BOOLEAN, actual.getType());
        assertEquals(JCheckBox.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JCheckBox) actual.getComponent()).isEnabled());
        assertEquals(false, actual.getValue());
        assertEquals(jsonStr, actual.toJsonString());

        jsonStr = "{\"value\":\"true\"}";
        jsonNode = mapper.readTree(jsonStr);
        isEditable = false;
        actual = (BasicNode) BasicNodeFactory.createFromJson(BasicType.BOOLEAN, jsonNode, isEditable);
        assertEquals(BasicType.BOOLEAN, actual.getType());
        assertEquals(JCheckBox.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JCheckBox) actual.getComponent()).isEnabled());
        assertEquals(true, actual.getValue());
        assertEquals(jsonStr, actual.toJsonString());
        isEditable = true;
        actual = (BasicNode) BasicNodeFactory.createFromJson(BasicType.BOOLEAN, jsonNode, isEditable);
        assertEquals(BasicType.BOOLEAN, actual.getType());
        assertEquals(JCheckBox.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JCheckBox) actual.getComponent()).isEnabled());
        assertEquals(true, actual.getValue());
        assertEquals(jsonStr, actual.toJsonString());
    }

    @Test
    public void createDateType() {
        boolean isEditable = false;
        final LocalDate currDate = LocalDate.now();
        BasicNode actual = BasicNodeFactory.createDateType(currDate, isEditable);
        assertEquals(BasicType.DATE, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(currDate, actual.getValue());
        assertTrue(actual.toJsonString().matches("\\{\"day\":\\d{1,2},\"month\":\\d{1,2},\"year\":\\d{4},"
                + "?\"timezone\":\\{\"hours\":0}\\}"), actual.toJsonString());

        isEditable = true;
        actual = BasicNodeFactory.createDateType(currDate, isEditable);
        assertEquals(BasicType.DATE, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JSpinner) actual.getComponent()).isEnabled());
        assertEquals(currDate, actual.getValue());
        assertTrue(actual.toJsonString().matches("\\{\"day\":\\d{1,2},\"month\":\\d{1,2},\"year\":\\d{4},"
                + "?\"timezone\":\\{\"hours\":0}\\}"), actual.toJsonString());
    }

    @Test
    public void createDate() {
        boolean isEditable = false;
        BasicNode actual = BasicNodeFactory.create(BasicType.DATE, isEditable);
        assertEquals(BasicType.DATE, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        String actStr = actual.getValue().toString();
        assertTrue(actStr.matches("\\d{4}-\\d{2}-\\d{2}"), actStr);
        assertTrue(actual.toJsonString().matches("\\{\"day\":\\d{1,2},\"month\":\\d{1,2},\"year\":\\d{4},"
                + "?\"timezone\":\\{\"hours\":0}\\}"), actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.create(BasicType.DATE, isEditable);
        assertEquals(BasicType.DATE, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        actStr = actual.getValue().toString();
        assertTrue(actStr.matches("\\d{4}-\\d{2}-\\d{2}"), actStr);
        assertTrue(actual.toJsonString().matches("\\{\"day\":\\d{1,2},\"month\":\\d{1,2},\"year\":\\d{4},"
                + "?\"timezone\":\\{\"hours\":0}\\}"), actual.toJsonString());
    }

    @Test
    public void createDateFromJson() throws JsonProcessingException {
        String inputValue = "2020-06-29";
        String inputJsonStr = "{\"value\":\"" + inputValue + "\"}";
        JsonNode jsonNode = mapper.readTree(inputJsonStr);
        boolean isEditable = false;
        BasicNode actual = (BasicNode) BasicNodeFactory.createFromJson(BasicType.DATE, jsonNode, isEditable);
        assertEquals(BasicType.DATE, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(DateTimeParser.parseIsoDate(inputValue), actual.getValue());
        assertTrue(actual.toJsonString().matches("\\{\"day\":29,\"month\":6,\"year\":2020,"
                + "\"timezone\":\\{\"hours\":0}\\}"), actual.toJsonString());
        isEditable = true;
        actual = (BasicNode) BasicNodeFactory.createFromJson(BasicType.DATE, jsonNode, isEditable);
        assertEquals(BasicType.DATE, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertEquals(DateTimeParser.parseIsoDate(inputValue), actual.getValue());
        assertTrue(actual.toJsonString().matches("\\{\"day\":29,\"month\":6,\"year\":2020,"
                + "\"timezone\":\\{\"hours\":0}\\}"), actual.toJsonString());
    }

    @Test
    public void createIntegerType() {
        boolean isEditable = false;
        BasicNode actual = BasicNodeFactory.createIntegerType(0, isEditable);
        assertEquals(BasicType.INTEGER, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(0l, actual.getValue());
        assertEquals("{\"value\":\"0\"}", actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.createIntegerType(4711, isEditable);
        assertEquals(BasicType.INTEGER, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertEquals(4711l, actual.getValue());
        assertEquals("{\"value\":\"4711\"}", actual.toJsonString());
        actual = BasicNodeFactory.createIntegerType(-42, isEditable);
        assertEquals(BasicType.INTEGER, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertEquals(-42l, actual.getValue());
        assertEquals("{\"value\":\"-42\"}", actual.toJsonString());
    }

    @Test
    public void createInteger() {
        long expValue = 0;
        String expJsonStr = "{\"value\":\"" + expValue + "\"}";
        boolean isEditable = false;
        BasicNode actual = BasicNodeFactory.create(BasicType.INTEGER, isEditable);
        assertEquals(BasicType.INTEGER, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.create(BasicType.INTEGER, isEditable);
        assertEquals(BasicType.INTEGER, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
    }

    @Test
    public void createIntegerFromJson() throws JsonProcessingException {
        long expValue = 42;
        String expJsonStr = "{\"value\":\"" + expValue + "\"}";
        JsonNode jsonNode = mapper.readTree(expJsonStr);
        boolean isEditable = false;
        BasicNode actual = (BasicNode) BasicNodeFactory.createFromJson(BasicType.INTEGER, jsonNode, isEditable);
        assertEquals(BasicType.INTEGER, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
        isEditable = true;
        actual = (BasicNode) BasicNodeFactory.createFromJson(BasicType.INTEGER, jsonNode, isEditable);
        assertEquals(BasicType.INTEGER, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
    }

    @Test
    public void createRealType() {
        boolean isEditable = false;
        BasicNode actual = BasicNodeFactory.createRealType(0, isEditable);
        assertEquals(BasicType.REAL, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(0.0, actual.getValue());
        assertEquals("{\"value\":\"0.0\"}", actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.createRealType(3.141592, isEditable);
        assertEquals(BasicType.REAL, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JSpinner) actual.getComponent()).isEnabled());
        assertEquals(3.141592, actual.getValue());
        actual = BasicNodeFactory.createRealType(-42, isEditable);
        assertEquals(BasicType.REAL, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JSpinner) actual.getComponent()).isEnabled());
        assertEquals(-42.0, actual.getValue());
    }

    @Test
    public void createReal() {
        double expValue = 0.0;
        String expJsonStr = "{\"value\":\"" + expValue + "\"}";
        boolean isEditable = false;
        BasicNode actual = BasicNodeFactory.create(BasicType.REAL, isEditable);
        assertEquals(BasicType.REAL, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.create(BasicType.REAL, isEditable);
        assertEquals(BasicType.REAL, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
    }

    @Test
    public void createRealFromJson() throws JsonProcessingException {
        double expValue = 42.123;
        String expJsonStr = "{\"value\":\"" + expValue + "\"}";
        JsonNode jsonNode = mapper.readTree(expJsonStr);
        boolean isEditable = false;
        BasicNode actual = (BasicNode) BasicNodeFactory.createFromJson(BasicType.REAL, jsonNode, isEditable);
        assertEquals(BasicType.REAL, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
        isEditable = true;
        actual = (BasicNode) BasicNodeFactory.createFromJson(BasicType.REAL, jsonNode, isEditable);
        assertEquals(BasicType.REAL, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
    }

    @Test
    public void createStringType() {
        boolean isEditable = false;
        BasicNode actual = BasicNodeFactory.createStringType("abcdefg", isEditable);
        assertEquals(BasicType.STRING, actual.getType());
        assertEquals(JTextField.class,
                actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals("abcdefg", actual.getValue());
        assertEquals("{\"value\":\"abcdefg\"}", actual.toJsonString());
        isEditable = true;
        final String loremIpsum = "Lorem ipsum dolor sit amet";
        actual = BasicNodeFactory.createStringType(loremIpsum, isEditable);
        assertEquals(BasicType.STRING, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(loremIpsum, actual.getValue());
        final String unwantedStr = "\"äöü\\n\\tß\\\\0xf00bar:<></>\\\";€¶@æ²³\\b01101®testâ€™\\\\u0048{[]}Test\\u2212\";";
        actual = BasicNodeFactory.createStringType(unwantedStr, isEditable);
        assertEquals(BasicType.STRING, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(unwantedStr, actual.getValue());

        try {
            BasicNodeFactory.createStringType(null, false);
            fail("NullPointerException was expected but not thrown.");
        } catch (NullPointerException ex) {
        } catch (Exception ex) {
            fail("Only a NullPointerException was expected but got:" + ex.getMessage());
        }
    }

    @Test
    public void createString() {
        String expValue = "";
        String expJsonStr = "{\"value\":\"" + expValue + "\"}";
        boolean isEditable = false;
        BasicNode actual = BasicNodeFactory.create(BasicType.STRING, isEditable);
        assertEquals(BasicType.STRING, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.create(BasicType.STRING, isEditable);
        assertEquals(BasicType.STRING, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
    }

    @Test
    public void createStringFromJson() throws JsonProcessingException {
        String expValue = "Lorem ipsum dolor sit amet";
        String expJsonStr = "{\"value\":\"" + expValue + "\"}";
        JsonNode jsonNode = mapper.readTree(expJsonStr);
        boolean isEditable = false;
        BasicNode actual = (BasicNode) BasicNodeFactory.createFromJson(BasicType.STRING, jsonNode, isEditable);
        assertEquals(BasicType.STRING, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
        isEditable = true;
        actual = (BasicNode) BasicNodeFactory.createFromJson(BasicType.STRING, jsonNode, isEditable);
        assertEquals(BasicType.STRING, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
    }

    @Test
    public void createTimeType() {
        boolean isEditable = false;
        final OffsetTime currentTime = OffsetTime.now();
        BasicNode actual = BasicNodeFactory.createTimeType(currentTime, isEditable);
        String actStr = actual.getValue().toString();
        assertEquals(BasicType.TIME, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(currentTime.withOffsetSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS).toString(), actStr);
        assertTrue(actStr.matches("\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z"), actStr);
        assertTrue(actual.toJsonString().matches("\\{\"second\":\\d{1,2},\"minute\":\\d{1,2},\"hour\":\\d{1,2},"
                + "?\"timezone\":\\{\"hours\":0\\}\\}"), actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.createTimeType(currentTime, isEditable);
        actStr = actual.getValue().toString();
        assertEquals(BasicType.TIME, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JSpinner) actual.getComponent()).isEnabled());
        assertTrue(actStr.matches("\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z"), actStr);
    }

    @Test
    public void createTime() throws JsonProcessingException {
        boolean isEditable = false;
        BasicNode actual = BasicNodeFactory.create(BasicType.TIME, isEditable);
        assertEquals(BasicType.TIME, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        String actStr = actual.getValue().toString();
        assertTrue(actStr.matches("\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z"), actStr);
        assertTrue(actual.toJsonString().matches("\\{\"second\":\\d{1,2},\"minute\":\\d{1,2},\"hour\":\\d{1,2},"
                + "?\"timezone\":\\{\"hours\":0}\\}"), actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.create(BasicType.TIME, isEditable);

        assertEquals(BasicType.TIME, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        actStr = actual.getValue().toString();
        assertTrue(actStr.matches("\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z"), actStr);
        assertTrue(actual.toJsonString().matches("\\{\"second\":\\d{1,2},\"minute\":\\d{1,2},\"hour\":\\d{1,2},"
                + "?\"timezone\":\\{\"hours\":0}\\}"), actual.toJsonString());
    }

    @Test
    public void createTimeFromJson() throws JsonProcessingException {
        OffsetTime expValue = OffsetTime.of(20, 15, 10, 123 * 1000000, ZoneOffset.UTC);
        String inputJsonStr = "{\"value\":\"20:15:10.123Z\"}";
        JsonNode jsonNode = mapper.readTree(inputJsonStr);
        boolean isEditable = false;
        BasicNode actual = (BasicNode) BasicNodeFactory.createFromJson(BasicType.TIME, jsonNode, isEditable);
        assertEquals(BasicType.TIME, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(expValue, actual.getValue());
        String actStr = actual.getValue().toString();
        assertTrue(actStr.matches("20:15:10.123Z"), actStr);
        assertTrue(actual.toJsonString().matches("\\{\"second\":10,\"minute\":15,\"hour\":20,"
                + "?\"timezone\":\\{\"hours\":0}\\}"), actual.toJsonString());
        isEditable = true;
        actual = (BasicNode) BasicNodeFactory.createFromJson(BasicType.TIME, jsonNode, isEditable);
        assertEquals(BasicType.TIME, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertEquals(expValue, actual.getValue());
    }

    @Test
    public void createTimestampType() {
        boolean isEditable = false;
        final OffsetDateTime currentTimestamp = OffsetDateTime.now();
        BasicNode actual = BasicNodeFactory.createTimestampType(currentTimestamp, isEditable);
        assertEquals(BasicType.TIMESTAMP, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(currentTimestamp.withOffsetSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS), actual.getValue());
        String actStr = actual.getValue().toString();
        assertTrue(actStr.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z"), actStr);
        assertTrue(actual.toJsonString().matches("\\{\"second\":\\d{1,2},\"minute\":\\d{1,2},\"hour\":\\d{1,2},"
                + "\"day\":\\d{1,2},\"month\":\\d{1,2},\"year\":\\d{4},"
                + "?\"timezone\":\\{\"hours\":0}\\}"), actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.createTimestampType(currentTimestamp, isEditable);
        assertEquals(BasicType.TIMESTAMP, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JSpinner) actual.getComponent()).isEnabled());
        assertEquals(currentTimestamp.withOffsetSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS), actual.getValue());
        actStr = actual.getValue().toString();
        assertTrue(actStr.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z"), actStr);
        assertTrue(actual.toJsonString().matches("\\{\"second\":\\d{1,2},\"minute\":\\d{1,2},\"hour\":\\d{1,2},"
                + "\"day\":\\d{1,2},\"month\":\\d{1,2},\"year\":\\d{4},"
                + "?\"timezone\":\\{\"hours\":0}\\}"), actual.toJsonString());
    }

    @Test
    public void createTimestamp() {
        boolean isEditable = false;
        BasicNode actual = BasicNodeFactory.create(BasicType.TIMESTAMP, isEditable);
        String actStr = actual.getValue().toString();
        assertEquals(BasicType.TIMESTAMP, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertTrue(actStr.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z"), actStr);
        assertTrue(actual.toJsonString().matches("\\{\"second\":\\d{1,2},\"minute\":\\d{1,2},\"hour\":\\d{1,2},"
                + "\"day\":\\d{1,2},\"month\":\\d{1,2},\"year\":\\d{4},"
                + "?\"timezone\":\\{\"hours\":0}\\}"), actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.create(BasicType.TIMESTAMP, isEditable);
        actStr = actual.getValue().toString();
        assertEquals(BasicType.TIMESTAMP, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertTrue(actStr.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z"), actStr);
        assertTrue(actual.toJsonString().matches("\\{\"second\":\\d{1,2},\"minute\":\\d{1,2},\"hour\":\\d{1,2},"
                + "\"day\":\\d{1,2},\"month\":\\d{1,2},\"year\":\\d{4},"
                + "?\"timezone\":\\{\"hours\":0}\\}"), actual.toJsonString());
    }

    @Test
    public void createTimestampFromJson() throws JsonProcessingException {
        final String inputValue = "2020-06-29T20:15:10.123Z";
        OffsetDateTime expValue = DateTimeParser.parseIsoDateTime(inputValue);
        String inputJsonStr = "{\"value\":\"" + inputValue + "\"}";
        JsonNode jsonNode = mapper.readTree(inputJsonStr);
        boolean isEditable = false;
        BasicNode actual = (BasicNode) BasicNodeFactory.createFromJson(BasicType.TIMESTAMP, jsonNode, isEditable);
        assertEquals(BasicType.TIMESTAMP, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(expValue, actual.getValue());
        assertTrue(actual.toJsonString().matches("\\{\"second\":10,\"minute\":15,\"hour\":20,"
                + "\"day\":29,\"month\":6,\"year\":2020,"
                + "?\"timezone\":\\{\"hours\":0}\\}"), actual.toJsonString());
        isEditable = true;
        actual = (BasicNode) BasicNodeFactory.createFromJson(BasicType.TIMESTAMP, jsonNode, isEditable);
        assertEquals(BasicType.TIMESTAMP, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertEquals(expValue, actual.getValue());
    }
}
