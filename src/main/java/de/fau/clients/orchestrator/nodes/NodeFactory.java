package de.fau.clients.orchestrator.nodes;

import com.fasterxml.jackson.databind.JsonNode;
import de.fau.clients.orchestrator.ctx.FeatureContext;
import java.util.List;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import sila_java.library.core.models.DataTypeType;
import sila_java.library.core.models.SiLAElement;

/**
 * The main Factory for all <code>SilaNode</code>s. This class can completely create all available
 * node types from a SiLA Feature.
 *
 * @see SilaNode
 * @see BasicNode
 * @see CompositNode
 * @see ListNode
 * @see DefTypeNode
 */
@Slf4j
public final class NodeFactory {

    private NodeFactory() {
        throw new UnsupportedOperationException("Instantiation not allowed.");
    }

    public final static SilaNode createFromElements(
            @NonNull final FeatureContext featCtx,
            @NonNull final List<SiLAElement> elements
    ) {
        return CompositNode.create(featCtx, elements);
    }

    public final static SilaNode createFromElementsWithJson(
            @NonNull final FeatureContext featCtx,
            @NonNull final List<SiLAElement> elements,
            @NonNull final JsonNode jsonNode,
            boolean isEditable
    ) {
        return CompositNode.createFromJson(featCtx, elements, jsonNode, isEditable);
    }

    public final static SilaNode createFromDataType(
            final FeatureContext featCtx,
            @NonNull final DataTypeType type
    ) {
        if (type.getBasic() != null) {
            // basic type
            return BasicNodeFactory.create(type.getBasic(), true);
        } else {
            // derived type
            if (type.getConstrained() != null) {
                final DataTypeType conType = type.getConstrained().getDataType();
                if (conType != null) {
                    if (conType.getBasic() != null) {
                        return ConstraintBasicNodeFactory.create(featCtx,
                                conType.getBasic(),
                                type.getConstrained().getConstraints());
                    } else if (type.getConstrained().getDataType().getList() != null) {
                        return ListNode.createWithConstraint(featCtx,
                                conType.getList(),
                                type.getConstrained().getConstraints(),
                                null);
                    } else {
                        log.error("A Constrained type can only contain a Basic- or a List-type");
                    }
                } else {
                    log.error("Constrained type is null");
                }
            } else if (type.getList() != null) {
                return ListNode.create(featCtx, type.getList());
            } else if (type.getStructure() != null) {
                return CompositNode.create(featCtx, type.getStructure().getElement());
            } else if (type.getDataTypeIdentifier() != null) {
                return DefTypeNode.create(featCtx, type.getDataTypeIdentifier());
            } else {
                log.error("Unknown type of DataTypeType");
            }
        }
        return null;
    }

    public final static SilaNode createFromJson(
            final FeatureContext featCtx,
            @NonNull final DataTypeType type,
            final JsonNode jsonNode,
            boolean isEditable
    ) {
        if (type.getBasic() != null) {
            // basic type
            return BasicNodeFactory.createFromJson(type.getBasic(), jsonNode, isEditable);
        } else {
            // derived type
            if (type.getConstrained() != null) {
                final DataTypeType conType = type.getConstrained().getDataType();
                if (conType != null) {
                    if (conType.getBasic() != null) {
                        return ConstraintBasicNodeFactory.createFromJson(featCtx,
                                conType.getBasic(),
                                type.getConstrained().getConstraints(),
                                jsonNode);
                    } else if (type.getConstrained().getDataType().getList() != null) {
                        return ListNode.createWithConstraint(featCtx,
                                conType.getList(),
                                type.getConstrained().getConstraints(),
                                jsonNode);
                    } else {
                        log.error("A Constrained type can only contain a Basic- or a List-type");
                    }
                } else {
                    log.error("Constrained type is null");
                }
            } else if (type.getList() != null) {
                return ListNode.createFromJson(featCtx, type.getList(), jsonNode, isEditable);
            } else if (type.getStructure() != null) {
                return CompositNode.createFromJson(featCtx,
                        type.getStructure().getElement(),
                        jsonNode,
                        isEditable);
            } else if (type.getDataTypeIdentifier() != null) {
                return DefTypeNode.createFormJson(featCtx,
                        type.getDataTypeIdentifier(),
                        jsonNode,
                        isEditable);
            } else {
                log.error("Unknown type of DataTypeType");
            }
        }
        return null;
    }
}
