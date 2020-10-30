package de.fau.clients.orchestrator.nodes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
            assertTrue(act.toJsonString().matches("^\\{\"value\":\".*\"\\}$"));
        }

        // check default values
        BasicNode actual = BasicNodeFactory.create(BasicType.BINARY, false);
        assertEquals("", actual.getValue());
        actual = BasicNodeFactory.create(BasicType.BOOLEAN, false);
        assertEquals("false", actual.getValue());
        actual = BasicNodeFactory.create(BasicType.DATE, false);
        assertTrue(actual.getValue().matches("\\d{4}-\\d{2}-\\d{2}"), actual.getValue());
        actual = BasicNodeFactory.create(BasicType.INTEGER, false);
        assertEquals("0", actual.getValue());
        actual = BasicNodeFactory.create(BasicType.REAL, false);
        assertEquals("0.0", actual.getValue());
        actual = BasicNodeFactory.create(BasicType.STRING, false);
        assertEquals("", actual.getValue());
        actual = BasicNodeFactory.create(BasicType.TIME, false);
        assertTrue(actual.getValue().matches("\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z"), actual.getValue());
        actual = BasicNodeFactory.create(BasicType.TIMESTAMP, false);
        assertTrue(actual.getValue().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z"), actual.getValue());
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
    public void createAnyTypeFromJson() throws JsonProcessingException {
        String jsonStr = "{\"Any\":{\"type\":\""
                + "<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\" standalone=\\\"yes\\\"?>\\n"
                + "<dataTypeType>\\n"
                + "    <Basic>String</Basic>\\n"
                + "</dataTypeType>\\n\","
                + "\"payload\":\"CgRTaUxB\"}}";
        JsonNode jsonNode = mapper.readTree(jsonStr);
        boolean isEditable = false;
        BasicNode actual = BasicNodeFactory.createFromJson(BasicType.ANY, jsonNode.get("Any"), isEditable);
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
        actual = BasicNodeFactory.createFromJson(BasicType.ANY, jsonNode.get("Any"), isEditable);
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
        actual = BasicNodeFactory.createFromJson(BasicType.ANY, jsonNode.get("Any"), isEditable);
        assertEquals(BasicType.INTEGER, actual.getType());
        assertEquals(false, actual.isEditable);
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals("1337", actual.getValue());
        assertEquals("{\"value\":\"1337\"}", actual.toJsonString());

        jsonStr = "{\"Any\":{\"type\":\""
                + "<dataTypeType>\\n"
                + "    <Basic>Integer</Basic>\\n"
                + "</dataTypeType>\\n\","
                + "\"payload\":\"CIkG\"}}";
        jsonNode = mapper.readTree(jsonStr);
        actual = BasicNodeFactory.createFromJson(BasicType.ANY, jsonNode.get("Any"), isEditable);
        assertEquals(BasicType.INTEGER, actual.getType());
        assertEquals(false, actual.isEditable);
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals("777", actual.getValue());
        assertEquals("{\"value\":\"777\"}", actual.toJsonString());

        isEditable = true;
        actual = BasicNodeFactory.createFromJson(BasicType.ANY, jsonNode.get("Any"), isEditable);
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

        actual = BasicNodeFactory.createFromJson(BasicType.ANY, jsonNode.get("Any"), isEditable);
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
        actual = BasicNodeFactory.createFromJson(BasicType.ANY, jsonNode.get("Any"), isEditable);
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
        actual = BasicNodeFactory.createFromJson(BasicType.ANY, jsonNode.get("Any"), isEditable);
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
        byte[] payload = testData.getBytes();
        boolean isEditable = false;
        BasicNode actual = BasicNodeFactory.createBinaryType(payload, isEditable);
        assertEquals(BasicType.BINARY, actual.getType());
        assertEquals(JScrollPane.class, actual.getComponent().getClass());
        assertEquals(JEditorPane.class, ((JViewport) actual.getComponent().getComponent(0)).getComponent(0).getClass());
        assertEquals(isEditable, ((JEditorPane) ((JViewport) actual.getComponent().getComponent(0)).getComponent(0)).isEnabled());
        assertEquals(testData, ((JEditorPane) ((JViewport) actual.getComponent().getComponent(0)).getComponent(0)).getText());
        assertEquals(enc.encodeToString(testData.getBytes()), actual.getValue());

        isEditable = true;
        actual = BasicNodeFactory.createBinaryType(payload, isEditable);
        assertEquals(BasicType.BINARY, actual.getType());
        assertEquals(isEditable, ((JEditorPane) ((JViewport) actual.getComponent().getComponent(0)).getComponent(0)).isEnabled());
        assertEquals(testData, ((JEditorPane) ((JViewport) actual.getComponent().getComponent(0)).getComponent(0)).getText());
        assertEquals(enc.encodeToString(testData.getBytes()), actual.getValue());

        testData = "äöü\n\tß\\0xf00bar:<></>\";€¶@æ²³\b01101®testâ€™\\u0048{[]}Test\u2212";
        payload = testData.getBytes();
        isEditable = false;
        actual = BasicNodeFactory.createBinaryType(payload, isEditable);
        assertEquals(BasicType.BINARY, actual.getType());
        assertEquals(JScrollPane.class, actual.getComponent().getClass());
        assertEquals(JEditorPane.class, ((JViewport) actual.getComponent().getComponent(0)).getComponent(0).getClass());
        assertEquals(isEditable, ((JEditorPane) ((JViewport) actual.getComponent().getComponent(0)).getComponent(0)).isEnabled());
        assertEquals(testData, ((JEditorPane) ((JViewport) actual.getComponent().getComponent(0)).getComponent(0)).getText());
        assertEquals(enc.encodeToString(testData.getBytes()), actual.getValue());

        isEditable = true;
        actual = BasicNodeFactory.createBinaryType(payload, isEditable);
        assertEquals(BasicType.BINARY, actual.getType());
        assertEquals(isEditable, ((JEditorPane) ((JViewport) actual.getComponent().getComponent(0)).getComponent(0)).isEnabled());
        assertEquals(testData, ((JEditorPane) ((JViewport) actual.getComponent().getComponent(0)).getComponent(0)).getText());
        assertEquals(enc.encodeToString(testData.getBytes()), actual.getValue());

        payload = new byte[]{(byte) 0x00, (byte) 0xd0, (byte) 0x12, (byte) 0xff, (byte) 0xfd};
        isEditable = false;
        actual = BasicNodeFactory.createBinaryType(payload, isEditable);
        assertEquals(BasicType.BINARY, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEnabled());
        assertEquals("SHA-256: cbf2a37578fc1ec0eb92cf57a406f126514036a67aef4df071e2116876ebfb4a",
                ((JTextField) actual.getComponent()).getText());
        assertEquals(enc.encodeToString(payload), actual.getValue());

        isEditable = true;
        actual = BasicNodeFactory.createBinaryType(payload, isEditable);
        assertEquals(BasicType.BINARY, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(false, ((JTextField) actual.getComponent()).isEnabled());
        assertEquals("SHA-256: cbf2a37578fc1ec0eb92cf57a406f126514036a67aef4df071e2116876ebfb4a",
                ((JTextField) actual.getComponent()).getText());
        assertEquals(enc.encodeToString(payload), actual.getValue());
    }

    @Test
    public void createBooleanType() {
        boolean isEditable = false;
        BasicNode actual = BasicNodeFactory.createBooleanType(false, isEditable);
        assertEquals(BasicType.BOOLEAN, actual.getType());
        assertEquals(JCheckBox.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JCheckBox) actual.getComponent()).isEnabled());
        assertEquals("false", actual.getValue());
        assertEquals("{\"value\":\"false\"}", actual.toJsonString());
        actual = BasicNodeFactory.createBooleanType(true, isEditable);
        assertEquals(BasicType.BOOLEAN, actual.getType());
        assertEquals(JCheckBox.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JCheckBox) actual.getComponent()).isEnabled());
        assertEquals("true", actual.getValue());
        assertEquals("{\"value\":\"true\"}", actual.toJsonString());

        isEditable = true;
        actual = BasicNodeFactory.createBooleanType(false, isEditable);
        assertEquals(BasicType.BOOLEAN, actual.getType());
        assertEquals(JCheckBox.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JCheckBox) actual.getComponent()).isEnabled());
        assertEquals("false", actual.getValue());
        actual = BasicNodeFactory.createBooleanType(true, isEditable);
        assertEquals(BasicType.BOOLEAN, actual.getType());
        assertEquals(JCheckBox.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JCheckBox) actual.getComponent()).isEnabled());
        assertEquals("true", actual.getValue());
    }

    @Test
    public void createBooleanTypeFromJson() throws JsonProcessingException {
        String jsonStr = "{\"value\":\"false\"}";
        boolean isEditable = false;
        BasicNode actual = BasicNodeFactory.create(BasicType.BOOLEAN, isEditable);
        assertEquals(BasicType.BOOLEAN, actual.getType());
        assertEquals(JCheckBox.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JCheckBox) actual.getComponent()).isEnabled());
        assertEquals("false", actual.getValue());
        assertEquals(jsonStr, actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.create(BasicType.BOOLEAN, isEditable);
        assertEquals(BasicType.BOOLEAN, actual.getType());
        assertEquals(JCheckBox.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JCheckBox) actual.getComponent()).isEnabled());
        assertEquals("false", actual.getValue());
        assertEquals(jsonStr, actual.toJsonString());

        jsonStr = "{\"value\":\"false\"}";
        JsonNode jsonNode = mapper.readTree(jsonStr);
        isEditable = false;
        actual = BasicNodeFactory.createFromJson(BasicType.BOOLEAN, jsonNode, isEditable);
        assertEquals(BasicType.BOOLEAN, actual.getType());
        assertEquals(JCheckBox.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JCheckBox) actual.getComponent()).isEnabled());
        assertEquals("false", actual.getValue());
        assertEquals(jsonStr, actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.createFromJson(BasicType.BOOLEAN, jsonNode, isEditable);
        assertEquals(BasicType.BOOLEAN, actual.getType());
        assertEquals(JCheckBox.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JCheckBox) actual.getComponent()).isEnabled());
        assertEquals("false", actual.getValue());
        assertEquals(jsonStr, actual.toJsonString());

        jsonStr = "{\"value\":\"true\"}";
        jsonNode = mapper.readTree(jsonStr);
        isEditable = false;
        actual = BasicNodeFactory.createFromJson(BasicType.BOOLEAN, jsonNode.get("value"), isEditable);
        assertEquals(BasicType.BOOLEAN, actual.getType());
        assertEquals(JCheckBox.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JCheckBox) actual.getComponent()).isEnabled());
        assertEquals("true", actual.getValue());
        assertEquals(jsonStr, actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.createFromJson(BasicType.BOOLEAN, jsonNode.get("value"), isEditable);
        assertEquals(BasicType.BOOLEAN, actual.getType());
        assertEquals(JCheckBox.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JCheckBox) actual.getComponent()).isEnabled());
        assertEquals("true", actual.getValue());
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
        assertEquals(currDate.toString(), actual.getValue());
        assertTrue(actual.toJsonString().matches("\\{\"value\":\"\\d{4}-\\d{2}-\\d{2}\"\\}"), actual.toJsonString());

        isEditable = true;
        actual = BasicNodeFactory.createDateType(currDate, isEditable);
        assertEquals(BasicType.DATE, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JSpinner) actual.getComponent()).isEnabled());
        assertEquals(currDate.toString(), actual.getValue());
        assertTrue(actual.toJsonString().matches("\\{\"value\":\"\\d{4}-\\d{2}-\\d{2}\"\\}"), actual.toJsonString());
    }

    @Test
    public void createDateTypeFromJson() throws JsonProcessingException {
        boolean isEditable = false;
        BasicNode actual = BasicNodeFactory.create(BasicType.DATE, isEditable);
        assertEquals(BasicType.DATE, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertTrue(actual.getValue().matches("\\d{4}-\\d{2}-\\d{2}"), actual.getValue());
        assertTrue(actual.toJsonString().matches("\\{\"value\":\"\\d{4}-\\d{2}-\\d{2}\"\\}"), actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.create(BasicType.DATE, isEditable);
        assertEquals(BasicType.DATE, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertTrue(actual.getValue().matches("\\d{4}-\\d{2}-\\d{2}"), actual.getValue());
        assertTrue(actual.toJsonString().matches("\\{\"value\":\"\\d{4}-\\d{2}-\\d{2}\"\\}"), actual.toJsonString());

        String expValue = "2020-06-29";
        String expJsonStr = "{\"value\":\"" + expValue + "\"}";
        JsonNode jsonNode = mapper.readTree(expJsonStr);
        isEditable = false;
        actual = BasicNodeFactory.createFromJson(BasicType.DATE, jsonNode.get("value"), isEditable);
        assertEquals(BasicType.DATE, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.createFromJson(BasicType.DATE, jsonNode.get("value"), isEditable);
        assertEquals(BasicType.DATE, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
    }

    @Test
    public void createIntegerType() {
        boolean isEditable = false;
        BasicNode actual = BasicNodeFactory.createIntegerType(0, isEditable);
        assertEquals(BasicType.INTEGER, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals("0", actual.getValue());
        assertEquals("{\"value\":\"0\"}", actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.createIntegerType(4711, isEditable);
        assertEquals(BasicType.INTEGER, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertEquals("4711", actual.getValue());
        assertEquals("{\"value\":\"4711\"}", actual.toJsonString());
        actual = BasicNodeFactory.createIntegerType(-42, isEditable);
        assertEquals(BasicType.INTEGER, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertEquals("-42", actual.getValue());
        assertEquals("{\"value\":\"-42\"}", actual.toJsonString());
    }

    @Test
    public void createIntegerTypeFromJson() throws JsonProcessingException {
        String expValue = "0";
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

        expValue = "42";
        expJsonStr = "{\"value\":\"" + expValue + "\"}";
        JsonNode jsonNode = mapper.readTree(expJsonStr);
        isEditable = false;
        actual = BasicNodeFactory.createFromJson(BasicType.INTEGER, jsonNode.get("value"), isEditable);
        assertEquals(BasicType.INTEGER, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.createFromJson(BasicType.INTEGER, jsonNode.get("value"), isEditable);
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
        assertEquals("0.0", actual.getValue());
        assertEquals("{\"value\":\"0.0\"}", actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.createRealType(3.141592, isEditable);
        assertEquals(BasicType.REAL, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JSpinner) actual.getComponent()).isEnabled());
        assertEquals("3.141592", actual.getValue());
        actual = BasicNodeFactory.createRealType(-42, isEditable);
        assertEquals(BasicType.REAL, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JSpinner) actual.getComponent()).isEnabled());
        assertEquals("-42.0", actual.getValue());
    }

    @Test
    public void createRealTypeFromJson() throws JsonProcessingException {
        String expValue = "0.0";
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

        expValue = "42.123";
        expJsonStr = "{\"value\":\"" + expValue + "\"}";
        JsonNode jsonNode = mapper.readTree(expJsonStr);
        isEditable = false;
        actual = BasicNodeFactory.createFromJson(BasicType.REAL, jsonNode.get("value"), isEditable);
        assertEquals(BasicType.REAL, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.createFromJson(BasicType.REAL, jsonNode.get("value"), isEditable);
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
    public void createStringTypeFromJson() throws JsonProcessingException {
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

        expValue = "Lorem ipsum dolor sit amet";
        expJsonStr = "{\"value\":\"" + expValue + "\"}";
        JsonNode jsonNode = mapper.readTree(expJsonStr);
        isEditable = false;
        actual = BasicNodeFactory.createFromJson(BasicType.STRING, jsonNode.get("value"), isEditable);
        assertEquals(BasicType.STRING, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.createFromJson(BasicType.STRING, jsonNode.get("value"), isEditable);
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
        assertEquals(BasicType.TIME, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(currentTime.withOffsetSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS).toString(), actual.getValue());
        assertTrue(actual.getValue().matches("\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z"), actual.getValue());
        assertTrue(actual.toJsonString().matches("\\{\"value\":\"\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z\"\\}"), actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.createTimeType(currentTime, isEditable);
        assertEquals(BasicType.TIME, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JSpinner) actual.getComponent()).isEnabled());
        assertTrue(actual.getValue().matches("\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z"), actual.getValue());
    }

    @Test
    public void createTimeTypeFromJson() throws JsonProcessingException {
        boolean isEditable = false;
        BasicNode actual = BasicNodeFactory.create(BasicType.TIME, isEditable);
        assertEquals(BasicType.TIME, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertTrue(actual.getValue().matches("\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z"), actual.getValue());
        assertTrue(actual.toJsonString().matches("\\{\"value\":\"\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z\"\\}"), actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.create(BasicType.TIME, isEditable);
        assertEquals(BasicType.TIME, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertTrue(actual.getValue().matches("\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z"), actual.getValue());
        assertTrue(actual.toJsonString().matches("\\{\"value\":\"\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z\"\\}"), actual.toJsonString());

        String expValue = "20:15:10.123Z";
        String expJsonStr = "{\"value\":\"" + expValue + "\"}";
        JsonNode jsonNode = mapper.readTree(expJsonStr);
        isEditable = false;
        actual = BasicNodeFactory.createFromJson(BasicType.TIME, jsonNode.get("value"), isEditable);
        assertEquals(BasicType.TIME, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.createFromJson(BasicType.TIME, jsonNode.get("value"), isEditable);
        assertEquals(BasicType.TIME, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
    }

    @Test
    public void createTimestampType() {
        boolean isEditable = false;
        final OffsetDateTime currentTimestamp = OffsetDateTime.now();
        BasicNode actual = BasicNodeFactory.createTimestampType(currentTimestamp, isEditable);
        assertEquals(BasicType.TIMESTAMP, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertTrue(actual.getValue().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z"), actual.getValue());
        assertTrue(actual.toJsonString().matches("\\{\"value\":\"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z\"\\}"), actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.createTimestampType(currentTimestamp, isEditable);
        assertEquals(BasicType.TIMESTAMP, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JSpinner) actual.getComponent()).isEnabled());
        assertTrue(actual.getValue().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z"), actual.getValue());
    }

    @Test
    public void createTimestamp() throws JsonProcessingException {
        boolean isEditable = false;
        BasicNode actual = BasicNodeFactory.create(BasicType.TIMESTAMP, isEditable);
        assertEquals(BasicType.TIMESTAMP, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertTrue(actual.getValue().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z"), actual.getValue());
        assertTrue(actual.toJsonString().matches("\\{\"value\":\"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z\"\\}"), actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.create(BasicType.TIMESTAMP, isEditable);
        assertEquals(BasicType.TIMESTAMP, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertTrue(actual.getValue().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z"), actual.getValue());
        assertTrue(actual.toJsonString().matches("\\{\"value\":\"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z\"\\}"), actual.toJsonString());

        String expValue = "2020-06-29T20:15:10.123Z";
        String expJsonStr = "{\"value\":\"" + expValue + "\"}";
        JsonNode jsonNode = mapper.readTree(expJsonStr);
        isEditable = false;
        actual = BasicNodeFactory.createFromJson(BasicType.TIMESTAMP, jsonNode.get("value"), isEditable);
        assertEquals(BasicType.TIMESTAMP, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.createFromJson(BasicType.TIMESTAMP, jsonNode.get("value"), isEditable);
        assertEquals(BasicType.TIMESTAMP, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
    }
}
