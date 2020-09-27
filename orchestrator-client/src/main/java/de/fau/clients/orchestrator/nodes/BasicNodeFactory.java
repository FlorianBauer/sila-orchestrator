package de.fau.clients.orchestrator.nodes;

import com.fasterxml.jackson.databind.JsonNode;
import de.fau.clients.orchestrator.utils.DateTimeParser;
import de.fau.clients.orchestrator.utils.LocalDateSpinnerEditor;
import de.fau.clients.orchestrator.utils.LocalDateSpinnerModel;
import de.fau.clients.orchestrator.utils.OffsetDateTimeSpinnerEditor;
import de.fau.clients.orchestrator.utils.OffsetDateTimeSpinnerEditor.FormatterType;
import de.fau.clients.orchestrator.utils.OffsetDateTimeSpinnerModel;
import de.fau.clients.orchestrator.utils.OffsetTimeSpinnerEditor;
import de.fau.clients.orchestrator.utils.OffsetTimeSpinnerModel;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.function.Supplier;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import sila_java.library.core.models.BasicType;

/**
 * A Factory for <code>BasicNode</code> objects.
 *
 * @see BasicNode
 */
final class BasicNodeFactory {

    /**
     * The precision of the offset-limit of an exclusive float range (e.g. the exclusive upper-limit
     * of the value <code>1.0</code> could be <code>0.9</code>, <code>0.99</code>,
     * <code>0.999</code>, etc.)
     */
    private static final double REAL_STEP_SIZE = 0.1;

    private BasicNodeFactory() {
        throw new UnsupportedOperationException("Instantiation not allowed.");
    }

    protected static BasicNode create(final BasicType type) {
        switch (type) {
            case ANY:
                // TODO: implement
                return new BasicNode(type, new JLabel("placeholder 01"), () -> ("not implemented 01"));
            case BINARY:
                return BasicNodeFactory.createBinaryTypeFromJson(null, true);
            case BOOLEAN:
                return BasicNodeFactory.createBooleanTypeFromJson(null, true);
            case DATE:
                return BasicNodeFactory.createDateTypeFromJson(null, true);
            case INTEGER:
                return BasicNodeFactory.createIntegerTypeFromJson(null, true);
            case REAL:
                return BasicNodeFactory.createRealTypeFromJson(null, true);
            case STRING:
                return BasicNodeFactory.createStringTypeFromJson(null, true);
            case TIME:
                return BasicNodeFactory.createTimeTypeFromJson(null, true);
            case TIMESTAMP:
                return BasicNodeFactory.createTimestampTypeFromJson(null, true);
            default:
                throw new IllegalArgumentException("Not a supported BasicType.");
        }
    }

    protected static BasicNode createFromJson(
            final BasicType type,
            final JsonNode jsonNode,
            boolean isEditable
    ) {
        switch (type) {
            case ANY:
                // TODO: implement
                return new BasicNode(type, new JLabel("placeholder 05"), () -> ("not implemented 05"));
            case BINARY:
                return BasicNodeFactory.createBinaryTypeFromJson(jsonNode, isEditable);
            case BOOLEAN:
                return BasicNodeFactory.createBooleanTypeFromJson(jsonNode, isEditable);
            case DATE:
                return BasicNodeFactory.createDateTypeFromJson(jsonNode, isEditable);
            case INTEGER:
                return BasicNodeFactory.createIntegerTypeFromJson(jsonNode, isEditable);
            case REAL:
                return BasicNodeFactory.createRealTypeFromJson(jsonNode, isEditable);
            case STRING:
                return BasicNodeFactory.createStringTypeFromJson(jsonNode, isEditable);
            case TIME:
                return BasicNodeFactory.createTimeTypeFromJson(jsonNode, isEditable);
            case TIMESTAMP:
                return BasicNodeFactory.createTimestampTypeFromJson(jsonNode, isEditable);
            default:
                throw new IllegalArgumentException("Not a supported BasicType.");
        }
    }

    /**
     * Creates a <code>BasicNode</code> of the type <code>BasicType.Binary</code>. If the given
     * jsonNode is <code>null</code>, the node is initialize with an empty String.
     *
     * @param jsonNode A JSON node with a value to initialize or <code>null</code>.
     * @param isEditable Determines whether the user can edit the represented value or not.
     * @return The initialize BasicNode representing a binary value.
     */
    protected static BasicNode createBinaryTypeFromJson(
            final JsonNode jsonNode,
            boolean isEditable
    ) {
        final JEditorPane editorPane = new JEditorPane();
        editorPane.setEnabled(isEditable);
        editorPane.setContentType("text/plain");
        String plainTxt = "";
        if (jsonNode != null) {
            plainTxt = new String(Base64.getDecoder().decode(jsonNode.asText()),
                    StandardCharsets.UTF_8);
        }
        editorPane.setText(plainTxt);
        final Supplier<String> supp = () -> {
            return Base64.getEncoder().encodeToString(editorPane
                    .getText()
                    .getBytes(StandardCharsets.UTF_8));
        };

        final JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setMaximumSize(MaxDim.TEXT_FIELD_MULTI_LINE.getDim());
        return new BasicNode(BasicType.BINARY, scrollPane, supp);
    }

