package de.fau.clients.orchestrator.nodes;

import java.time.LocalDate;
import java.time.LocalTime;
import javax.swing.AbstractSpinnerModel;
import javax.swing.SpinnerModel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import sila_java.library.core.models.Constraints;

public class ConstraintSpinnerModelFactoryTest {

    @Test
    public void createRangeConstrainedIntModel() {
        {
            Constraints con = new Constraints();
            con.setMaximalInclusive("5");
            con.setMinimalInclusive("2");
            SpinnerModel sm = ConstraintSpinnerModelFactory.createRangeConstrainedIntModel(con);
            assertEquals(2, sm.getValue());
            assertEquals(3, sm.getNextValue());
            assertEquals(null, sm.getPreviousValue());
            sm.setValue(5);
            assertEquals(5, sm.getValue());
            assertEquals(null, sm.getNextValue());
            assertEquals(4, sm.getPreviousValue());
        }

        {
            Constraints con = new Constraints();
            con.setMaximalInclusive("-2");
            con.setMinimalInclusive("-5");
            SpinnerModel sm = ConstraintSpinnerModelFactory.createRangeConstrainedIntModel(con);
            assertEquals(-2, sm.getValue());
            assertEquals(null, sm.getNextValue());
            assertEquals(-3, sm.getPreviousValue());
            sm.setValue(-5);
            assertEquals(-5, sm.getValue());
            assertEquals(-4, sm.getNextValue());
            assertEquals(null, sm.getPreviousValue());
        }

        {
            Constraints con = new Constraints();
            con.setMaximalExclusive("6");
            con.setMinimalExclusive("2");
            SpinnerModel sm = ConstraintSpinnerModelFactory.createRangeConstrainedIntModel(con);
            assertEquals(3, sm.getValue());
            assertEquals(4, sm.getNextValue());
            assertEquals(null, sm.getPreviousValue());
            sm.setValue(5);
            assertEquals(5, sm.getValue());
            assertEquals(null, sm.getNextValue());
            assertEquals(4, sm.getPreviousValue());
        }

        {
            Constraints con = new Constraints();
            con.setMaximalExclusive("-2");
            con.setMinimalExclusive("-6");
            SpinnerModel sm = ConstraintSpinnerModelFactory.createRangeConstrainedIntModel(con);
            assertEquals(-3, sm.getValue());
            assertEquals(null, sm.getNextValue());
            assertEquals(-4, sm.getPreviousValue());
            sm.setValue(-5);
            assertEquals(-5, sm.getValue());
            assertEquals(-4, sm.getNextValue());
            assertEquals(null, sm.getPreviousValue());
        }

        {
            Constraints con = new Constraints();
            con.setMaximalInclusive("5");
            con.setMinimalInclusive("2");
            SpinnerModel sm = ConstraintSpinnerModelFactory.createRangeConstrainedIntModel(con);
            sm.setValue(8);
            assertEquals(8, sm.getValue());
            assertEquals(null, sm.getNextValue());
            assertEquals(null, sm.getPreviousValue());
        }

        {
            Constraints con = new Constraints();
            con.setMaximalInclusive("2");
            con.setMinimalInclusive("5");
            try {
                ConstraintSpinnerModelFactory.createRangeConstrainedIntModel(con);
                fail("IllegalArgumentException was expected but not thrown.");
            } catch (IllegalArgumentException ex) {
            } catch (Exception ex) {
                fail("Only a IllegalArgumentException was expected.");
            }
        }
    }

