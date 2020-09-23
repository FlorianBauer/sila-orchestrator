package de.fau.clients.orchestrator.nodes;

import com.fasterxml.jackson.databind.JsonNode;
import de.fau.clients.orchestrator.utils.DateTimeParser;
import de.fau.clients.orchestrator.utils.DocumentLengthFilter;
import de.fau.clients.orchestrator.utils.IconProvider;
import de.fau.clients.orchestrator.utils.ImagePanel;
import de.fau.clients.orchestrator.utils.LocalDateSpinnerEditor;
import de.fau.clients.orchestrator.utils.LocalTimeSpinnerEditor;
import de.fau.clients.orchestrator.utils.OffsetDateTimeSpinnerEditor;
import de.fau.clients.orchestrator.utils.ValidatorUtils;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.text.AbstractDocument;
import lombok.NonNull;
import sila_java.library.core.models.BasicType;
import sila_java.library.core.models.Constraints;

/**
 * A Factory for <code>ConstraintBasicNode</code>s.
 *
 * @see ConstraintBasicNode
 */
class ConstraintBasicNodeFactory {

    private static final URL IMAGE_MISSING = ConstraintBasicNode.class.getResource("/icons/document-missing-64px.png");

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
            final TypeDefLut typeDefs,
            @NonNull final BasicType type,
            @NonNull final Constraints constraints,
            final JsonNode jsonNode
    ) {
        final JComponent comp;
        final Supplier<String> supp;
        switch (type) {
            case ANY:
                // TODO: implement
                comp = new JLabel("placeholder 03");
                supp = () -> ("not implemented 03");
                break;
            case BINARY:
                return createConstrainedBinaryTypeFromJson(constraints, jsonNode);
            case BOOLEAN:
                return BasicNodeFactory.createBooleanTypeFromJson(jsonNode, true);
            case DATE:
                return createConstrainedDateTypeFromJson(constraints, jsonNode);
            case INTEGER:
                return createConstrainedIntegerTypeFromJson(constraints, jsonNode);
            case REAL:
                return createConstrainedRealTypeFromJson(constraints, jsonNode);
            case STRING:
                return createConstrainedStringTypeFromJson(constraints, jsonNode, typeDefs);
            case TIME:
                return createConstrainedTimeTypeFromJson(constraints, jsonNode);
            case TIMESTAMP:
                return createConstrainedTimestampeTypeFromJson(constraints, jsonNode);
            default:
                throw new IllegalArgumentException("Not a valid BasicType.");
        }
        return new ConstraintBasicNode(typeDefs, type, comp, supp, constraints);
    }

    protected static BasicNode createConstrainedBinaryTypeFromJson(
            @NonNull final Constraints constraints,
            final JsonNode jsonNode
    ) {
        final JComponent comp;
        final Supplier<String> supp;
        final Constraints.ContentType contentType = constraints.getContentType();
        if (contentType != null) {
            if (contentType.getType().equalsIgnoreCase("image")) {
                final String subtype = contentType.getSubtype();
                if (subtype.equalsIgnoreCase("jpeg")
                        || subtype.equalsIgnoreCase("png")
                        || subtype.equalsIgnoreCase("bmp")
                        || subtype.equalsIgnoreCase("gif")) {
                    BufferedImage img = null;
                    try {
                        if (jsonNode != null) {
                            img = ImageIO.read(new ByteArrayInputStream(jsonNode.binaryValue()));
                        } else {
                            img = ImageIO.read(IMAGE_MISSING);
                        }
                    } catch (IOException ex) {
                        System.err.println(ex.getMessage());
                    }

                    if (img != null) {
                        comp = new ImagePanel(img);
                        final BufferedImage tmpImg = img;
                        supp = () -> {
                            final ByteArrayOutputStream os = new ByteArrayOutputStream();
                            boolean hasWriter;
                            try {
                                hasWriter = ImageIO.write(tmpImg,
                                        subtype.toLowerCase(),
                                        Base64.getEncoder().wrap(os));
                                os.close();
                            } catch (IOException ex) {
                                return ex.getMessage();
                            }
                            if (hasWriter) {
                                return os.toString(StandardCharsets.UTF_8);
                            } else {
                                return "No ImageWriter found.";
                            }
                        };
                        return new ConstraintBasicNode(BasicType.BINARY, comp, supp, constraints);
                    }
                }
            }
        }
        comp = new JLabel("Unknown constrained binary type");
        supp = () -> ("not implemented 04");
        return new ConstraintBasicNode(BasicType.BINARY, comp, supp, constraints);
    }

    protected static BasicNode createConstrainedDateTypeFromJson(
            @NonNull final Constraints constraints,
            final JsonNode jsonNode
    ) {
        final JComponent comp;
        final Supplier<String> supp;
        if (constraints.getSet() != null) {
            final List<String> dateSet = constraints.getSet().getValue();
            final LocalDate[] dates = new LocalDate[dateSet.size()];
            for (int i = 0; i < dateSet.size(); i++) {
                dates[i] = DateTimeParser.parseIsoDate(dateSet.get(i));
            }
            final JComboBox<LocalDate> dateComboBox = new JComboBox<>(dates);
            dateComboBox.setMaximumSize(MaxDim.DATE_TIME_SPINNER.getDim());
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

    protected static BasicNode createConstrainedIntegerTypeFromJson(
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
            supp = () -> (Integer.valueOf(numberComboBox.getSelectedItem().toString()).toString());
            comp = numberComboBox;
        } else {
            final SpinnerModel model = ConstraintSpinnerModelFactory.createRangeConstrainedIntModel(constraints);
            final JSpinner numericSpinner = new JSpinner(model);
            numericSpinner.setMaximumSize(MaxDim.NUMERIC_SPINNER.getDim());
            if (jsonNode != null) {
                numericSpinner.setValue(jsonNode.asInt());
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
        return new ConstraintBasicNode(BasicType.INTEGER, comp, supp, constraints);
    }

    protected static BasicNode createConstrainedRealTypeFromJson(
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
            final SpinnerModel model = ConstraintSpinnerModelFactory.createRangeConstrainedRealModel(constraints);
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

    protected static BasicNode createConstrainedStringTypeFromJson(
            @NonNull final Constraints constraints,
            final JsonNode jsonNode,
            final TypeDefLut typeDefs
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
            final JFormattedTextField strField = new JFormattedTextField();
            strField.setMaximumSize(MaxDim.TEXT_FIELD.getDim());
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
                        validator = () -> (ValidatorUtils.isXmlWellFormed(new ByteArrayInputStream(
                                strField.getText().getBytes(StandardCharsets.UTF_8))) // 
                                // FIXME: A proper validation against the schema is not done
                                //        yet due to the poor support and the optional 
                                //        character of this feature. To enable validation,
                                //        simply uncomment the code blocks below. 
                                //        (2020-09-06, florian.bauer.dev@gmail.com)
                                /* && ValidatorUtils.isXmlValid(new ByteArrayInputStream(
                                                   strField.getText().getBytes(StandardCharsets.UTF_8)),
                                                   new StreamSource(schema.getUrl())) */);
                    } else if (schema.getInline() != null) {
                        validator = () -> (ValidatorUtils.isXmlWellFormed(new ByteArrayInputStream(
                                strField.getText().getBytes(StandardCharsets.UTF_8))) /*
                                        && ValidatorUtils.isXmlValid(new ByteArrayInputStream(
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
                    validator = () -> (ValidatorUtils.isJsonValid(strField.getText()));
                    conditionDesc = "Json";
                } else {
                    validator = () -> (false);
                    conditionDesc = INVALID_CONSTRAINT;
                }
            } else if (constraints.getFullyQualifiedIdentifier() != null) {
                final String fqiType = constraints.getFullyQualifiedIdentifier();
                validator = () -> (ValidatorUtils.isFullyQualifiedIdentifierValid(
                        fqiType,
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

    protected static BasicNode createConstrainedTimeTypeFromJson(
            @NonNull final Constraints constraints,
            final JsonNode jsonNode
    ) {
        final JComponent comp;
        final Supplier<String> supp;
        if (constraints.getSet() != null) {
            final List<String> timeSet = constraints.getSet().getValue();
            final OffsetTime[] times = new OffsetTime[timeSet.size()];
            for (int i = 0; i < timeSet.size(); i++) {
                times[i] = DateTimeParser.parseIsoTime(timeSet.get(i));
            }
            final JComboBox<OffsetTime> timeComboBox = new JComboBox<>(times);
            timeComboBox.setMaximumSize(MaxDim.DATE_TIME_SPINNER.getDim());
            supp = () -> {
                return ((OffsetTime) timeComboBox.getSelectedItem()).toString();
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
            LocalTime initTime = LocalTime.now().truncatedTo(ChronoUnit.SECONDS);
            if (jsonNode != null) {
                try {
                    initTime = LocalTime.parse(jsonNode.asText());
                } catch (Exception ex) {
                    // do nothing and use the current time instead
                }
            }
            timeSpinner.setModel(ConstraintSpinnerModelFactory.createRangeConstrainedTimeModel(
                    initTime,
                    constraints));
            timeSpinner.setMaximumSize(MaxDim.DATE_TIME_SPINNER.getDim());
            timeSpinner.setEditor(new LocalTimeSpinnerEditor(timeSpinner));

            supp = () -> {
                return timeSpinner.getValue().toString();
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

    protected static BasicNode createConstrainedTimestampeTypeFromJson(
            @NonNull final Constraints constraints,
            final JsonNode jsonNode
    ) {
        final JComponent comp;
        final Supplier<String> supp;
        if (constraints.getSet() != null) {
            final List<String> timeSet = constraints.getSet().getValue();
            final OffsetDateTime[] times = new OffsetDateTime[timeSet.size()];
            for (int i = 0; i < timeSet.size(); i++) {
                times[i] = DateTimeParser.parseIsoDateTime(timeSet.get(i));
            }
            final JComboBox<OffsetDateTime> timestampComboBox = new JComboBox<>(times);
            timestampComboBox.setMaximumSize(MaxDim.TIMESTAMP_SPINNER.getDim());
            supp = () -> {
                return timestampComboBox.getSelectedItem().toString();
            };
            comp = timestampComboBox;
        } else {
            String minBounds = null;
            if (constraints.getMinimalExclusive() != null) {
                minBounds = GREATER_THAN + DateTimeParser.parseIsoDateTime(
                        constraints.getMinimalExclusive())
                        .withOffsetSameInstant(DateTimeParser.LOCAL_OFFSET).toString();
            } else if (constraints.getMinimalInclusive() != null) {
                minBounds = GREATER_OR_EQUAL + DateTimeParser.parseIsoDateTime(
                        constraints.getMinimalInclusive())
                        .withOffsetSameInstant(DateTimeParser.LOCAL_OFFSET).toString();
            }

            String maxBounds = null;
            if (constraints.getMaximalExclusive() != null) {
                maxBounds = LESS_THAN + DateTimeParser.parseIsoDateTime(
                        constraints.getMaximalExclusive())
                        .withOffsetSameInstant(DateTimeParser.LOCAL_OFFSET).toString();
            } else if (constraints.getMaximalInclusive() != null) {
                maxBounds = LESS_OR_EQUAL + DateTimeParser.parseIsoDateTime(
                        constraints.getMaximalInclusive())
                        .withOffsetSameInstant(DateTimeParser.LOCAL_OFFSET).toString();
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

            final JSpinner timestampSpinner = new JSpinner();
            OffsetDateTime initDateTime = OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS);
            if (jsonNode != null) {
                try {
                    initDateTime = DateTimeParser.parseIsoDateTime(jsonNode.asText());
                } catch (Exception ex) {
                    // do nothing and use the current time instead
                }
            }
            timestampSpinner.setModel(ConstraintSpinnerModelFactory
                    .createRangeConstrainedDateTimeModel(
                            initDateTime,
                            constraints));
            timestampSpinner.setMaximumSize(MaxDim.TIMESTAMP_SPINNER.getDim());
            timestampSpinner.setEditor(new OffsetDateTimeSpinnerEditor(timestampSpinner));
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
}
