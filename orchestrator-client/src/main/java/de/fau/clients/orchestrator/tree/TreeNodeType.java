package de.fau.clients.orchestrator.tree;

import sila_java.library.core.models.Feature;
import sila_java.library.core.models.Feature.Command;
import sila_java.library.core.models.Feature.Property;

/**
 * Class only for representing various node widgets in the <code>JTree</code> (featureTree).
 */
class TreeNodeType {

    private TreeRenderSymbol symbol;
    private String description = null;
    private String displayName = "";

    public TreeNodeType(final ServerTreeNode serverNode) {
        this.symbol = TreeRenderSymbol.SERVER_ONLINE;
        description = serverNode.getDescription();
        displayName = serverNode.getServerLabel();
    }

    public TreeNodeType(final Feature feat, boolean isCoreFeat) {
        if (isCoreFeat) {
            this.symbol = TreeRenderSymbol.FEATURE_CORE;
        } else {
            this.symbol = TreeRenderSymbol.FEATURE;
        }
        description = feat.getDescription();
        displayName = "<html><b>" + feat.getDisplayName() + "</b></html>";
    }

    public TreeNodeType(final Command cmd) {
        this.symbol = TreeRenderSymbol.COMMAND;
        description = cmd.getDescription();
        displayName = cmd.getDisplayName();
    }

    public TreeNodeType(final Property prop) {
        this.symbol = TreeRenderSymbol.PROPERTY;
        description = prop.getDescription();
        displayName = prop.getDisplayName();
    }

    public TreeRenderSymbol getTreeRenderSymbol() {
        return symbol;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public void setTreeRenderSymbol(final TreeRenderSymbol symbol) {
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
