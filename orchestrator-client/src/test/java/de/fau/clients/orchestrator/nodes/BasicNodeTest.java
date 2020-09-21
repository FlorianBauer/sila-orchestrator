package de.fau.clients.orchestrator.nodes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import sila_java.library.core.models.BasicType;

public class BasicNodeTest {

    @Test
    public void toJsonString() {
        String actual = BasicNodeFactory.create(BasicType.ANY).toJsonString();
        assertEquals("{\"value\":\"not implemented 01\"}", actual);
        actual = BasicNodeFactory.create(BasicType.BINARY).toJsonString();
        assertEquals("{\"value\":\"not implemented 02\"}", actual);
        actual = BasicNodeFactory.create(BasicType.BOOLEAN).toJsonString();
        assertEquals("{\"value\":\"false\"}", actual);
        actual = BasicNodeFactory.create(BasicType.DATE).toJsonString();
        // e.g. {"value":"2020-06-04"}
        assertTrue(actual.matches("\\{\"value\":\"\\d{4}-\\d{2}-\\d{2}\"\\}"), actual);
        actual = BasicNodeFactory.create(BasicType.INTEGER).toJsonString();
        assertEquals("{\"value\":\"0\"}", actual);
        actual = BasicNodeFactory.create(BasicType.REAL).toJsonString();
        assertEquals("{\"value\":\"0.0\"}", actual);
        actual = BasicNodeFactory.create(BasicType.STRING).toJsonString();
        assertEquals("{\"value\":\"\"}", actual);
        actual = BasicNodeFactory.create(BasicType.TIME).toJsonString();
        // e.g. {"value":"20:15:00.000Z"}
        assertTrue(actual.matches("\\{\"value\":\"\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z\"\\}"), actual);
        actual = BasicNodeFactory.create(BasicType.TIMESTAMP).toJsonString();
        // e.g. {"value":"2020-06-04T20:15:00.000Z"}
        assertTrue(actual.matches("\\{\"value\":\"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z\"\\}"), actual);
    }

    @Test
    public void cloneNode() {
        BasicNode exp;
        BasicNode act;
        for (final BasicType type : BasicType.values()) {
            exp = BasicNodeFactory.create(type);
            act = exp.cloneNode();
            assertNotNull(act);
            assertEquals(exp.toString(), act.toString());
        }
    }
}
