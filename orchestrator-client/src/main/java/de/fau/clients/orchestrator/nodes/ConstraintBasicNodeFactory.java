package de.fau.clients.orchestrator.nodes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
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
import de.fau.clients.orchestrator.utils.SilaBasicTypeUtils;
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
import java.util.Vector;
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
import sila_java.library.core.sila.mapping.feature.MalformedSiLAFeature;
import sila_java.library.core.sila.mapping.grpc.ProtoMapper;

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
                return createConstrainedRealType(constraints, 0.0);
            case STRING:
                return createConstrainedStringTypeFromJson(constraints, null, featCtx);
            case TIME:
                return createConstrainedTimeType(constraints, OffsetTime.now());
            case TIMESTAMP:
                return createConstrainedTimestampeTypeFromJson(constraints, null);
            default:
                throw new IllegalArgumentException("Not a valid BasicType.");
        }
    }

    protected static SilaNode createFromJson(
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
                } catch (final Exception ex) {
                    return createErrorType(type, ex.getMessage());
                }
                return createConstrainedAnyType(featCtx, constraints, dtt, payload);
            }
            case BINARY: {
                final byte[] binaryVal;
                try {
                    binaryVal = jsonNode.get("value").binaryValue();
                } catch (final Exception ex) {
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
                final LocalDate dateVal = SilaBasicTypeUtils.dateFromJsonNode(jsonNode);
                if (dateVal == null) {
                    return BasicNodeFactory.createErrorType(type, "Date value is 'null'.");
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
            case REAL: {
                final double realVal;
                try {
                    realVal = Double.parseDouble(jsonNode.get("value").asText());
                } catch (final Exception ex) {
                    return BasicNodeFactory.createErrorType(type, ex.getMessage());
                }
                return createConstrainedRealType(constraints, realVal);
            }
            case STRING:
                return createConstrainedStringTypeFromJson(constraints, jsonNode.get("value"), featCtx);
            case TIME: {
                final OffsetTime timeVal = SilaBasicTypeUtils.timeFromJsonNode(jsonNode);
                if (timeVal == null) {
                    return BasicNodeFactory.createErrorType(type, "Time value is 'null'.");
                }
                return createConstrainedTimeType(constraints, timeVal);
            }
            case TIMESTAMP:
                final OffsetDateTime timestampVal = SilaBasicTypeUtils.timestampFromJsonNode(jsonNode);
                if (timestampVal == null) {
                    return BasicNodeFactory.createErrorType(type, "Timestamp value is 'null'.");
                }
                // FIXME: Replace function with not (yet) implemented 'createConstrainedTimestampeType()' (2020-11-19 florian.bauer.dev@gmail.com). 
                return createConstrainedTimestampeTypeFromJson(constraints, jsonNode.get("value"));
            default:
                throw new IllegalArgumentException("Not a valid BasicType.");
        }
    }

    protected static SilaNode createConstrainedAnyType(
            final FeatureContext featCtx,
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
                        return BasicNodeFactory.createAnyType(featCtx, dtt, payload, false);
                    }
                }
            } else if (dtt.getConstrained() != null) {
                return createConstrainedAnyType(featCtx, dtt.getConstrained().getConstraints(), dtt, payload);
            } else if (dtt.getList() != null) {
                // TODO: implement allowed type check
                return BasicNodeFactory.createAnyType(featCtx, dtt, payload, false);
            } else if (dtt.getStructure() != null) {
                // TODO: implement allowed type check
                return BasicNodeFactory.createAnyType(featCtx, dtt, payload, false);
            }
        } else {
            final DynamicMessage dynMsg;
            try {
                dynMsg = DynamicMessage.parseFrom(ProtoMapper.dataTypeToDescriptor(dtt), payload);
            } catch (final MalformedSiLAFeature | InvalidProtocolBufferException ex) {
                return createErrorType(BasicType.ANY, ex.getMessage());
            }

            final ObjectMapper jsonMapper = new ObjectMapper();
            final JsonNode jsonNode;
            try {
                jsonNode = jsonMapper.readTree(JsonFormat.printer().print(dynMsg));
            } catch (final InvalidProtocolBufferException | JsonProcessingException ex) {
                return createErrorType(BasicType.ANY, ex.getMessage());
            }
            return NodeFactory.createFromJson(featCtx, dtt, jsonNode, false);
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
        return new ConstraintBasicNode(BasicType.BINARY, errorLabel, () -> ("".getBytes()), constraints);
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
        final Supplier<LocalDate> supp;
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
                return DateTimeParser.parseIsoDate(dateComboBox.getSelectedItem().toString());
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
                return DateTimeParser.parseIsoDate(dateSpinner.getValue().toString());
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
            long intValue
    ) {
        final JComponent comp;
        final Supplier<Long> supp;
        if (constraints.getSet() != null) {
            final List<String> numberSet = constraints.getSet().getValue();
            final JComboBox<String> numberComboBox = new JComboBox<>(numberSet.toArray(new String[0]));
            numberComboBox.setMaximumSize(MaxDim.NUMERIC_SPINNER.getDim());
            int selectionIdx = 0;
            for (int i = 0; i < numberSet.size(); i++) {
                if (numberSet.get(i).equals(Long.toString(intValue))) {
                    selectionIdx = i;
                    break;
                }
            }
            numberComboBox.setSelectedIndex(selectionIdx);
            supp = () -> (Long.parseLong(numberComboBox.getSelectedItem().toString()));
            comp = numberComboBox;
        } else {
            final SpinnerModel model = ConstraintSpinnerModelFactory
                    .createRangeConstrainedIntModel(intValue, constraints);
            final JSpinner numericSpinner = new JSpinner(model);
            numericSpinner.setMaximumSize(MaxDim.NUMERIC_SPINNER.getDim());
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
            supp = () -> ((long) numericSpinner.getValue());
        }
        return new ConstraintBasicNode(BasicType.INTEGER, comp, supp, constraints);
    }

    /**
     * Creates a <code>ConstraintBasicNode</code> of the type <code>BasicType.REAL</code>.
     *
     * @param constraints The applied floating point constraints.
     * @param intValue The double value to initialize the node with.
     * @return The initialized, constrained BasicNode representing a double value.
     */
    protected static ConstraintBasicNode createConstrainedRealType(
            @NonNull final Constraints constraints,
            double realValue
    ) {
        final JComponent comp;
        final Supplier<Double> supp;
        if (constraints.getSet() != null) {
            final List<String> numberSet = constraints.getSet().getValue();
            final JComboBox<String> numberComboBox = new JComboBox<>(numberSet.toArray(new String[0]));
            numberComboBox.setMaximumSize(MaxDim.NUMERIC_SPINNER.getDim());
            int selectionIdx = 0;
            for (int i = 0; i < numberSet.size(); i++) {
                if (numberSet.get(i).equals(Double.toString(realValue))) {
                    selectionIdx = i;
                    break;
                }
            }
            numberComboBox.setSelectedIndex(selectionIdx);
            supp = () -> (Double.parseDouble(numberComboBox.getSelectedItem().toString()));
            comp = numberComboBox;
        } else {
            final SpinnerModel model = ConstraintSpinnerModelFactory
                    .createRangeConstrainedRealModel(realValue, constraints);
            final JSpinner numericSpinner = new JSpinner(model);
            numericSpinner.setMaximumSize(MaxDim.NUMERIC_SPINNER.getDim());
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
            supp = () -> ((double) numericSpinner.getValue());
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
            // FIXME: Select item entry from jsonNode as default. (2020-11-19 florian.bauer.dev@gmail.com)
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

    /**
     * Creates a <code>ConstraintBasicNode</code> of the type <code>BasicType.TIME</code>.
     *
     * @param constraints The applied time constraints.
     * @param timeValue The time to initialize the node with.
     * @return The initialized, constrained BasicNode representing a time value.
     */
    protected static ConstraintBasicNode createConstrainedTimeType(
            @NonNull final Constraints constraints,
            @NonNull final OffsetTime timeValue
    ) {
        final JComponent comp;
        final Supplier<OffsetTime> supp;
        if (constraints.getSet() != null) {
            final List<String> timeSet = constraints.getSet().getValue();
            final Vector<OffsetTime> times = new Vector<OffsetTime>(timeSet.size());
            int selectionIdx = 0;
            int j = 0;
            for (final String timeEntry : timeSet) {
                final OffsetTime time;
                try {
                    time = DateTimeParser.parseIsoTime(timeEntry)
                            .withOffsetSameInstant(DateTimeParser.LOCAL_OFFSET);
                } catch (final Exception ex) {
                    // skip invalid entries
                    continue;
                }
                times.add(time);
                if (time.isEqual(timeValue)) {
                    selectionIdx = j;
                }
                j++;
            }
            final JComboBox<OffsetTime> timeComboBox = new JComboBox<>(times);
            timeComboBox.setMaximumSize(MaxDim.DATE_TIME_SPINNER.getDim());
            timeComboBox.setSelectedIndex(selectionIdx);
            supp = () -> {
                return ((OffsetTime) timeComboBox.getSelectedItem())
                        .withOffsetSameInstant(ZoneOffset.UTC);
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

            final OffsetTime initTime = timeValue
                    .truncatedTo(ChronoUnit.SECONDS)
                    .withOffsetSameInstant(DateTimeParser.LOCAL_OFFSET);
            final JSpinner timeSpinner = new JSpinner();
            timeSpinner.setModel(ConstraintSpinnerModelFactory
                    .createRangeConstrainedTimeModel(initTime, constraints));
            timeSpinner.setMaximumSize(MaxDim.DATE_TIME_SPINNER.getDim());
            timeSpinner.setEditor(new OffsetTimeSpinnerEditor(timeSpinner));
            supp = () -> {
                return ((OffsetTime) timeSpinner.getValue())
                        .withOffsetSameInstant(ZoneOffset.UTC);
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
        final Supplier<OffsetDateTime> supp;
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
                        .withOffsetSameInstant(ZoneOffset.UTC);
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
                return (OffsetDateTime) timestampSpinner.getValue();
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
        final Supplier<byte[]> supp = () -> (editorPane.getText().getBytes(StandardCharsets.UTF_8));
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

        final Supplier<byte[]> supp = () -> (editorPane.getText().getBytes(StandardCharsets.UTF_8));
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
                    () -> ("".getBytes()),
                    constraints);
        }

        final ImagePanel imgPanel = new ImagePanel(img);
        final Supplier<byte[]> supp = () -> {
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            final boolean hasWriter;
            try {
                hasWriter = ImageIO.write(img,
                        constraints.getContentType().getSubtype().toLowerCase(),
                        Base64.getEncoder().wrap(os));
                os.close();
            } catch (final Exception ex) {
                return "".getBytes();
            }
            if (hasWriter) {
                return os.toByteArray();
            } else {
                return "".getBytes();
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
