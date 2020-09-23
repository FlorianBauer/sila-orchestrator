package de.fau.clients.orchestrator.utils;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import javax.swing.AbstractSpinnerModel;
import lombok.NonNull;

/**
 * A custom Spinner model based on the <code>LocalTime</code> type introduced in Java 8. This is
 * necessary since the default <code>SpinnerDateModel</code>, based on the old <code>Date</code>
 * type, is buggy and broken beyond repairs.
 */
@SuppressWarnings("serial")
public class LocalTimeSpinnerModel extends AbstractSpinnerModel {

    private final LocalTime start;
    private final LocalTime end;
    private final ChronoUnit step;
    private LocalTime currentValue;

    /**
     * Constructor.
     *
     * @param initValue The time to initialize the spinner with (must not be <code>null</code>).
     * @param start The min. time limit or <code>null</code> for the start of the day.
     * @param end The max. time limit or <code>null</code> for the end of the day.
     * @param step The step size or <code>null</code> for <code>ChronoUnit.MINUTES</code> as
     * default.
     *
     * @see LocalTime
     * @see ChronoUnit
     */
    public LocalTimeSpinnerModel(
            @NonNull final LocalTime initValue,
            final LocalTime start,
            final LocalTime end,
            final ChronoUnit step
    ) {
        this.currentValue = initValue;
        this.start = start;
        this.end = end;
        this.step = (step != null) ? step : ChronoUnit.MINUTES;
    }

    @Override
    public void setValue(Object value) {
        if (value != null) {
            final LocalTime tmp = (LocalTime) value;
            if (tmp.compareTo(start) >= 0 && tmp.compareTo(end) <= 0) {
                currentValue = tmp;
            }
        }
        // inform the editor to (re-)set the value
        this.fireStateChanged();
    }

    @Override
    public Object getValue() {
        return currentValue;
    }

    @Override
    public Object getNextValue() {
        final LocalTime next = currentValue.plus(1, step);
        if (next.isAfter(end)) {
            return null;
        } else {
            return next;
        }
    }

    @Override
    public Object getPreviousValue() {
        final LocalTime previous = currentValue.minus(1, step);
        if (previous.isBefore(start)) {
            return null;
        } else {
            return previous;
        }
    }
}
