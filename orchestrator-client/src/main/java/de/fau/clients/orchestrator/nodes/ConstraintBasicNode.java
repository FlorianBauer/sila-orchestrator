package de.fau.clients.orchestrator.nodes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fau.clients.orchestrator.utils.DateTimeParser;
import de.fau.clients.orchestrator.utils.DocumentLengthFilter;
import de.fau.clients.orchestrator.utils.IconProvider;
import de.fau.clients.orchestrator.utils.LocalDateSpinnerEditor;
import de.fau.clients.orchestrator.utils.LocalTimeSpinnerEditor;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerModel;
import javax.swing.text.AbstractDocument;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.SAXException;
import sila_java.library.core.models.BasicType;
import sila_java.library.core.models.Constraints;
import sila_java.library.core.models.Feature;
import sila_java.library.core.models.Feature.Command;
import sila_java.library.core.models.Feature.DefinedExecutionError;
import sila_java.library.core.models.Feature.Metadata;
import sila_java.library.core.models.Feature.Property;
import sila_java.library.core.models.SiLAElement;

/**
 * A <code>BasicNode</code> with additional restrictions given by the SiLA-Constraint object.
 *
 * @see BasicNode
 * @see BasicNodeFactory
 * @see ConstraintBasicNodeFactory
 */
@Slf4j
public class ConstraintBasicNode extends BasicNode {

    /**
     * The date-format used by the GUI-components.
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    /**
     * The time-format used by the GUI-components.
     */
    private static final String TIME_FORMAT = "HH:mm:ss";
    private static final String DATE_TIME_FORMAT = DATE_FORMAT + " " + TIME_FORMAT;
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
    private final TypeDefLut typeDefs;
    private final Constraints constraints;

    protected ConstraintBasicNode(
            @NonNull final BasicType type,
            @NonNull final JComponent component,
            @NonNull final Supplier<String> valueSupplier,
            @NonNull final Constraints constraints) {
        super(type, component, valueSupplier);
        this.typeDefs = null;
        this.constraints = constraints;
    }

    protected ConstraintBasicNode(
            @NonNull final TypeDefLut typeDefs,
            @NonNull final BasicType type,
            @NonNull final JComponent component,
            @NonNull final Supplier<String> valueSupplier,
            @NonNull final Constraints constraints) {
        super(type, component, valueSupplier);
        this.typeDefs = typeDefs;
        this.constraints = constraints;
    }