    @Test
    public void createRangeConstrainedRealModel() {
        final double DELTA = 0.00001;
        {
            Constraints con = new Constraints();
            con.setMaximalInclusive("5.5");
            con.setMinimalInclusive("2.2");
            SpinnerModel sm = ConstraintSpinnerModelFactory.createRangeConstrainedRealModel(con);
            assertEquals(2.2, (double) sm.getValue(), DELTA);
            assertEquals(2.3, (double) sm.getNextValue(), DELTA);
            assertEquals(null, sm.getPreviousValue());
            sm.setValue(5.432);
            assertEquals(5.432, (double) sm.getValue(), DELTA);
            assertEquals(null, sm.getNextValue());
            assertEquals(5.332, (double) sm.getPreviousValue(), DELTA);
        }

        {
            Constraints con = new Constraints();
            con.setMaximalInclusive("-2.2");
            con.setMinimalInclusive("-5.5");
            SpinnerModel sm = ConstraintSpinnerModelFactory.createRangeConstrainedRealModel(con);
            assertEquals(-2.2, (double) sm.getValue(), DELTA);
            assertEquals(null, sm.getNextValue());
            assertEquals(-2.3, (double) sm.getPreviousValue(), DELTA);
            sm.setValue(-5.432);
            assertEquals(-5.432, (double) sm.getValue(), DELTA);
            assertEquals(-5.332, (double) sm.getNextValue(), DELTA);
            assertEquals(null, sm.getPreviousValue());
        }

        {
            Constraints con = new Constraints();
            con.setMaximalExclusive("5.5");
            con.setMinimalExclusive("2.2");
            SpinnerModel sm = ConstraintSpinnerModelFactory.createRangeConstrainedRealModel(con);
            assertEquals(2.201, (double) sm.getValue(), DELTA);
            assertEquals(2.301, (double) sm.getNextValue(), DELTA);
            assertEquals(null, sm.getPreviousValue());
            sm.setValue(5.432);
            assertEquals(5.432, (double) sm.getValue(), DELTA);
            assertEquals(null, sm.getNextValue());
            assertEquals(5.332, (double) sm.getPreviousValue(), DELTA);
        }

        {
            Constraints con = new Constraints();
            con.setMaximalExclusive("-2.2");
            con.setMinimalExclusive("-5.5");
            SpinnerModel sm = ConstraintSpinnerModelFactory.createRangeConstrainedRealModel(con);
            assertEquals(-2.201, (double) sm.getValue(), DELTA);
            assertEquals(null, sm.getNextValue());
            assertEquals(-2.301, (double) sm.getPreviousValue(), DELTA);
            sm.setValue(-5.432);
            assertEquals(-5.432, (double) sm.getValue(), DELTA);
            assertEquals(-5.332, (double) sm.getNextValue(), DELTA);
            assertEquals(null, sm.getPreviousValue());
        }

        {
            Constraints con = new Constraints();
            con.setMaximalInclusive("-5.5");
            con.setMinimalInclusive("-2.2");
            try {
                ConstraintSpinnerModelFactory.createRangeConstrainedRealModel(con);
                fail("IllegalArgumentException was expected but not thrown.");
            } catch (IllegalArgumentException ex) {
            } catch (Exception ex) {
                fail("Only a IllegalArgumentException was expected.");
            }
        }
    }

    @Test
    public void createRangeConstrainedDateModel() {
        {
            Constraints con = new Constraints();
            con.setMaximalInclusive("2019-12-31");
            LocalDate initDate = LocalDate.of(2019, 12, 31);
            AbstractSpinnerModel asm = ConstraintSpinnerModelFactory.createRangeConstrainedDateModel(initDate, con);
            assertEquals(asm.getValue(), initDate);
            assertEquals(asm.getNextValue(), null);
            assertEquals(asm.getPreviousValue(), LocalDate.of(2019, 12, 30));
        }

        {
            Constraints con = new Constraints();
            con.setMinimalInclusive("20200101");
            LocalDate initDate = LocalDate.of(2020, 1, 1);
            AbstractSpinnerModel asm = ConstraintSpinnerModelFactory.createRangeConstrainedDateModel(initDate, con);
            assertEquals(asm.getValue(), initDate);
            assertEquals(asm.getNextValue(), LocalDate.of(2020, 1, 2));
            assertEquals(asm.getPreviousValue(), null);
        }

        {
            Constraints con = new Constraints();
            con.setMinimalInclusive("2020-01-01");
            con.setMaximalInclusive("2020-12-31");
            LocalDate initDate = LocalDate.of(2019, 10, 11);
            AbstractSpinnerModel asm = ConstraintSpinnerModelFactory.createRangeConstrainedDateModel(initDate, con);
            assertEquals(asm.getValue(), LocalDate.of(2020, 1, 1));
            assertEquals(asm.getNextValue(), LocalDate.of(2020, 1, 2));
            assertEquals(asm.getPreviousValue(), null);
            initDate = LocalDate.of(2021, 2, 3);
            asm = ConstraintSpinnerModelFactory.createRangeConstrainedDateModel(initDate, con);
            assertEquals(asm.getValue(), LocalDate.of(2020, 12, 31));
            assertEquals(asm.getNextValue(), null);
            assertEquals(asm.getPreviousValue(), LocalDate.of(2020, 12, 30));
        }

        {
            Constraints con = new Constraints();
            con.setMinimalExclusive("2020-01-01");
            con.setMaximalExclusive("2020-12-31");
            LocalDate initDate = LocalDate.of(2019, 10, 11);
            AbstractSpinnerModel asm = ConstraintSpinnerModelFactory.createRangeConstrainedDateModel(initDate, con);
            assertEquals(asm.getValue(), LocalDate.of(2020, 1, 2));
            assertEquals(asm.getNextValue(), LocalDate.of(2020, 1, 3));
            assertEquals(asm.getPreviousValue(), null);
            initDate = LocalDate.of(2021, 2, 3);
            asm = ConstraintSpinnerModelFactory.createRangeConstrainedDateModel(initDate, con);
            assertEquals(asm.getValue(), LocalDate.of(2020, 12, 30));
            assertEquals(asm.getNextValue(), null);
            assertEquals(asm.getPreviousValue(), LocalDate.of(2020, 12, 29));
        }

        {
            // Exlusive binds more then inclusive.
            Constraints con = new Constraints();
            con.setMinimalExclusive("2020-01-01");
            con.setMinimalInclusive("2020-01-01");
            con.setMaximalExclusive("2020-12-31");
            con.setMaximalInclusive("2020-12-31");
            LocalDate initDate = LocalDate.of(2019, 10, 11);
            AbstractSpinnerModel asm = ConstraintSpinnerModelFactory.createRangeConstrainedDateModel(initDate, con);
            assertEquals(asm.getValue(), LocalDate.of(2020, 1, 2));
            assertEquals(asm.getNextValue(), LocalDate.of(2020, 1, 3));
            assertEquals(asm.getPreviousValue(), null);
            initDate = LocalDate.of(2021, 2, 3);
            asm = ConstraintSpinnerModelFactory.createRangeConstrainedDateModel(initDate, con);
            assertEquals(asm.getValue(), LocalDate.of(2020, 12, 30));
            assertEquals(asm.getNextValue(), null);
            assertEquals(asm.getPreviousValue(), LocalDate.of(2020, 12, 29));
        }
    }

