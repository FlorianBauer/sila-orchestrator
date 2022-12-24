package de.fau.clients.orchestrator.utils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import javax.swing.AbstractSpinnerModel;
import lombok.NonNull;

/**
 * A custom Spinner model based on the <code>OffsetTime</code> type introduced in Java 8. This is
 * necessary since the default <code>SpinnerDateModel</code>, based on the old <code>Date</code>
 * type, is buggy and broken beyond repairs.
 */
@SuppressWarnings("serial")
public class OffsetTimeSpinnerModel extends AbstractSpinnerModel {

    /**
     * A base-date and the conversion to an OffsetDateTime is necessary, since the zone-offset may
     * cause a day-overflow resulting in a false handling of the min/max-bounds.
     */
    private static final LocalDate BASE_DATE = LocalDate.of(2000, 1, 1);
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
     * @param step The step size or <code>null</code> for <code>ChronoUnit.MINUTES</code> as
     * default.
     *
     * @see OffsetTime
     * @see ChronoUnit
     */
    public OffsetTimeSpinnerModel(
            @NonNull final OffsetTime initValue,
            final OffsetTime start,
            final OffsetTime end,
            final ChronoUnit step
    ) {
        // Adding a Date is neccesary to avoid day overflows when converting zone offsets.
        final OffsetDateTime initOdt = initValue.atDate(BASE_DATE);
        this.currentValue = initValue.atDate(BASE_DATE);
        this.offset = initValue.getOffset();
        this.start = (start != null) ? start.atDate(BASE_DATE) : OffsetTime.MIN.atDate(BASE_DATE);
        this.end = (end != null) ? end.atDate(BASE_DATE) : OffsetTime.MAX.atDate(BASE_DATE);
        this.step = (step != null) ? step : ChronoUnit.MINUTES;
        if (initOdt.isBefore(this.start)) {
            this.currentValue = this.start.withOffsetSameInstant(offset);
        }

        if (initOdt.isAfter(this.end)) {
            this.currentValue = this.end.withOffsetSameInstant(offset);
        }
    }

    @Override
    public void setValue(Object value) {
        if (value != null) {
            final OffsetDateTime tmp = ((OffsetTime) value).atDate(BASE_DATE).withOffsetSameInstant(offset);
            if (tmp.compareTo(start) >= 0 && tmp.compareTo(end) <= 0) {
                currentValue = tmp;
            }
        }
        // inform the editor to (re-)set the value
        this.fireStateChanged();
    }

    @Override
    public Object getValue() {
        return currentValue.toOffsetTime();
    }

    @Override
    public Object getNextValue() {
        final OffsetDateTime next = currentValue.plus(1, step);
        if (next.isAfter(end)) {
            return null;
        } else {
            return next.toOffsetTime();
        }
    }

    @Override
    public Object getPreviousValue() {
        final OffsetDateTime previous = currentValue.minus(1, step);
        if (previous.isBefore(start)) {
            return null;
        } else {
            return previous.toOffsetTime();
        }
    }
}
