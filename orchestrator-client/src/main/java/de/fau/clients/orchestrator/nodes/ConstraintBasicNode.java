package de.fau.clients.orchestrator.nodes;

import com.fasterxml.jackson.databind.JsonNode;
import de.fau.clients.orchestrator.utils.DateTimeParser;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import lombok.NonNull;
import sila_java.library.core.models.BasicType;
import sila_java.library.core.models.Constraints;

/**
 * A <code>BasicNode</code> with additional restrictions given by the SiLA-Constraint object.
 *
 * @see BasicNode
 * @see BasicNodeFactory
 */
public class ConstraintBasicNode extends BasicNode {

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
    private static final ImageIcon VALIDATON_OK = new ImageIcon(ConstraintBasicNode.class.getResource("/icons/status-ok.png"));
    private static final ImageIcon VALIDATON_WARN = new ImageIcon(ConstraintBasicNode.class.getResource("/icons/status-warning.png"));
    /**
     * The horizontal gap size between parameter component, condition description and validation
     * icon. Only used for components with constraints.
     */
    private static final int HORIZONTAL_STRUT = 5;
    private final Constraints constraints;

    protected ConstraintBasicNode(
            @NonNull final BasicType type,
            @NonNull final JComponent component,
            @NonNull final Supplier<String> valueSupplier,
            @NonNull final Constraints constraints) {
        super(type, component, valueSupplier);
        this.constraints = constraints;
    }

