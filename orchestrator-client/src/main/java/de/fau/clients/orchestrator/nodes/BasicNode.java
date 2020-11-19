package de.fau.clients.orchestrator.nodes;

import com.fasterxml.jackson.databind.JsonNode;
import de.fau.clients.orchestrator.utils.SilaBasicTypeUtils;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.Base64;
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
class BasicNode extends SilaNode {

    protected final BasicType type;
    protected final JComponent component;
    protected final Supplier<? extends Object> valueSupplier;
    protected final boolean isEditable;

    protected BasicNode(
            @NonNull final BasicType type,
            @NonNull final JComponent component,
            @NonNull final Supplier<? extends Object> valueSupplier,
            boolean isEditable
    ) {
        this.type = type;
        this.component = component;
        this.valueSupplier = valueSupplier;
        this.isEditable = isEditable;
    }

    @Override
    public BasicNode cloneNode() {
        return BasicNodeFactory.create(this.type, isEditable);
    }

    @Override
    public String toString() {
        return "(" + this.type + ", "
                + this.component.getClass() + ", "
                + this.valueSupplier.getClass() + ")";
    }

    @Override
    public JsonNode toJson() {
        switch (type) {
            case ANY:
            // The 'Any'-type should have been resolved into a concrete type.
            case BOOLEAN:
            case INTEGER:
            case REAL:
            case STRING:
                return jsonMapper.createObjectNode().put(SilaBasicTypeUtils.FIELD_VALUE,
                        valueSupplier.get().toString());
            case BINARY:
                final byte[] initVal = (byte[]) valueSupplier.get();
                if (initVal == null) {
                    return null;
                }
                final String payload = Base64.getEncoder().encodeToString(initVal);
                return jsonMapper.createObjectNode().put(SilaBasicTypeUtils.FIELD_VALUE, payload);
            case DATE:
                final LocalDate initDate = (LocalDate) valueSupplier.get();
                if (initDate == null) {
                    return null;
                }
                return SilaBasicTypeUtils.dateAsJsonNode(OffsetDateTime.of(initDate, LocalTime.MIN, ZoneOffset.UTC));
            case TIME: {
                final OffsetTime initTime = (OffsetTime) valueSupplier.get();
                if (initTime == null) {
                    return null;
                }
                return SilaBasicTypeUtils.timeAsJsonNode(initTime);
            }
            case TIMESTAMP: {
                final OffsetDateTime initTimestamp = (OffsetDateTime) valueSupplier.get();
                if (initTimestamp == null) {
                    return null;
                }
                return SilaBasicTypeUtils.timestampAsJsonNode(initTimestamp);
            }
            default:
                throw new IllegalArgumentException("Not a supported BasicType.");
        }
    }

    @Override
    public JComponent getComponent() {
        component.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        return component;
    }

    protected BasicType getType() {
        return this.type;
    }

    protected Object getValue() {
        return this.valueSupplier.get();
    }
}
