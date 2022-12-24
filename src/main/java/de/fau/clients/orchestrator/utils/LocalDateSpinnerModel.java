package de.fau.clients.orchestrator.utils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import javax.swing.AbstractSpinnerModel;
import lombok.NonNull;

/**
 * A custom Spinner model based on the <code>LocalDate</code> type introduced in Java 8. This is
 * necessary since the default <code>SpinnerDateModel</code>, based on the old <code>Date</code>
 * type, is buggy and broken beyond repairs.
 */
@SuppressWarnings("serial")
public class LocalDateSpinnerModel extends AbstractSpinnerModel {

    private final LocalDate start;
    private final LocalDate end;
    private final ChronoUnit step;
    private LocalDate currentValue;

    /**
     * Constructor.
     *
     * @param initValue The date to initialize the spinner with (must not be <code>null</code>).
     * @param start The min. date of the constraint or <code>null</code> for no min. limit.
     * @param end The max. date of the constraint or <code>null</code> for no max. limit.
     * @param step The unit of the spinner step or <code>null</code> for
     * <code>ChronoUnit.DAYS</code> as default.
     *
     * @see LocalDate
     * @see ChronoUnit
     */
    public LocalDateSpinnerModel(
            @NonNull final LocalDate initValue,
            final LocalDate start,
            final LocalDate end,
            final ChronoUnit step
    ) {
        this.currentValue = initValue;
        this.start = (start != null) ? start : LocalDate.MIN;
        this.end = (end != null) ? end : LocalDate.MAX;
        this.step = (step != null) ? step : ChronoUnit.DAYS;
    }

    @Override
    public void setValue(Object value) {
        if (value != null) {
            final LocalDate tmp = (LocalDate) value;
            if (start != null && end != null) {
                if (tmp.compareTo(start) >= 0 && tmp.compareTo(end) <= 0) {
                    currentValue = tmp;
                }
            } else if (start != null) {
                if (tmp.compareTo(start) >= 0) {
                    currentValue = tmp;
                }
            } else if (end != null) {
                if (tmp.compareTo(end) <= 0) {
                    currentValue = tmp;
                }
            } else {
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
        final LocalDate next = currentValue.plus(1, step);
        if (end != null) {
            if (next.isAfter(end)) {
                return null;
            }
        }
        return next;
    }

    @Override
    public Object getPreviousValue() {
        final LocalDate previous = currentValue.minus(1, step);
        if (start != null) {
            if (previous.isBefore(start)) {
                return null;
            }
        }
        return previous;
    }
}
