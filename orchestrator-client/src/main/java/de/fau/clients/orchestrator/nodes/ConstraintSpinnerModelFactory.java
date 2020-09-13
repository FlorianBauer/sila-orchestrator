package de.fau.clients.orchestrator.nodes;

import de.fau.clients.orchestrator.utils.DateTimeParser;
import de.fau.clients.orchestrator.utils.LocalDateSpinnerModel;
import de.fau.clients.orchestrator.utils.LocalTimeSpinnerModel;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import javax.swing.AbstractSpinnerModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import sila_java.library.core.models.Constraints;

/**
 * A Factory to create constrained <code>SpinnerModel</code>s for <code>Integer</code>,
 * <code>Double</code>, <code>LocalDate</code> and <code>LocalTime</code> types.
 */
public class ConstraintSpinnerModelFactory {

    /**
     * The precision of the offset-limit of an exclusive float range (e.g. the exclusive upper-limit
     * of the value <code>1.0</code> could be <code>0.9</code>, <code>0.99</code>,
     * <code>0.999</code>, etc.)
     */
    private static final double REAL_EXCLUSIVE_OFFSET = 0.001;
    private static final double REAL_STEP_SIZE = 0.1;

    private ConstraintSpinnerModelFactory() {
        throw new UnsupportedOperationException("Instantiation not allowed.");
    }

    /**
     * Creates a Integer based, range-limited model for constraining input in
     * <code>JSpinner</code>-components. This functions does not consider any
     * <code>Set</code>-constraints. Only the following functions describing the range-limits are
     * considered:
     * <ul>
     * <li><code>getMinimalExclusive()</code></li>
     * <li><code>getMinimalInclusive()</code></li>
     * <li><code>getMaximalExclusive()</code></li>
     * <li><code>getMaximalInclusive()</code></li>
     * </ul>
     *
     * @param constraints The SiLA-Constraints element defining the value limits.
     * @return The spinner-model for a <code>JSpinner</code>-component.
     */
    public static SpinnerModel createRangeConstrainedIntModel(final Constraints constraints) {
        int initVal = 0;
        Integer min = null;
        Integer max = null;
        String conStr = constraints.getMinimalExclusive();
        if (conStr != null) {
            min = Integer.parseInt(conStr) + 1;
            initVal = Math.max(min, initVal);
        }
        conStr = constraints.getMinimalInclusive();
        if (conStr != null) {
            min = Integer.parseInt(conStr);
            initVal = Math.max(min, initVal);
        }

        conStr = constraints.getMaximalExclusive();
        if (conStr != null) {
            max = Integer.parseInt(conStr) - 1;
        }
        conStr = constraints.getMaximalInclusive();
        if (conStr != null) {
            max = Integer.parseInt(conStr);
        }
        initVal = (max != null && max < initVal) ? max : initVal;
        return new SpinnerNumberModel((Number) initVal, min, max, 1);
    }

    /**
     * Creates a Double based, range-limited model to constrain input in
     * <code>JSpinner</code>-components. This functions does not consider any
     * <code>Set</code>-constraints. Only the following functions describing the range-limits are
     * considered:
     * <ul>
     * <li><code>getMinimalExclusive()</code></li>
     * <li><code>getMinimalInclusive()</code></li>
     * <li><code>getMaximalExclusive()</code></li>
     * <li><code>getMaximalInclusive()</code></li>
     * </ul>
     *
     * @param constraints The SiLA-Constraints element defining the value limits.
     * @return The spinner-model for a <code>JSpinner</code>-component.
     */
    public static SpinnerModel createRangeConstrainedRealModel(final Constraints constraints) {
        double initVal = 0.0;
        Double min = null;
        Double max = null;
        if (constraints.getMinimalExclusive() != null) {
            min = Double.parseDouble(constraints.getMinimalExclusive()) + REAL_EXCLUSIVE_OFFSET;
            initVal = Math.max(min, initVal);
        } else if (constraints.getMinimalInclusive() != null) {
            min = Double.parseDouble(constraints.getMinimalInclusive());
            initVal = Math.max(min, initVal);
        }

        if (constraints.getMaximalExclusive() != null) {
            max = Double.parseDouble(constraints.getMaximalExclusive()) - REAL_EXCLUSIVE_OFFSET;
        } else if (constraints.getMaximalInclusive() != null) {
            max = Double.parseDouble(constraints.getMaximalInclusive());
        }
        initVal = (max != null && max < initVal) ? max : initVal;
        return new SpinnerNumberModel((Number) initVal, min, max, REAL_STEP_SIZE);
    }

