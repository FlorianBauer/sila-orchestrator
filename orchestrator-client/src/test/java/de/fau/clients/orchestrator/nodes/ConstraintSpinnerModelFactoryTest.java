package de.fau.clients.orchestrator.nodes;

import de.fau.clients.orchestrator.utils.DateTimeParser;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
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
            SpinnerModel sm = ConstraintSpinnerModelFactory.createRangeConstrainedIntModel(0, con);
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
            SpinnerModel sm = ConstraintSpinnerModelFactory.createRangeConstrainedIntModel(0, con);
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
            SpinnerModel sm = ConstraintSpinnerModelFactory.createRangeConstrainedIntModel(0, con);
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
            con.setMaximalExclusive("6");
            SpinnerModel sm = ConstraintSpinnerModelFactory.createRangeConstrainedIntModel(7, con);
            assertEquals(5, sm.getValue());
            assertEquals(null, sm.getNextValue());
            assertEquals(4, sm.getPreviousValue());
        }

        {
            Constraints con = new Constraints();
            con.setMaximalExclusive("-2");
            con.setMinimalExclusive("-6");
            SpinnerModel sm = ConstraintSpinnerModelFactory.createRangeConstrainedIntModel(0, con);
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
            SpinnerModel sm = ConstraintSpinnerModelFactory.createRangeConstrainedIntModel(0, con);
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
                ConstraintSpinnerModelFactory.createRangeConstrainedIntModel(0, con);
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
            SpinnerModel sm = ConstraintSpinnerModelFactory.createRangeConstrainedRealModel(0, con);
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
            SpinnerModel sm = ConstraintSpinnerModelFactory.createRangeConstrainedRealModel(0, con);
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
            SpinnerModel sm = ConstraintSpinnerModelFactory.createRangeConstrainedRealModel(0, con);
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
            con.setMaximalExclusive("5.5");
            SpinnerModel sm = ConstraintSpinnerModelFactory.createRangeConstrainedRealModel(7, con);
            assertEquals(5.499, (double) sm.getValue(), DELTA);
            assertEquals(null, sm.getNextValue());
            assertEquals(5.399, (double) sm.getPreviousValue(), DELTA);
        }

        {
            Constraints con = new Constraints();
            con.setMaximalExclusive("-2.2");
            con.setMinimalExclusive("-5.5");
            SpinnerModel sm = ConstraintSpinnerModelFactory.createRangeConstrainedRealModel(0, con);
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
                ConstraintSpinnerModelFactory.createRangeConstrainedRealModel(0, con);
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
            con.setMaximalInclusive("185959Z");
            OffsetTime initTime = OffsetTime.of(18, 59, 59, 0, ZoneOffset.UTC);
            AbstractSpinnerModel asm = ConstraintSpinnerModelFactory
                    .createRangeConstrainedTimeModel(initTime, con);
            assertEquals(asm.getValue(), initTime);
            assertEquals(asm.getNextValue(), null);
            assertEquals(asm.getPreviousValue(), OffsetTime.of(18, 58, 59, 0, ZoneOffset.UTC));
        }

        {
            Constraints con = new Constraints();
            con.setMinimalInclusive("07:00:00Z");
            OffsetTime initTime = OffsetTime.of(7, 0, 0, 0, ZoneOffset.UTC);
            AbstractSpinnerModel asm = ConstraintSpinnerModelFactory
                    .createRangeConstrainedTimeModel(initTime, con);
            assertEquals(asm.getValue(), initTime);
            assertEquals(asm.getNextValue(), OffsetTime.of(7, 1, 0, 0, ZoneOffset.UTC));
            assertEquals(asm.getPreviousValue(), null);
        }

        {
            Constraints con = new Constraints();
            con.setMaximalInclusive("185959");
            OffsetTime initTime = OffsetTime.of(18, 59, 59, 0, DateTimeParser.LOCAL_OFFSET);
            AbstractSpinnerModel asm = ConstraintSpinnerModelFactory
                    .createRangeConstrainedTimeModel(initTime, con);
            assertEquals(asm.getValue(), initTime);
            assertEquals(asm.getNextValue(), null);
            assertEquals(asm.getPreviousValue(), OffsetTime.of(18, 58, 59, 0, DateTimeParser.LOCAL_OFFSET));
        }

        {
            Constraints con = new Constraints();
            con.setMinimalInclusive("07:00:00");
            OffsetTime initTime = OffsetTime.of(7, 0, 0, 0, DateTimeParser.LOCAL_OFFSET);
            AbstractSpinnerModel asm = ConstraintSpinnerModelFactory
                    .createRangeConstrainedTimeModel(initTime, con);
            assertEquals(asm.getValue(), initTime);
            assertEquals(asm.getNextValue(), OffsetTime.of(7, 1, 0, 0, DateTimeParser.LOCAL_OFFSET));
            assertEquals(asm.getPreviousValue(), null);
        }

        {
            Constraints con = new Constraints();
            con.setMinimalInclusive("08:00:00Z");
            con.setMaximalInclusive("20:00:00Z");
            OffsetTime initTime = OffsetTime.of(7, 0, 0, 0, ZoneOffset.UTC);
            AbstractSpinnerModel asm = ConstraintSpinnerModelFactory
                    .createRangeConstrainedTimeModel(initTime, con);
            assertEquals(asm.getValue(), OffsetTime.of(8, 0, 0, 0, ZoneOffset.UTC));
            assertEquals(asm.getNextValue(), OffsetTime.of(8, 1, 0, 0, ZoneOffset.UTC));
            assertEquals(asm.getPreviousValue(), null);
            initTime = OffsetTime.of(21, 0, 0, 0, ZoneOffset.UTC);
            asm = ConstraintSpinnerModelFactory.createRangeConstrainedTimeModel(initTime, con);
            assertEquals(asm.getValue(), OffsetTime.of(20, 0, 0, 0, ZoneOffset.UTC));
            assertEquals(asm.getNextValue(), null);
            assertEquals(asm.getPreviousValue(), OffsetTime.of(19, 59, 0, 0, ZoneOffset.UTC));
        }

        {
            Constraints con = new Constraints();
            con.setMinimalExclusive("08:00:00Z");
            con.setMaximalExclusive("20:00:00Z");
            OffsetTime initTime = OffsetTime.of(7, 0, 0, 0, ZoneOffset.UTC);
            AbstractSpinnerModel asm = ConstraintSpinnerModelFactory
                    .createRangeConstrainedTimeModel(initTime, con);
            assertEquals(asm.getValue(), OffsetTime.of(8, 0, 1, 0, ZoneOffset.UTC));
            assertEquals(asm.getNextValue(), OffsetTime.of(8, 1, 1, 0, ZoneOffset.UTC));
            assertEquals(asm.getPreviousValue(), null);
            initTime = OffsetTime.of(21, 0, 0, 0, ZoneOffset.UTC);
            asm = ConstraintSpinnerModelFactory.createRangeConstrainedTimeModel(initTime, con);
            assertEquals(asm.getValue(), OffsetTime.of(19, 59, 59, 0, ZoneOffset.UTC));
            assertEquals(asm.getNextValue(), null);
            assertEquals(asm.getPreviousValue(), OffsetTime.of(19, 58, 59, 0, ZoneOffset.UTC));
        }

        {
            // Exlusive binds more then inclusive.
            Constraints con = new Constraints();
            con.setMinimalExclusive("08:00:00Z");
            con.setMinimalInclusive("08:00:00Z");
            con.setMaximalExclusive("20:00:00Z");
            con.setMaximalInclusive("20:00:00Z");
            OffsetTime initTime = OffsetTime.of(7, 0, 0, 0, ZoneOffset.UTC);
            AbstractSpinnerModel asm = ConstraintSpinnerModelFactory
                    .createRangeConstrainedTimeModel(initTime, con);
            assertEquals(asm.getValue(), OffsetTime.of(8, 0, 1, 0, ZoneOffset.UTC));
            assertEquals(asm.getNextValue(), OffsetTime.of(8, 1, 1, 0, ZoneOffset.UTC));
            assertEquals(asm.getPreviousValue(), null);
            initTime = OffsetTime.of(21, 0, 0, 0, ZoneOffset.UTC);
            asm = ConstraintSpinnerModelFactory.createRangeConstrainedTimeModel(initTime, con);
            assertEquals(asm.getValue(), OffsetTime.of(19, 59, 59, 0, ZoneOffset.UTC));
            assertEquals(asm.getNextValue(), null);
            assertEquals(asm.getPreviousValue(), OffsetTime.of(19, 58, 59, 0, ZoneOffset.UTC));
        }
    }

    @Test
    public void createRangeConstrainedDateTimeModel() {
        {
            Constraints con = new Constraints();
            con.setMaximalInclusive("2020-09-23T18:59:59Z");
            OffsetDateTime initDateTime = OffsetDateTime
                    .of(2020, 9, 23, 18, 59, 59, 0, ZoneOffset.UTC);
            AbstractSpinnerModel asm = ConstraintSpinnerModelFactory
                    .createRangeConstrainedDateTimeModel(initDateTime, con);
            assertEquals(initDateTime, asm.getValue());
            assertEquals(null, asm.getNextValue());
            assertEquals(initDateTime.minusDays(1), asm.getPreviousValue());
        }

        {
            Constraints con = new Constraints();
            con.setMinimalInclusive("2020-09-23T07:00:00Z");
            OffsetDateTime initDateTime = OffsetDateTime
                    .of(2020, 9, 23, 7, 0, 0, 0, ZoneOffset.UTC);
            AbstractSpinnerModel asm = ConstraintSpinnerModelFactory
                    .createRangeConstrainedDateTimeModel(initDateTime, con);
            assertEquals(initDateTime, asm.getValue());
            assertEquals(initDateTime.plusDays(1), asm.getNextValue());
            assertEquals(null, asm.getPreviousValue());
        }

        {
            Constraints con = new Constraints();
            con.setMaximalInclusive("2020-09-23T18:59:59Z");
            OffsetDateTime initDateTime = OffsetDateTime
                    .of(2020, 9, 24, 20, 59, 59, 0, ZoneOffset.UTC);
            OffsetDateTime exp = OffsetDateTime.of(2020, 9, 23, 18, 59, 59, 0, ZoneOffset.UTC);
            AbstractSpinnerModel asm = ConstraintSpinnerModelFactory
                    .createRangeConstrainedDateTimeModel(initDateTime, con);
            assertEquals(exp, asm.getValue());
            assertEquals(null, asm.getNextValue());
            assertEquals(exp.minusDays(1), asm.getPreviousValue());
        }

        {
            Constraints con = new Constraints();
            con.setMinimalInclusive("2020-09-23T07:00:00Z");
            OffsetDateTime initDateTime = OffsetDateTime
                    .of(2020, 9, 20, 5, 0, 0, 0, ZoneOffset.UTC);
            OffsetDateTime exp = OffsetDateTime.of(2020, 9, 23, 7, 0, 0, 0, ZoneOffset.UTC);
            AbstractSpinnerModel asm = ConstraintSpinnerModelFactory
                    .createRangeConstrainedDateTimeModel(initDateTime, con);
            assertEquals(exp, asm.getValue());
            assertEquals(exp.plusDays(1), asm.getNextValue());
            assertEquals(null, asm.getPreviousValue());
        }

        {
            Constraints con = new Constraints();
            con.setMaximalInclusive("2020-09-23T18:59:59Z");
            OffsetDateTime initDateTime = OffsetDateTime
                    .of(2020, 9, 23, 21, 59, 59, 0, ZoneOffset.ofHours(3));
            AbstractSpinnerModel asm = ConstraintSpinnerModelFactory
                    .createRangeConstrainedDateTimeModel(initDateTime, con);
            assertEquals(initDateTime, asm.getValue());
            assertEquals(null, asm.getNextValue());
            assertEquals(initDateTime.minusDays(1), asm.getPreviousValue());
        }

        {
            Constraints con = new Constraints();
            con.setMinimalInclusive("2020-09-23T07:00:00Z");
            OffsetDateTime initDateTime = OffsetDateTime
                    .of(2020, 9, 23, 4, 0, 0, 0, ZoneOffset.ofHours(-3));
            AbstractSpinnerModel asm = ConstraintSpinnerModelFactory
                    .createRangeConstrainedDateTimeModel(initDateTime, con);
            assertEquals(initDateTime, asm.getValue());
            assertEquals(initDateTime.plusDays(1), asm.getNextValue());
            assertEquals(null, asm.getPreviousValue());
        }
    }
}
