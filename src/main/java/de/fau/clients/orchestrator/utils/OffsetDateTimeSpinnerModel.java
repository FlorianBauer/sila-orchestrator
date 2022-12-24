package de.fau.clients.orchestrator.utils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import javax.swing.AbstractSpinnerModel;
import lombok.NonNull;

/**
 * A custom Spinner model based on the <code>OffsetDateTime</code> type introduced in Java 8. This
 * is necessary since the default <code>SpinnerDateModel</code>, based on the old <code>Date</code>
 * type, is buggy and broken beyond repairs.
 */
@SuppressWarnings("serial")
public class OffsetDateTimeSpinnerModel extends AbstractSpinnerModel {

    private final ZoneOffset offset;
    private final OffsetDateTime start;
    private final OffsetDateTime end;
    private final ChronoUnit step;
    private OffsetDateTime currentValue;

    /**
     * Constructor.
     *
     * @param initValue The time to initialize the spinner with (must not be <code>null</code>).
     * Also determines the used zone offset.
     * @param start The min. time limit or <code>null</code> for no limit.
     * @param end The max. time limit or <code>null</code> for no limit.
     * @param step The step size or <code>null</code> for <code>ChronoUnit.DAYS</code> as default.
     *
     * @see OffsetDateTime
     * @see ChronoUnit
     */
    public OffsetDateTimeSpinnerModel(
            @NonNull final OffsetDateTime initValue,
            final OffsetDateTime start,
            final OffsetDateTime end,
            final ChronoUnit step
    ) {
        this.currentValue = initValue;
        this.offset = initValue.getOffset();
        this.start = (start != null) ? start : OffsetDateTime.MIN;
        this.end = (end != null) ? end : OffsetDateTime.MAX;
        this.step = (step != null) ? step : ChronoUnit.DAYS;

        if (initValue.compareTo(this.start) < 0) {
            this.currentValue = this.start.withOffsetSameInstant(offset);
        }

        if (initValue.compareTo(this.end) > 0) {
            this.currentValue = this.end.withOffsetSameInstant(offset);
        }
    }

    @Override
    public void setValue(Object value) {
        if (value != null) {
            final OffsetDateTime tmp = (OffsetDateTime) value;
            if (tmp.compareTo(start) >= 0 && tmp.compareTo(end) <= 0) {
                currentValue = tmp;
            }
        }
        // inform the editor to (re-)set the value
        this.fireStateChanged();
    }

    @Override
    public Object getValue() {
        return currentValue.withOffsetSameInstant(offset);
    }

    @Override
    public Object getNextValue() {
        final OffsetDateTime next = currentValue.plus(1, step);
        if (next.isAfter(end)) {
            return null;
        } else {
            return next.withOffsetSameInstant(offset);
        }
    }

    @Override
    public Object getPreviousValue() {
        final OffsetDateTime previous = currentValue.minus(1, step);
        if (previous.isBefore(start)) {
            return null;
        } else {
            return previous.withOffsetSameInstant(offset);
        }
    }
}
