package de.fau.clients.orchestrator.feature_explorer;

import com.fasterxml.jackson.databind.JsonNode;
import java.awt.Dimension;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import lombok.NonNull;
import sila_java.library.core.models.BasicType;
import sila_java.library.core.models.Constraints;

final class BasicNode implements SilaNode {

    private static final int MAX_HEIGHT = 42;
    private static final Dimension MAX_SIZE_TEXT_FIELD = new Dimension(4096, MAX_HEIGHT);
    private static final Dimension MAX_SIZE_NUMERIC_SPINNER = new Dimension(160, MAX_HEIGHT);
    private static final Dimension MAX_SIZE_DATE_TIME_SPINNER = new Dimension(160, MAX_HEIGHT);
    private static final Dimension MAX_SIZE_TIMESTAMP_SPINNER = new Dimension(200, MAX_HEIGHT);
    /**
     * The precision of the offset-limit of an exclusive float range (e.g. the exclusive upper-limit
     * of the value <code>1.0</code> could be <code>0.9</code>, <code>0.99</code>,
     * <code>0.999</code>, etc.)
     */
    private static final double REAL_EXCLUSIVE_OFFSET = 0.001;
    private static final double REAL_STEP_SIZE = 0.1;
    private static final ZoneOffset LOCAL_OFFSET = ZoneId.systemDefault().getRules().getOffset(Instant.now());
    private static final int LOCAL_OFFSET_IN_SEC = LOCAL_OFFSET.getTotalSeconds();
    /**
     * The date-format used by the GUI-components.
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    /**
     * The time-format used by the GUI-components.
     */
    private static final String TIME_FORMAT = "HH:mm:ss";
    private static final String DATE_TIME_FORMAT = DATE_FORMAT + " " + TIME_FORMAT;
    private final BasicType type;
    private Constraints constraints = null;
    private Supplier<String> valueSupplier;
    private JComponent component;

    private BasicNode(@NonNull final BasicType type) {
        this.type = type;
    }

