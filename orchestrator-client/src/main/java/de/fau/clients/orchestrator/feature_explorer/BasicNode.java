package de.fau.clients.orchestrator.feature_explorer;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.Box;
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

/**
 * A <code>SilaNode</code> implementation representing SiLA Basic Types and its corresponding
 * GUI-Components. See also {@link BasicNodeFactory}.
 */
final class BasicNode implements SilaNode {

    /**
     * The precision of the offset-limit of an exclusive float range (e.g. the exclusive upper-limit
     * of the value <code>1.0</code> could be <code>0.9</code>, <code>0.99</code>,
     * <code>0.999</code>, etc.)
     */
    private static final double REAL_EXCLUSIVE_OFFSET = 0.001;
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
    private final BasicType type;
    private final JComponent component;
    private final Supplier<String> valueSupplier;
    private final Constraints constraints;

    protected BasicNode(
            @NonNull final BasicType type,
            final JComponent component,
            final Supplier<String> valueSupplier) {
        this.type = type;
        this.component = component;
        this.valueSupplier = valueSupplier;
        this.constraints = null;
    }

    protected BasicNode(
            @NonNull final BasicType type,
            final JComponent component,
            final Supplier<String> valueSupplier,
            final Constraints constraints) {
        this.type = type;
        this.component = component;
        this.valueSupplier = valueSupplier;
        this.constraints = constraints;
    }

    protected static BasicNode create(final BasicType type) {
        switch (type) {
            case ANY:
                // TODO: implement
                return new BasicNode(type, new JLabel("placeholder 01"), () -> ("not implemented 01"));
            case BINARY:
                // TODO: implement
                return new BasicNode(type, new JLabel("placeholder 02"), () -> ("not implemented 02"));
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
                throw new IllegalArgumentException("Not a valid BasicType.");
        }
    }

