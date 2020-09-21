package de.fau.clients.orchestrator.nodes;

import java.util.function.Supplier;
import javax.swing.JComponent;
import lombok.NonNull;
import sila_java.library.core.models.BasicType;

/**
 * A <code>SilaNode</code> implementation representing SiLA Basic Types and its corresponding
 * GUI-Components.
 *
 * @see BasciNodeFactory
 * @see ConstraintBasicNode
 */
class BasicNode implements SilaNode {

    protected final BasicType type;
    protected final JComponent component;
    protected final Supplier<String> valueSupplier;

    protected BasicNode(
            @NonNull final BasicType type,
            @NonNull final JComponent component,
            @NonNull final Supplier<String> valueSupplier
    ) {
        this.type = type;
        this.component = component;
        this.valueSupplier = valueSupplier;
    }

    @Override
    public BasicNode cloneNode() {
        return BasicNodeFactory.create(this.type);
    }

    @Override
    public String toString() {
        return "(" + this.type + ", "
                + this.component.getClass() + ", "
                + this.valueSupplier.getClass() + ")";
    }

    @Override
    public String toJsonString() {
        return "{\"value\":\"" + valueSupplier.get() + "\"}";
    }

    @Override
    public JComponent getComponent() {
        component.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        return component;
    }

    protected BasicType getType() {
        return this.type;
    }

    protected String getValue() {
        return this.valueSupplier.get();
    }
}
