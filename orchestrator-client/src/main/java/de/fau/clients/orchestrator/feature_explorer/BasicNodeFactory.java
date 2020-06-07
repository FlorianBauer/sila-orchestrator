package de.fau.clients.orchestrator.feature_explorer;

import com.fasterxml.jackson.databind.JsonNode;
import java.awt.Dimension;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.function.Supplier;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import sila_java.library.core.models.BasicType;

/**
 * A Factory for <code>BasicNode</code>-Objects. See also {@link BasicNode}.
 */
final class BasicNodeFactory {

    private static final int MAX_HEIGHT = 42;
    protected static final Dimension MAX_SIZE_TEXT_FIELD = new Dimension(4096, MAX_HEIGHT);
    protected static final Dimension MAX_SIZE_NUMERIC_SPINNER = new Dimension(160, MAX_HEIGHT);
    protected static final Dimension MAX_SIZE_DATE_TIME_SPINNER = new Dimension(160, MAX_HEIGHT);
    protected static final Dimension MAX_SIZE_TIMESTAMP_SPINNER = new Dimension(220, MAX_HEIGHT);
    /**
     * The precision of the offset-limit of an exclusive float range (e.g. the exclusive upper-limit
     * of the value <code>1.0</code> could be <code>0.9</code>, <code>0.99</code>,
     * <code>0.999</code>, etc.)
     */
    private static final double REAL_STEP_SIZE = 0.1;
    /**
     * The date-format used by the GUI-components.
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    /**
     * The time-format used by the GUI-components.
     */
    private static final String TIME_FORMAT = "HH:mm:ss";
    private static final String DATE_TIME_FORMAT = DATE_FORMAT + " " + TIME_FORMAT;

    private BasicNodeFactory() {
        throw new UnsupportedOperationException("Instantiation not allowed.");
    }

    protected static BasicNode createBooleanTypeFromJson(final JsonNode jsonNode, boolean isEditable) {
        final JCheckBox checkBox = new JCheckBox();
        checkBox.setEnabled(isEditable);
        if (jsonNode != null) {
            checkBox.setSelected(jsonNode.asBoolean());
        }
        final Supplier<String> supp = () -> (checkBox.isSelected() ? "true" : "false");
        return new BasicNode(BasicType.BOOLEAN, checkBox, supp);
    }

