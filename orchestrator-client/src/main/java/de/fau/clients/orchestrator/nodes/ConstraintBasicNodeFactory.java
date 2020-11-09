package de.fau.clients.orchestrator.nodes;

import com.fasterxml.jackson.databind.JsonNode;
import de.fau.clients.orchestrator.ctx.FeatureContext;
import static de.fau.clients.orchestrator.nodes.BasicNodeFactory.createErrorType;
import de.fau.clients.orchestrator.utils.DateTimeParser;
import de.fau.clients.orchestrator.utils.DocumentLengthFilter;
import de.fau.clients.orchestrator.utils.IconProvider;
import de.fau.clients.orchestrator.utils.ImagePanel;
import de.fau.clients.orchestrator.utils.LocalDateSpinnerEditor;
import de.fau.clients.orchestrator.utils.OffsetDateTimeSpinnerEditor;
import de.fau.clients.orchestrator.utils.OffsetDateTimeSpinnerEditor.FormatterType;
import de.fau.clients.orchestrator.utils.OffsetTimeSpinnerEditor;
import de.fau.clients.orchestrator.utils.ValidatorUtils;
import de.fau.clients.orchestrator.utils.XmlUtils;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import lombok.NonNull;
import sila_java.library.core.models.BasicType;
import sila_java.library.core.models.Constraints;
import sila_java.library.core.models.DataTypeType;

/**
 * A Factory for <code>ConstraintBasicNode</code>s.
 *
 * @see ConstraintBasicNode
 */
class ConstraintBasicNodeFactory {

    /**
     * The horizontal gap size between parameter component, condition description and validation
     * icon. Only used for components with constraints.
     */
    private static final int HORIZONTAL_STRUT = 5;
    private static final String LESS_THAN = "< ";
    private static final String GREATER_THAN = "> ";
    private static final String LESS_OR_EQUAL = "≤ "; // '\u2264'
    private static final String GREATER_OR_EQUAL = "≥ "; // '\u2265'
    private static final String AND_SIGN = " ∧ "; // '\u2227'
    private static final String INVALID_CONSTRAINT = "Invalid Constraint";

    private ConstraintBasicNodeFactory() {
        throw new UnsupportedOperationException("Instantiation not allowed.");
    }

    protected static BasicNode create(
            final FeatureContext featCtx,
            @NonNull final BasicType type,
            @NonNull final Constraints constraints
    ) {
        switch (type) {
            case ANY:
                throw new IllegalArgumentException("'Any'-type without typ info not supported.");
            case BINARY:
                return createConstrainedBinaryType(constraints, "".getBytes());
            case BOOLEAN:
                // Whoever is trying to constrain a boolean even more deserves a special exception message.
                throw new IllegalArgumentException("Booleans can not be constrained. Try using "
                        + "'BasicNodeFactory.createBooleanType(boolValue, false);' for a "
                        + "non-editable bool node.");
            case DATE:
                return createConstrainedDateType(constraints, LocalDate.now());
            case INTEGER:
                return createConstrainedIntegerType(constraints, 0);
            case REAL:
                return createConstrainedRealTypeFromJson(constraints, null);
            case STRING:
                return createConstrainedStringTypeFromJson(constraints, null, featCtx);
            case TIME:
                return createConstrainedTimeTypeFromJson(constraints, null);
            case TIMESTAMP:
                return createConstrainedTimestampeTypeFromJson(constraints, null);
            default:
                throw new IllegalArgumentException("Not a valid BasicType.");
        }
    }

    protected static BasicNode createFromJson(
            final FeatureContext featCtx,
            @NonNull final BasicType type,
            @NonNull final Constraints constraints,
            final JsonNode jsonNode
    ) {
        if (jsonNode == null) {
            return create(featCtx, type, constraints);
        }

        switch (type) {
            case ANY: {
                final DataTypeType dtt;
                final byte[] payload;
                try {
                    dtt = XmlUtils.parseXmlDataType(jsonNode.get("type").asText());
                    payload = jsonNode.get("payload").binaryValue();
                } catch (final IOException ex) {
                    return createErrorType(type, ex.getMessage());
                }
                return createConstrainedAnyType(constraints, dtt, payload);
            }
            case BINARY: {
                final byte[] binaryVal;
                try {
                    binaryVal = jsonNode.get("value").binaryValue();
                } catch (final IOException ex) {
                    return BasicNodeFactory.createErrorType(type, ex.getMessage());
                }
                return createConstrainedBinaryType(constraints, binaryVal);
            }
            case BOOLEAN:
                // Whoever is trying to constrain a boolean even more deserves a special exception message.
                throw new IllegalArgumentException("Booleans can not be constrained. Try using "
                        + "'BasicNodeFactory.createBooleanType(boolValue, false);' for a "
                        + "non-editable bool node.");
            case DATE: {
                final LocalDate dateVal;
                try {
                    dateVal = DateTimeParser.parseIsoDate(jsonNode.get("value").asText());
                } catch (final Exception ex) {
                    return BasicNodeFactory.createErrorType(type, ex.getMessage());
                }
                return createConstrainedDateType(constraints, dateVal);
            }
            case INTEGER: {
                final int intVal;
                try {
                    intVal = Integer.parseInt(jsonNode.get("value").asText());
                } catch (final Exception ex) {
                    return BasicNodeFactory.createErrorType(type, ex.getMessage());
                }
                return createConstrainedIntegerType(constraints, intVal);
            }
            case REAL:
                return createConstrainedRealTypeFromJson(constraints, jsonNode.get("value"));
            case STRING:
                return createConstrainedStringTypeFromJson(constraints, jsonNode.get("value"), featCtx);
            case TIME:
                return createConstrainedTimeTypeFromJson(constraints, jsonNode.get("value"));
            case TIMESTAMP:
                return createConstrainedTimestampeTypeFromJson(constraints, jsonNode.get("value"));
            default:
                throw new IllegalArgumentException("Not a valid BasicType.");
        }
    }

