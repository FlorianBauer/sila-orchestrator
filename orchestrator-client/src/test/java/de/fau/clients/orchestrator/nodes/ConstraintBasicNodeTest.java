package de.fau.clients.orchestrator.nodes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import sila_java.library.core.models.BasicType;
import sila_java.library.core.models.Constraints;

public class ConstraintBasicNodeTest {

    @Test
    public void createConstrainedBoolean() {
        Constraints con = new Constraints();
        BasicNode node = ConstraintBasicNode.create(null, BasicType.BOOLEAN, con, null);
        assertNotNull(node);
        assertEquals(BasicType.BOOLEAN, node.getType());
        assertEquals("false", node.getValue());
        assertEquals("{\"value\":\"false\"}", node.toJsonString());
    }
}
