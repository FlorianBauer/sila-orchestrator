package de.fau.clients.orchestrator.nodes;

import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sila_java.library.core.models.BasicType;

public class BasicNodeTest {

    private static BasicNode binaryNode;
    private static BasicNode booleanNode;
    private static BasicNode dateNode;
    private static BasicNode integerNode;
    private static BasicNode realNode;
    private static BasicNode stringNode;
    private static BasicNode timeNode;
    private static BasicNode timestampNode;

    @BeforeAll
    public static void initNodes() {
        binaryNode = BasicNodeFactory.create(BasicType.BINARY, true);
        booleanNode = BasicNodeFactory.create(BasicType.BOOLEAN, true);
        dateNode = BasicNodeFactory.create(BasicType.DATE, true);
        integerNode = BasicNodeFactory.create(BasicType.INTEGER, true);
        realNode = BasicNodeFactory.create(BasicType.REAL, true);
        stringNode = BasicNodeFactory.create(BasicType.STRING, true);
        timeNode = BasicNodeFactory.create(BasicType.TIME, true);
        timestampNode = BasicNodeFactory.create(BasicType.TIMESTAMP, true);
    }

    @Test
    public void getType() {
        assertEquals(BasicType.BINARY, binaryNode.getType());
        assertEquals(BasicType.BOOLEAN, booleanNode.getType());
        assertEquals(BasicType.DATE, dateNode.getType());
        assertEquals(BasicType.INTEGER, integerNode.getType());
        assertEquals(BasicType.REAL, realNode.getType());
        assertEquals(BasicType.STRING, stringNode.getType());
        assertEquals(BasicType.TIME, timeNode.getType());
        assertEquals(BasicType.TIMESTAMP, timestampNode.getType());
    }

    @Test
    public void getComponent() {
        assertEquals(JScrollPane.class, binaryNode.getComponent().getClass());
        assertEquals(JCheckBox.class, booleanNode.getComponent().getClass());
        assertEquals(JSpinner.class, dateNode.getComponent().getClass());
        assertEquals(JSpinner.class, integerNode.getComponent().getClass());
        assertEquals(JSpinner.class, realNode.getComponent().getClass());
        assertEquals(JTextField.class, stringNode.getComponent().getClass());
        assertEquals(JSpinner.class, timeNode.getComponent().getClass());
        assertEquals(JSpinner.class, timestampNode.getComponent().getClass());
    }

    @Test
    public void toJsonString() {
        String actual = binaryNode.toJsonString();
        assertEquals("{\"value\":\"\"}", actual);
        actual = booleanNode.toJsonString();
        assertEquals("{\"value\":\"false\"}", actual);
        actual = dateNode.toJsonString();
        assertTrue(actual.matches("\\{\"day\":\\d{1,2},\"month\":\\d{1,2},\"year\":\\d{4},"
                + "?\"timezone\":\\{\"hours\":0}\\}"), actual);
        actual = integerNode.toJsonString();
        assertEquals("{\"value\":\"0\"}", actual);
        actual = realNode.toJsonString();
        assertEquals("{\"value\":\"0.0\"}", actual);
        actual = stringNode.toJsonString();
        assertEquals("{\"value\":\"\"}", actual);
        actual = timeNode.toJsonString();
        assertTrue(actual.matches("\\{\"second\":\\d{1,2},\"minute\":\\d{1,2},\"hour\":\\d{1,2},"
                + "?\"timezone\":\\{\"hours\":0}\\}"), actual);
        actual = timestampNode.toJsonString();
        assertTrue(actual.matches("\\{\"second\":\\d{1,2},\"minute\":\\d{1,2},\"hour\":\\d{1,2},"
                + "\"day\":\\d{1,2},\"month\":\\d{1,2},\"year\":\\d{4},"
                + "?\"timezone\":\\{\"hours\":0}\\}"), actual);
    }

    @Test
    public void cloneNode() {
        BasicNode exp = binaryNode;
        BasicNode act = exp.cloneNode();
        assertNotNull(act);
        assertEquals(exp.toString(), act.toString());
        assertEquals(exp.getType(), act.getType());

        exp = booleanNode;
        act = exp.cloneNode();
        assertNotNull(act);
        assertEquals(exp.toString(), act.toString());
        assertEquals(exp.getType(), act.getType());

        exp = dateNode;
        act = exp.cloneNode();
        assertNotNull(act);
        assertEquals(exp.toString(), act.toString());
        assertEquals(exp.getType(), act.getType());

        exp = integerNode;
        act = exp.cloneNode();
        assertNotNull(act);
        assertEquals(exp.toString(), act.toString());
        assertEquals(exp.getType(), act.getType());

        exp = realNode;
        act = exp.cloneNode();
        assertNotNull(act);
        assertEquals(exp.toString(), act.toString());
        assertEquals(exp.getType(), act.getType());

        exp = stringNode;
        act = exp.cloneNode();
        assertNotNull(act);
        assertEquals(exp.toString(), act.toString());
        assertEquals(exp.getType(), act.getType());

        exp = timeNode;
        act = exp.cloneNode();
        assertNotNull(act);
        assertEquals(exp.toString(), act.toString());
        assertEquals(exp.getType(), act.getType());

        exp = timestampNode;
        act = exp.cloneNode();
        assertNotNull(act);
        assertEquals(exp.toString(), act.toString());
        assertEquals(exp.getType(), act.getType());
    }
}