    /**
     * Creates a <code>BasicNode</code> of the type <code>BasicType.BOOLEAN</code>. If the given
     * jsonNode is <code>null</code>, the node is initialize with <code>false</code>.
     *
     * @param jsonNode A JSON node with a value to initialize or <code>null</code>.
     * @param isEditable Determines whether the user can edit the represented value or not.
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
     * @param isEditable Determines whether the user can edit the represented value or not.
     * @return The initialize BasicNode representing a date value.
     */
    protected static BasicNode createDateTypeFromJson(final JsonNode jsonNode, boolean isEditable) {
        LocalDate parsedDate = null;
        if (jsonNode != null) {
            try {
                parsedDate = DateTimeParser.parseIsoDate(jsonNode.asText());
            } catch (Exception ex) {
                // do nothing and use the current date instead
            }
        }

        final LocalDate initDate = (parsedDate != null) ? parsedDate : LocalDate.now();
        if (isEditable) {
            final JSpinner dateSpinner = new JSpinner();
            dateSpinner.setModel(new LocalDateSpinnerModel(initDate, null, null, null));
            dateSpinner.setEditor(new LocalDateSpinnerEditor(dateSpinner));
            dateSpinner.setMaximumSize(MaxDim.DATE_TIME_SPINNER.getDim());
            final Supplier<String> supp = () -> {
                return initDate.toString();
            };
            return new BasicNode(BasicType.DATE, dateSpinner, supp);
        } else {
            final JTextField strField = new JTextField();
            strField.setEditable(false);
            strField.setMaximumSize(MaxDim.DATE_TIME_SPINNER.getDim());
            strField.setText(initDate.toString());
            return new BasicNode(BasicType.DATE, strField, () -> (strField.getText()));
        }
    }

    /**
     * Creates a <code>BasicNode</code> of the type <code>BasicType.INTEGER</code>. If the given
     * jsonNode is <code>null</code>, the node is initialize with <code>0</code>.
     *
     * @param jsonNode A JSON node with a value to initialize or <code>null</code>.
     * @param isEditable Determines whether the user can edit the represented value or not.
     * @return The initialize BasicNode representing a integer value.
     */
    protected static BasicNode createIntegerTypeFromJson(final JsonNode jsonNode, boolean isEditable) {
        if (isEditable) {
            final SpinnerModel model = new SpinnerNumberModel();
            final JSpinner spinner = new JSpinner(model);
            spinner.setMaximumSize(MaxDim.NUMERIC_SPINNER.getDim());
            model.setValue((jsonNode != null) ? jsonNode.asLong() : 0L);
            return new BasicNode(BasicType.INTEGER, spinner, () -> (spinner.getValue().toString()));
        } else {
            final JTextField strField = new JTextField();
            strField.setEditable(false);
            strField.setMaximumSize(MaxDim.NUMERIC_SPINNER.getDim());
            strField.setText((jsonNode != null) ? jsonNode.asText() : "0");
            return new BasicNode(BasicType.INTEGER, strField, () -> (strField.getText()));
        }
    }

    /**
     * Creates a <code>BasicNode</code> of the type <code>BasicType.REAL</code>. If the given
     * jsonNode is <code>null</code>, the node is initialize with <code>0.0</code>.
     *
     * @param jsonNode A JSON node with a value to initialize or <code>null</code>.
     * @param isEditable Determines whether the user can edit the represented value or not.
     * @return The initialize BasicNode representing a double value.
     */
    protected static BasicNode createRealTypeFromJson(final JsonNode jsonNode, boolean isEditable) {
        if (isEditable) {
            final SpinnerModel model = new SpinnerNumberModel(0.0, null, null, REAL_STEP_SIZE);
            final JSpinner spinner = new JSpinner(model);
            spinner.setMaximumSize(MaxDim.NUMERIC_SPINNER.getDim());
            model.setValue((jsonNode != null) ? jsonNode.asDouble() : 0.0);
            return new BasicNode(BasicType.REAL, spinner, () -> (spinner.getValue().toString()));
        } else {
            final JTextField strField = new JTextField();
            strField.setEditable(false);
            strField.setMaximumSize(MaxDim.NUMERIC_SPINNER.getDim());
            strField.setText((jsonNode != null) ? jsonNode.asText() : "0.0");
            return new BasicNode(BasicType.REAL, strField, () -> (strField.getText()));
        }
    }

