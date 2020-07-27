package de.fau.clients.orchestrator.nodes;

import com.fasterxml.jackson.databind.JsonNode;
import java.awt.Dimension;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
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
 * A Factory for <code>BasicNode</code> objects.
 *
 * @see BasicNode
 */
public final class BasicNodeFactory {

    public static final int MAX_HEIGHT = 42;
    public static final Dimension MAX_SIZE_TEXT_FIELD = new Dimension(4096, MAX_HEIGHT);
    public static final Dimension MAX_SIZE_NUMERIC_SPINNER = new Dimension(160, MAX_HEIGHT);
    public static final Dimension MAX_SIZE_DATE_TIME_SPINNER = new Dimension(160, MAX_HEIGHT);
    public static final Dimension MAX_SIZE_TIMESTAMP_SPINNER = new Dimension(220, MAX_HEIGHT);
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

    /**
     * Creates a <code>BasicNode</code> of the type <code>BasicType.BOOLEAN</code>. If the given
     * jsonNode is <code>null</code>, the node is initialize with <code>false</code>.
     *
     * @param jsonNode A JSON node with a value to initialize or <code>null</code>.
     * @param isEditable Determines wether the user can edit the represented value or not.
     * @return The initialize BasicNode representing a boolean value.
     */
    protected static BasicNode createBooleanTypeFromJson(final JsonNode jsonNode, boolean isEditable) {
        final JCheckBox checkBox = new JCheckBox();
        checkBox.setEnabled(isEditable);
        checkBox.setSelected((jsonNode != null) ? jsonNode.asBoolean() : false);
        final Supplier<String> supp = () -> (checkBox.isSelected() ? "true" : "false");
        return new BasicNode(BasicType.BOOLEAN, checkBox, supp);
    }