    protected static BasicNode createWithConstraint(
            final BasicType type,
            final Constraints constraints) {
        final JComponent comp;
        final Supplier<String> supp;
        switch (type) {
            case ANY:
                // TODO: implement
                comp = new JLabel("placeholder 03");
                supp = () -> ("not implemented 03");
                break;
            case BINARY:
                // TODO: implement
                comp = new JLabel("placeholder 04");
                supp = () -> ("not implemented 04");
                break;
            case BOOLEAN:
                return BasicNodeFactory.createBooleanTypeFromJson(null, true);
            case DATE:
                final JSpinner dateSpinner;
                if (constraints.getSet() != null) {
                    final List<String> dateSet = constraints.getSet().getValue();
                    ArrayList<LocalDate> dates = new ArrayList<>(dateSet.size());
                    for (final String element : dateSet) {
                        dates.add(DateTimeUtils.parseIsoDate(element));
                    }
                    dateSpinner = new JSpinner(new SpinnerListModel(dates));
                    supp = () -> {
                        return ((LocalDate) dateSpinner.getValue()).toString();
                    };
                } else {
                    dateSpinner = new JSpinner(createRangeConstrainedDateModel(constraints));
                    dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, DATE_FORMAT));
                    supp = () -> {
                        Date date = (Date) dateSpinner.getValue();
                        return LocalDate.ofInstant(date.toInstant(), DateTimeUtils.LOCAL_OFFSET).toString();
                    };
                }
                dateSpinner.setMaximumSize(BasicNodeFactory.MAX_SIZE_DATE_TIME_SPINNER);
                comp = dateSpinner;
                break;
            case INTEGER:
            case REAL:
                final SpinnerModel model;
                if (constraints.getSet() != null) {
                    model = new SpinnerListModel(constraints.getSet().getValue());
                } else {
                    model = (type == BasicType.INTEGER)
                            ? createRangeConstrainedIntModel(constraints)
                            : createRangeConstrainedRealModel(constraints);
                }
                final JSpinner numericSpinner = new JSpinner(model);
                numericSpinner.setMaximumSize(BasicNodeFactory.MAX_SIZE_NUMERIC_SPINNER);
                if (constraints.getUnit() != null) {
                    Box hbox = Box.createHorizontalBox();
                    hbox.add(numericSpinner);
                    hbox.add(Box.createHorizontalStrut(5));
                    hbox.add(new JLabel(constraints.getUnit().getLabel()));
                    hbox.setMaximumSize(BasicNodeFactory.MAX_SIZE_TEXT_FIELD);
                    comp = hbox;
                } else {
                    comp = numericSpinner;
                }
                supp = () -> (numericSpinner.getValue().toString());
                break;
            case STRING:
                // TODO: implement string pattern
                final JTextField strField = new JTextField();
                strField.setMaximumSize(BasicNodeFactory.MAX_SIZE_TEXT_FIELD);
                comp = strField;
                supp = () -> (strField.getText());
                break;
            case TIME:
                final JSpinner timeSpinner;
                if (constraints.getSet() != null) {
                    final List<String> timeSet = constraints.getSet().getValue();
                    ArrayList<OffsetTime> times = new ArrayList<>(timeSet.size());
                    for (final String element : timeSet) {
                        times.add(DateTimeUtils.parseIsoTime(element));
                    }
                    timeSpinner = new JSpinner(new SpinnerListModel(times));
                    supp = () -> {
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
                    supp = () -> {
                        Date time = (Date) timeSpinner.getValue();
                        return OffsetTime.ofInstant(time.toInstant(), DateTimeUtils.LOCAL_OFFSET)
                                .withOffsetSameInstant(ZoneOffset.UTC)
                                .toString();
                    };
                }
                timeSpinner.setMaximumSize(BasicNodeFactory.MAX_SIZE_DATE_TIME_SPINNER);
                comp = timeSpinner;
                break;
            case TIMESTAMP:
                final JSpinner timeStampSpinner;
                if (constraints.getSet() != null) {
                    final List<String> timeSet = constraints.getSet().getValue();
                    ArrayList<OffsetDateTime> times = new ArrayList<>(timeSet.size());
                    for (final String element : timeSet) {
                        times.add(DateTimeUtils.parseIsoDateTime(element));
                    }
                    timeStampSpinner = new JSpinner(new SpinnerListModel(times));
                    supp = () -> {
                        return ((OffsetDateTime) timeStampSpinner.getValue()).toString();
                    };
                } else {
                    timeStampSpinner = new JSpinner(
                            // TODO: implement createRangeConstrainedTimestampModel(constraints)
                            new SpinnerDateModel()
                    );
                    timeStampSpinner.setEditor(new JSpinner.DateEditor(timeStampSpinner, DATE_TIME_FORMAT));
                    supp = () -> {
                        Date time = (Date) timeStampSpinner.getValue();
                        return OffsetDateTime.ofInstant(time.toInstant(), ZoneOffset.UTC).toString();
                    };
                }
                timeStampSpinner.setMaximumSize(BasicNodeFactory.MAX_SIZE_TIMESTAMP_SPINNER);
                comp = timeStampSpinner;
                break;
            default:
                throw new IllegalArgumentException("Not a valid BasicType.");
        }
        return new BasicNode(type, comp, supp, constraints);
    }

    protected static BasicNode createFromJson(
            final BasicType type,
            final JsonNode jsonNode,
            boolean isEditable) {
        switch (type) {
            case ANY:
                // TODO: implement
                return new BasicNode(type, new JLabel("placeholder 05"), () -> ("not implemented 05"));
            case BINARY:
                // TODO: implement
                return new BasicNode(type, new JLabel("placeholder 06"), () -> ("not implemented 06"));
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
                throw new IllegalArgumentException("Not a valid BasicType.");
        }
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
    public String toString() {
        return "(" + this.type + ", "
                + this.component.getClass() + ", "
                + this.valueSupplier.getClass() + ", "
                + this.constraints + ")";
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

    protected BasicType getType() {
        return this.type;
    }

    protected String getValue() {
        return this.valueSupplier.get();
    }

    protected Constraints getConstaint() {
        return this.constraints;
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
            start = DateTimeUtils.parseIsoDate(constraints.getMinimalExclusive()).plusDays(1);
        } else if (constraints.getMinimalInclusive() != null) {
            start = DateTimeUtils.parseIsoDate(constraints.getMinimalInclusive());
        }

        Date startDate = null;
        if (start != null) {
            if (start.isAfter(init)) {
                init = start;
            }
            startDate = Date.from(start.atStartOfDay(DateTimeUtils.LOCAL_OFFSET).toInstant());
        }

        LocalDate end = null;
        if (constraints.getMaximalExclusive() != null) {
            end = DateTimeUtils.parseIsoDate(constraints.getMaximalExclusive()).minusDays(1);
        } else if (constraints.getMaximalInclusive() != null) {
            end = DateTimeUtils.parseIsoDate(constraints.getMaximalInclusive());
        }

        Date endDate = null;
        if (end != null) {
            if (end.isBefore(init)) {
                init = end;
            }
            endDate = Date.from(end.atStartOfDay(DateTimeUtils.LOCAL_OFFSET).toInstant());
        }

        Date initDate = Date.from(init.atStartOfDay(DateTimeUtils.LOCAL_OFFSET).toInstant());
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
            start = DateTimeUtils.parseIsoTime(constraints.getMinimalExclusive())
                    .atDate(init.toLocalDate())
                    .plusSeconds(1);
        } else if (constraints.getMinimalInclusive() != null) {
            start = DateTimeUtils.parseIsoTime(constraints.getMinimalInclusive())
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
            end = DateTimeUtils.parseIsoTime(constraints.getMaximalExclusive())
                    .atDate(init.toLocalDate())
                    .minusSeconds(1);
        } else if (constraints.getMaximalInclusive() != null) {
            end = DateTimeUtils.parseIsoTime(constraints.getMaximalInclusive())
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
}