    /**
     * Creates a date based, range-limited model to constrain input in
     * <code>JSpinner</code>-components. This functions does not consider any
     * <code>Set</code>-constraints.
     *
     * @param initDate The initial date value the model is set to.
     * @param constraints The SiLA-Constraints element defining the date limits.
     * @return The spinner-model for a <code>JSpinner</code>-component.
     */
    public static AbstractSpinnerModel createRangeConstrainedDateModel(
            LocalDate initDate,
            final Constraints constraints) {

        LocalDate start = null;
        if (constraints.getMinimalExclusive() != null) {
            start = DateTimeParser.parseIsoDate(constraints.getMinimalExclusive()).plusDays(1);
        } else if (constraints.getMinimalInclusive() != null) {
            start = DateTimeParser.parseIsoDate(constraints.getMinimalInclusive());
        }

        if (start != null && start.isAfter(initDate)) {
            initDate = start;
        }

        LocalDate end = null;
        if (constraints.getMaximalExclusive() != null) {
            end = DateTimeParser.parseIsoDate(constraints.getMaximalExclusive()).minusDays(1);
        } else if (constraints.getMaximalInclusive() != null) {
            end = DateTimeParser.parseIsoDate(constraints.getMaximalInclusive());
        }

        if (end != null && end.isBefore(initDate)) {
            initDate = end;
        }
        return new LocalDateSpinnerModel(initDate, start, end, ChronoUnit.DAYS);
    }

    /**
     * Creates a local time based, range-limited model to constrain input in
     * <code>JSpinner</code>-components. This functions does not consider any
     * <code>Set</code>-constraints.
     *
     * @param initTime The initial time value the model is set to.
     * @param constraints The SiLA-Constraints element defining the time limits.
     * @return The spinner-model for a <code>JSpinner</code>-component.
     */
    public static AbstractSpinnerModel createRangeConstrainedTimeModel(
            LocalTime initTime,
            final Constraints constraints) {
        final LocalTime start;
        if (constraints.getMinimalExclusive() != null) {
            start = DateTimeParser.parseIsoTime(constraints.getMinimalExclusive())
                    .toLocalTime()
                    .plusSeconds(1);
        } else if (constraints.getMinimalInclusive() != null) {
            start = DateTimeParser.parseIsoTime(constraints.getMinimalInclusive())
                    .toLocalTime();
        } else {
            start = LocalTime.MIN;
        }

        final LocalTime end;
        if (constraints.getMaximalExclusive() != null) {
            end = DateTimeParser.parseIsoTime(constraints.getMaximalExclusive())
                    .toLocalTime()
                    .minusSeconds(1);
        } else if (constraints.getMaximalInclusive() != null) {
            end = DateTimeParser.parseIsoTime(constraints.getMaximalInclusive())
                    .toLocalTime();
        } else {
            end = LocalTime.MAX.truncatedTo(ChronoUnit.SECONDS);
        }

        if (initTime.compareTo(start) < 0) {
            initTime = start;
        }

        if (initTime.compareTo(end) > 0) {
            initTime = end;
        }
        return new LocalTimeSpinnerModel(initTime, start, end, ChronoUnit.MINUTES);
    }
}
