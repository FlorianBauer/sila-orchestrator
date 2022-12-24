package de.fau.clients.orchestrator.utils;

import java.text.ParseException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.text.DefaultFormatterFactory;
import lombok.NonNull;

/**
 * A custom Spinner editor based on the <code>OffsetDateTime</code> type introduced in Java 8. This
 * is to simplify the handling of time types without converting to legacy types like the old
 * <code>Date</code>-type.
 */
@SuppressWarnings("serial")
public class OffsetDateTimeSpinnerEditor extends JSpinner.DefaultEditor {

    public static enum FormatterType {
        OFFSET_TIMESTAMP,
        LOCAL_TIMESTAMP;
    }

    private static final JFormattedTextField.AbstractFormatter otf = new OffsetTimestampFormatter();
    private static final JFormattedTextField.AbstractFormatter ltf = new LocalTimestampFormatter();

    public OffsetDateTimeSpinnerEditor(
            @NonNull final JSpinner spinner,
            @NonNull final FormatterType type
    ) {
        super(spinner);
        final JFormattedTextField ftf = this.getTextField();
        ftf.setEditable(true);

        switch (type) {
            case OFFSET_TIMESTAMP:
                ftf.setFormatterFactory(new DefaultFormatterFactory(otf, otf, otf));
                break;
            case LOCAL_TIMESTAMP:
                ftf.setFormatterFactory(new DefaultFormatterFactory(ltf, ltf, ltf));
                break;
            default:
                throw new IllegalArgumentException("Not a vaild FormatterType.");
        }
    }

    @Override
    public void stateChanged(ChangeEvent evt) {
        getTextField().setValue(getSpinner().getValue());
    }

    static class OffsetTimestampFormatter extends JFormattedTextField.AbstractFormatter {

        @Override
        public Object stringToValue(String string) {
            try {
                return DateTimeParser.parseIsoDateTime(string);
            } catch (DateTimeParseException ex) {
                return null;
            }
        }

        @Override
        public String valueToString(Object obj) throws ParseException {
            return obj.toString();
        }
    }

    static class LocalTimestampFormatter extends OffsetTimestampFormatter {

        @Override
        public String valueToString(Object obj) throws ParseException {
            return ((OffsetDateTime) obj).toLocalDateTime().toString();
        }
    }
}
