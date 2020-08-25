package de.fau.clients.orchestrator;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

@SuppressWarnings("serial")
class FeatureTreeRenderer extends DefaultTreeCellRenderer {

    private static final Icon serverOnlineIcon = new ImageIcon(FeatureTreeRenderer.class.getResource("/icons/server-online.png"));
    private static final Icon serverOfflineIcon = new ImageIcon(FeatureTreeRenderer.class.getResource("/icons/server-offline.png"));
    private static final Icon silaIcon = new ImageIcon(FeatureTreeRenderer.class.getResource("/icons/sila-feature.png"));
    private static final Icon commandIcon = new ImageIcon(FeatureTreeRenderer.class.getResource("/icons/command.png"));
    private static final Icon propertyIcon = new ImageIcon(FeatureTreeRenderer.class.getResource("/icons/property.png"));

    public FeatureTreeRenderer() {
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
            boolean hasFocus) {

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
        if (!(obj instanceof FeatureTreeType)) {
            return this;
        }

        final FeatureTreeType nodeInfo = (FeatureTreeType) obj;
        switch (nodeInfo.getNodeRenderSymbol()) {
            case SERVER_ONLINE:
                this.setIcon(serverOnlineIcon);
                break;
            case SERVER_OFFLINE:
                this.setIcon(serverOfflineIcon);
                break;
            case FEATURE:
                this.setIcon(silaIcon);
                break;
            case COMMAND:
                this.setIcon(commandIcon);
                break;
            case PROPERTY:
                this.setIcon(propertyIcon);
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
