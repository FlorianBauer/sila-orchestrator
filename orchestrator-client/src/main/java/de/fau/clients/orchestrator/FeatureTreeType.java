package de.fau.clients.orchestrator;

import sila_java.library.core.models.Feature;
import sila_java.library.core.models.Feature.Command;
import sila_java.library.core.models.Feature.Property;

/**
 * Class only for representing various node widgets in the <code>JTree</code> (featureTree).
 */
class FeatureTreeType {

    private NodeRenderSymbol symbol;
    private String description = null;
    private String displayName = "";

    public FeatureTreeType(final ServerTreeNode serverNode) {
        this.symbol = NodeRenderSymbol.SERVER_ONLINE;
        description = serverNode.getDescription();
        displayName = serverNode.getServerLabel();
    }

    public FeatureTreeType(final Feature feat) {
        this.symbol = NodeRenderSymbol.FEATURE;
        description = feat.getDescription();
        displayName = "<html><b>" + feat.getDisplayName() + "</b></html>";
    }

    public FeatureTreeType(final Command cmd) {
        this.symbol = NodeRenderSymbol.COMMAND;
        description = cmd.getDescription();
        displayName = cmd.getDisplayName();
    }

    public FeatureTreeType(final Property prop) {
        this.symbol = NodeRenderSymbol.PROPERTY;
        description = prop.getDescription();
        displayName = prop.getDisplayName();
    }

    public NodeRenderSymbol getNodeRenderSymbol() {
        return symbol;
    }

    public void setNodeRenderSymbol(NodeRenderSymbol symbol) {
        this.symbol = symbol;
    }

    public String getDescripton() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
