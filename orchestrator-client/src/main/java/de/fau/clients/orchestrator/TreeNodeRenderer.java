package de.fau.clients.orchestrator;

import de.fau.clients.orchestrator.utils.IconProvider;
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * Renderer to set and draw the corresponding icon for every node type in the tree view.
 */
@SuppressWarnings("serial")
class TreeNodeRenderer extends DefaultTreeCellRenderer {

    public TreeNodeRenderer() {
        this.openIcon = null;
        this.closedIcon = null;
        this.leafIcon = null;
    }

    @Override
    public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean isSelected,
            boolean isExpanded,
            boolean isLeaf,
            int rowIdx,
            boolean hasFocus
    ) {
        super.getTreeCellRendererComponent(
                tree,
                value,
                isSelected,
                isExpanded,
                isLeaf,
                rowIdx,
                hasFocus);

        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        final Object obj = node.getUserObject();
        if (!(obj instanceof TreeNodeType)) {
            return this;
        }

        final TreeNodeType nodeInfo = (TreeNodeType) obj;
        switch (nodeInfo.getTreeRenderSymbol()) {
            case SERVER_ONLINE:
                this.setIcon(IconProvider.SERVER_ONLINE.getIcon());
                break;
            case SERVER_OFFLINE:
                this.setIcon(IconProvider.SERVER_OFFLINE.getIcon());
                break;
            case FEATURE:
                this.setIcon(IconProvider.SILA_FEATURE.getIcon());
                break;
            case FEATURE_CORE:
                this.setIcon(IconProvider.SILA_FEATURE_CORE.getIcon());
                break;
            case COMMAND:
                this.setIcon(IconProvider.COMMAND.getIcon());
                break;
            case PROPERTY:
                this.setIcon(IconProvider.PROPERTY.getIcon());
                break;
            case META:
            default:
            // no icon on default
        }

        final String desc = nodeInfo.getDescripton();
        if (desc != null) {
            final String toolTipTxt = (desc.length() < 100)
                    ? desc.strip()
                    : "<html><p width=\"500\">" + desc + "</p></html>";
            this.setToolTipText(toolTipTxt);
        }

        return this;
    }
}