    /**
     * Creates a <code>BasicNode</code> of the type <code>BasicType.DATE</code>. If the given
     * jsonNode is <code>null</code>, the node is initialize with the current date.
     *
     * @param jsonNode A JSON node with a value to initialize or <code>null</code>.
     * @param isEditable Determines wether the user can edit the represented value or not.
     * @return The initialize BasicNode representing a date value.
     */
    protected static BasicNode createDateTypeFromJson(final JsonNode jsonNode, boolean isEditable) {
        if (isEditable) {
            final SpinnerDateModel model = new SpinnerDateModel();
            final JSpinner dateSpinner = new JSpinner(model);
            dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, DATE_FORMAT));
            dateSpinner.setMaximumSize(MAX_SIZE_DATE_TIME_SPINNER);
            final LocalDate localDate;
            if (jsonNode != null) {
                localDate = DateTimeUtils.parseIsoDate(jsonNode.asText());
            } else {
                localDate = LocalDate.now();
            }
            model.setValue(Date.from(localDate.atStartOfDay().atOffset(DateTimeUtils.LOCAL_OFFSET).toInstant()));
            final Supplier<String> supp = () -> {
                return LocalDate.ofInstant(model.getDate().toInstant(), DateTimeUtils.LOCAL_OFFSET).toString();
            };
            return new BasicNode(BasicType.DATE, dateSpinner, supp);
        } else {
            final JTextField strField = new JTextField();
            strField.setEditable(false);
            strField.setMaximumSize(MAX_SIZE_DATE_TIME_SPINNER);
            strField.setText((jsonNode != null) ? jsonNode.asText() : LocalDate.now().toString());
            return new BasicNode(BasicType.DATE, strField, () -> (strField.getText()));
        }
    }

    /**
     * Creates a <code>BasicNode</code> of the type <code>BasicType.INTEGER</code>. If the given
     * jsonNode is <code>null</code>, the node is initialize with <code>0</code>.
     *
     * @param jsonNode A JSON node with a value to initialize or <code>null</code>.
     * @param isEditable Determines wether the user can edit the represented value or not.
     * @return The initialize BasicNode representing a integer value.
     */
    protected static BasicNode createIntegerTypeFromJson(final JsonNode jsonNode, boolean isEditable) {
        if (isEditable) {
            final SpinnerModel model = new SpinnerNumberModel();
            final JSpinner spinner = new JSpinner(model);
            spinner.setMaximumSize(MAX_SIZE_NUMERIC_SPINNER);
            model.setValue((jsonNode != null) ? jsonNode.asLong() : 0L);
            return new BasicNode(BasicType.INTEGER, spinner, () -> (spinner.getValue().toString()));
        } else {
            final JTextField strField = new JTextField();
            strField.setEditable(false);
            strField.setMaximumSize(MAX_SIZE_NUMERIC_SPINNER);
            strField.setText((jsonNode != null) ? jsonNode.asText() : "0");
            return new BasicNode(BasicType.INTEGER, strField, () -> (strField.getText()));
        }
    }

    /**
     * Creates a <code>BasicNode</code> of the type <code>BasicType.REAL</code>. If the given
     * jsonNode is <code>null</code>, the node is initialize with <code>0.0</code>.
     *
     * @param jsonNode A JSON node with a value to initialize or <code>null</code>.
     * @param isEditable Determines wether the user can edit the represented value or not.
     * @return The initialize BasicNode representing a double value.
     */
    protected static BasicNode createRealTypeFromJson(final JsonNode jsonNode, boolean isEditable) {
        if (isEditable) {
            final SpinnerModel model = new SpinnerNumberModel(0.0, null, null, REAL_STEP_SIZE);
            final JSpinner spinner = new JSpinner(model);
            spinner.setMaximumSize(MAX_SIZE_NUMERIC_SPINNER);
            model.setValue((jsonNode != null) ? jsonNode.asDouble() : 0.0);
            return new BasicNode(BasicType.REAL, spinner, () -> (spinner.getValue().toString()));
        } else {
            final JTextField strField = new JTextField();
            strField.setEditable(false);
            strField.setMaximumSize(MAX_SIZE_NUMERIC_SPINNER);
            strField.setText((jsonNode != null) ? jsonNode.asText() : "0.0");
            return new BasicNode(BasicType.REAL, strField, () -> (strField.getText()));
        }
    }

    /**
     * Creates a <code>BasicNode</code> of the type <code>BasicType.STRING</code>. If the given
     * jsonNode is <code>null</code>, the node is initialize with an empty String.
     *
     * @param jsonNode A JSON node with a value to initialize or <code>null</code>.
     * @param isEditable Determines wether the user can edit the represented value or not.
     * @return The initialize BasicNode representing a String.
     */
    protected static BasicNode createStringTypeFromJson(final JsonNode jsonNode, boolean isEditable) {
        final JTextField strField = new JTextField();
        strField.setEditable(isEditable);
        strField.setMaximumSize(MAX_SIZE_TEXT_FIELD);
        strField.setText((jsonNode != null) ? jsonNode.asText() : "");
        return new BasicNode(BasicType.STRING, strField, () -> (strField.getText()));
    }

    /**
     * Creates a <code>BasicNode</code> of the type <code>BasicType.TIME</code>. If the given
     * jsonNode is <code>null</code>, the node is initialize with the current time.
     *
     * @param jsonNode A JSON node with a value to initialize or <code>null</code>.
     * @param isEditable Determines wether the user can edit the represented value or not.
     * @return The initialize BasicNode representing a time value.
     */
    protected static BasicNode createTimeTypeFromJson(final JsonNode jsonNode, boolean isEditable) {
        if (isEditable) {
            final SpinnerDateModel model = new SpinnerDateModel();
            final JSpinner timeSpinner = new JSpinner(model);
            timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, TIME_FORMAT));
            timeSpinner.setMaximumSize(MAX_SIZE_DATE_TIME_SPINNER);
            final OffsetDateTime dateTime;
            if (jsonNode != null) {
                dateTime = DateTimeUtils.parseIsoTime(jsonNode.asText()).atDate(LocalDate.now());
            } else {
                dateTime = OffsetDateTime.now();
            }
            model.setValue(Date.from(dateTime.toInstant()));
            final Supplier<String> supp = () -> {
                return OffsetTime.ofInstant(model.getDate().toInstant(), DateTimeUtils.LOCAL_OFFSET)
                        .withOffsetSameInstant(ZoneOffset.UTC)
                        .truncatedTo(ChronoUnit.MILLIS)
                        .toString();
            };
            return new BasicNode(BasicType.TIME, timeSpinner, supp);
        } else {
            final JTextField strField = new JTextField();
            strField.setEditable(false);
            strField.setMaximumSize(MAX_SIZE_DATE_TIME_SPINNER);
            final String txt;
            if (jsonNode != null) {
                txt = jsonNode.asText();
            } else {
                txt = OffsetTime.now()
                        .withOffsetSameInstant(ZoneOffset.UTC)
                        .truncatedTo(ChronoUnit.MILLIS)
                        .toString();
            }
            strField.setText(txt);
            return new BasicNode(BasicType.TIME, strField, () -> (strField.getText()));
        }
    }

    /**
     * Creates a <code>BasicNode</code> of the type <code>BasicType.TIMESTAMP</code>. If the given
     * jsonNode is <code>null</code>, the node is initialize with the current date and time.
     *
     * @param jsonNode A JSON node with a value to initialize or <code>null</code>.
     * @param isEditable Determines wether the user can edit the represented value or not.
     * @return The initialize BasicNode representing a timestamp value.
     */
    protected static BasicNode createTimestampTypeFromJson(final JsonNode jsonNode, boolean isEditable) {
        if (isEditable) {
            final SpinnerDateModel model = new SpinnerDateModel();
            final JSpinner timeStampSpinner = new JSpinner(model);
            timeStampSpinner.setEditor(new JSpinner.DateEditor(timeStampSpinner, DATE_TIME_FORMAT));
            timeStampSpinner.setMaximumSize(MAX_SIZE_TIMESTAMP_SPINNER);
            final OffsetDateTime dateTime;
            if (jsonNode != null) {
                dateTime = DateTimeUtils.parseIsoDateTime(jsonNode.asText());
            } else {
                dateTime = OffsetDateTime.now();
            }
            model.setValue(Date.from(dateTime.toInstant()));
            final Supplier<String> supp = () -> {
                return OffsetDateTime.ofInstant(model.getDate().toInstant(), ZoneOffset.UTC)
                        .truncatedTo(ChronoUnit.MILLIS)
                        .toString();
            };
            return new BasicNode(BasicType.TIMESTAMP, timeStampSpinner, supp);
        } else {
            final JTextField strField = new JTextField();
            strField.setEditable(false);
            strField.setMaximumSize(MAX_SIZE_TIMESTAMP_SPINNER);
            final String txt;
            if (jsonNode != null) {
                txt = jsonNode.asText();
            } else {
                txt = OffsetDateTime.now()
                        .withOffsetSameInstant(ZoneOffset.UTC)
                        .truncatedTo(ChronoUnit.MILLIS)
                        .toString();
            }
            strField.setText(txt);
            return new BasicNode(BasicType.TIMESTAMP, strField, () -> (strField.getText()));
        }
    }
}
