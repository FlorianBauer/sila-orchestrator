package de.fau.clients.orchestrator.feature_explorer;

import java.awt.Dimension;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    public static final Dimension MAX_SIZE_TEXT_FIELD = new Dimension(4096, 32);
    public static final Dimension PREFERRED_SIZE_TEXT_FIELD = new Dimension(256, 32);
    public static final Dimension MAX_SIZE_SPINNER = new Dimension(128, 32);
    public static final Dimension PREFERRED_SIZE_SPINNER = new Dimension(48, 32);
    /**
     * The precision of the offset-limit of an exclusive float range (e.g. the exclusive upper-limit
     * of the value <code>1.0</code> could be <code>0.9</code>, <code>0.99</code>,
     * <code>0.999</code>, etc.)
     */
    private static final double REAL_EXCLUSIVE_OFFSET = 0.001;
    private static final double REAL_STEP_SIZE = 0.1;
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final DateTimeFormatter SILA_DATE_FORMATTER = DateTimeFormatter.ofPattern("uuuuMMdd");
    private static final String TIME_FORMAT = "HH:mm:ss";
    private static final DateTimeFormatter SILA_TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");
    private BasicType type = null;
    private Constraints constraints;
    private Supplier<String> valueSupplier;
    private JComponent component = null;

    private BasicNode() {
    }

    protected static BasicNode create(@NonNull final BasicType type) {
        BasicNode node = new BasicNode();
        node.type = type;
        switch (type) {
            case BINARY:
                // TODO: implement
                node.component = new JLabel("placeholder 01");
                node.valueSupplier = () -> ("not implemented 01");
                break;
            case BOOLEAN:
                JCheckBox checkBox = new JCheckBox();
                node.valueSupplier = () -> (checkBox.isSelected() ? "true" : "false");
                node.component = checkBox;
                break;
            case DATE:
                JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
                dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, DATE_FORMAT));
                dateSpinner.setMaximumSize(MAX_SIZE_SPINNER);
                dateSpinner.setPreferredSize(PREFERRED_SIZE_SPINNER);
                node.valueSupplier = () -> {
                    Date date = (Date) dateSpinner.getValue();
                    return LocalDate.ofInstant(date.toInstant(), ZONE).format(SILA_DATE_FORMATTER);
                };
                node.component = dateSpinner;
                break;
            case INTEGER:
            case REAL:
                SpinnerModel model = (type == BasicType.INTEGER)
                        ? new SpinnerNumberModel()
                        : new SpinnerNumberModel(0.0, null, null, REAL_STEP_SIZE);
                JSpinner numericSpinner = new JSpinner(model);
                numericSpinner.setMaximumSize(MAX_SIZE_SPINNER);
                numericSpinner.setPreferredSize(PREFERRED_SIZE_SPINNER);
                node.valueSupplier = () -> (numericSpinner.getValue().toString());
                node.component = numericSpinner;
                break;
            case STRING:
                JTextField strField = new JTextField();
                strField.setMaximumSize(MAX_SIZE_TEXT_FIELD);
                strField.setPreferredSize(PREFERRED_SIZE_TEXT_FIELD);
                node.valueSupplier = () -> (strField.getText());
                node.component = strField;
                break;
            case TIME:
                JSpinner timeSpinner = new JSpinner(new SpinnerDateModel());
                timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, TIME_FORMAT));
                timeSpinner.setMaximumSize(MAX_SIZE_SPINNER);
                timeSpinner.setPreferredSize(PREFERRED_SIZE_SPINNER);
                node.valueSupplier = () -> {
                    Date time = (Date) timeSpinner.getValue();
                    return LocalTime.ofInstant(time.toInstant(), ZONE).format(SILA_TIME_FORMATTER);
                };
                node.component = timeSpinner;
                break;
            case TIMESTAMP:
                // TODO: implement
                node.component = new JLabel("placeholder 03");
                node.valueSupplier = () -> ("not implemented 03");
                break;
            case ANY:
                // TODO: implement
                node.component = new JLabel("placeholder 04");
                node.valueSupplier = () -> ("not implemented 04");
                break;
            default:
                // TODO: implement
                return null;
        }
        return node;
    }

    protected static BasicNode createWithConstraint(
            @NonNull final BasicType type,
            final Constraints constraints) {

        BasicNode node = new BasicNode();
        node.type = type;
        switch (type) {
            case BINARY:
                // TODO: implement
                node.component = new JLabel("placeholder 01");
                node.valueSupplier = () -> ("not implemented 01");
                break;
            case BOOLEAN:
                JCheckBox checkBox = new JCheckBox();
                node.valueSupplier = () -> (checkBox.isSelected() ? "true" : "false");
                node.component = checkBox;
                break;
            case DATE:
                final JSpinner dateSpinner;
                if (constraints.getSet() != null) {
                    final List<String> dateSet = constraints.getSet().getValue();
                    ArrayList<LocalDate> dates = new ArrayList<>(dateSet.size());
                    for (final String element : dateSet) {
                        dates.add(LocalDate.parse(element, SILA_DATE_FORMATTER));
                    }
                    dateSpinner = new JSpinner(new SpinnerListModel(dates));
                    node.valueSupplier = () -> {
                        return ((LocalDate) dateSpinner.getValue()).format(SILA_DATE_FORMATTER);
                    };
                } else {
                    dateSpinner = new JSpinner(createRangeConstrainedDateModel(constraints));
                    dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, DATE_FORMAT));
                    node.valueSupplier = () -> {
                        Date date = (Date) dateSpinner.getValue();
                        return LocalDate.ofInstant(date.toInstant(), ZONE).format(SILA_DATE_FORMATTER);
                    };
                }
                dateSpinner.setMaximumSize(MAX_SIZE_SPINNER);
                dateSpinner.setPreferredSize(PREFERRED_SIZE_SPINNER);
                node.component = dateSpinner;
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
                JSpinner numericSpinner = new JSpinner(model);
                numericSpinner.setMaximumSize(MAX_SIZE_SPINNER);
                numericSpinner.setPreferredSize(PREFERRED_SIZE_SPINNER);
                node.valueSupplier = () -> (numericSpinner.getValue().toString());
                if (constraints.getUnit() != null) {
                    Box hbox = Box.createHorizontalBox();
                    hbox.add(numericSpinner);
                    hbox.add(Box.createHorizontalStrut(5));
                    hbox.add(new JLabel(constraints.getUnit().getLabel()));
                    node.component = hbox;
                } else {
                    node.component = numericSpinner;
                }
                break;
            case STRING:
                JTextField strField = new JTextField();
                strField.setMaximumSize(MAX_SIZE_TEXT_FIELD);
                strField.setPreferredSize(PREFERRED_SIZE_TEXT_FIELD);
                node.valueSupplier = () -> (strField.getText());
                node.component = strField;
                break;
            case TIME:
                final JSpinner timeSpinner;
                if (constraints.getSet() != null) {
                    final List<String> timeSet = constraints.getSet().getValue();
                    ArrayList<LocalTime> times = new ArrayList<>(timeSet.size());
                    for (final String element : timeSet) {
                        times.add(LocalTime.parse(element, SILA_TIME_FORMATTER));
                    }
                    timeSpinner = new JSpinner(new SpinnerListModel(times));
                    node.valueSupplier = () -> {
                        return ((LocalTime) timeSpinner.getValue()).format(SILA_TIME_FORMATTER);
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
                        return LocalTime.ofInstant(time.toInstant(), ZONE).format(SILA_TIME_FORMATTER);
                    };
                }
                timeSpinner.setMaximumSize(MAX_SIZE_SPINNER);
                timeSpinner.setPreferredSize(PREFERRED_SIZE_SPINNER);
                node.component = timeSpinner;
                break;
            case TIMESTAMP:
                // TODO: implement
                node.component = new JLabel("placeholder 03");
                node.valueSupplier = () -> ("not implemented 03");
                break;
            case ANY:
                // TODO: implement
                node.component = new JLabel("placeholder 04");
                node.valueSupplier = () -> ("not implemented 04");
                break;
            default:
                // TODO: implement
                return null;
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
        if (valueStr.isEmpty()) {
            return "";
        }
        return "{\"value\":\"" + valueStr + "\"}";
    }

    @Override
    public JComponent getComponent() {
        return this.component;
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
            start = LocalDate.parse(constraints.getMinimalExclusive(), SILA_DATE_FORMATTER).plusDays(1);
        } else if (constraints.getMinimalInclusive() != null) {
            start = LocalDate.parse(constraints.getMinimalInclusive(), SILA_DATE_FORMATTER);
        }

        Date startDate = null;
        if (start != null) {
            if (start.isAfter(init)) {
                init = start;
            }
            startDate = Date.from(start.atStartOfDay(ZONE).toInstant());
        }

        LocalDate end = null;
        if (constraints.getMaximalExclusive() != null) {
            end = LocalDate.parse(constraints.getMaximalExclusive(), SILA_DATE_FORMATTER).minusDays(1);
        } else if (constraints.getMaximalInclusive() != null) {
            end = LocalDate.parse(constraints.getMaximalInclusive(), SILA_DATE_FORMATTER);
        }

        Date endDate = null;
        if (end != null) {
            if (end.isBefore(init)) {
                init = end;
            }
            endDate = Date.from(end.atStartOfDay(ZONE).toInstant());
        }

        Date initDate = Date.from(init.atStartOfDay(ZONE).toInstant());
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
        LocalDateTime init = LocalDateTime.now();
        LocalDateTime start = null;
        if (constraints.getMinimalExclusive() != null) {
            start = LocalTime.parse(constraints.getMinimalExclusive(), SILA_TIME_FORMATTER)
                    .atDate(init.toLocalDate())
                    .plusSeconds(1);
        } else if (constraints.getMinimalInclusive() != null) {
            start = LocalTime.parse(constraints.getMinimalInclusive(), SILA_TIME_FORMATTER)
                    .atDate(init.toLocalDate());
        }

        Date startTime = null;
        if (start != null) {
            if (start.compareTo(init) >= 0) {
                init = start.plusSeconds(1);
            }
            startTime = Date.from(start.atZone(ZONE).toInstant());
        }

        LocalDateTime end = null;
        if (constraints.getMaximalExclusive() != null) {
            end = LocalTime.parse(constraints.getMaximalExclusive(), SILA_TIME_FORMATTER)
                    .atDate(init.toLocalDate())
                    .minusSeconds(1);
        } else if (constraints.getMaximalInclusive() != null) {
            end = LocalTime.parse(constraints.getMaximalInclusive(), SILA_TIME_FORMATTER)
                    .atDate(init.toLocalDate());
        }

        Date endTime = null;
        if (end != null) {
            if (end.compareTo(init) <= 0) {
                init = end.minusSeconds(1);
            }
            endTime = Date.from(end.atZone(ZONE).toInstant());
        }

        Date initDate = Date.from(init.atZone(ZONE).toInstant());
        return new SpinnerDateModel(initDate, startTime, endTime, Calendar.MINUTE);
    }
}
