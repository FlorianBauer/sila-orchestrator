package de.fau.clients.orchestrator.nodes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.JTextField;
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
            BasicNodeFactory.create(null);
            fail("NullPointerException was expected but not thrown.");
        } catch (NullPointerException ex) {
        } catch (Exception ex) {
            fail("Only a NullPointerException was expected but got:" + ex.getMessage());
        }

        BasicNode node;
        for (final BasicType type : BasicType.values()) {
            node = BasicNodeFactory.create(type);
            assertNotNull(node);
            assertTrue(node.toJsonString().matches("^\\{\"value\":\".*\"\\}$"));
        }
    }

    @Test
    public void createBooleanTypeFromJson() throws JsonProcessingException {
        String jsonStr = "{\"value\":\"false\"}";
        JsonNode jsonNode = null;
        boolean isEditable = false;
        BasicNode actual = BasicNodeFactory.createBooleanTypeFromJson(jsonNode, isEditable);
        assertEquals(BasicType.BOOLEAN, actual.getType());
        assertEquals(JCheckBox.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JCheckBox) actual.getComponent()).isEnabled());
        assertEquals("false", actual.getValue());
        assertEquals(jsonStr, actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.createBooleanTypeFromJson(jsonNode, isEditable);
        assertEquals(BasicType.BOOLEAN, actual.getType());
        assertEquals(JCheckBox.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JCheckBox) actual.getComponent()).isEnabled());
        assertEquals("false", actual.getValue());
        assertEquals(jsonStr, actual.toJsonString());

        jsonStr = "{\"value\":\"false\"}";
        jsonNode = mapper.readTree(jsonStr);
        isEditable = false;
        actual = BasicNodeFactory.createBooleanTypeFromJson(jsonNode, isEditable);
        assertEquals(BasicType.BOOLEAN, actual.getType());
        assertEquals(JCheckBox.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JCheckBox) actual.getComponent()).isEnabled());
        assertEquals("false", actual.getValue());
        assertEquals(jsonStr, actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.createBooleanTypeFromJson(jsonNode, isEditable);
        assertEquals(BasicType.BOOLEAN, actual.getType());
        assertEquals(JCheckBox.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JCheckBox) actual.getComponent()).isEnabled());
        assertEquals("false", actual.getValue());
        assertEquals(jsonStr, actual.toJsonString());

        jsonStr = "{\"value\":\"true\"}";
        jsonNode = mapper.readTree(jsonStr);
        isEditable = false;
        actual = BasicNodeFactory.createBooleanTypeFromJson(jsonNode.get("value"), isEditable);
        assertEquals(BasicType.BOOLEAN, actual.getType());
        assertEquals(JCheckBox.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JCheckBox) actual.getComponent()).isEnabled());
        assertEquals("true", actual.getValue());
        assertEquals(jsonStr, actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.createBooleanTypeFromJson(jsonNode.get("value"), isEditable);
        assertEquals(BasicType.BOOLEAN, actual.getType());
        assertEquals(JCheckBox.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JCheckBox) actual.getComponent()).isEnabled());
        assertEquals("true", actual.getValue());
        assertEquals(jsonStr, actual.toJsonString());
    }

    @Test
    public void createDateTypeFromJson() throws JsonProcessingException {
        JsonNode jsonNode = null;
        boolean isEditable = false;
        BasicNode actual = BasicNodeFactory.createDateTypeFromJson(jsonNode, isEditable);
        assertEquals(BasicType.DATE, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertTrue(actual.getValue().matches("\\d{4}-\\d{2}-\\d{2}"), actual.getValue());
        assertTrue(actual.toJsonString().matches("\\{\"value\":\"\\d{4}-\\d{2}-\\d{2}\"\\}"), actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.createDateTypeFromJson(jsonNode, isEditable);
        assertEquals(BasicType.DATE, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertTrue(actual.getValue().matches("\\d{4}-\\d{2}-\\d{2}"), actual.getValue());
        assertTrue(actual.toJsonString().matches("\\{\"value\":\"\\d{4}-\\d{2}-\\d{2}\"\\}"), actual.toJsonString());

        String expValue = "2020-06-29";
        String expJsonStr = "{\"value\":\"" + expValue + "\"}";
        jsonNode = mapper.readTree(expJsonStr);
        isEditable = false;
        actual = BasicNodeFactory.createDateTypeFromJson(jsonNode.get("value"), isEditable);
        assertEquals(BasicType.DATE, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.createDateTypeFromJson(jsonNode.get("value"), isEditable);
        assertEquals(BasicType.DATE, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
    }

    @Test
    public void createIntegerTypeFromJson() throws JsonProcessingException {
        String expValue = "0";
        String expJsonStr = "{\"value\":\"" + expValue + "\"}";
        JsonNode jsonNode = null;
        boolean isEditable = false;
        BasicNode actual = BasicNodeFactory.createIntegerTypeFromJson(jsonNode, isEditable);
        assertEquals(BasicType.INTEGER, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.createIntegerTypeFromJson(jsonNode, isEditable);
        assertEquals(BasicType.INTEGER, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());

        expValue = "42";
        expJsonStr = "{\"value\":\"" + expValue + "\"}";
        jsonNode = mapper.readTree(expJsonStr);
        isEditable = false;
        actual = BasicNodeFactory.createIntegerTypeFromJson(jsonNode.get("value"), isEditable);
        assertEquals(BasicType.INTEGER, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.createIntegerTypeFromJson(jsonNode.get("value"), isEditable);
        assertEquals(BasicType.INTEGER, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
    }

    @Test
    public void createRealTypeFromJson() throws JsonProcessingException {
        String expValue = "0.0";
        String expJsonStr = "{\"value\":\"" + expValue + "\"}";
        JsonNode jsonNode = null;
        boolean isEditable = false;
        BasicNode actual = BasicNodeFactory.createRealTypeFromJson(jsonNode, isEditable);
        assertEquals(BasicType.REAL, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.createRealTypeFromJson(jsonNode, isEditable);
        assertEquals(BasicType.REAL, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());

        expValue = "42.123";
        expJsonStr = "{\"value\":\"" + expValue + "\"}";
        jsonNode = mapper.readTree(expJsonStr);
        isEditable = false;
        actual = BasicNodeFactory.createRealTypeFromJson(jsonNode.get("value"), isEditable);
        assertEquals(BasicType.REAL, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.createRealTypeFromJson(jsonNode.get("value"), isEditable);
        assertEquals(BasicType.REAL, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
    }

    @Test
    public void createStringTypeFromJson() throws JsonProcessingException {
        String expValue = "";
        String expJsonStr = "{\"value\":\"" + expValue + "\"}";
        JsonNode jsonNode = null;
        boolean isEditable = false;
        BasicNode actual = BasicNodeFactory.createStringTypeFromJson(jsonNode, isEditable);
        assertEquals(BasicType.STRING, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.createStringTypeFromJson(jsonNode, isEditable);
        assertEquals(BasicType.STRING, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());

        expValue = "Lorem ipsum dolor sit amet";
        expJsonStr = "{\"value\":\"" + expValue + "\"}";
        jsonNode = mapper.readTree(expJsonStr);
        isEditable = false;
        actual = BasicNodeFactory.createStringTypeFromJson(jsonNode.get("value"), isEditable);
        assertEquals(BasicType.STRING, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.createStringTypeFromJson(jsonNode.get("value"), isEditable);
        assertEquals(BasicType.STRING, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
    }

    @Test
    public void createTimeTypeFromJson() throws JsonProcessingException {
        JsonNode jsonNode = null;
        boolean isEditable = false;
        BasicNode actual = BasicNodeFactory.createTimeTypeFromJson(jsonNode, isEditable);
        assertEquals(BasicType.TIME, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertTrue(actual.getValue().matches("\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z"), actual.getValue());
        assertTrue(actual.toJsonString().matches("\\{\"value\":\"\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z\"\\}"), actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.createTimeTypeFromJson(jsonNode, isEditable);
        assertEquals(BasicType.TIME, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertTrue(actual.getValue().matches("\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z"), actual.getValue());
        assertTrue(actual.toJsonString().matches("\\{\"value\":\"\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z\"\\}"), actual.toJsonString());

        String expValue = "20:15:10.123Z";
        String expJsonStr = "{\"value\":\"" + expValue + "\"}";
        jsonNode = mapper.readTree(expJsonStr);
        isEditable = false;
        actual = BasicNodeFactory.createTimeTypeFromJson(jsonNode.get("value"), isEditable);
        assertEquals(BasicType.TIME, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.createTimeTypeFromJson(jsonNode.get("value"), isEditable);
        assertEquals(BasicType.TIME, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
    }

    @Test
    public void createTimestampTypeFromJson() throws JsonProcessingException {
        JsonNode jsonNode = null;
        boolean isEditable = false;
        BasicNode actual = BasicNodeFactory.createTimestampTypeFromJson(jsonNode, isEditable);
        assertEquals(BasicType.TIMESTAMP, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertTrue(actual.getValue().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z"), actual.getValue());
        assertTrue(actual.toJsonString().matches("\\{\"value\":\"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z\"\\}"), actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.createTimestampTypeFromJson(jsonNode, isEditable);
        assertEquals(BasicType.TIMESTAMP, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertTrue(actual.getValue().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z"), actual.getValue());
        assertTrue(actual.toJsonString().matches("\\{\"value\":\"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z\"\\}"), actual.toJsonString());

        String expValue = "2020-06-29T20:15:10.123Z";
        String expJsonStr = "{\"value\":\"" + expValue + "\"}";
        jsonNode = mapper.readTree(expJsonStr);
        isEditable = false;
        actual = BasicNodeFactory.createTimestampTypeFromJson(jsonNode.get("value"), isEditable);
        assertEquals(BasicType.TIMESTAMP, actual.getType());
        assertEquals(JTextField.class, actual.getComponent().getClass());
        assertEquals(isEditable, ((JTextField) actual.getComponent()).isEditable());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
        isEditable = true;
        actual = BasicNodeFactory.createTimestampTypeFromJson(jsonNode.get("value"), isEditable);
        assertEquals(BasicType.TIMESTAMP, actual.getType());
        assertEquals(JSpinner.class, actual.getComponent().getClass());
        assertEquals(expValue, actual.getValue());
        assertEquals(expJsonStr, actual.toJsonString());
    }
}
