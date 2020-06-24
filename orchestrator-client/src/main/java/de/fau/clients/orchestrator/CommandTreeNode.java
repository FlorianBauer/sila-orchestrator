package de.fau.clients.orchestrator;

import de.fau.clients.orchestrator.feature_explorer.TypeDefLut;
import java.util.UUID;
import javax.swing.tree.DefaultMutableTreeNode;
import sila_java.library.core.models.Feature.Command;

public class CommandTreeNode extends DefaultMutableTreeNode {

    private final UUID serverId;
    private final String featureId;
    private final TypeDefLut typeDefs;
    private final Command command;

    public CommandTreeNode(
            final UUID serverId,
            final String featureId,
            final TypeDefLut typeDefs,
            final Command command) {

        super();
        this.serverId = serverId;
        this.featureId = featureId;
        this.typeDefs = typeDefs;
        this.command = command;
    }

    /**
     * Creates a new table entry for the task queue.
     *
     * @return The command used for the task queue table.
     */
    public CommandTask createTableEntry() {
        return new CommandTask(this.serverId, this.featureId, this.typeDefs, this.command);
    }

    @Override
    public String toString() {
        return command.getDisplayName();
    }
}
