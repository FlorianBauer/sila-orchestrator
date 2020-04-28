package de.fau.clients.orchestrator.feature_explorer;

import javax.swing.JComponent;
import lombok.NonNull;

/**
 * Node referencing to a custom defined SiLA-DataType.
 */
public class DefTypeNode implements SilaNode {

    private final String typeId;
    private final TypeDefLut typeDefs;
    private final SilaNode defNode;

    public DefTypeNode(@NonNull final TypeDefLut typeDefs, final String typeIdentifier) {
        this.typeId = typeIdentifier;
        this.typeDefs = typeDefs;
        this.defNode = typeDefs.getNode(typeIdentifier).cloneNode();
    }

    @Override
    public SilaNode cloneNode() {
        return new DefTypeNode(this.typeDefs, this.typeId);
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
