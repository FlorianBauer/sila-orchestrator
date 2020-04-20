package de.fau.clients.orchestrator.feature_explorer;

import java.awt.Dimension;
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
    public static final Dimension MAX_SIZE_SPINNER = new Dimension(128, 32);
    public static final Dimension PREFERRED_SIZE_TEXT_FIELD = new Dimension(256, 32);
    public static final Dimension PREFERRED_SIZE_SPINNER = new Dimension(48, 32);
    /**
     * The precision of the offset-limit of an exclusive float range (e.g. the exclusive upper-limit
     * of the value <code>1.0</code> could be <code>0.9</code>, <code>0.99</code>,
     * <code>0.999</code>, etc.)
     */
    private static final double REAL_EXCLUSIVE_OFFSET = 0.001;
    private static final double REAL_STEP_SIZE = 0.1;
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
                dateSpinner.setMaximumSize(MAX_SIZE_SPINNER);
                dateSpinner.setPreferredSize(PREFERRED_SIZE_SPINNER);
                node.valueSupplier = () -> (dateSpinner.getValue().toString());
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
                // TODO: implement
                node.component = new JLabel("placeholder 02");
                node.valueSupplier = () -> ("not implemented 02");
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
                JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
                dateSpinner.setMaximumSize(MAX_SIZE_SPINNER);
                dateSpinner.setPreferredSize(PREFERRED_SIZE_SPINNER);
                node.valueSupplier = () -> (dateSpinner.getValue().toString());
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
                // TODO: implement
                node.component = new JLabel("placeholder 02");
                node.valueSupplier = () -> ("not implemented 02");
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
}
