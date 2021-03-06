package de.fau.clients.orchestrator.tree;

import sila_java.library.core.models.Feature;
import sila_java.library.core.models.Feature.Command;
import sila_java.library.core.models.Feature.Property;

/**
 * Class only for representing various node widgets in the <code>JTree</code> (featureTree).
 */
class TreeNodeType {

    private static final int MAX_CHARS_PER_LINE = 100;
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
        description = formatToolTipText(feat.getDisplayName(), feat.getDescription());
    }

    public TreeNodeType(final Command cmd) {
        symbol = TreeRenderSymbol.COMMAND;
        displayName = cmd.getDisplayName();
        description = formatToolTipText(displayName, cmd.getDescription());
    }

    public TreeNodeType(final Property prop) {
        symbol = TreeRenderSymbol.PROPERTY;
        displayName = prop.getDisplayName();
        description = formatToolTipText(displayName, prop.getDescription());
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

    /**
     * Formats the given text for display in a tool-tip box.
     *
     * @param headline The headline of the tool-tip (must not be null).
     * @param text The description text for the tool-tip.
     * @return The tool-tip with applied HTML formatting or null if no text was given.
     */
    static private String formatToolTipText(final String headline, final String text) {
        if (text != null) {
            final String widthStr = (text.length() > MAX_CHARS_PER_LINE) ? " width=\"600\"" : "";
            return "<html><p" + widthStr + ">"
                    + "<b>" + headline + "</b><br>"
                    + text.strip() + "</p></html>";
        }
        return null;
    }
}