    protected static BasicNode create(final BasicType type) {
        BasicNode node = new BasicNode(type);
        switch (node.type) {
            case ANY:
                // TODO: implement
                node.component = new JLabel("placeholder 01");
                node.valueSupplier = () -> ("not implemented 01");
                break;
            case BINARY:
                // TODO: implement
                node.component = new JLabel("placeholder 02");
                node.valueSupplier = () -> ("not implemented 02");
                break;
            case BOOLEAN:
                createBooleanType(node);
                break;
            case DATE:
                JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
                dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, DATE_FORMAT));
                dateSpinner.setMaximumSize(MAX_SIZE_DATE_TIME_SPINNER);
                node.valueSupplier = () -> {
                    Date date = (Date) dateSpinner.getValue();
                    return LocalDate.ofInstant(date.toInstant(), LOCAL_OFFSET).toString();
                };
                node.component = dateSpinner;
                break;
            case INTEGER:
            case REAL:
                SpinnerModel model = (node.type == BasicType.INTEGER)
                        ? new SpinnerNumberModel()
                        : new SpinnerNumberModel(0.0, null, null, REAL_STEP_SIZE);
                JSpinner numericSpinner = new JSpinner(model);
                numericSpinner.setMaximumSize(MAX_SIZE_NUMERIC_SPINNER);
                node.valueSupplier = () -> (numericSpinner.getValue().toString());
                node.component = numericSpinner;
                break;
            case STRING:
                JTextField strField = new JTextField();
                strField.setMaximumSize(MAX_SIZE_TEXT_FIELD);
                node.valueSupplier = () -> (strField.getText());
                node.component = strField;
                break;
            case TIME:
                JSpinner timeSpinner = new JSpinner(new SpinnerDateModel());
                timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, TIME_FORMAT));
                timeSpinner.setMaximumSize(MAX_SIZE_DATE_TIME_SPINNER);
                node.valueSupplier = () -> {
                    Date time = (Date) timeSpinner.getValue();
                    return OffsetTime.ofInstant(time.toInstant(), LOCAL_OFFSET)
                            .withOffsetSameInstant(ZoneOffset.UTC)
                            .toString();
                };
                node.component = timeSpinner;
                break;
            case TIMESTAMP:
                JSpinner timeStampSpinner = new JSpinner(new SpinnerDateModel());
                timeStampSpinner.setEditor(new JSpinner.DateEditor(timeStampSpinner, DATE_TIME_FORMAT));
                timeStampSpinner.setMaximumSize(MAX_SIZE_TIMESTAMP_SPINNER);
                node.valueSupplier = () -> {
                    Date time = (Date) timeStampSpinner.getValue();
                    return OffsetDateTime.ofInstant(time.toInstant(), ZoneOffset.UTC).toString();
                };
                node.component = timeStampSpinner;
                break;
            default:
                throw new IllegalArgumentException("Not a valid BasicType.");
        }
        return node;
    }

    protected static BasicNode createWithConstraint(
            final BasicType type,
            final Constraints constraints) {

        BasicNode node = new BasicNode(type);
        node.constraints = constraints;
        switch (node.type) {
            case ANY:
                // TODO: implement
                node.component = new JLabel("placeholder 03");
                node.valueSupplier = () -> ("not implemented 03");
                break;
            case BINARY:
                // TODO: implement
                node.component = new JLabel("placeholder 04");
                node.valueSupplier = () -> ("not implemented 04");
                break;
            case BOOLEAN:
                createBooleanType(node);
                break;
            case DATE:
                final JSpinner dateSpinner;
                if (node.constraints.getSet() != null) {
                    final List<String> dateSet = node.constraints.getSet().getValue();
                    ArrayList<LocalDate> dates = new ArrayList<>(dateSet.size());
                    for (final String element : dateSet) {
                        dates.add(parseIsoDate(element));
                    }
                    dateSpinner = new JSpinner(new SpinnerListModel(dates));
                    node.valueSupplier = () -> {
                        return ((LocalDate) dateSpinner.getValue()).toString();
                    };
                } else {
                    dateSpinner = new JSpinner(createRangeConstrainedDateModel(node.constraints));
                    dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, DATE_FORMAT));
                    node.valueSupplier = () -> {
                        Date date = (Date) dateSpinner.getValue();
                        return LocalDate.ofInstant(date.toInstant(), LOCAL_OFFSET).toString();
                    };
                }
                dateSpinner.setMaximumSize(MAX_SIZE_DATE_TIME_SPINNER);
                node.component = dateSpinner;
                break;
            case INTEGER:
            case REAL:
                final SpinnerModel model;
                if (node.constraints.getSet() != null) {
                    model = new SpinnerListModel(node.constraints.getSet().getValue());
                } else {
                    model = (type == BasicType.INTEGER)
                            ? createRangeConstrainedIntModel(node.constraints)
                            : createRangeConstrainedRealModel(node.constraints);
                }
                JSpinner numericSpinner = new JSpinner(model);
                numericSpinner.setMaximumSize(MAX_SIZE_NUMERIC_SPINNER);
                node.valueSupplier = () -> (numericSpinner.getValue().toString());
                if (node.constraints.getUnit() != null) {
                    Box hbox = Box.createHorizontalBox();
                    hbox.add(numericSpinner);
                    hbox.add(Box.createHorizontalStrut(5));
                    hbox.add(new JLabel(node.constraints.getUnit().getLabel()));
                    hbox.setMaximumSize(MAX_SIZE_TEXT_FIELD);
                    node.component = hbox;
                } else {
                    node.component = numericSpinner;
                }
                break;
            case STRING:
                JTextField strField = new JTextField();
                strField.setMaximumSize(MAX_SIZE_TEXT_FIELD);
                node.valueSupplier = () -> (strField.getText());
                node.component = strField;
                break;
            case TIME:
                final JSpinner timeSpinner;
                if (node.constraints.getSet() != null) {
                    final List<String> timeSet = node.constraints.getSet().getValue();
                    ArrayList<OffsetTime> times = new ArrayList<>(timeSet.size());
                    for (final String element : timeSet) {
                        times.add(parseIsoTime(element));
                    }
                    timeSpinner = new JSpinner(new SpinnerListModel(times));
                    node.valueSupplier = () -> {
                        return ((OffsetTime) timeSpinner.getValue())
                                .withOffsetSameInstant(ZoneOffset.UTC)
                                .toString();
                    };
                } else {
                    timeSpinner = new JSpinner(
                            // FIXME: The out-commented function below seems to return a valid 
                            // spinner-model, but the spinner itself does not work correctly. Therefore 
                            // a unrestrained default-spinner-model is used as a temporary hack.
                            // createRangeConstrainedTimeModel(constraints)
                            new SpinnerDateModel()
                    );
                    timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, TIME_FORMAT));
                    node.valueSupplier = () -> {
                        Date time = (Date) timeSpinner.getValue();
                        return OffsetTime.ofInstant(time.toInstant(), LOCAL_OFFSET)
                                .withOffsetSameInstant(ZoneOffset.UTC)
                                .toString();
                    };
                }
                timeSpinner.setMaximumSize(MAX_SIZE_DATE_TIME_SPINNER);
                node.component = timeSpinner;
                break;
            case TIMESTAMP:
                final JSpinner timeStampSpinner;
                if (node.constraints.getSet() != null) {
                    final List<String> timeSet = node.constraints.getSet().getValue();
                    ArrayList<OffsetDateTime> times = new ArrayList<>(timeSet.size());
                    for (final String element : timeSet) {
                        times.add(parseIsoDateTime(element));
                    }
                    timeStampSpinner = new JSpinner(new SpinnerListModel(times));
                    node.valueSupplier = () -> {
                        return ((OffsetDateTime) timeStampSpinner.getValue()).toString();
                    };
                } else {
                    timeStampSpinner = new JSpinner(
                            // TODO: implement createRangeConstrainedTimestampModel(constraints)
                            new SpinnerDateModel()
                    );
                    timeStampSpinner.setEditor(new JSpinner.DateEditor(timeStampSpinner, DATE_TIME_FORMAT));
                    node.valueSupplier = () -> {
                        Date time = (Date) timeStampSpinner.getValue();
                        return OffsetDateTime.ofInstant(time.toInstant(), ZoneOffset.UTC).toString();
                    };
                }
                timeStampSpinner.setMaximumSize(MAX_SIZE_TIMESTAMP_SPINNER);
                node.component = timeStampSpinner;
                break;
            default:
                throw new IllegalArgumentException("Not a valid BasicType.");
        }
        return node;
    }

    protected static BasicNode createFromJson(
            final BasicType type,
            final JsonNode jsonNode,
            boolean isReadOnly) {

        BasicNode node = new BasicNode(type);
        switch (node.type) {
            case ANY:
                // TODO: implement
                node.component = new JLabel("placeholder 05");
                node.valueSupplier = () -> ("not implemented 05");
                break;
            case BINARY:
                // TODO: implement
                node.component = new JLabel("placeholder 06");
                node.valueSupplier = () -> ("not implemented 06");
                break;
            case BOOLEAN:
                createBooleanType(node, jsonNode, isReadOnly);
                break;
            case DATE:
                // TODO: implement
                node.component = new JLabel("placeholder 07");
                node.valueSupplier = () -> ("not implemented 07");
                break;
            case INTEGER:
            case REAL:
                createNumberType(node, jsonNode, isReadOnly);
                break;
            case STRING:
                createStringType(node, jsonNode, isReadOnly);
                break;
            case TIME:
                // TODO: implement
                node.component = new JLabel("placeholder 08");
                node.valueSupplier = () -> ("not implemented 08");
                break;
            case TIMESTAMP:
                // TODO: implement
                node.component = new JLabel("placeholder 09");
                node.valueSupplier = () -> ("not implemented 09");
                break;
            default:
                throw new IllegalArgumentException("Not a valid BasicType.");
        }
        return node;
    }

    @Override
    public BasicNode cloneNode() {
        if (this.constraints == null) {
            return create(this.type);
        } else {
            return createWithConstraint(this.type, this.constraints);
        }
    }

    @Override
    public String toJsonString() {
        final String valueStr = valueSupplier.get();
        return "{\"value\":\"" + valueStr + "\"}";
    }

    @Override
    public JComponent getComponent() {
        return this.component;
    }

    @Override
    public String toString() {
        return "(" + this.type + ", "
                + this.constraints + ", "
                + this.valueSupplier.getClass() + ", "
                + this.component.getClass() + ")";
    }

    private static void createBooleanType(BasicNode node) {
        JCheckBox checkBox = new JCheckBox();
        node.valueSupplier = () -> (checkBox.isSelected() ? "true" : "false");
        node.component = checkBox;
    }

    private static void createBooleanType(BasicNode node, JsonNode jsonNode, boolean isReadOnly) {
        JCheckBox checkBox = new JCheckBox();
        if (jsonNode != null) {
            checkBox.setSelected(jsonNode.asBoolean());
        }
        if (isReadOnly) {
            checkBox.setEnabled(false);
        } else {
            node.valueSupplier = () -> (checkBox.isSelected() ? "true" : "false");
        }
        node.component = checkBox;
    }

    private static void createStringType(BasicNode node, JsonNode jsonNode, boolean isReadOnly) {
        JTextField strField = new JTextField();
        strField.setMaximumSize(MAX_SIZE_TEXT_FIELD);
        if (jsonNode != null) {
            String textValue = jsonNode.asText();
            strField.setText(textValue);
        }

        if (isReadOnly) {
            strField.setEnabled(false);
        } else {
            node.valueSupplier = () -> (jsonNode.textValue());
        }
        node.component = strField;
    }

    private static void createNumberType(BasicNode node, JsonNode jsonNode, boolean isReadOnly) {
        if (!isReadOnly) {
            SpinnerModel model = (node.type == BasicType.INTEGER)
                    ? new SpinnerNumberModel()
                    : new SpinnerNumberModel(0.0, null, null, REAL_STEP_SIZE);
            JSpinner numericSpinner = new JSpinner(model);
            numericSpinner.setMaximumSize(MAX_SIZE_NUMERIC_SPINNER);
            if (jsonNode != null) {
                model.setValue(jsonNode.asDouble());
            }
            node.valueSupplier = () -> (numericSpinner.getValue().toString());
            node.component = numericSpinner;
        } else {
            JTextField strField = new JTextField();
            strField.setMaximumSize(MAX_SIZE_TEXT_FIELD);
            strField.setText(jsonNode.asText());
            strField.setEnabled(false);
            node.component = strField;
        }
    }

    /**
     * Creates a Integer based, range-limited model for constraining input in
     * <code>JSpinner</code>-components. This functions does not consider any
     * <code>Set</code>-constraints. Only the following functions describing the range-limits are
     * considered:
     * <ul>
     * <li><code>getMinimalExclusive()</code></li>
     * <li><code>getMinimalInclusive()</code></li>
     * <li><code>getMaximalExclusive()</code></li>
     * <li><code>getMaximalInclusive()</code></li>
     * </ul>
     *
     * @param constraints The SiLA-Constraints element defining the value limits.
     * @return The spinner-model for a <code>JSpinner</code>-component.
     */
    private static SpinnerModel createRangeConstrainedIntModel(final Constraints constraints) {
        int initVal = 0;
        Integer min = null;
        Integer max = null;
        String conStr = constraints.getMinimalExclusive();
        if (conStr != null) {
            min = Integer.parseInt(conStr) + 1;
            initVal = Math.max(min, initVal);
        }
        conStr = constraints.getMinimalInclusive();
        if (conStr != null) {
            min = Integer.parseInt(conStr);
            initVal = Math.max(min, initVal);
        }

        conStr = constraints.getMaximalExclusive();
        if (conStr != null) {
            max = Integer.parseInt(conStr) - 1;
        }
        conStr = constraints.getMaximalInclusive();
        if (conStr != null) {
            max = Integer.parseInt(conStr);
        }
        initVal = (max != null && max < initVal) ? max : initVal;
        return new SpinnerNumberModel((Number) initVal, min, max, 1);
    }

    /**
     * Creates a Double based, range-limited model to constrain input in
     * <code>JSpinner</code>-components. This functions does not consider any
     * <code>Set</code>-constraints. Only the following functions describing the range-limits are
     * considered:
     * <ul>
     * <li><code>getMinimalExclusive()</code></li>
     * <li><code>getMinimalInclusive()</code></li>
     * <li><code>getMaximalExclusive()</code></li>
     * <li><code>getMaximalInclusive()</code></li>
     * </ul>
     *
     * @param constraints The SiLA-Constraints element defining the value limits.
     * @return The spinner-model for a <code>JSpinner</code>-component.
     */
    private static SpinnerModel createRangeConstrainedRealModel(final Constraints constraints) {
        double initVal = 0.0;
        Double min = null;
        Double max = null;
        if (constraints.getMinimalExclusive() != null) {
            min = Double.parseDouble(constraints.getMinimalExclusive()) + REAL_EXCLUSIVE_OFFSET;
            initVal = Math.max(min, initVal);
        } else if (constraints.getMinimalInclusive() != null) {
            min = Double.parseDouble(constraints.getMinimalInclusive());
            initVal = Math.max(min, initVal);
        }

        if (constraints.getMaximalExclusive() != null) {
            max = Double.parseDouble(constraints.getMaximalExclusive()) - REAL_EXCLUSIVE_OFFSET;
        } else if (constraints.getMaximalInclusive() != null) {
            max = Double.parseDouble(constraints.getMaximalInclusive());
        }
        initVal = (max != null && max < initVal) ? max : initVal;
        return new SpinnerNumberModel((Number) initVal, min, max, REAL_STEP_SIZE);
    }

    /**
     * Creates a Date based, range-limited model to constrain input in
     * <code>JSpinner</code>-components. This functions does not consider any
     * <code>Set</code>-constraints.
     *
     * @param constraints The SiLA-Constraints element defining the date limits.
     * @return The spinner-model for a <code>JSpinner</code>-component.
     */
    private static SpinnerDateModel createRangeConstrainedDateModel(final Constraints constraints) {
        LocalDate init = LocalDate.now();
        LocalDate start = null;
        if (constraints.getMinimalExclusive() != null) {
            start = parseIsoDate(constraints.getMinimalExclusive()).plusDays(1);

        } else if (constraints.getMinimalInclusive() != null) {
            start = parseIsoDate(constraints.getMinimalInclusive());
        }

        Date startDate = null;
        if (start != null) {
            if (start.isAfter(init)) {
                init = start;
            }
            startDate = Date.from(start.atStartOfDay(LOCAL_OFFSET).toInstant());
        }

        LocalDate end = null;
        if (constraints.getMaximalExclusive() != null) {
            end = parseIsoDate(constraints.getMaximalExclusive()).minusDays(1);
        } else if (constraints.getMaximalInclusive() != null) {
            end = parseIsoDate(constraints.getMaximalInclusive());
        }

        Date endDate = null;
        if (end != null) {
            if (end.isBefore(init)) {
                init = end;
            }
            endDate = Date.from(end.atStartOfDay(LOCAL_OFFSET).toInstant());
        }

        Date initDate = Date.from(init.atStartOfDay(LOCAL_OFFSET).toInstant());
        return new SpinnerDateModel(initDate, startDate, endDate, Calendar.DAY_OF_MONTH);
    }

    /**
     * FIXME: The Swing spinner is buggy. The underlying model seems to be correct, however the
     * component does not accept the range-limit.
     *
     * @param constraints
     * @return
     * @hidden bug
     */
    private static SpinnerDateModel createRangeConstrainedTimeModel(final Constraints constraints) {
        OffsetDateTime init = OffsetDateTime.now();
        OffsetDateTime start = null;
        if (constraints.getMinimalExclusive() != null) {
            start = parseIsoTime(constraints.getMinimalExclusive())
                    .atDate(init.toLocalDate())
                    .plusSeconds(1);
        } else if (constraints.getMinimalInclusive() != null) {
            start = parseIsoTime(constraints.getMinimalInclusive())
                    .atDate(init.toLocalDate());
        }

        Date startTime = null;
        if (start != null) {
            if (start.compareTo(init) >= 0) {
                init = start.plusSeconds(1);
            }
            startTime = Date.from(start.toInstant());
        }

        OffsetDateTime end = null;
        if (constraints.getMaximalExclusive() != null) {
            end = parseIsoTime(constraints.getMaximalExclusive())
                    .atDate(init.toLocalDate())
                    .minusSeconds(1);
        } else if (constraints.getMaximalInclusive() != null) {
            end = parseIsoTime(constraints.getMaximalInclusive())
                    .atDate(init.toLocalDate());
        }

        Date endTime = null;
        if (end != null) {
            if (end.compareTo(init) <= 0) {
                init = end.minusSeconds(1);
            }
            endTime = Date.from(end.toInstant());
        }

        Date initDate = Date.from(init.toInstant());
        return new SpinnerDateModel(initDate, startTime, endTime, Calendar.MINUTE);
    }

    /**
     * Parses ISO-8601 date-Strings of the form <code>yyyy-MM-dd</code>. Additional
     * time-zone-offsets are going to be ignored.
     *
     * @param isoDateStr a ISO-8601 conform date-String.
     * @return A LocalDate-date or <code>null</code> on error.
     */
    private static LocalDate parseIsoDate(String isoDateStr) {
        final List<DateTimeFormatter> formatters = Arrays.asList(
                DateTimeFormatter.ISO_DATE,
                DateTimeFormatter.ofPattern("uuuu-MM-ddX"),
                new DateTimeFormatterBuilder()
                        .appendPattern("uuuuMMdd[X]")
                        .toFormatter()
        );

        for (final DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(isoDateStr, formatter);
            } catch (DateTimeParseException ex) {
            }
        }
        return null;
    }

    /**
     * Parses ISO-8601 time-Strings of the form <code>HH:mm:ss</code>.
     *
     * @param isoDateTimeStr A ISO-8601 conform time-String.
     * @return A OffsetTime-timestamp adjusted to the current systems time-offset or
     * <code>null</code> on error.
     */
    private static OffsetTime parseIsoTime(String isoTimeStr) {
        final List<DateTimeFormatter> formatters = Arrays.asList(
                DateTimeFormatter.ISO_OFFSET_TIME,
                new DateTimeFormatterBuilder()
                        .appendPattern("HH:mm:ss[.SSS][X]")
                        .parseDefaulting(ChronoField.OFFSET_SECONDS, LOCAL_OFFSET_IN_SEC)
                        .toFormatter(),
                new DateTimeFormatterBuilder()
                        .appendPattern("HHmmss[.SSS][X]")
                        .parseDefaulting(ChronoField.OFFSET_SECONDS, LOCAL_OFFSET_IN_SEC)
                        .toFormatter()
        );

        for (final DateTimeFormatter formatter : formatters) {
            try {
                return OffsetTime.parse(isoTimeStr, formatter).withOffsetSameInstant(LOCAL_OFFSET);
            } catch (DateTimeParseException ex) {
            }
        }
        return null;
    }

    /**
     * Parses ISO-8601 timestamps of the form <code>yyyy-MM-ddTHH:mm:ss</code>.
     *
     * @param isoDateTimeStr A ISO-8601 conform date-time-String.
     * @return A OffsetDateTime-timestamp adjusted to UTC or <code>null</code> on error.
     */
    private static OffsetDateTime parseIsoDateTime(String isoDateTimeStr) {
        final List<DateTimeFormatter> formatters = Arrays.asList(
                DateTimeFormatter.ISO_OFFSET_DATE_TIME,
                new DateTimeFormatterBuilder()
                        .appendPattern("uuuu-MM-dd'T'HH:mm:ss[.SSS][X]")
                        .parseDefaulting(ChronoField.OFFSET_SECONDS, LOCAL_OFFSET_IN_SEC)
                        .toFormatter(),
                new DateTimeFormatterBuilder()
                        .appendPattern("uuuuMMdd'T'HHmmss[.SSS][X]")
                        .parseDefaulting(ChronoField.OFFSET_SECONDS, LOCAL_OFFSET_IN_SEC)
                        .toFormatter()
        );

        for (final DateTimeFormatter fmt : formatters) {
            try {
                return OffsetDateTime.parse(isoDateTimeStr, fmt).withOffsetSameInstant(ZoneOffset.UTC);
            } catch (DateTimeParseException ex) {
            }
        }
        return null;
    }
}