    protected static BasicNode create(
            final TypeDefLut typeDefs,
            final BasicType type,
            final Constraints constraints,
            final JsonNode jsonNode) {
        final JComponent comp;
        final Supplier<String> supp;
        switch (type) {
            case ANY:
                // TODO: implement
                comp = new JLabel("placeholder 03");
                supp = () -> ("not implemented 03");
                break;
            case BINARY:
                return ConstraintBasicNodeFactory.createConstrainedBinaryTypeFromJson(constraints, jsonNode);
            case BOOLEAN:
                return BasicNodeFactory.createBooleanTypeFromJson(jsonNode, true);
            case DATE:
                if (constraints.getSet() != null) {
                    final List<String> dateSet = constraints.getSet().getValue();
                    final LocalDate[] dates = new LocalDate[dateSet.size()];
                    for (int i = 0; i < dateSet.size(); i++) {
                        dates[i] = DateTimeParser.parseIsoDate(dateSet.get(i));
                    }
                    final JComboBox<LocalDate> dateComboBox = new JComboBox<>(dates);
                    dateComboBox.setMaximumSize(BasicNodeFactory.MAX_SIZE_DATE_TIME_SPINNER);
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

                    LocalDate initDate = LocalDate.now();
                    if (jsonNode != null) {
                        try {
                            initDate = DateTimeParser.parseIsoDate(jsonNode.asText());
                        } catch (Exception ex) {
                            // do nothing
                        }
                    }

                    final JSpinner dateSpinner = new JSpinner(
                            ConstraintSpinnerModelFactory.createRangeConstrainedDateModel(initDate, constraints));
                    dateSpinner.setMaximumSize(BasicNodeFactory.MAX_SIZE_DATE_TIME_SPINNER);
                    dateSpinner.setEditor(new LocalDateSpinnerEditor(dateSpinner));
                    supp = () -> {
                        return dateSpinner.getValue().toString();
                    };
                    final Box hbox = Box.createHorizontalBox();
                    hbox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
                    hbox.add(dateSpinner);
                    hbox.add(Box.createHorizontalStrut(HORIZONTAL_STRUT));
                    hbox.add(new JLabel(conditionDesc));
                    comp = hbox;
                }
                break;
            case INTEGER:
            case REAL:
                if (constraints.getSet() != null) {
                    final List<String> numberSet = constraints.getSet().getValue();
                    final JComboBox<String> numberComboBox = new JComboBox<>(numberSet.toArray(new String[0]));
                    numberComboBox.setMaximumSize(BasicNodeFactory.MAX_SIZE_NUMERIC_SPINNER);

                    if (jsonNode != null) {
                        numberComboBox.setSelectedItem(jsonNode.asText());
                    }

                    if (type == BasicType.INTEGER) {
                        supp = () -> (Integer.valueOf(numberComboBox.getSelectedItem().toString()).toString());
                    } else { // REAL
                        supp = () -> (Double.valueOf(numberComboBox.getSelectedItem().toString()).toString());
                    }
                    comp = numberComboBox;
                } else {
                    final SpinnerModel model = (type == BasicType.INTEGER)
                            ? ConstraintSpinnerModelFactory.createRangeConstrainedIntModel(constraints)
                            : ConstraintSpinnerModelFactory.createRangeConstrainedRealModel(constraints);
                    final JSpinner numericSpinner = new JSpinner(model);
                    numericSpinner.setMaximumSize(BasicNodeFactory.MAX_SIZE_NUMERIC_SPINNER);
                    if (jsonNode != null) {
                        if (type == BasicType.INTEGER) {
                            numericSpinner.setValue(jsonNode.asInt());
                        } else { // REAL
                            numericSpinner.setValue(jsonNode.asDouble());
                        }
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
                            conditionDesc = INVALID_CONSTRAINT;;
                        }
                    }

                    final Box hbox = Box.createHorizontalBox();
                    hbox.add(numericSpinner);
                    hbox.add(Box.createHorizontalStrut(HORIZONTAL_STRUT));
                    hbox.add(new JLabel(conditionDesc));
                    hbox.setMaximumSize(BasicNodeFactory.MAX_SIZE_TEXT_FIELD);
                    comp = hbox;
                    supp = () -> (numericSpinner.getValue().toString());
                }
                break;
            case STRING:
                final Constraints.Set conSet = constraints.getSet();
                if (conSet != null) {
                    final JComboBox<String> comboBox = new JComboBox<>();
                    comboBox.setMaximumSize(BasicNodeFactory.MAX_SIZE_TEXT_FIELD);
                    for (final String item : conSet.getValue()) {
                        comboBox.addItem(item);
                    }
                    comp = comboBox;
                    supp = () -> ((String) comboBox.getSelectedItem());
                } else {
                    final JFormattedTextField strField = new JFormattedTextField();
                    strField.setMaximumSize(BasicNodeFactory.MAX_SIZE_TEXT_FIELD);
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
                    } else if (constraints.getSchema() != null) {
                        final Constraints.Schema schema = constraints.getSchema();
                        final String schemaType = schema.getType();
                        if (schemaType.equalsIgnoreCase("Xml")) {
                            if (schema.getUrl() != null) {
                                validator = () -> (isXmlWellFormed(new ByteArrayInputStream(
                                        strField.getText().getBytes(StandardCharsets.UTF_8))) // 
                                        // FIXME: A proper validation against the schema is not done
                                        //        yet due to the poor support and the optional 
                                        //        character of this feature. To enable validation,
                                        //        simply uncomment the code blocks below. 
                                        //        (2020-09-06, florian.bauer.dev@gmail.com)
                                        /* && isXmlValid(new ByteArrayInputStream(
                                                   strField.getText().getBytes(StandardCharsets.UTF_8)),
                                                   new StreamSource(schema.getUrl())) */);
                            } else if (schema.getInline() != null) {
                                validator = () -> (isXmlWellFormed(new ByteArrayInputStream(
                                        strField.getText().getBytes(StandardCharsets.UTF_8))) /*
                                        && isXmlValid(new ByteArrayInputStream(
                                                strField.getText().getBytes(StandardCharsets.UTF_8)),
                                                new StreamSource(new ByteArrayInputStream(schema
                                                        .getInline()
                                                        .getBytes(StandardCharsets.UTF_8))))*/);
                            } else {
                                validator = () -> (false);
                            }
                            conditionDesc = "Xml";
                        } else if (schemaType.equalsIgnoreCase("Json")) {
                            // TODO: Implement proper JSON schema handling by URL and Inline.
                            validator = () -> (isJsonValid(strField.getText()));
                            conditionDesc = "Json";
                        } else {
                            validator = () -> (false);
                            conditionDesc = INVALID_CONSTRAINT;
                        }
                    } else if (constraints.getFullyQualifiedIdentifier() != null) {
                        final String fqiType = constraints.getFullyQualifiedIdentifier();
                        validator = () -> (vlidateFullyQualifiedIdentifier(fqiType,
                                strField.getText(),
                                typeDefs));
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

                    final Box hbox = Box.createHorizontalBox();
                    hbox.add(strField);
                    hbox.add(Box.createHorizontalStrut(HORIZONTAL_STRUT));
                    hbox.add(new JLabel(conditionDesc));
                    hbox.add(Box.createHorizontalStrut(HORIZONTAL_STRUT));
                    hbox.add(validationLabel);
                    hbox.setMaximumSize(BasicNodeFactory.MAX_SIZE_TEXT_FIELD);

                    if (jsonNode != null) {
                        strField.setText(jsonNode.asText());
                        // validate after import
                        validationLabel.setEnabled(validator.get());
                    }
                    comp = hbox;
                    supp = () -> (strField.getText());
                }
                break;
            case TIME:
                if (constraints.getSet() != null) {
                    final List<String> timeSet = constraints.getSet().getValue();
                    final OffsetTime[] times = new OffsetTime[timeSet.size()];
                    for (int i = 0; i < timeSet.size(); i++) {
                        times[i] = DateTimeParser.parseIsoTime(timeSet.get(i));
                    }
                    final JComboBox<OffsetTime> timeComboBox = new JComboBox<>(times);
                    timeComboBox.setMaximumSize(BasicNodeFactory.MAX_SIZE_DATE_TIME_SPINNER);
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

                    final JSpinner timeSpinner = new JSpinner();
                    LocalTime initTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
                    if (jsonNode != null) {
                        try {
                            initTime = LocalTime.parse(jsonNode.asText());
                        } catch (Exception ex) {
                            // do nothing and use the current time instead
                        }
                    }
                    timeSpinner.setModel(
                            ConstraintSpinnerModelFactory.createRangeConstrainedTimeModel(initTime, constraints));
                    timeSpinner.setMaximumSize(BasicNodeFactory.MAX_SIZE_DATE_TIME_SPINNER);
                    timeSpinner.setEditor(new LocalTimeSpinnerEditor(timeSpinner));

                    supp = () -> {
                        return timeSpinner.getValue().toString();
                    };

                    final Box hbox = Box.createHorizontalBox();
                    hbox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
                    hbox.add(timeSpinner);
                    hbox.add(Box.createHorizontalStrut(HORIZONTAL_STRUT));
                    hbox.add(new JLabel(conditionDescr));
                    comp = hbox;
                }
                break;
            case TIMESTAMP:
                if (constraints.getSet() != null) {
                    final List<String> timeSet = constraints.getSet().getValue();
                    final OffsetDateTime[] times = new OffsetDateTime[timeSet.size()];
                    for (int i = 0; i < timeSet.size(); i++) {
                        times[i] = DateTimeParser.parseIsoDateTime(timeSet.get(i));
                    }
                    final JComboBox<OffsetDateTime> timestampComboBox = new JComboBox<>(times);
                    timestampComboBox.setMaximumSize(BasicNodeFactory.MAX_SIZE_TIMESTAMP_SPINNER);
                    supp = () -> {
                        return timestampComboBox.getSelectedItem().toString();
                    };
                    comp = timestampComboBox;
                } else {
                    final JSpinner timestampSpinner = new JSpinner(
                            // TODO: implement createRangeConstrainedTimestampModel(constraints)
                            new SpinnerDateModel()
                    );
                    timestampSpinner.setMaximumSize(BasicNodeFactory.MAX_SIZE_TIMESTAMP_SPINNER);
                    timestampSpinner.setEditor(new JSpinner.DateEditor(timestampSpinner, DATE_TIME_FORMAT));
                    supp = () -> {
                        Date time = (Date) timestampSpinner.getValue();
                        return OffsetDateTime.ofInstant(time.toInstant(), ZoneOffset.UTC).toString();
                    };
                    comp = timestampSpinner;
                }
                break;
            default:
                throw new IllegalArgumentException("Not a valid BasicType.");
        }
        return new ConstraintBasicNode(typeDefs, type, comp, supp, constraints);
    }

    @Override
    public BasicNode cloneNode() {
        return create(this.typeDefs, this.type, this.constraints, null);
    }

    @NonNull
    @Override
    public String toString() {
        return "(" + this.type + ", "
                + this.component.getClass() + ", "
                + this.valueSupplier.getClass() + ", "
                + this.constraints + ")";
    }

    protected Constraints getConstaint() {
        return this.constraints;
    }

    /**
     * Validates a <code>FullyQualifiedIdentifier</code>. Example:
     * <code>org.silastandard/core/SiLAService/v1</code>.
     *
     * @param fqiType The <code>FullyQualifiedIdentifier</code>-type to validate (e.g.
     * <code>FeatureIdentifier</code>).
     * @param fqiUri The FQI string to validate (e.g.
     * <code>org.silastandard/core/SiLAService/v1</code>).
     * @param typeDefs The type definitions.
     * @return <code>true</code> if valid, otherwise <code>false</code>.
     */
    private static boolean vlidateFullyQualifiedIdentifier(
            final String fqiType,
            final String fqiUri,
            final TypeDefLut typeDefs) {
        final List<Feature> featList = typeDefs.getServer().getFeatures();
        final String[] sections = fqiUri.split("/");
        if (fqiType.equalsIgnoreCase(FullyQualifiedIdentifier.FEATURE_IDENTIFIER.toString())) {
            if (sections.length < FullyQualifiedIdentifier.FEATURE_IDENTIFIER.getSectionCount()) {
                return false;
            }
            for (final Feature feat : featList) {
                if (feat.getIdentifier().equalsIgnoreCase(sections[2])) {
                    return true;
                }
            }
        } else if (fqiType.equalsIgnoreCase(FullyQualifiedIdentifier.COMMAND_IDENTIFIER.toString())) {
            if (sections.length < FullyQualifiedIdentifier.COMMAND_IDENTIFIER.getSectionCount()) {
                return false;
            }
            for (final Feature feat : featList) {
                if (feat.getIdentifier().equalsIgnoreCase(sections[2])) {
                    for (final Command cmd : feat.getCommand()) {
                        if (cmd.getIdentifier().equalsIgnoreCase(sections[5])) {
                            return true;
                        }
                    }
                }
            }
        } else if (fqiType.equalsIgnoreCase(FullyQualifiedIdentifier.COMMAND_PARAMETER_IDENTIFIER.toString())) {
            if (sections.length < FullyQualifiedIdentifier.COMMAND_PARAMETER_IDENTIFIER.getSectionCount()) {
                return false;
            }
            for (final Feature feat : featList) {
                if (feat.getIdentifier().equalsIgnoreCase(sections[2])) {
                    for (final Command cmd : feat.getCommand()) {
                        if (cmd.getIdentifier().equalsIgnoreCase(sections[5])) {
                            for (final SiLAElement param : cmd.getParameter()) {
                                if (param.getIdentifier().equalsIgnoreCase(sections[7])) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        } else if (fqiType.equalsIgnoreCase(FullyQualifiedIdentifier.COMMAND_RESPONSE_IDENTIFIER.toString())) {
            if (sections.length < FullyQualifiedIdentifier.COMMAND_RESPONSE_IDENTIFIER.getSectionCount()) {
                return false;
            }
            for (final Feature feat : featList) {
                if (feat.getIdentifier().equalsIgnoreCase(sections[2])) {
                    for (final Command cmd : feat.getCommand()) {
                        if (cmd.getIdentifier().equalsIgnoreCase(sections[5])) {
                            for (final SiLAElement resp : cmd.getResponse()) {
                                if (resp.getIdentifier().equalsIgnoreCase(sections[7])) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        } else if (fqiType.equalsIgnoreCase(FullyQualifiedIdentifier.INTERMEDIATE_COMMAND_RESPONSEIDENTIFIER.toString())) {
            if (sections.length < FullyQualifiedIdentifier.INTERMEDIATE_COMMAND_RESPONSEIDENTIFIER.getSectionCount()) {
                return false;
            }
            for (final Feature feat : featList) {
                if (feat.getIdentifier().equalsIgnoreCase(sections[2])) {
                    for (final Command cmd : feat.getCommand()) {
                        if (cmd.getIdentifier().equalsIgnoreCase(sections[5])) {
                            for (final SiLAElement interResp : cmd.getIntermediateResponse()) {
                                if (interResp.getIdentifier().equalsIgnoreCase(sections[7])) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        } else if (fqiType.equalsIgnoreCase(FullyQualifiedIdentifier.DEFINED_EXECUTION_ERROR_IDENTIFIER.toString())) {
            if (sections.length < FullyQualifiedIdentifier.DEFINED_EXECUTION_ERROR_IDENTIFIER.getSectionCount()) {
                return false;
            }
            for (final Feature feat : featList) {
                if (feat.getIdentifier().equalsIgnoreCase(sections[2])) {
                    for (final DefinedExecutionError err : feat.getDefinedExecutionError()) {
                        if (err.getIdentifier().equalsIgnoreCase(sections[5])) {
                            return true;
                        }
                    }
                }
            }
        } else if (fqiType.equalsIgnoreCase(FullyQualifiedIdentifier.PROPERTY_IDENTIFIER.toString())) {
            if (sections.length < FullyQualifiedIdentifier.PROPERTY_IDENTIFIER.getSectionCount()) {
                return false;
            }
            for (final Feature feat : featList) {
                if (feat.getIdentifier().equalsIgnoreCase(sections[2])) {
                    for (final Property prop : feat.getProperty()) {
                        if (prop.getIdentifier().equalsIgnoreCase(sections[5])) {
                            return true;
                        }
                    }
                }
            }
        } else if (fqiType.equalsIgnoreCase(FullyQualifiedIdentifier.TYPE_IDENTIFIER.toString())) {
            if (sections.length < FullyQualifiedIdentifier.TYPE_IDENTIFIER.getSectionCount()) {
                return false;
            }
            for (final Feature feat : featList) {
                if (feat.getIdentifier().equalsIgnoreCase(sections[2])) {
                    for (final SiLAElement dataTypeDef : feat.getDataTypeDefinition()) {
                        if (dataTypeDef.getIdentifier().equalsIgnoreCase(sections[5])) {
                            return true;
                        }
                    }
                }
            }
        } else if (fqiType.equalsIgnoreCase(FullyQualifiedIdentifier.METADATA_IDENTIFIER.toString())) {
            if (sections.length < FullyQualifiedIdentifier.METADATA_IDENTIFIER.getSectionCount()) {
                return false;
            }
            for (final Feature feat : featList) {
                if (feat.getIdentifier().equalsIgnoreCase(sections[2])) {
                    for (final Metadata meta : feat.getMetadata()) {
                        if (meta.getIdentifier().equalsIgnoreCase(sections[5])) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if the given XML input contains correct, well-formed XML syntax. To validate against a
     * schema, please see <code> isXmlValid()</code>.
     *
     * @param xml The XML input to check.
     * @return <code>true</code> if valid, otherwise <code>false</code>.
     *
     * @see #isXmlValid
     */
    public static boolean isXmlWellFormed(final InputStream xml) {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        try {
            final DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(null); // suppress error prints
            builder.parse(xml);
            return true;
        } catch (IOException | ParserConfigurationException | SAXException ex) {
            log.warn(ex.getMessage());
            return false;
        }
    }

    /**
     * Validates the given XML data against the provided XML Schema Definition (XSD). If just a
     * check on correct XML syntax is needed, please use <code>isXmlWellFormed()</code> instead.
     *
     * @deprecated This code is experimental and is most likely not conform with the intended
     * behavior described in the SiLA 2 standard (v1.0). However, it serves as a useful placeholder
     * and may be rewritten, extended or removed in the future. (2020-09-05,
     * florian.bauer.dev@gmail.com)
     *
     * @param xml The XML input to validate.
     * @param xsd The stream source of the XSD data to validate against.
     *
     * @return <code>true</code> if valid, otherwise <code>false</code>.
     *
     * @see https://www.edankert.com/validate.html
     * @see https://docs.oracle.com/javase/tutorial/jaxp/dom/validating.html
     * @see #isXmlWellFormed
     */
    @Deprecated
    public static boolean isXmlValid(final InputStream xml, final StreamSource xsd) {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        try {
            final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            final Schema schema = schemaFactory.newSchema(xsd);
            factory.setSchema(schema);
            final Validator validator = schema.newValidator();
            validator.validate(new StreamSource(xml));
            return true;
        } catch (IOException | SAXException ex) {
            log.warn(ex.getMessage());
            return false;
        }
    }

    /**
     * Checks if the given JSON string is valid.
     *
     * @deprecated A proper schema validation is not done yet. This may change in the future when
     * the standardization of JSON-schema is final. A library to do this can be found here:
     * https://github.com/networknt/json-schema-validator (2020-09-05, florian.bauer.dev@gmail.com)
     *
     * @param jsonStr The JSON string to validate.
     * @return <code>true</code> if valid, otherwise <code>false</code>.
     */
    @Deprecated
    public static boolean isJsonValid(final String jsonStr) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(jsonStr);
            return true;
        } catch (IOException ex) {
            log.warn(ex.getMessage());
            return false;
        }
    }
}
