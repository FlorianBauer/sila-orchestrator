package de.fau.clients.orchestrator.nodes;

import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import sila_java.library.core.models.BasicType;
import sila_java.library.core.models.Constraints;

public class ConstraintBasicNodeFactoryTest {

    @Test
    public void createConstrainedBoolean() {
        Constraints con = new Constraints();
        try {
            ConstraintBasicNodeFactory.create(null, BasicType.BOOLEAN, con, null);
            fail("IllegalArgumentException was expected but not thrown.");
        } catch (IllegalArgumentException ex) {
        } catch (Exception ex) {
            fail("Only a IllegalArgumentException was expected.");
        }
    }
}
