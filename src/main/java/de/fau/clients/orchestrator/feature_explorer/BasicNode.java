package de.fau.clients.orchestrator.feature_explorer;

import java.awt.Dimension;
import java.util.function.Supplier;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import lombok.NonNull;
import sila_java.library.core.models.BasicType;

final class BasicNode implements SilaNode {

    public static final Dimension MAX_SIZE = new Dimension(4096, 32);
    public static final Dimension PREFERRED_SIZE = new Dimension(256, 32);

    private JComponent component = null;
    private BasicType type = null;
    private Supplier<String> valueSupplier;

    private BasicNode() {
    }

    protected static BasicNode create(@NonNull final BasicType type) {
        BasicNode node = new BasicNode();
        node.type = type;
        switch (type) {
            case BINARY:
                // TODO: implement
                break;
            case BOOLEAN:
                // TODO: implement
                break;
            case DATE:
                // TODO: implement
                break;
            case INTEGER:
                JSpinner intSpinner = new JSpinner();
                intSpinner.setMaximumSize(MAX_SIZE);
                intSpinner.setPreferredSize(PREFERRED_SIZE);
                intSpinner.setModel(new SpinnerNumberModel());
                node.valueSupplier = () -> (intSpinner.getValue().toString());
                node.component = intSpinner;
//                return node;
            case REAL:
                JSpinner realSpinner = new JSpinner();
                realSpinner.setMaximumSize(MAX_SIZE);
                realSpinner.setPreferredSize(PREFERRED_SIZE);
                realSpinner.setModel(new SpinnerNumberModel());
                node.valueSupplier = () -> (realSpinner.getValue().toString());
                node.component = realSpinner;
            case STRING:
                JTextField strField = new JTextField();
                strField.setMaximumSize(MAX_SIZE);
                strField.setPreferredSize(PREFERRED_SIZE);
                node.valueSupplier = () -> (strField.getText());
                node.component = strField;
            case TIME:
                // TODO: implement
                break;
            case TIMESTAMP:
                // TODO: implement
                break;
            case ANY:
            default:
                // TODO: implement
                return null;
        }
        return node;
    }

    @Override
    public BasicNode cloneNode() {
        return create(this.type);
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
