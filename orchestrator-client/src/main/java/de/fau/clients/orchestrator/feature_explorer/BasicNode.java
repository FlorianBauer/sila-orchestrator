package de.fau.clients.orchestrator.feature_explorer;

import java.awt.Dimension;
import java.util.function.Supplier;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import lombok.NonNull;
import sila_java.library.core.models.BasicType;
import sila_java.library.core.models.Constraints;

final class BasicNode implements SilaNode {

    public static final Dimension MAX_SIZE_TEXT_FIELD = new Dimension(4096, 32);
    public static final Dimension MAX_SIZE_SPINNER = new Dimension(128, 32);
    public static final Dimension PREFERRED_SIZE_TEXT_FIELD = new Dimension(256, 32);
    public static final Dimension PREFERRED_SIZE_SPINNER = new Dimension(48, 32);

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
                JSpinner intSpinner = new JSpinner(new SpinnerNumberModel());
                intSpinner.setMaximumSize(MAX_SIZE_SPINNER);
                intSpinner.setPreferredSize(PREFERRED_SIZE_SPINNER);
                node.valueSupplier = () -> (intSpinner.getValue().toString());
                node.component = intSpinner;
                break;
            case REAL:
                JSpinner realSpinner = new JSpinner(new SpinnerNumberModel());
                realSpinner.setMaximumSize(MAX_SIZE_SPINNER);
                realSpinner.setPreferredSize(PREFERRED_SIZE_SPINNER);
                node.valueSupplier = () -> (realSpinner.getValue().toString());
                node.component = realSpinner;
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
                JSpinner intSpinner = new JSpinner(new SpinnerNumberModel());
                intSpinner.setMaximumSize(MAX_SIZE_SPINNER);
                intSpinner.setPreferredSize(PREFERRED_SIZE_SPINNER);
                node.valueSupplier = () -> (intSpinner.getValue().toString());
                node.component = intSpinner;
                break;
            case REAL:
                JSpinner realSpinner = new JSpinner(new SpinnerNumberModel());
                realSpinner.setMaximumSize(MAX_SIZE_SPINNER);
                realSpinner.setPreferredSize(PREFERRED_SIZE_SPINNER);
                node.valueSupplier = () -> (realSpinner.getValue().toString());
                node.component = realSpinner;
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
}
