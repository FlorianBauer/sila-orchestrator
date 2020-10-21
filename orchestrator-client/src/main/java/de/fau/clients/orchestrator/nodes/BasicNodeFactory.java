package de.fau.clients.orchestrator.nodes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.fau.clients.orchestrator.utils.DateTimeParser;
import de.fau.clients.orchestrator.utils.LocalDateSpinnerEditor;
import de.fau.clients.orchestrator.utils.LocalDateSpinnerModel;
import de.fau.clients.orchestrator.utils.OffsetDateTimeSpinnerEditor;
import de.fau.clients.orchestrator.utils.OffsetDateTimeSpinnerEditor.FormatterType;
import de.fau.clients.orchestrator.utils.OffsetDateTimeSpinnerModel;
import de.fau.clients.orchestrator.utils.OffsetTimeSpinnerEditor;
import de.fau.clients.orchestrator.utils.OffsetTimeSpinnerModel;
import de.fau.clients.orchestrator.utils.ValidatorUtils;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import lombok.NonNull;
import sila2.org.silastandard.SiLAFramework;
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

    protected static BasicNode create(@NonNull final BasicType type, boolean isEditable) {
        switch (type) {
            case ANY:
                throw new IllegalArgumentException("'Any'-type without typ info not supported.");
            case BINARY:
                return createBinaryType("".getBytes(), isEditable);
            case BOOLEAN:
                return createBooleanType(false, isEditable);
            case DATE:
                return createDateType(LocalDate.now(), isEditable);
            case INTEGER:
                return createIntegerType(0, isEditable);
            case REAL:
                return createRealType(0.0, isEditable);
            case STRING:
                return createStringType("", isEditable);
            case TIME:
                return createTimeType(OffsetTime.now(), isEditable);
            case TIMESTAMP:
                return createTimestampType(OffsetDateTime.now(), isEditable);
            default:
                throw new IllegalArgumentException("Not a supported BasicType.");
        }
    }

    protected static BasicNode createFromJson(
            @NonNull final BasicType type,
            final JsonNode jsonNode,
            boolean isEditable
    ) {
        if (jsonNode == null) {
            return create(type, isEditable);
        }

        switch (type) {
            case ANY:
                return createAnyTypeFromJson(jsonNode, isEditable);
            case BINARY: {
                try {
                    return createBinaryType(jsonNode.binaryValue(), isEditable);
                } catch (final IOException ex) {
                    return createErrorType(type, ex.getMessage());
                }
            }
            case BOOLEAN:
                return createBooleanType(jsonNode.asBoolean(), isEditable);
            case DATE:
                final LocalDate initDate = DateTimeParser.parseIsoDate(jsonNode.asText());
                return createDateType(initDate, isEditable);
            case INTEGER:
                return createIntegerType(jsonNode.asLong(), isEditable);
            case REAL:
                return createRealType(jsonNode.asDouble(), isEditable);
            case STRING:
                return createStringType(jsonNode.asText(), isEditable);
            case TIME:
                final OffsetTime initTime = DateTimeParser.parseIsoTime(jsonNode.asText());
                return createTimeType(initTime, isEditable);
            case TIMESTAMP:
                final OffsetDateTime initTimesamp = DateTimeParser.parseIsoDateTime(jsonNode.asText());
                return createTimestampType(initTimesamp, isEditable);
            default:
                throw new IllegalArgumentException("Not a supported BasicType.");
        }
    }

    protected static BasicNode createAnyTypeFromJson(
            @NonNull final JsonNode jsonNode,
            boolean isEditable
    ) {
        final BasicType basicType = BasicType.ANY;
        final XmlMapper xmlMapper = new XmlMapper();
        final JsonNode xmlTypeNode;
        final String type;
        final byte[] payload;
        try {
            xmlTypeNode = xmlMapper.readTree(jsonNode.get("type").asText());
            type = xmlTypeNode.get("Basic").asText();
            payload = jsonNode.get("payload").binaryValue();
        } catch (final Exception ex) {
            return createErrorType(basicType, ex.getMessage());
        }

        if (type.equals(BasicType.BINARY.value())) {
            try {
                final ByteString byteVal = SiLAFramework.Binary.parseFrom(payload).getValue();
                return createBinaryType(byteVal.toByteArray(), isEditable);
            } catch (final InvalidProtocolBufferException ex) {
                return createErrorType(basicType, ex.getMessage());
            }
        } else if (type.equals(BasicType.BOOLEAN.value())) {
            try {
                final boolean boolVal = SiLAFramework.Boolean.parseFrom(payload).getValue();
                return createBooleanType(boolVal, isEditable);
            } catch (final InvalidProtocolBufferException ex) {
                return createErrorType(basicType, ex.getMessage());
            }
        } else if (type.equals(BasicType.DATE.value())) {
            final SiLAFramework.Date dateVal;
            try {
                dateVal = SiLAFramework.Date.parseFrom(payload);
            } catch (final InvalidProtocolBufferException ex) {
                return createErrorType(basicType, ex.getMessage());
            }
            return createDateType(
                    LocalDate.of(dateVal.getDay(), dateVal.getMonth(), dateVal.getYear()),
                    isEditable);
        } else if (type.equals(BasicType.INTEGER.value())) {
            try {
                final long intVal = SiLAFramework.Integer.parseFrom(payload).getValue();
                return createIntegerType(intVal, isEditable);
            } catch (final InvalidProtocolBufferException ex) {
                return createErrorType(basicType, ex.getMessage());
            }
        } else if (type.equals(BasicType.REAL.value())) {
            try {
                final double realVal = SiLAFramework.Real.parseFrom(payload).getValue();
                return createRealType(realVal, isEditable);
            } catch (final InvalidProtocolBufferException ex) {
                return createErrorType(basicType, ex.getMessage());
            }
        } else if (type.equals(BasicType.STRING.value())) {
            try {
                final String stringVal = SiLAFramework.String.parseFrom(payload).getValue();
                return createStringType(stringVal, isEditable);
            } catch (final InvalidProtocolBufferException ex) {
                return createErrorType(basicType, ex.getMessage());
            }
        } else if (type.equals(BasicType.TIME.value())) {
            final SiLAFramework.Time timeVal;
            try {
                timeVal = SiLAFramework.Time.parseFrom(payload);
            } catch (final InvalidProtocolBufferException ex) {
                return createErrorType(basicType, ex.getMessage());
            }
            final SiLAFramework.Timezone tz = timeVal.getTimezone();
            final ZoneOffset zoneOffset = ZoneOffset.ofHoursMinutes(tz.getHours(), tz.getMinutes());
            final OffsetTime offsetTime = OffsetTime.of(
                    timeVal.getHour(),
                    timeVal.getMinute(),
                    timeVal.getSecond(),
                    0,
                    zoneOffset);
            return createTimeType(offsetTime, isEditable);
        } else if (type.equals(BasicType.TIMESTAMP.value())) {
            final SiLAFramework.Timestamp timestampVal;
            try {
                timestampVal = SiLAFramework.Timestamp.parseFrom(payload);
            } catch (final InvalidProtocolBufferException ex) {
                return createErrorType(basicType, ex.getMessage());
            }
            final SiLAFramework.Timezone tz = timestampVal.getTimezone();
            final ZoneOffset zoneOffset = ZoneOffset.ofHoursMinutes(tz.getHours(), tz.getMinutes());
            final OffsetDateTime timestamp = OffsetDateTime.of(
                    timestampVal.getYear(),
                    timestampVal.getMonth(),
                    timestampVal.getDay(),
                    timestampVal.getHour(),
                    timestampVal.getMinute(),
                    timestampVal.getSecond(),
                    0,
                    zoneOffset);
            return createTimestampType(timestamp, isEditable);
        } else {
            return createErrorType(basicType, "Undefined 'Any'-type.");
        }
    }

    /**
     * Creates a <code>BasicNode</code> of the type <code>BasicType.Binary</code>. Input data which
     * can not be interpreted as UTF-8 string is not editable even if the provided parameter states
     * otherwise.
     *
     * @param byteValue The byte array to initialize the node with.
     * @param isEditable Determines whether the user can edit the represented value or not.
     * @return The initialize BasicNode representing a binary value.
     */
    protected static BasicNode createBinaryType(
            @NonNull final byte[] byteValue,
            boolean isEditable
    ) {
        final JComponent comp;
        final Supplier<String> supp;
        String binaryReprStr;
        final boolean isUtf8 = ValidatorUtils.isValidUtf8(byteValue);
        if (isUtf8) {
            binaryReprStr = new String(byteValue, StandardCharsets.UTF_8);
            final JEditorPane editorPane = new JEditorPane();
            editorPane.setEnabled(isEditable);
            editorPane.setText(binaryReprStr);
            final JScrollPane scrollPane = new JScrollPane(editorPane);
            scrollPane.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane.setMaximumSize(MaxDim.TEXT_FIELD_MULTI_LINE.getDim());
            comp = scrollPane;
            supp = () -> {
                return Base64.getEncoder().encodeToString(editorPane
                        .getText()
                        .getBytes(StandardCharsets.UTF_8));
            };
        } else {
            try {
                final MessageDigest md = MessageDigest.getInstance("SHA-256");
                binaryReprStr = "SHA-256: " + toHexString(md.digest(byteValue));
            } catch (final NoSuchAlgorithmException ex) {
                binaryReprStr = "SHA-256: error";
            }
            final JTextField textField = new JTextField();
            textField.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            textField.setMaximumSize(MaxDim.TEXT_FIELD.getDim());
            // Raw (non UTF-8) data can not be edited.
            textField.setEnabled(false);
            textField.setText(binaryReprStr);
            comp = textField;
            supp = () -> (Base64.getEncoder().encodeToString(byteValue));
            isEditable = false;
        }
        return new BasicNode(BasicType.BINARY, comp, supp, isEditable);
    }

    /**
     * Creates a <code>BasicNode</code> of the type <code>BasicType.BOOLEAN</code>.
     *
     * @param boolValue The bool value to initialize the node with.
     * @param isEditable Determines whether the user can edit the represented value or not.
     * @return The initialize BasicNode representing a boolean value.
     */
    protected static BasicNode createBooleanType(final boolean boolValue, boolean isEditable) {
        final JCheckBox checkBox = new JCheckBox();
        checkBox.setEnabled(isEditable);
        checkBox.setSelected(boolValue);
        final Supplier<String> supp = () -> (checkBox.isSelected() ? "true" : "false");
        return new BasicNode(BasicType.BOOLEAN, checkBox, supp, isEditable);
    }

    /**
     * Creates a <code>BasicNode</code> of the type <code>BasicType.DATE</code>.
     *
     * @param jsonNode The <code>LocalDate</code> to initialize the node with.
     * @param isEditable Determines whether the user can edit the represented value or not.
     * @return The initialize BasicNode representing a date value.
     */
    protected static BasicNode createDateType(
            @NonNull final LocalDate dateValue,
            boolean isEditable
    ) {
        if (isEditable) {
            final JSpinner dateSpinner = new JSpinner();
            dateSpinner.setModel(new LocalDateSpinnerModel(dateValue, null, null, null));
            dateSpinner.setEditor(new LocalDateSpinnerEditor(dateSpinner));
            dateSpinner.setMaximumSize(MaxDim.DATE_TIME_SPINNER.getDim());
            final Supplier<String> supp = () -> {
                return dateValue.toString();
            };
            return new BasicNode(BasicType.DATE, dateSpinner, supp, isEditable);
        } else {
            final JTextField strField = new JTextField();
            strField.setEditable(false);
            strField.setMaximumSize(MaxDim.DATE_TIME_SPINNER.getDim());
            strField.setText(dateValue.toString());
            return new BasicNode(BasicType.DATE, strField, () -> (strField.getText()), isEditable);
        }
    }

    /**
     * Creates a <code>BasicNode</code> of the type <code>BasicType.INTEGER</code>.
     *
     * @param intValue The integer value to initialize the node with.
     * @param isEditable Determines whether the user can edit the represented value or not.
     * @return The initialize BasicNode representing a integer value.
     */
    protected static BasicNode createIntegerType(final long intValue, boolean isEditable) {
        if (isEditable) {
            final SpinnerModel model = new SpinnerNumberModel();
            final JSpinner spinner = new JSpinner(model);
            spinner.setMaximumSize(MaxDim.NUMERIC_SPINNER.getDim());
            model.setValue(intValue);
            return new BasicNode(BasicType.INTEGER, spinner, () -> (spinner.getValue().toString()), isEditable);
        } else {
            final JTextField strField = new JTextField();
            strField.setEditable(false);
            strField.setMaximumSize(MaxDim.NUMERIC_SPINNER.getDim());
            strField.setText(Long.toString(intValue));
            return new BasicNode(BasicType.INTEGER, strField, () -> (strField.getText()), isEditable);
        }
    }

    /**
     * Creates a <code>BasicNode</code> of the type <code>BasicType.REAL</code>.
     *
     * @param realValue The double value to initialize the node with.
     * @param isEditable Determines whether the user can edit the represented value or not.
     * @return The initialize BasicNode representing a double value.
     */
    protected static BasicNode createRealType(final double realValue, boolean isEditable) {
        if (isEditable) {
            final SpinnerModel model = new SpinnerNumberModel(realValue, null, null, REAL_STEP_SIZE);
            final JSpinner spinner = new JSpinner(model);
            spinner.setMaximumSize(MaxDim.NUMERIC_SPINNER.getDim());
            return new BasicNode(BasicType.REAL, spinner, () -> (spinner.getValue().toString()), isEditable);
        } else {
            final JTextField strField = new JTextField();
            strField.setEditable(false);
            strField.setMaximumSize(MaxDim.NUMERIC_SPINNER.getDim());
            strField.setText(Double.toString(realValue));
            return new BasicNode(BasicType.REAL, strField, () -> (strField.getText()), isEditable);
        }
    }

    /**
     * Creates a <code>BasicNode</code> of the type <code>BasicType.STRING</code>.
     *
     * @param stringValue The String to initialize the node with.
     * @param isEditable Determines whether the user can edit the represented value or not.
     * @return The initialize BasicNode representing a String.
     */
    protected static BasicNode createStringType(
            @NonNull final String stringValue,
            boolean isEditable
    ) {
        final JTextField strField = new JTextField();
        strField.setEditable(isEditable);
        strField.setMaximumSize(MaxDim.TEXT_FIELD.getDim());
        strField.setText(stringValue);
        return new BasicNode(BasicType.STRING, strField, () -> (strField.getText()), isEditable);
    }

    /**
     * Creates a <code>BasicNode</code> of the type <code>BasicType.TIME</code>.
     *
     * @param timeValue The <code>OffsetTime</code> to initialize the node with.
     * @param isEditable Determines whether the user can edit the represented value or not.
     * @return The initialize BasicNode representing a time value.
     */
    protected static BasicNode createTimeType(
            @NonNull final OffsetTime timeValue,
            boolean isEditable
    ) {
        final OffsetTime initTime = timeValue.truncatedTo(ChronoUnit.MILLIS);
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
            return new BasicNode(BasicType.TIME, timeSpinner, supp, isEditable);
        } else {
            final JTextField strField = new JTextField();
            strField.setEditable(false);
            strField.setMaximumSize(MaxDim.DATE_TIME_SPINNER.getDim());
            strField.setText(initTime.toLocalTime().toString());
            final Supplier<String> supp = () -> {
                return initTime.withOffsetSameInstant(ZoneOffset.UTC).toString();
            };
            return new BasicNode(BasicType.TIME, strField, supp, isEditable);
        }
    }

    /**
     * Creates a <code>BasicNode</code> of the type <code>BasicType.TIMESTAMP</code>.
     *
     * @param timestampValue The <code>OffsetDateTime</code> to initialize the node with.
     * @param isEditable Determines whether the User can edit the represented value or not.
     * @return The initialized BasicNode representing a timestamp value.
     */
    protected static BasicNode createTimestampType(
            @NonNull final OffsetDateTime timestampValue,
            boolean isEditable
    ) {
        final OffsetDateTime initDateTime = timestampValue.truncatedTo(ChronoUnit.MILLIS);
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
            return new BasicNode(BasicType.TIMESTAMP, timestampSpinner, supp, isEditable);
        } else {
            final JTextField strField = new JTextField();
            strField.setEditable(false);
            strField.setMaximumSize(MaxDim.TIMESTAMP_SPINNER.getDim());
            strField.setText(initDateTime.toString());
            final Supplier<String> supp = () -> {
                return initDateTime.withOffsetSameInstant(ZoneOffset.UTC).toString();
            };
            return new BasicNode(BasicType.TIMESTAMP, strField, supp, isEditable);
        }
    }

    protected static BasicNode createErrorType(
            @NonNull final BasicType basicType,
            final String errorMsg
    ) {
        final JLabel errLabel = new JLabel("Error: " + errorMsg);
        errLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        return new BasicNode(basicType, errLabel, () -> (""), false);
    }

    /**
     * Converts hexadecimal byte values to a string.
     *
     * @param hexValues The hex values to represent as string.
     * @return A the hex values as String.
     */
    public static String toHexString(byte[] hexValues) {
        final BigInteger number = new BigInteger(1, hexValues);
        final StringBuilder hexString = new StringBuilder(number.toString(16));
        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }
        return hexString.toString();
    }
}
