package de.fau.clients.orchestrator.nodes;

import com.fasterxml.jackson.databind.JsonNode;
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
            @NonNull final TypeDefLut typeDefs,
            @NonNull final List<SiLAElement> elements) {

        return CompositNode.create(typeDefs, elements);
    }

    public final static SilaNode createFromElementsWithJson(
            @NonNull final TypeDefLut typeDefs,
            @NonNull final List<SiLAElement> elements,
            @NonNull final JsonNode jsonNode) {

        return CompositNode.createFromJson(typeDefs, elements, jsonNode, true);
    }

    public final static SilaNode createFromDataType(
            @NonNull final TypeDefLut typeDefs,
            @NonNull final DataTypeType type) {

        if (type.getBasic() != null) {
            // basic type
            return BasicNode.create(type.getBasic());
        } else {
            // derived type
            if (type.getConstrained() != null) {
                final DataTypeType conType = type.getConstrained().getDataType();
                if (conType != null) {
                    if (conType.getBasic() != null) {
                        return ConstraintBasicNode.create(typeDefs,
                                conType.getBasic(),
                                type.getConstrained().getConstraints(),
                                null);
                    } else if (type.getConstrained().getDataType().getList() != null) {
                        return ListNode.createWithConstraint(typeDefs,
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
                return ListNode.create(typeDefs, type.getList());
            } else if (type.getStructure() != null) {
                return CompositNode.create(typeDefs, type.getStructure().getElement());
            } else if (type.getDataTypeIdentifier() != null) {
                return DefTypeNode.create(typeDefs, type.getDataTypeIdentifier());
            } else {
                log.error("Unknown type of DataTypeType");
            }
        }
        return null;
    }

    public final static SilaNode createFromJson(
            @NonNull final TypeDefLut typeDefs,
            @NonNull final DataTypeType type,
            @NonNull final JsonNode jsonNode,
            boolean isEditable) {

        if (type.getBasic() != null) {
            // basic type
            return BasicNode.createFromJson(type.getBasic(), jsonNode.get("value"), isEditable);
        } else {
            // derived type
            if (type.getConstrained() != null) {
                final DataTypeType conType = type.getConstrained().getDataType();
                if (conType != null) {
                    if (conType.getBasic() != null) {
                        return ConstraintBasicNode.create(typeDefs,
                                conType.getBasic(),
                                type.getConstrained().getConstraints(),
                                jsonNode.get("value"));
                    } else if (type.getConstrained().getDataType().getList() != null) {
                        return ListNode.createWithConstraint(typeDefs,
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
                return ListNode.createFromJson(typeDefs, type.getList(), jsonNode, isEditable);
            } else if (type.getStructure() != null) {
                return CompositNode.createFromJson(typeDefs, type.getStructure().getElement(), jsonNode, isEditable);
            } else if (type.getDataTypeIdentifier() != null) {
                return DefTypeNode.createFormJson(typeDefs, type.getDataTypeIdentifier(), jsonNode, isEditable);
            } else {
                log.error("Unknown type of DataTypeType");
            }
        }
        return null;
    }
}
