package de.fau.clients.orchestrator.utils;

import java.text.ParseException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import javax.swing.JSpinner;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class OffsetDateTimeSpinnerEditorTest {

    @Test
    public void offsetTimestampSpinnerEditor() throws ParseException {
        final JSpinner spinner = new JSpinner();
        final OffsetDateTime init = OffsetDateTime.now();
        spinner.setModel(new OffsetDateTimeSpinnerModel(
                init.withOffsetSameInstant(ZoneOffset.UTC), null, null, null));

        final OffsetDateTimeSpinnerEditor spinnerEditor = new OffsetDateTimeSpinnerEditor(spinner,
                OffsetDateTimeSpinnerEditor.FormatterType.OFFSET_TIMESTAMP);
        spinner.setEditor(spinnerEditor);

        final OffsetDateTime tmp = OffsetDateTime.of(2020, 9, 25, 12, 34, 56, 0,
                ZoneOffset.ofHours(3));
        spinnerEditor.getTextField().setText(tmp.toString());
        assertEquals("2020-09-25T12:34:56+03:00", spinnerEditor.getTextField().getText());
        spinnerEditor.getTextField().setValue(tmp);
        assertEquals(tmp.withOffsetSameInstant(ZoneOffset.UTC),
                spinnerEditor.getTextField().getValue());
        assertEquals("2020-09-25T09:34:56Z", spinnerEditor.getTextField().getText());

        spinner.setModel(new OffsetDateTimeSpinnerModel(
                init.withOffsetSameInstant(ZoneOffset.ofHours(2)), null, null, null));
        spinnerEditor.getTextField().setText(tmp.toString());
        assertEquals("2020-09-25T12:34:56+03:00", spinnerEditor.getTextField().getText());
        spinnerEditor.getTextField().setValue(tmp);
        assertEquals(tmp.withOffsetSameInstant(ZoneOffset.ofHours(2)),
                spinnerEditor.getTextField().getValue());
        assertEquals("2020-09-25T11:34:56+02:00", spinnerEditor.getTextField().getText());

        spinner.setModel(new OffsetDateTimeSpinnerModel(
                init.withOffsetSameInstant(DateTimeParser.LOCAL_OFFSET), null, null, null));
        spinnerEditor.getTextField().setText("2020-10-11T12:13:14");
        assertEquals("2020-10-11T12:13:14", spinnerEditor.getTextField().getText());
        final OffsetDateTime exp = OffsetDateTime.of(2020, 10, 11, 12, 13, 14, 0,
                DateTimeParser.LOCAL_OFFSET);
        spinnerEditor.commitEdit();
        assertEquals(exp, spinnerEditor.getTextField().getValue());
    }

    @Test
    public void localTimestampSpinnerEditor() throws ParseException {
        final JSpinner spinner = new JSpinner();
        final OffsetDateTime init = OffsetDateTime.now();
        spinner.setModel(new OffsetDateTimeSpinnerModel(
                init.withOffsetSameInstant(ZoneOffset.UTC), null, null, null));

        final OffsetDateTimeSpinnerEditor spinnerEditor = new OffsetDateTimeSpinnerEditor(spinner,
                OffsetDateTimeSpinnerEditor.FormatterType.LOCAL_TIMESTAMP);
        spinner.setEditor(spinnerEditor);

        final OffsetDateTime tmp = OffsetDateTime.of(2020, 9, 25, 12, 34, 56, 0,
                ZoneOffset.ofHours(3));
        spinnerEditor.getTextField().setText(tmp.toString());
        assertEquals("2020-09-25T12:34:56+03:00", spinnerEditor.getTextField().getText());
        spinnerEditor.getTextField().setValue(tmp);
        assertEquals(tmp.withOffsetSameInstant(ZoneOffset.UTC),
                spinnerEditor.getTextField().getValue());
        assertEquals("2020-09-25T09:34:56", spinnerEditor.getTextField().getText());

        spinner.setModel(new OffsetDateTimeSpinnerModel(
                init.withOffsetSameInstant(ZoneOffset.ofHours(2)), null, null, null));
        spinnerEditor.getTextField().setText(tmp.toString());
        assertEquals("2020-09-25T12:34:56+03:00", spinnerEditor.getTextField().getText());
        spinnerEditor.getTextField().setValue(tmp);
        assertEquals(tmp.withOffsetSameInstant(ZoneOffset.ofHours(2)),
                spinnerEditor.getTextField().getValue());
        assertEquals("2020-09-25T11:34:56", spinnerEditor.getTextField().getText());

        spinner.setModel(new OffsetDateTimeSpinnerModel(
                init.withOffsetSameInstant(DateTimeParser.LOCAL_OFFSET), null, null, null));
        spinnerEditor.getTextField().setText("2020-10-11T12:13:14");
        assertEquals("2020-10-11T12:13:14", spinnerEditor.getTextField().getText());
        final OffsetDateTime exp = OffsetDateTime.of(2020, 10, 11, 12, 13, 14, 0,
                DateTimeParser.LOCAL_OFFSET);
        spinnerEditor.commitEdit();
        assertEquals(exp, spinnerEditor.getTextField().getValue());
    }
}
