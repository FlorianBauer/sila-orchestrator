package de.fau.clients.orchestrator.nodes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.fau.clients.orchestrator.ctx.FeatureContext;
import javax.swing.JComponent;
import lombok.NonNull;

/**
 * Node referencing to a custom defined SiLA-DataType.
 */
final class DefTypeNode extends SilaNode {

    
    private final FeatureContext featCtx;
    private final String typeId;
    private SilaNode defNode;

    private DefTypeNode(
            @NonNull final FeatureContext featCtx, 
            @NonNull final String typeIdentifier
    ) {
        this.featCtx = featCtx;
        this.typeId = typeIdentifier;
    }

    protected static DefTypeNode create(
            @NonNull final FeatureContext featCtx,
            @NonNull final String typeIdentifier
    ) {
        final DefTypeNode node = new DefTypeNode(featCtx, typeIdentifier);
        node.defNode = NodeFactory.createFromDataType(featCtx, featCtx.getElement(typeIdentifier));
        return node;
    }

    protected static DefTypeNode createFormJson(
            @NonNull final FeatureContext featCtx,
            @NonNull final String typeIdentifier,
            final JsonNode jsonNode,
            boolean isEditable
    ) {
        final DefTypeNode node = new DefTypeNode(featCtx, typeIdentifier);
        node.defNode = NodeFactory.createFromJson(
                featCtx,
                featCtx.getElement(typeIdentifier),
                jsonNode.get(typeIdentifier),
                isEditable);
        return node;
    }

    @Override
    public SilaNode cloneNode() {
        return DefTypeNode.create(this.featCtx, this.typeId);
    }

    @Override
    public JsonNode toJson() {
        final ObjectNode objNode = jsonMapper.createObjectNode();
        objNode.set(typeId, defNode.toJson());
        return objNode;
    }

    @Override
    public JComponent getComponent() {
        return defNode.getComponent();
    }
}
