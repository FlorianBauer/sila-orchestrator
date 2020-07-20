package de.fau.clients.orchestrator.nodes;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.function.Supplier;
import javax.swing.JComponent;
import javax.swing.JLabel;
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
            @NonNull final Supplier<String> valueSupplier) {
        this.type = type;
        this.component = component;
        this.valueSupplier = valueSupplier;
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
        return create(this.type);
    }

    @Override
    public String toString() {
        return "(" + this.type + ", "
                + this.component.getClass() + ", "
                + this.valueSupplier.getClass() + ")";
    }

    @Override
    public String toJsonString() {
        final String valueStr = valueSupplier.get();
        return "{\"value\":\"" + valueStr + "\"}";
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