    protected static BasicNode createDateTypeFromJson(final JsonNode jsonNode, boolean isEditable) {
        if (isEditable) {
            final SpinnerDateModel model = new SpinnerDateModel();
            final JSpinner dateSpinner = new JSpinner(model);
            dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, DATE_FORMAT));
            dateSpinner.setMaximumSize(MAX_SIZE_DATE_TIME_SPINNER);
            if (jsonNode != null) {
                model.setValue(DateTimeUtils.parseIsoDate(jsonNode.asText()));
            }
            final Supplier<String> supp = () -> {
                Date date = (Date) dateSpinner.getValue();
                return LocalDate.ofInstant(date.toInstant(), DateTimeUtils.LOCAL_OFFSET).toString();
            };
            return new BasicNode(BasicType.DATE, dateSpinner, supp);
        } else {
            JTextField strField = new JTextField();
            strField.setEditable(false);
            strField.setMaximumSize(MAX_SIZE_DATE_TIME_SPINNER);
            if (jsonNode != null) {
                strField.setText(jsonNode.asText());
            }
            return new BasicNode(BasicType.DATE, strField, () -> (strField.getText()));
        }
    }

    protected static BasicNode createIntegerTypeFromJson(final JsonNode jsonNode, boolean isEditable) {
        if (isEditable) {
            final SpinnerModel model = new SpinnerNumberModel();
            final JSpinner spinner = new JSpinner(model);
            spinner.setMaximumSize(MAX_SIZE_NUMERIC_SPINNER);
            if (jsonNode != null) {
                model.setValue(jsonNode.asLong());
            }
            return new BasicNode(BasicType.INTEGER, spinner, () -> (spinner.getValue().toString()));
        } else {
            JTextField strField = new JTextField();
            strField.setEditable(false);
            strField.setMaximumSize(MAX_SIZE_NUMERIC_SPINNER);
            if (jsonNode != null) {
                strField.setText(jsonNode.asText());
            }
            return new BasicNode(BasicType.INTEGER, strField, () -> (strField.getText()));
        }
    }

    protected static BasicNode createRealTypeFromJson(final JsonNode jsonNode, boolean isEditable) {
        if (isEditable) {
            final SpinnerModel model = new SpinnerNumberModel(0.0, null, null, REAL_STEP_SIZE);
            final JSpinner spinner = new JSpinner(model);
            spinner.setMaximumSize(MAX_SIZE_NUMERIC_SPINNER);
            if (jsonNode != null) {
                model.setValue(jsonNode.asDouble());
            }
            return new BasicNode(BasicType.REAL, spinner, () -> (spinner.getValue().toString()));
        } else {
            JTextField strField = new JTextField();
            strField.setEditable(false);
            strField.setMaximumSize(MAX_SIZE_NUMERIC_SPINNER);
            if (jsonNode != null) {
                strField.setText(jsonNode.asText());
            }
            return new BasicNode(BasicType.REAL, strField, () -> (strField.getText()));
        }
    }

    protected static BasicNode createStringTypeFromJson(final JsonNode jsonNode, boolean isEditable) {
        final JTextField strField = new JTextField();
        strField.setEditable(isEditable);
        strField.setMaximumSize(MAX_SIZE_TEXT_FIELD);
        if (jsonNode != null) {
            strField.setText(jsonNode.asText());
        }
        return new BasicNode(BasicType.STRING, strField, () -> (strField.getText()));
    }

    protected static BasicNode createTimeTypeFromJson(final JsonNode jsonNode, boolean isEditable) {
        if (isEditable) {
            final SpinnerDateModel model = new SpinnerDateModel();
            final JSpinner timeSpinner = new JSpinner(model);
            timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, TIME_FORMAT));
            timeSpinner.setMaximumSize(MAX_SIZE_DATE_TIME_SPINNER);
            if (jsonNode != null) {
                model.setValue(DateTimeUtils.parseIsoTime(jsonNode.asText()));
            }
            final Supplier<String> supp = () -> {
                Date time = (Date) timeSpinner.getValue();
                return OffsetTime.ofInstant(time.toInstant(), DateTimeUtils.LOCAL_OFFSET)
                        .withOffsetSameInstant(ZoneOffset.UTC)
                        .toString();
            };
            return new BasicNode(BasicType.TIME, timeSpinner, supp);
        } else {
            JTextField strField = new JTextField();
            strField.setEditable(false);
            strField.setMaximumSize(MAX_SIZE_DATE_TIME_SPINNER);
            if (jsonNode != null) {
                strField.setText(jsonNode.asText());
            }
            return new BasicNode(BasicType.TIME, strField, () -> (strField.getText()));
        }
    }

    protected static BasicNode createTimestampTypeFromJson(final JsonNode jsonNode, boolean isEditable) {
        if (isEditable) {
            final SpinnerDateModel model = new SpinnerDateModel();
            final JSpinner timeStampSpinner = new JSpinner(model);
            timeStampSpinner.setEditor(new JSpinner.DateEditor(timeStampSpinner, DATE_TIME_FORMAT));
            timeStampSpinner.setMaximumSize(MAX_SIZE_TIMESTAMP_SPINNER);
            if (jsonNode != null) {
                model.setValue(DateTimeUtils.parseIsoDateTime(jsonNode.asText()));
            }
            final Supplier<String> supp = () -> {
                Date time = (Date) timeStampSpinner.getValue();
                return OffsetDateTime.ofInstant(time.toInstant(), ZoneOffset.UTC).toString();
            };
            return new BasicNode(BasicType.TIMESTAMP, timeStampSpinner, supp);
        } else {
            JTextField strField = new JTextField();
            strField.setEditable(false);
            strField.setMaximumSize(MAX_SIZE_TIMESTAMP_SPINNER);
            if (jsonNode != null) {
                strField.setText(jsonNode.asText());
            }
            return new BasicNode(BasicType.TIMESTAMP, strField, () -> (strField.getText()));
        }
    }

}