    /**
     * Creates a <code>BasicNode</code> of the type <code>BasicType.STRING</code>. If the given
     * jsonNode is <code>null</code>, the node is initialize with an empty String.
     *
     * @param jsonNode A JSON node with a value to initialize or <code>null</code>.
     * @param isEditable Determines whether the user can edit the represented value or not.
     * @return The initialize BasicNode representing a String.
     */
    protected static BasicNode createStringTypeFromJson(final JsonNode jsonNode, boolean isEditable) {
        final JTextField strField = new JTextField();
        strField.setEditable(isEditable);
        strField.setMaximumSize(MaxDim.TEXT_FIELD.getDim());
        strField.setText((jsonNode != null) ? jsonNode.asText() : "");
        return new BasicNode(BasicType.STRING, strField, () -> (strField.getText()));
    }

    /**
     * Creates a <code>BasicNode</code> of the type <code>BasicType.TIME</code>. If the given
     * jsonNode is <code>null</code>, the node is initialize with the current time.
     *
     * @param jsonNode A JSON node with a value to initialize or <code>null</code>.
     * @param isEditable Determines whether the user can edit the represented value or not.
     * @return The initialize BasicNode representing a time value.
     */
    protected static BasicNode createTimeTypeFromJson(final JsonNode jsonNode, boolean isEditable) {
        OffsetTime parsedTime = null;
        if (jsonNode != null) {
            try {
                parsedTime = DateTimeParser.parseIsoTime(jsonNode.asText())
                        .withOffsetSameInstant(DateTimeParser.LOCAL_OFFSET);
            } catch (Exception ex) {
                // do nothing and use the current time instead
            }
        }

        final OffsetTime initTime = (parsedTime != null)
                ? parsedTime
                : OffsetTime.now().truncatedTo(ChronoUnit.MILLIS);

        if (isEditable) {
            final JSpinner timeSpinner = new JSpinner();
            timeSpinner.setModel(new OffsetTimeSpinnerModel(initTime, null, null, null));
            timeSpinner.setEditor(new OffsetTimeSpinnerEditor(timeSpinner));
            timeSpinner.setMaximumSize(MaxDim.DATE_TIME_SPINNER.getDim());
            final Supplier<String> supp = () -> {
                return ((OffsetTime) timeSpinner.getValue())
                        .withOffsetSameInstant(ZoneOffset.UTC)
                        .toString();
            };
            return new BasicNode(BasicType.TIME, timeSpinner, supp);
        } else {
            final JTextField strField = new JTextField();
            strField.setEditable(false);
            strField.setMaximumSize(MaxDim.DATE_TIME_SPINNER.getDim());
            strField.setText(initTime.toLocalTime().toString());
            final Supplier<String> supp = () -> {
                return initTime.withOffsetSameInstant(ZoneOffset.UTC).toString();
            };
            return new BasicNode(BasicType.TIME, strField, supp);
        }
    }

    /**
     * Creates a <code>BasicNode</code> of the type <code>BasicType.TIMESTAMP</code>. If the given
     * jsonNode is <code>null</code>, the node is initialized with the current date and time.
     *
     * @param jsonNode A JSON node with a value to initialize or <code>null</code>.
     * @param isEditable Determines whether the User can edit the represented value or not.
     * @return The initialized BasicNode representing a timestamp value.
     */
    protected static BasicNode createTimestampTypeFromJson(
            final JsonNode jsonNode,
            boolean isEditable
    ) {
        OffsetDateTime parsedDateTime = null;
        if (jsonNode != null) {
            try {
                parsedDateTime = DateTimeParser.parseIsoDateTime(jsonNode.asText())
                        .withOffsetSameInstant(DateTimeParser.LOCAL_OFFSET);
            } catch (Exception ex) {
                // do nothing and use the current time instead
            }
        }

        final OffsetDateTime initDateTime = (parsedDateTime != null)
                ? parsedDateTime
                : OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS);

        if (isEditable) {
            final JSpinner timestampSpinner = new JSpinner();
            timestampSpinner.setModel(
                    new OffsetDateTimeSpinnerModel(initDateTime, null, null, ChronoUnit.HOURS));
            timestampSpinner.setEditor(new OffsetDateTimeSpinnerEditor(
                    timestampSpinner,
                    FormatterType.OFFSET_TIMESTAMP));
            timestampSpinner.setMaximumSize(MaxDim.TIMESTAMP_SPINNER.getDim());
            final Supplier<String> supp = () -> {
                return ((OffsetDateTime) timestampSpinner.getValue())
                        .withOffsetSameInstant(ZoneOffset.UTC)
                        .toString();
            };
            return new BasicNode(BasicType.TIMESTAMP, timestampSpinner, supp);
        } else {
            final JTextField strField = new JTextField();
            strField.setEditable(false);
            strField.setMaximumSize(MaxDim.TIMESTAMP_SPINNER.getDim());
            strField.setText(initDateTime.toString());
            final Supplier<String> supp = () -> {
                return initDateTime.withOffsetSameInstant(ZoneOffset.UTC).toString();
            };
            return new BasicNode(BasicType.TIMESTAMP, strField, supp);
        }
    }
}