    protected static BasicNode createConstrainedAnyType(
            @NonNull final Constraints constraints,
            @NonNull final DataTypeType dtt,
            @NonNull final byte[] payload
    ) {
        final Constraints.AllowedTypes allowedTypes = constraints.getAllowedTypes();
        if (allowedTypes != null) {
            final List<DataTypeType> list = allowedTypes.getDataType();
            if (dtt.getBasic() != null) {
                for (final DataTypeType allowedType : list) {
                    if (dtt.getBasic().compareTo(allowedType.getBasic()) == 0) {
                        return BasicNodeFactory.createAnyType(dtt, payload, false);
                    }
                }
            } else if (dtt.getConstrained() != null) {
                return createConstrainedAnyType(dtt.getConstrained().getConstraints(), dtt, payload);
            } else if (dtt.getList() != null) {
                // TODO: implement allowed type check
                return BasicNodeFactory.createAnyType(dtt, payload, false);
            } else if (dtt.getStructure() != null) {
                // TODO: implement allowed type check
                return BasicNodeFactory.createAnyType(dtt, payload, false);
            }
        } else if (constraints.getMaximalExclusive() != null
                || constraints.getMaximalInclusive() != null
                || constraints.getMinimalExclusive() != null
                || constraints.getMinimalInclusive() != null) {
            // Only basic types can have these constraints.
            return BasicNodeFactory.createAnyType(dtt.getConstrained().getDataType(), payload, false);
        } else if (constraints.getUnit() != null) {
            // Only INTEGER and REAL types can have these constraints.
            // TODO: Implement function which put the unit label besides the BasicType.
            return BasicNodeFactory.createAnyType(dtt.getConstrained().getDataType(), payload, false);
        }
        return BasicNodeFactory.createErrorType(BasicType.ANY, "Type not allowed.");
    }

    /**
     * Creates a binary type node. This function is also responsible for the supported binary
     * formats and their representation.
     *
     * @param constraints The constraints.
     * @param binaryValue The binary values as byte array.
     * @return The binary node.
     */
    protected static ConstraintBasicNode createConstrainedBinaryType(
            @NonNull final Constraints constraints,
            @NonNull final byte[] binaryValue
    ) {
        boolean isValid = true;
        String errorMsg = "Unsupported constrained binary type.";
        if (constraints.getLength() != null) {
            final int len = constraints.getLength().intValue();
            if (binaryValue.length != len) {
                isValid = false;
                errorMsg = "Wrong binary length (len != " + len + " bytes).";
            }
        } else {
            if (constraints.getMinimalLength() != null) {
                final int minLen = constraints.getMinimalLength().intValue();
                if (binaryValue.length < minLen) {
                    isValid = false;
                    errorMsg = "Wrong binary length (len < " + minLen + " bytes).";
                }
            }
            if (constraints.getMaximalLength() != null) {
                final int maxLen = constraints.getMaximalLength().intValue();
                if (binaryValue.length > maxLen) {
                    isValid = false;
                    errorMsg = "Wrong binary length (len > " + maxLen + " bytes).";
                }
            }
        }

        // If a previous applied constraint failed, we can omit to check for the content type.
        if (isValid) {
            final Constraints.ContentType contType = constraints.getContentType();
            if (contType != null) {
                final InternalContentType ict = getSupportedContentType(contType);
                switch (ict) {
                    case UNKNOWN:
                        // We don't know the type, so check for a valid UTF-8 enconding and present it as plain text.
                        if (ValidatorUtils.isValidUtf8(binaryValue)) {
                            return createTextNodeFromBinary(constraints, binaryValue);
                        }
                        break;
                    case TEXT:
                        return createTextNodeFromBinary(constraints, binaryValue);
                    case TEXT_XML:
                        return createXmlNodeFromBinary(constraints, binaryValue);
                    case IMAGE:
                        return createImageNodeFromBinary(constraints, binaryValue);
                }
            } else {
                if (ValidatorUtils.isValidUtf8(binaryValue)) {
                    return createTextNodeFromBinary(constraints, binaryValue);
                }
            }
        }
        final JLabel errorLabel = new JLabel("Error: " + errorMsg);
        return new ConstraintBasicNode(BasicType.BINARY, errorLabel, () -> (""), constraints);
    }

