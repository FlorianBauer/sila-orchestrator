package de.fau.clients.orchestrator.utils;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import javax.swing.AbstractSpinnerModel;

/**
 * A custom Spinner model based on the new <code>LocalTime</code> type introduced in Java 8. This is
 * necessary since the default <code>SpinnerDateModel</code>, based on the old <code>Date</code>
 * type, is buggy and broken beyond repairs.
 */
@SuppressWarnings("serial")
public class LocalTimeSpinnerModel extends AbstractSpinnerModel {

    private final LocalTime start;
    private final LocalTime end;
    private final ChronoUnit step;
    private LocalTime currentValue;

    public LocalTimeSpinnerModel(
            LocalTime initValue,
            LocalTime start,
            LocalTime end,
            ChronoUnit step) {
        this.currentValue = initValue;
        this.start = start;
        this.end = end;
        this.step = step;
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
        final LocalTime next = currentValue.plus(step.getDuration());
        if (next.isAfter(end)) {
            return null;
        } else {
            return next;
        }
    }

    @Override
    public Object getPreviousValue() {
        final LocalTime previous = currentValue.minus(step.getDuration());
        if (previous.isBefore(start)) {
            return null;
        } else {
            return previous;
        }
    }
}
