package de.fau.clients.orchestrator;

import sila_java.library.core.models.Feature;
import sila_java.library.core.models.Feature.Command;
import sila_java.library.core.models.Feature.Property;

/**
 * Class only for representing various node widgets in the <code>JTree</code> (featureTree).
 */
class FeatureTreeType {

    private NodeEnum nodeEnum;
    private String description = null;
    private String displayName = "";

    public FeatureTreeType(final ServerTreeNode serverNode) {
        this.nodeEnum = NodeEnum.SERVER_ONLINE;
        description = serverNode.getDescription();
        displayName = serverNode.getServerLabel();
    }

    public FeatureTreeType(final Feature feat) {
        this.nodeEnum = NodeEnum.FEATURE;
        description = feat.getDescription();
        displayName = "<html><b>" + feat.getDisplayName() + "</b></html>";
    }

    public FeatureTreeType(final Command cmd) {
        this.nodeEnum = NodeEnum.COMMAND;
        description = cmd.getDescription();
        displayName = cmd.getDisplayName();
    }

    public FeatureTreeType(final Property prop) {
        this.nodeEnum = NodeEnum.PROPERTY;
        description = prop.getDescription();
        displayName = prop.getDisplayName();
    }

    public NodeEnum getNodeEnum() {
        return nodeEnum;
    }

    public void setNodeEnum(NodeEnum nodeEnum) {
        this.nodeEnum = nodeEnum;
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