    /**
     * Creates a <code>ConstraintBasicNode</code> of the type <code>BasicType.DATE</code>.
     *
     * @param constraints The applied date constraints.
     * @param dateValue The date to initialize the node with.
     * @return The initialized, constrained BasicNode representing a date value.
     */
    protected static ConstraintBasicNode createConstrainedDateType(
            @NonNull final Constraints constraints,
            @NonNull final LocalDate dateValue
    ) {
        final JComponent comp;
        final Supplier<String> supp;
        if (constraints.getSet() != null) {
            final List<String> dateSet = constraints.getSet().getValue();
            final LocalDate[] dates = new LocalDate[dateSet.size()];
            int selectionIdx = 0;
            for (int i = 0; i < dateSet.size(); i++) {
                dates[i] = DateTimeParser.parseIsoDate(dateSet.get(i));
                if (dates[i].equals(dateValue)) {
                    selectionIdx = i;
                }
            }
            final JComboBox<LocalDate> dateComboBox = new JComboBox<>(dates);
            dateComboBox.setMaximumSize(MaxDim.DATE_TIME_SPINNER.getDim());
            dateComboBox.setSelectedIndex(selectionIdx);
            supp = () -> {
                return dateComboBox.getSelectedItem().toString();
            };
            comp = dateComboBox;
        } else {
            String minBounds = null;
            if (constraints.getMinimalExclusive() != null) {
                minBounds = GREATER_THAN + DateTimeParser.parseIsoDate(
                        constraints.getMinimalExclusive()).toString();
            } else if (constraints.getMinimalInclusive() != null) {
                minBounds = GREATER_OR_EQUAL + DateTimeParser.parseIsoDate(
                        constraints.getMinimalInclusive()).toString();
            }

            String maxBounds = null;
            if (constraints.getMaximalExclusive() != null) {
                maxBounds = LESS_THAN + DateTimeParser.parseIsoDate(
                        constraints.getMaximalExclusive()).toString();
            } else if (constraints.getMaximalInclusive() != null) {
                maxBounds = LESS_OR_EQUAL + DateTimeParser.parseIsoDate(
                        constraints.getMaximalInclusive()).toString();
            }

            final String conditionDesc;
            if (minBounds != null && maxBounds != null) {
                conditionDesc = minBounds + AND_SIGN + maxBounds;
            } else if (minBounds != null) {
                conditionDesc = minBounds;
            } else if (maxBounds != null) {
                conditionDesc = maxBounds;
            } else {
                conditionDesc = INVALID_CONSTRAINT;
            }

            final JSpinner dateSpinner = new JSpinner(ConstraintSpinnerModelFactory
                    .createRangeConstrainedDateModel(dateValue, constraints));
            dateSpinner.setMaximumSize(MaxDim.DATE_TIME_SPINNER.getDim());
            dateSpinner.setEditor(new LocalDateSpinnerEditor(dateSpinner));
            supp = () -> {
                return dateSpinner.getValue().toString();
            };
            final Box hBox = Box.createHorizontalBox();
            hBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            hBox.add(dateSpinner);
            hBox.add(Box.createHorizontalStrut(HORIZONTAL_STRUT));
            hBox.add(new JLabel(conditionDesc));
            comp = hBox;
        }
        return new ConstraintBasicNode(BasicType.DATE, comp, supp, constraints);
    }

