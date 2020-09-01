package de.fau.clients.orchestrator.nodes;

import java.time.LocalTime;
import javax.swing.AbstractSpinnerModel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import sila_java.library.core.models.Constraints;

public class ConstraintBasicNodeTest {

    @Test
    public void createRangeConstrainedTimeModel() {
        {
            Constraints con = new Constraints();
            con.setMaximalInclusive("185959");
            LocalTime initTime = LocalTime.of(18, 59, 59);
            AbstractSpinnerModel asm = ConstraintBasicNode.createRangeConstrainedTimeModel(initTime, con);
            assertEquals(asm.getValue(), initTime);
            assertEquals(asm.getNextValue(), null);
            assertEquals(asm.getPreviousValue(), LocalTime.of(18, 58, 59));
        }

        {
            Constraints con = new Constraints();
            con.setMinimalInclusive("07:00:00");
            LocalTime initTime = LocalTime.of(7, 0, 0);
            AbstractSpinnerModel asm = ConstraintBasicNode.createRangeConstrainedTimeModel(initTime, con);
            assertEquals(asm.getValue(), initTime);
            assertEquals(asm.getNextValue(), LocalTime.of(7, 1, 0));
            assertEquals(asm.getPreviousValue(), null);
        }

        {
            Constraints con = new Constraints();
            con.setMinimalInclusive("08:00:00");
            con.setMaximalInclusive("20:00:00");
            LocalTime initTime = LocalTime.of(7, 0, 0);
            AbstractSpinnerModel asm = ConstraintBasicNode.createRangeConstrainedTimeModel(initTime, con);
            assertEquals(asm.getValue(), LocalTime.of(8, 0, 0));
            assertEquals(asm.getNextValue(), LocalTime.of(8, 1, 0));
            assertEquals(asm.getPreviousValue(), null);
            initTime = LocalTime.of(21, 0, 0);
            asm = ConstraintBasicNode.createRangeConstrainedTimeModel(initTime, con);
            assertEquals(asm.getValue(), LocalTime.of(20, 0, 0));
            assertEquals(asm.getNextValue(), null);
            assertEquals(asm.getPreviousValue(), LocalTime.of(19, 59, 0));
        }

        {
            Constraints con = new Constraints();
            con.setMinimalExclusive("08:00:00");
            con.setMaximalExclusive("20:00:00");
            LocalTime initTime = LocalTime.of(7, 0, 0);
            AbstractSpinnerModel asm = ConstraintBasicNode.createRangeConstrainedTimeModel(initTime, con);
            assertEquals(asm.getValue(), LocalTime.of(8, 0, 1));
            assertEquals(asm.getNextValue(), LocalTime.of(8, 1, 1));
            assertEquals(asm.getPreviousValue(), null);
            initTime = LocalTime.of(21, 0, 0);
            asm = ConstraintBasicNode.createRangeConstrainedTimeModel(initTime, con);
            assertEquals(asm.getValue(), LocalTime.of(19, 59, 59));
            assertEquals(asm.getNextValue(), null);
            assertEquals(asm.getPreviousValue(), LocalTime.of(19, 58, 59));
        }

        {
            // Exlusive binds more then inclusive.
            Constraints con = new Constraints();
            con.setMinimalExclusive("08:00:00");
            con.setMinimalInclusive("08:00:00");
            con.setMaximalExclusive("20:00:00");
            con.setMaximalInclusive("20:00:00");
            LocalTime initTime = LocalTime.of(7, 0, 0);
            AbstractSpinnerModel asm = ConstraintBasicNode.createRangeConstrainedTimeModel(initTime, con);
            assertEquals(asm.getValue(), LocalTime.of(8, 0, 1));
            assertEquals(asm.getNextValue(), LocalTime.of(8, 1, 1));
            assertEquals(asm.getPreviousValue(), null);
            initTime = LocalTime.of(21, 0, 0);
            asm = ConstraintBasicNode.createRangeConstrainedTimeModel(initTime, con);
            assertEquals(asm.getValue(), LocalTime.of(19, 59, 59));
            assertEquals(asm.getNextValue(), null);
            assertEquals(asm.getPreviousValue(), LocalTime.of(19, 58, 59));
        }
    }
}