    protected static BasicNode create(
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
                // TODO: implement
                comp = new JLabel("placeholder 04");
                supp = () -> ("not implemented 04");
                break;
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
                    final JSpinner dateSpinner = new JSpinner(createRangeConstrainedDateModel(constraints));
                    dateSpinner.setMaximumSize(BasicNodeFactory.MAX_SIZE_DATE_TIME_SPINNER);
                    dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, DATE_FORMAT));
                    supp = () -> {
                        Date date = (Date) dateSpinner.getValue();
                        return LocalDate.ofInstant(date.toInstant(), DateTimeParser.LOCAL_OFFSET).toString();
                    };
                    comp = dateSpinner;
                }
                break;
            case INTEGER:
            case REAL:
                if (constraints.getSet() != null) {
                    final List<String> numberSet = constraints.getSet().getValue();
                    final JComboBox<String> numberComboBox = new JComboBox<>(numberSet.toArray(new String[0]));
                    numberComboBox.setMaximumSize(BasicNodeFactory.MAX_SIZE_NUMERIC_SPINNER);
                    if (type == BasicType.INTEGER) {
                        supp = () -> (Integer.valueOf((String) numberComboBox.getSelectedItem()).toString());
                    } else { // REAL
                        supp = () -> (Double.valueOf((String) numberComboBox.getSelectedItem()).toString());
                    }
                    comp = numberComboBox;
                } else {
                    final SpinnerModel model = (type == BasicType.INTEGER)
                            ? createRangeConstrainedIntModel(constraints)
                            : createRangeConstrainedRealModel(constraints);
                    final JSpinner numericSpinner = new JSpinner(model);
                    numericSpinner.setMaximumSize(BasicNodeFactory.MAX_SIZE_NUMERIC_SPINNER);
                    if (constraints.getUnit() != null) {
                        final Box hbox = Box.createHorizontalBox();
                        hbox.add(numericSpinner);
                        hbox.add(Box.createHorizontalStrut(HORIZONTAL_STRUT));
                        hbox.add(new JLabel(constraints.getUnit().getLabel()));
                        hbox.setMaximumSize(BasicNodeFactory.MAX_SIZE_TEXT_FIELD);
                        comp = hbox;
                    } else {
                        comp = numericSpinner;
                    }
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
                    break;
                }

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
                    ((AbstractDocument) strField.getDocument()).setDocumentFilter(new LengthFilter(len));
                    validator = () -> (strField.getText().length() == len);
                    conditionDesc = "= " + len;
                } else {
                    final BigInteger min = constraints.getMinimalLength();
                    final BigInteger max = constraints.getMaximalLength();
                    if (max != null) {
                        ((AbstractDocument) strField.getDocument()).setDocumentFilter(new LengthFilter(max.intValue()));
                    }

                    if (min != null && max != null) {
                        validator = () -> {
                            final int len = strField.getText().length();
                            return (len >= min.intValue() && len <= max.intValue());
                        };
                        conditionDesc = ">= " + min + " && <= " + max;
                    } else if (min != null) {
                        validator = () -> (strField.getText().length() >= min.intValue());
                        conditionDesc = ">= " + min;
                    } else if (max != null) {
                        validator = () -> (strField.getText().length() <= max.intValue());
                        conditionDesc = "<= " + max;
                    } else {
                        validator = () -> (false);
                        conditionDesc = "invalid constraint";
                    }
                }

                final JLabel validationLabel = new JLabel(VALIDATON_OK);
                validationLabel.setDisabledIcon(VALIDATON_WARN);
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
                    final JSpinner timeSpinner = new JSpinner(
                            // FIXME: The out-commented function below seems to return a valid 
                            // spinner-model, but the spinner itself does not work correctly. Therefore 
                            // a unrestrained default-spinner-model is used as a temporary hack.
                            createRangeConstrainedTimeModel(constraints)
                    // new SpinnerDateModel()
                    );
                    timeSpinner.setMaximumSize(BasicNodeFactory.MAX_SIZE_DATE_TIME_SPINNER);
                    timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, TIME_FORMAT));
                    supp = () -> {
                        Date time = (Date) timeSpinner.getValue();
                        return OffsetTime.ofInstant(time.toInstant(), DateTimeParser.LOCAL_OFFSET)
                                .withOffsetSameInstant(ZoneOffset.UTC)
                                .toString();
                    };
                    comp = timeSpinner;
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
        return new ConstraintBasicNode(type, comp, supp, constraints);
    }

    @Override
    public BasicNode cloneNode() {
        return create(this.type, this.constraints, null);
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
            start = DateTimeParser.parseIsoDate(constraints.getMinimalExclusive()).plusDays(1);
        } else if (constraints.getMinimalInclusive() != null) {
            start = DateTimeParser.parseIsoDate(constraints.getMinimalInclusive());
        }

        Date startDate = null;
        if (start != null) {
            if (start.isAfter(init)) {
                init = start;
            }
            startDate = Date.from(start.atStartOfDay(DateTimeParser.LOCAL_OFFSET).toInstant());
        }

        LocalDate end = null;
        if (constraints.getMaximalExclusive() != null) {
            end = DateTimeParser.parseIsoDate(constraints.getMaximalExclusive()).minusDays(1);
        } else if (constraints.getMaximalInclusive() != null) {
            end = DateTimeParser.parseIsoDate(constraints.getMaximalInclusive());
        }

        Date endDate = null;
        if (end != null) {
            if (end.isBefore(init)) {
                init = end;
            }
            endDate = Date.from(end.atStartOfDay(DateTimeParser.LOCAL_OFFSET).toInstant());
        }

        final Date initDate = Date.from(init.atStartOfDay(DateTimeParser.LOCAL_OFFSET).toInstant());
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
            start = DateTimeParser.parseIsoTime(constraints.getMinimalExclusive())
                    .atDate(init.toLocalDate())
                    .plusSeconds(1);
        } else if (constraints.getMinimalInclusive() != null) {
            start = DateTimeParser.parseIsoTime(constraints.getMinimalInclusive())
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
            end = DateTimeParser.parseIsoTime(constraints.getMaximalExclusive())
                    .atDate(init.toLocalDate())
                    .minusSeconds(1);
        } else if (constraints.getMaximalInclusive() != null) {
            end = DateTimeParser.parseIsoTime(constraints.getMaximalInclusive())
                    .atDate(init.toLocalDate());
        }

        Date endTime = null;
        if (end != null) {
            if (end.compareTo(init) <= 0) {
                init = end.minusSeconds(1);
            }
            endTime = Date.from(end.toInstant());
        }

        final Date initDate = Date.from(init.toInstant());
        return new SpinnerDateModel(initDate, startTime, endTime, Calendar.MINUTE);
    }

    private static class LengthFilter extends DocumentFilter {

        /**
         * The character input limit (inclusive).
         */
        private final int charLimit;

        /**
         * Creates a length constraint <code>DocumentFilter</code>.
         *
         * @param maxLength The maximal character input limit (inclusive).
         */
        public LengthFilter(int maxLength) {
            this.charLimit = maxLength;
        }

        /**
         * Gets the character limit.
         *
         * @return The character input limit (inclusive).
         */
        public int getCharLimit() {
            return charLimit;
        }

        @Override
        public void insertString(DocumentFilter.FilterBypass fb,
                int offset,
                String str,
                AttributeSet attrs) throws BadLocationException {
            final int len = fb.getDocument().getLength() + str.length();
            if (len < charLimit) {
                super.insertString(fb, offset, str, attrs);
            }
        }

        @Override
        public void replace(DocumentFilter.FilterBypass fb,
                int offset,
                int length,
                String str,
                AttributeSet attrs) throws BadLocationException {
            final int len = fb.getDocument().getLength() + length;
            if (len < charLimit) {
                super.replace(fb, offset, length, str, attrs);
            }
        }
    }
}