    /**
     * Creates a <code>ConstraintBasicNode</code> of the type <code>BasicType.INTEGER</code>.
     *
     * @param constraints The applied integer constraints.
     * @param intValue The integer to initialize the node with.
     * @return The initialized, constrained BasicNode representing a integer value.
     */
    protected static ConstraintBasicNode createConstrainedIntegerType(
            @NonNull final Constraints constraints,
            int intValue
    ) {
        final JComponent comp;
        final Supplier<String> supp;
        if (constraints.getSet() != null) {
            final List<String> numberSet = constraints.getSet().getValue();
            final JComboBox<String> numberComboBox = new JComboBox<>(numberSet.toArray(new String[0]));
            numberComboBox.setMaximumSize(MaxDim.NUMERIC_SPINNER.getDim());
            int selectionIdx = 0;
            for (int i = 0; i < numberSet.size(); i++) {
                if (numberSet.get(i).equals(Integer.toString(intValue))) {
                    selectionIdx = i;
                    break;
                }
            }
            numberComboBox.setSelectedIndex(selectionIdx);
            supp = () -> (numberComboBox.getSelectedItem().toString());
            comp = numberComboBox;
        } else {
            final SpinnerModel model = ConstraintSpinnerModelFactory
                    .createRangeConstrainedIntModel(constraints);
            final JSpinner numericSpinner = new JSpinner(model);
            numericSpinner.setMaximumSize(MaxDim.NUMERIC_SPINNER.getDim());
            numericSpinner.setValue(intValue);
            final String conditionDesc;
            if (constraints.getUnit() != null) {
                conditionDesc = constraints.getUnit().getLabel();
            } else {
                String minBounds = null;
                if (constraints.getMinimalExclusive() != null) {
                    minBounds = GREATER_THAN + constraints.getMinimalExclusive();
                } else if (constraints.getMinimalInclusive() != null) {
                    minBounds = GREATER_OR_EQUAL + constraints.getMinimalInclusive();
                }

                String maxBounds = null;
                if (constraints.getMaximalExclusive() != null) {
                    maxBounds = LESS_THAN + constraints.getMaximalExclusive();
                } else if (constraints.getMaximalInclusive() != null) {
                    maxBounds = LESS_OR_EQUAL + constraints.getMaximalInclusive();
                }

                if (minBounds != null && maxBounds != null) {
                    conditionDesc = minBounds + AND_SIGN + maxBounds;
                } else if (minBounds != null) {
                    conditionDesc = minBounds;
                } else if (maxBounds != null) {
                    conditionDesc = maxBounds;
                } else {
                    conditionDesc = INVALID_CONSTRAINT;
                }
            }
            final Box hBox = Box.createHorizontalBox();
            hBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            hBox.add(numericSpinner);
            hBox.add(Box.createHorizontalStrut(HORIZONTAL_STRUT));
            hBox.add(new JLabel(conditionDesc));
            hBox.setMaximumSize(MaxDim.TEXT_FIELD.getDim());
            comp = hBox;
            supp = () -> (numericSpinner.getValue().toString());
        }
        return new ConstraintBasicNode(BasicType.INTEGER, comp, supp, constraints);
    }

    protected static ConstraintBasicNode createConstrainedRealTypeFromJson(
            @NonNull final Constraints constraints,
            final JsonNode jsonNode
    ) {
        final JComponent comp;
        final Supplier<String> supp;
        if (constraints.getSet() != null) {
            final List<String> numberSet = constraints.getSet().getValue();
            final JComboBox<String> numberComboBox = new JComboBox<>(numberSet.toArray(new String[0]));
            numberComboBox.setMaximumSize(MaxDim.NUMERIC_SPINNER.getDim());
            if (jsonNode != null) {
                numberComboBox.setSelectedItem(jsonNode.asText());
            }
            supp = () -> (Double.valueOf(numberComboBox.getSelectedItem().toString()).toString());
            comp = numberComboBox;
        } else {
            final SpinnerModel model = ConstraintSpinnerModelFactory
                    .createRangeConstrainedRealModel(constraints);
            final JSpinner numericSpinner = new JSpinner(model);
            numericSpinner.setMaximumSize(MaxDim.NUMERIC_SPINNER.getDim());
            if (jsonNode != null) {
                numericSpinner.setValue(jsonNode.asDouble());
            }
            final String conditionDesc;
            if (constraints.getUnit() != null) {
                conditionDesc = constraints.getUnit().getLabel();
            } else {
                String minBounds = null;
                if (constraints.getMinimalExclusive() != null) {
                    minBounds = GREATER_THAN + constraints.getMinimalExclusive();
                } else if (constraints.getMinimalInclusive() != null) {
                    minBounds = GREATER_OR_EQUAL + constraints.getMinimalInclusive();
                }

                String maxBounds = null;
                if (constraints.getMaximalExclusive() != null) {
                    maxBounds = LESS_THAN + constraints.getMaximalExclusive();
                } else if (constraints.getMaximalInclusive() != null) {
                    maxBounds = LESS_OR_EQUAL + constraints.getMaximalInclusive();
                }

                if (minBounds != null && maxBounds != null) {
                    conditionDesc = minBounds + AND_SIGN + maxBounds;
                } else if (minBounds != null) {
                    conditionDesc = minBounds;
                } else if (maxBounds != null) {
                    conditionDesc = maxBounds;
                } else {
                    conditionDesc = INVALID_CONSTRAINT;
                }
            }

            final Box hBox = Box.createHorizontalBox();
            hBox.add(numericSpinner);
            hBox.add(Box.createHorizontalStrut(HORIZONTAL_STRUT));
            hBox.add(new JLabel(conditionDesc));
            hBox.setMaximumSize(MaxDim.TEXT_FIELD.getDim());
            comp = hBox;
            supp = () -> (numericSpinner.getValue().toString());
        }
        return new ConstraintBasicNode(BasicType.REAL, comp, supp, constraints);
    }

