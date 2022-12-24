package de.fau.clients.orchestrator.utils;

import java.text.ParseException;
import java.time.format.DateTimeParseException;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.text.DefaultFormatterFactory;

/**
 * A custom Spinner editor based on the <code>OffsetTime</code> type introduced in Java 8. This is
 * to simplify the handling of time types without converting to legacy types like the old
 * <code>Date</code>-type.
 */
@SuppressWarnings("serial")
public class OffsetTimeSpinnerEditor extends JSpinner.DefaultEditor {

    private static final JFormattedTextField.AbstractFormatter ltf = new OffsetTimeFormatter();

    public OffsetTimeSpinnerEditor(final JSpinner spinner) {
        super(spinner);
        final JFormattedTextField ftf = this.getTextField();
        ftf.setFormatterFactory(new DefaultFormatterFactory(ltf, ltf, ltf));
        ftf.setEditable(true);
    }

    @Override
    public void stateChanged(ChangeEvent evt) {
        getTextField().setValue(getSpinner().getValue());
    }

    static class OffsetTimeFormatter extends JFormattedTextField.AbstractFormatter {

        @Override
        public Object stringToValue(String string) {
            try {
                return DateTimeParser.parseIsoTime(string);
            } catch (DateTimeParseException ex) {
                return null;
            }
        }

        @Override
        public String valueToString(Object obj) throws ParseException {
            return obj.toString();
        }
    }
}
