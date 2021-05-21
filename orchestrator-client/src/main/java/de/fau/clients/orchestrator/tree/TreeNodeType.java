package de.fau.clients.orchestrator.tree;

import de.fau.clients.orchestrator.utils.SilaDescriptionToolTip;
import sila_java.library.core.models.Feature;
import sila_java.library.core.models.Feature.Command;
import sila_java.library.core.models.Feature.Property;

/**
 * Class only for representing various node widgets in the <code>JTree</code> (featureTree).
 */
class TreeNodeType {

    private TreeRenderSymbol symbol;
    private String displayName = "";
    private String description = null;

    public TreeNodeType(final ServerTreeNode serverNode) {
        symbol = TreeRenderSymbol.SERVER_ONLINE;
        displayName = serverNode.getServerLabel();
        description = serverNode.getDescription();
    }

    public TreeNodeType(final Feature feat, boolean isCoreFeat) {
        if (isCoreFeat) {
            symbol = TreeRenderSymbol.FEATURE_CORE;
        } else {
            symbol = TreeRenderSymbol.FEATURE;
        }
        displayName = "<html><b>" + feat.getDisplayName() + "</b></html>";
        description = SilaDescriptionToolTip.formatHeadlineToolTip(
                feat.getDisplayName(),
                feat.getDescription());
    }

    public TreeNodeType(final Command cmd) {
        symbol = TreeRenderSymbol.COMMAND;
        displayName = cmd.getDisplayName();
        description = SilaDescriptionToolTip.formatHeadlineToolTip(displayName, cmd.getDescription());
    }

    public TreeNodeType(final Property prop) {
        symbol = TreeRenderSymbol.PROPERTY;
        displayName = prop.getDisplayName();
        description = SilaDescriptionToolTip.formatHeadlineToolTip(displayName, prop.getDescription());
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
