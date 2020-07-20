package de.fau.clients.orchestrator.nodes;

import com.fasterxml.jackson.databind.JsonNode;
import javax.swing.JComponent;
import lombok.NonNull;

/**
 * Node referencing to a custom defined SiLA-DataType.
 */
final class DefTypeNode implements SilaNode {

    private final String typeId;
    private final TypeDefLut typeDefs;
    private SilaNode defNode;

    private DefTypeNode(@NonNull final TypeDefLut typeDefs, final String typeIdentifier) {
        this.typeId = typeIdentifier;
        this.typeDefs = typeDefs;
    }

    protected static DefTypeNode create(
            @NonNull final TypeDefLut typeDefs,
            final String typeIdentifier) {

        final DefTypeNode node = new DefTypeNode(typeDefs, typeIdentifier);
        node.defNode = NodeFactory.createFromDataType(typeDefs, typeDefs.getElement(typeIdentifier));
        return node;
    }

    protected static DefTypeNode createFormJson(
            @NonNull final TypeDefLut typeDefs,
            final String typeIdentifier,
            final JsonNode jsonNode,
            boolean isEditable) {

        final DefTypeNode node = new DefTypeNode(typeDefs, typeIdentifier);
        node.defNode = NodeFactory.createFromJson(
                typeDefs,
                typeDefs.getElement(typeIdentifier),
                jsonNode.get(typeIdentifier),
                isEditable);
        return node;
    }

    @Override
    public SilaNode cloneNode() {
        return DefTypeNode.create(this.typeDefs, this.typeId);
    }

    @Override
    public String toJsonString() {
        String jsonChild = defNode.toJsonString();
        if (jsonChild.isEmpty()) {
            return "";
        }
        return "{\"" + typeId + "\":" + jsonChild + "}";
    }

    @Override
    public JComponent getComponent() {
        return defNode.getComponent();
    }
}
