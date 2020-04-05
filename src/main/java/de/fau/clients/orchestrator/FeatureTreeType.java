package de.fau.clients.orchestrator;

import sila_java.library.core.models.Feature;
import sila_java.library.core.models.Feature.Command;
import sila_java.library.core.models.Feature.Metadata;
import sila_java.library.core.models.Feature.Property;
import sila_java.library.manager.models.Server;

public class FeatureTreeType {

    final NodeEnum nodeEnum;
    String description = null;
    String displayName = "";

    public FeatureTreeType(final Server server) {
        this.nodeEnum = NodeEnum.SERVER;
        description = "Joined on " + server.getJoined();
        displayName = "<html>"
                + "<p>" + server.getConfiguration().getName() + "</p>"
                + "<p>UUID: " + server.getConfiguration().getUuid().toString() + "</p>"
                + "<p>Addr: " + server.getHostAndPort().toString() + "</p>"
                + "</html>";
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

    public FeatureTreeType(final Metadata meta) {
        this.nodeEnum = NodeEnum.META;
        description = meta.getDescription();
        displayName = meta.getDisplayName();
    }

    public NodeEnum getNodeEnum() {
        return nodeEnum;
    }

    public String getDescripton() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
