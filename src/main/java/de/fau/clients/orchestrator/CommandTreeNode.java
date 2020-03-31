package de.fau.clients.orchestrator;

import java.util.UUID;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JPanel;
import sila_java.library.core.models.Feature.Command;

public class CommandTreeNode extends DefaultMutableTreeNode {

    private final UUID serverId;
    private final String featureId;
    private final Command command;

    public CommandTreeNode(final JPanel panel, final UUID serverId, final String featureId, final Command command) {
        super();
        this.serverId = serverId;
        this.featureId = featureId;
        this.command = command;
    }

    /**
     * Creates a new table entry for the task queue.
     *
     * @return The command used for the task queue table.
     */
    public CommandTableEntry createTableEntry() {
        return new CommandTableEntry(this.serverId, this.featureId, this.command);
    }

    @Override
    public String toString() {
        return command.getDisplayName();
    }
}