    @Test
    public void createRangeConstrainedTimeModel() {
        {
            Constraints con = new Constraints();
            con.setMaximalInclusive("185959");
            LocalTime initTime = LocalTime.of(18, 59, 59);
            AbstractSpinnerModel asm = ConstraintSpinnerModelFactory.createRangeConstrainedTimeModel(initTime, con);
            assertEquals(asm.getValue(), initTime);
            assertEquals(asm.getNextValue(), null);
            assertEquals(asm.getPreviousValue(), LocalTime.of(18, 58, 59));
        }

        {
            Constraints con = new Constraints();
            con.setMinimalInclusive("07:00:00");
            LocalTime initTime = LocalTime.of(7, 0, 0);
            AbstractSpinnerModel asm = ConstraintSpinnerModelFactory.createRangeConstrainedTimeModel(initTime, con);
            assertEquals(asm.getValue(), initTime);
            assertEquals(asm.getNextValue(), LocalTime.of(7, 1, 0));
            assertEquals(asm.getPreviousValue(), null);
        }

        {
            Constraints con = new Constraints();
            con.setMinimalInclusive("08:00:00");
            con.setMaximalInclusive("20:00:00");
            LocalTime initTime = LocalTime.of(7, 0, 0);
            AbstractSpinnerModel asm = ConstraintSpinnerModelFactory.createRangeConstrainedTimeModel(initTime, con);
            assertEquals(asm.getValue(), LocalTime.of(8, 0, 0));
            assertEquals(asm.getNextValue(), LocalTime.of(8, 1, 0));
            assertEquals(asm.getPreviousValue(), null);
            initTime = LocalTime.of(21, 0, 0);
            asm = ConstraintSpinnerModelFactory.createRangeConstrainedTimeModel(initTime, con);
            assertEquals(asm.getValue(), LocalTime.of(20, 0, 0));
            assertEquals(asm.getNextValue(), null);
            assertEquals(asm.getPreviousValue(), LocalTime.of(19, 59, 0));
        }

        {
            Constraints con = new Constraints();
            con.setMinimalExclusive("08:00:00");
            con.setMaximalExclusive("20:00:00");
            LocalTime initTime = LocalTime.of(7, 0, 0);
            AbstractSpinnerModel asm = ConstraintSpinnerModelFactory.createRangeConstrainedTimeModel(initTime, con);
            assertEquals(asm.getValue(), LocalTime.of(8, 0, 1));
            assertEquals(asm.getNextValue(), LocalTime.of(8, 1, 1));
            assertEquals(asm.getPreviousValue(), null);
            initTime = LocalTime.of(21, 0, 0);
            asm = ConstraintSpinnerModelFactory.createRangeConstrainedTimeModel(initTime, con);
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
            AbstractSpinnerModel asm = ConstraintSpinnerModelFactory.createRangeConstrainedTimeModel(initTime, con);
            assertEquals(asm.getValue(), LocalTime.of(8, 0, 1));
            assertEquals(asm.getNextValue(), LocalTime.of(8, 1, 1));
            assertEquals(asm.getPreviousValue(), null);
            initTime = LocalTime.of(21, 0, 0);
            asm = ConstraintSpinnerModelFactory.createRangeConstrainedTimeModel(initTime, con);
            assertEquals(asm.getValue(), LocalTime.of(19, 59, 59));
            assertEquals(asm.getNextValue(), null);
            assertEquals(asm.getPreviousValue(), LocalTime.of(19, 58, 59));
        }
    }
}