    protected static ConstraintBasicNode createConstrainedStringTypeFromJson(
            @NonNull final Constraints constraints,
            final JsonNode jsonNode,
            final FeatureContext featCtx
    ) {
        final JComponent comp;
        final Supplier<String> supp;
        final Constraints.Set conSet = constraints.getSet();
        if (conSet != null) {
            final JComboBox<String> comboBox = new JComboBox<>();
            comboBox.setMaximumSize(MaxDim.TEXT_FIELD.getDim());
            for (final String item : conSet.getValue()) {
                comboBox.addItem(item);
            }
            comp = comboBox;
            supp = () -> ((String) comboBox.getSelectedItem());
        } else {
            if (constraints.getSchema() != null) {
                return createSchemaConstrainedStringTypeFromJson(constraints, jsonNode);
            }
            final JFormattedTextField strField = new JFormattedTextField();
            final Supplier<Boolean> validator;
            final String conditionDesc;
            if (constraints.getPattern() != null) {
                final String pattern = constraints.getPattern();
                validator = () -> (strField.getText().matches(pattern));
                conditionDesc = "match " + pattern;
            } else if (constraints.getLength() != null) {
                final int len = constraints.getLength().intValue();
                ((AbstractDocument) strField.getDocument()).setDocumentFilter(
                        new DocumentLengthFilter(len));
                validator = () -> (strField.getText().length() == len);
                conditionDesc = "= " + len;
            } else if (constraints.getFullyQualifiedIdentifier() != null) {
                final String fqiType = constraints.getFullyQualifiedIdentifier();
                validator = () -> (ValidatorUtils.isFullyQualifiedIdentifierValid(
                        fqiType,
                        strField.getText(),
                        featCtx));
                conditionDesc = fqiType;
            } else {
                final BigInteger min = constraints.getMinimalLength();
                final BigInteger max = constraints.getMaximalLength();
                if (max != null) {
                    ((AbstractDocument) strField.getDocument()).setDocumentFilter(
                            new DocumentLengthFilter(max.intValue()));
                }

                if (min != null && max != null) {
                    validator = () -> {
                        final int len = strField.getText().length();
                        return (len >= min.intValue() && len <= max.intValue());
                    };
                    conditionDesc = GREATER_OR_EQUAL + min + AND_SIGN + GREATER_OR_EQUAL + max;
                } else if (min != null) {
                    validator = () -> (strField.getText().length() >= min.intValue());
                    conditionDesc = GREATER_OR_EQUAL + min;
                } else if (max != null) {
                    validator = () -> (strField.getText().length() <= max.intValue());
                    conditionDesc = LESS_OR_EQUAL + max;
                } else {
                    validator = () -> (false);
                    conditionDesc = INVALID_CONSTRAINT;
                }
            }

            final JLabel validationLabel = new JLabel(IconProvider.STATUS_OK.getIcon());
            validationLabel.setDisabledIcon(IconProvider.STATUS_WARNING.getIcon());
            validationLabel.setEnabled(false);

            // validate on enter
            strField.addActionListener((evt) -> {
                validationLabel.setEnabled(validator.get());
            });

            // validate after focus was lost
            strField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent evt) {
                    validationLabel.setEnabled(validator.get());
                }
            });

            final Box hBox = Box.createHorizontalBox();
            hBox.add(strField);
            hBox.add(Box.createHorizontalStrut(HORIZONTAL_STRUT));
            hBox.add(new JLabel(conditionDesc));
            hBox.add(Box.createHorizontalStrut(HORIZONTAL_STRUT));
            hBox.add(validationLabel);
            hBox.setMaximumSize(MaxDim.TEXT_FIELD.getDim());

            if (jsonNode != null) {
                strField.setText(jsonNode.asText());
                // validate after import
                validationLabel.setEnabled(validator.get());
            }
            comp = hBox;
            supp = () -> (strField.getText());
        }
        return new ConstraintBasicNode(BasicType.STRING, comp, supp, constraints);
    }

    protected static ConstraintBasicNode createSchemaConstrainedStringTypeFromJson(
            @NonNull final Constraints constraints,
            final JsonNode jsonNode
    ) {
        final Supplier<Boolean> validator;
        final String conditionDesc;
        final JEditorPane editorPane = new JEditorPane();
        final JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        final Constraints.Schema schema = constraints.getSchema();
        final String schemaType = schema.getType();
        if (schemaType.equalsIgnoreCase("Xml")) {
            if (schema.getUrl() != null) {
                validator = () -> (ValidatorUtils.isXmlWellFormed(new ByteArrayInputStream(
                        editorPane.getText().getBytes(StandardCharsets.UTF_8))) // 
                        // FIXME: A proper validation against the schema is not done
                        //        yet due to the poor support and the optional 
                        //        character of this feature. To enable validation,
                        //        simply uncomment the code blocks below. 
                        //        (2020-09-06, florian.bauer.dev@gmail.com)
                        /* && ValidatorUtils.isXmlValid(new ByteArrayInputStream(
                                                   editorPane.getText().getBytes(StandardCharsets.UTF_8)),
                                                   new StreamSource(schema.getUrl())) */);
            } else if (schema.getInline() != null) {
                validator = () -> (ValidatorUtils.isXmlWellFormed(new ByteArrayInputStream(
                        editorPane.getText().getBytes(StandardCharsets.UTF_8))) /*
                                        && ValidatorUtils.isXmlValid(new ByteArrayInputStream(
                                                editorPane.getText().getBytes(StandardCharsets.UTF_8)),
                                                new StreamSource(new ByteArrayInputStream(schema
                                                        .getInline()
                                                        .getBytes(StandardCharsets.UTF_8))))*/);
            } else {
                validator = () -> (false);
            }
            conditionDesc = "Xml";
        } else if (schemaType.equalsIgnoreCase("Json")) {
            // TODO: Implement proper JSON schema handling by URL and Inline.
            validator = () -> (ValidatorUtils.isJsonValid(editorPane.getText()));
            conditionDesc = "Json";
        } else {
            validator = () -> (false);
            conditionDesc = INVALID_CONSTRAINT;
        }

        final JLabel validationLabel = new JLabel(IconProvider.STATUS_OK.getIcon());
        validationLabel.setDisabledIcon(IconProvider.STATUS_WARNING.getIcon());
        validationLabel.setEnabled(false);

        // validate on edit
        editorPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent de) {
                validationLabel.setEnabled(validator.get());
            }

            @Override
            public void removeUpdate(DocumentEvent de) {
                validationLabel.setEnabled(validator.get());
            }

            @Override
            public void changedUpdate(DocumentEvent de) {
                // not used in plain text components
            }
        });

        final Box hBox = Box.createHorizontalBox();
        hBox.add(scrollPane);
        hBox.add(Box.createHorizontalStrut(HORIZONTAL_STRUT));
        hBox.add(new JLabel(conditionDesc));
        hBox.add(Box.createHorizontalStrut(HORIZONTAL_STRUT));
        hBox.add(validationLabel);
        hBox.setMaximumSize(MaxDim.TEXT_FIELD_MULTI_LINE.getDim());

        if (jsonNode != null) {
            editorPane.setText(jsonNode.asText());
            // validate after import
            validationLabel.setEnabled(validator.get());
        }
        return new ConstraintBasicNode(BasicType.STRING, hBox, () -> (editorPane.getText()), constraints);
    }

    protected static ConstraintBasicNode createConstrainedTimeTypeFromJson(
            @NonNull final Constraints constraints,
            final JsonNode jsonNode
    ) {
        final JComponent comp;
        final Supplier<String> supp;
        if (constraints.getSet() != null) {
            final List<String> timeSet = constraints.getSet().getValue();
            final OffsetTime[] times = new OffsetTime[timeSet.size()];
            for (int i = 0; i < timeSet.size(); i++) {
                try {
                    times[i] = DateTimeParser.parseIsoTime(timeSet.get(i))
                            .withOffsetSameInstant(DateTimeParser.LOCAL_OFFSET);
                } catch (final Exception ex) {
                    // skip invalid entries
                }
            }
            final JComboBox<OffsetTime> timeComboBox = new JComboBox<>(times);
            timeComboBox.setMaximumSize(MaxDim.DATE_TIME_SPINNER.getDim());
            supp = () -> {
                return ((OffsetTime) timeComboBox.getSelectedItem())
                        .withOffsetSameInstant(ZoneOffset.UTC)
                        .toString();
            };
            comp = timeComboBox;
        } else {
            String minBounds = null;
            if (constraints.getMinimalExclusive() != null) {
                minBounds = GREATER_THAN + DateTimeParser.parseIsoTime(
                        constraints.getMinimalExclusive()).toLocalTime().toString();
            } else if (constraints.getMinimalInclusive() != null) {
                minBounds = GREATER_OR_EQUAL + DateTimeParser.parseIsoTime(
                        constraints.getMinimalInclusive()).toLocalTime().toString();
            }

            String maxBounds = null;
            if (constraints.getMaximalExclusive() != null) {
                maxBounds = LESS_THAN + DateTimeParser.parseIsoTime(
                        constraints.getMaximalExclusive()).toLocalTime().toString();
            } else if (constraints.getMaximalInclusive() != null) {
                maxBounds = LESS_OR_EQUAL + DateTimeParser.parseIsoTime(
                        constraints.getMaximalInclusive()).toLocalTime().toString();
            }

            final String conditionDescr;
            if (minBounds != null && maxBounds != null) {
                conditionDescr = minBounds + AND_SIGN + maxBounds;
            } else if (minBounds != null) {
                conditionDescr = minBounds;
            } else if (maxBounds != null) {
                conditionDescr = maxBounds;
            } else {
                conditionDescr = INVALID_CONSTRAINT;
            }

            OffsetTime initTime = OffsetTime.now().truncatedTo(ChronoUnit.SECONDS);
            if (jsonNode != null) {
                try {
                    initTime = DateTimeParser.parseIsoTime(jsonNode.asText())
                            .withOffsetSameInstant(DateTimeParser.LOCAL_OFFSET);
                } catch (final Exception ex) {
                    // do nothing and use the current time instead
                }
            }
            final JSpinner timeSpinner = new JSpinner();
            timeSpinner.setModel(ConstraintSpinnerModelFactory
                    .createRangeConstrainedTimeModel(initTime, constraints));
            timeSpinner.setMaximumSize(MaxDim.DATE_TIME_SPINNER.getDim());
            timeSpinner.setEditor(new OffsetTimeSpinnerEditor(timeSpinner));
            supp = () -> {
                return ((OffsetTime) timeSpinner.getValue())
                        .withOffsetSameInstant(ZoneOffset.UTC)
                        .toString();
            };

            final Box hBox = Box.createHorizontalBox();
            hBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            hBox.add(timeSpinner);
            hBox.add(Box.createHorizontalStrut(HORIZONTAL_STRUT));
            hBox.add(new JLabel(conditionDescr));
            comp = hBox;
        }
        return new ConstraintBasicNode(BasicType.TIME, comp, supp, constraints);
    }

    protected static ConstraintBasicNode createConstrainedTimestampeTypeFromJson(
            @NonNull final Constraints constraints,
            final JsonNode jsonNode
    ) {
        final JComponent comp;
        final Supplier<String> supp;
        if (constraints.getSet() != null) {
            final List<String> timeSet = constraints.getSet().getValue();
            final OffsetDateTime[] times = new OffsetDateTime[timeSet.size()];
            for (int i = 0; i < timeSet.size(); i++) {
                try {
                    times[i] = DateTimeParser.parseIsoDateTime(timeSet.get(i))
                            .withOffsetSameInstant(DateTimeParser.LOCAL_OFFSET);
                } catch (final Exception ex) {
                    // skip invalid entries
                }
            }
            final JComboBox<OffsetDateTime> timestampComboBox = new JComboBox<>(times);
            timestampComboBox.setMaximumSize(MaxDim.TIMESTAMP_SPINNER.getDim());
            supp = () -> {
                return ((OffsetDateTime) timestampComboBox.getSelectedItem())
                        .withOffsetSameInstant(ZoneOffset.UTC)
                        .toString();
            };
            comp = timestampComboBox;
        } else {
            String minBounds = null;
            if (constraints.getMinimalExclusive() != null) {
                minBounds = GREATER_THAN + DateTimeParser.parseIsoDateTime(constraints
                        .getMinimalExclusive()).withOffsetSameInstant(DateTimeParser.LOCAL_OFFSET)
                        .toString();
            } else if (constraints.getMinimalInclusive() != null) {
                minBounds = GREATER_OR_EQUAL + DateTimeParser.parseIsoDateTime(constraints
                        .getMinimalInclusive()).withOffsetSameInstant(DateTimeParser.LOCAL_OFFSET)
                        .toString();
            }

            String maxBounds = null;
            if (constraints.getMaximalExclusive() != null) {
                maxBounds = LESS_THAN + DateTimeParser.parseIsoDateTime(constraints
                        .getMaximalExclusive()).withOffsetSameInstant(DateTimeParser.LOCAL_OFFSET)
                        .toString();
            } else if (constraints.getMaximalInclusive() != null) {
                maxBounds = LESS_OR_EQUAL + DateTimeParser.parseIsoDateTime(constraints
                        .getMaximalInclusive()).withOffsetSameInstant(DateTimeParser.LOCAL_OFFSET)
                        .toString();
            }

            final String conditionDescr;
            if (minBounds != null && maxBounds != null) {
                conditionDescr = minBounds + AND_SIGN + maxBounds;
            } else if (minBounds != null) {
                conditionDescr = minBounds;
            } else if (maxBounds != null) {
                conditionDescr = maxBounds;
            } else {
                conditionDescr = INVALID_CONSTRAINT;
            }

            OffsetDateTime initDateTime = OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS);
            if (jsonNode != null) {
                try {
                    initDateTime = DateTimeParser.parseIsoDateTime(jsonNode.asText());
                } catch (final Exception ex) {
                    // do nothing and use the current time instead
                }
            }
            final JSpinner timestampSpinner = new JSpinner();
            timestampSpinner.setModel(ConstraintSpinnerModelFactory
                    .createRangeConstrainedDateTimeModel(initDateTime, constraints));
            timestampSpinner.setMaximumSize(MaxDim.TIMESTAMP_SPINNER.getDim());
            timestampSpinner.setEditor(new OffsetDateTimeSpinnerEditor(
                    timestampSpinner,
                    FormatterType.OFFSET_TIMESTAMP));
            supp = () -> {
                return timestampSpinner.getValue().toString();
            };

            final Box hBox = Box.createHorizontalBox();
            hBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            hBox.add(timestampSpinner);
            hBox.add(Box.createHorizontalStrut(HORIZONTAL_STRUT));
            hBox.add(new JLabel(conditionDescr));
            comp = hBox;
        }
        return new ConstraintBasicNode(BasicType.TIMESTAMP, comp, supp, constraints);
    }

    /**
     * Creates a plain text node from the given binary values. The text is represented in an editor
     * pane with scroll bars. Checks on a valid UTF-8 encoding have to be done beforehand.
     *
     * @param binaryValue The UTF-8 encoded input data.
     * @param constraints The constraints.
     * @return The constrained node.
     *
     * @see ValidatorUtils#isValidUtf8
     */
    protected static ConstraintBasicNode createTextNodeFromBinary(
            @NonNull final Constraints constraints,
            @NonNull final byte[] binaryValue
    ) {
        final String plainTxt = new String(binaryValue, StandardCharsets.UTF_8);
        final JEditorPane editorPane = new JEditorPane();
        editorPane.setText(plainTxt);
        final JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setMaximumSize(MaxDim.TEXT_FIELD_MULTI_LINE.getDim());
        final Supplier<String> supp = () -> {
            return Base64.getEncoder().encodeToString(editorPane
                    .getText()
                    .getBytes(StandardCharsets.UTF_8));
        };
        return new ConstraintBasicNode(BasicType.BINARY, editorPane, supp, constraints);
    }

    /**
     * Creates a plain text node with XML validation from the given binary values. Checks on a valid
     * UTF-8 encoding have to be done beforehand.
     *
     * @param binaryValue The UTF-8 encoded input data.
     * @param constraints The constraints.
     * @return The constrained node.
     *
     * @see ValidatorUtils#isValidUtf8
     */
    public static ConstraintBasicNode createXmlNodeFromBinary(
            @NonNull final Constraints constraints,
            @NonNull final byte[] binaryValue
    ) {
        final String plainText = new String(binaryValue, StandardCharsets.UTF_8);
        final JEditorPane editorPane = new JEditorPane();
        final Supplier<Boolean> validator = () -> (ValidatorUtils.isXmlWellFormed(
                new ByteArrayInputStream(editorPane.getText().getBytes(StandardCharsets.UTF_8))));
        final JLabel validationLabel = new JLabel(IconProvider.STATUS_OK.getIcon());
        validationLabel.setDisabledIcon(IconProvider.STATUS_WARNING.getIcon());
        validationLabel.setEnabled(false);
        // validate after focus was lost
        editorPane.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(final FocusEvent evt) {
                validationLabel.setEnabled(validator.get());
            }
        });

        final JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        final Box hBox = Box.createHorizontalBox();
        hBox.add(scrollPane);
        hBox.add(Box.createHorizontalStrut(HORIZONTAL_STRUT));
        hBox.add(new JLabel("Xml"));
        hBox.add(Box.createHorizontalStrut(HORIZONTAL_STRUT));
        hBox.add(validationLabel);
        hBox.setMaximumSize(MaxDim.TEXT_FIELD_MULTI_LINE.getDim());

        editorPane.setText(plainText);
        // validate after import
        validationLabel.setEnabled(validator.get());

        final Supplier<String> supp = () -> {
            return Base64.getEncoder().encodeToString(editorPane
                    .getText()
                    .getBytes(StandardCharsets.UTF_8));
        };
        return new ConstraintBasicNode(BasicType.BINARY, hBox, supp, constraints);
    }

    /**
     * Creates a image node from binary data. Only jpeg, png, bmp and gif formats are supported.
     *
     * @param binaryValue The binary data of the image.
     * @param constraints The constraints.
     * @return The image type constrained node.
     */
    protected static ConstraintBasicNode createImageNodeFromBinary(
            @NonNull final Constraints constraints,
            @NonNull final byte[] binaryValue
    ) {
        final BufferedImage img;
        try {
            img = ImageIO.read(new ByteArrayInputStream(binaryValue));
        } catch (final IOException ex) {
            return new ConstraintBasicNode(BasicType.BINARY,
                    new JLabel("Error: " + ex.getMessage()),
                    () -> (""),
                    constraints);
        }

        final ImagePanel imgPanel = new ImagePanel(img);
        final Supplier<String> supp = () -> {
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            final boolean hasWriter;
            try {
                hasWriter = ImageIO.write(img,
                        constraints.getContentType().getSubtype().toLowerCase(),
                        Base64.getEncoder().wrap(os));
                os.close();
            } catch (final Exception ex) {
                return "";
            }
            if (hasWriter) {
                return os.toString(StandardCharsets.UTF_8);
            } else {
                return "";
            }
        };
        return new ConstraintBasicNode(BasicType.BINARY, imgPanel, supp, constraints);
    }

    /**
     * Gets the internal used enumeration to mark (un-)supported types for the process of node
     * creation.
     *
     * @param contentType The ContentType object holding the IANA media type (MIME type).
     * @return The only internally used enum for (un-)supported types.
     */
    protected static InternalContentType getSupportedContentType(
            @NonNull final Constraints.ContentType contentType
    ) {
        final String type = contentType.getType().toLowerCase();
        final String subtype;
        if (contentType.getSubtype() != null) {
            subtype = contentType.getSubtype().toLowerCase();
        } else {
            subtype = "";
        }

        switch (type) {
            case "application":
                switch (subtype) {
                    case "animl":
                        return InternalContentType.TEXT_XML;
                }
                return InternalContentType.UNKNOWN;
            case "image":
                switch (subtype) {
                    case "jpeg":
                    case "png":
                    case "bmp":
                    case "gif":
                        return InternalContentType.IMAGE;
                }
                return InternalContentType.UNSUPPORTED;
            case "text":
                switch (subtype) {
                    case "xml":
                        return InternalContentType.TEXT_XML;
                }
                return InternalContentType.TEXT;
        }
        return InternalContentType.UNKNOWN;
    }
}
