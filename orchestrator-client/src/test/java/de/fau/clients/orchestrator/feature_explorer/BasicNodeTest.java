package de.fau.clients.orchestrator.feature_explorer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import sila_java.library.core.models.BasicType;

public class BasicNodeTest {

    @Test
    public void create() {
        try {
            BasicNode.create(null);
            fail("NullPointerException was expected but not thrown.");
        } catch (NullPointerException ex) {
        } catch (Exception ex) {
            fail("Only a NullPointerException was expected.");
        }

        BasicNode node;
        for (final BasicType type : BasicType.values()) {
            node = BasicNode.create(type);
            assertNotNull(node);
            assertTrue(node.toJsonString().matches("^\\{\"value\":\".*\"\\}$"));
        }
    }

    @Test
    public void toJsonString() {
        String actual = BasicNode.create(BasicType.ANY).toJsonString();
        assertEquals("{\"value\":\"not implemented 01\"}", actual);
        actual = BasicNode.create(BasicType.BINARY).toJsonString();
        assertEquals("{\"value\":\"not implemented 02\"}", actual);
        actual = BasicNode.create(BasicType.BOOLEAN).toJsonString();
        assertEquals("{\"value\":\"false\"}", actual);
        actual = BasicNode.create(BasicType.DATE).toJsonString();
        // e.g. {"value":"2020-06-04"}
        assertTrue(actual.matches("\\{\"value\":\"\\d{4}-\\d{2}-\\d{2}\"\\}"), actual);
        actual = BasicNode.create(BasicType.INTEGER).toJsonString();
        assertEquals("{\"value\":\"0\"}", actual);
        actual = BasicNode.create(BasicType.REAL).toJsonString();
        assertEquals("{\"value\":\"0.0\"}", actual);
        actual = BasicNode.create(BasicType.STRING).toJsonString();
        assertEquals("{\"value\":\"\"}", actual);
        actual = BasicNode.create(BasicType.TIME).toJsonString();
        // e.g. {"value":"20:15:00.000Z"}
        assertTrue(actual.matches("\\{\"value\":\"\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z\"\\}"), actual);
        actual = BasicNode.create(BasicType.TIMESTAMP).toJsonString();
        // e.g. {"value":"2020-06-04T20:15:00.000Z"}
        assertTrue(actual.matches("\\{\"value\":\"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z\"\\}"), actual);
    }

    @Test
    public void cloneNode() {
        BasicNode exp;
        BasicNode act;
        for (final BasicType type : BasicType.values()) {
            exp = BasicNode.create(type);
            act = exp.cloneNode();
            assertNotNull(act);
            assertEquals(exp.toString(), act.toString());
        }
    }
}
