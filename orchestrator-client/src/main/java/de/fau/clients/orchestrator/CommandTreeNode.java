package de.fau.clients.orchestrator;

import de.fau.clients.orchestrator.nodes.TypeDefLut;
import de.fau.clients.orchestrator.tasks.CommandTask;
import de.fau.clients.orchestrator.tasks.CommandTaskModel;
import de.fau.clients.orchestrator.utils.IconProvider;
import java.awt.BorderLayout;
import java.util.UUID;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import sila_java.library.core.models.Feature.Command;

@SuppressWarnings("serial")
public class CommandTreeNode extends DefaultMutableTreeNode {

    public static final JPanel COMMAND_USAGE_PANEL;

    static {
        COMMAND_USAGE_PANEL = new JPanel();
        final JLabel infoLabel1 = new JLabel("Click  ");
        infoLabel1.setEnabled(false);
        final JLabel infoLabel2 = new JLabel(IconProvider.ADD_ENTRY.getIcon());
        infoLabel2.setText(" or drag  ");
        infoLabel2.setEnabled(false);
        final JLabel infoLabel3 = new JLabel(IconProvider.COMMAND.getIcon());
        infoLabel3.setText(" into the above queue to specify the command parameters.");
        infoLabel3.setEnabled(false);
        COMMAND_USAGE_PANEL.add(Box.createVerticalStrut(100), BorderLayout.NORTH);
        final Box hbox = Box.createHorizontalBox();
        hbox.add(infoLabel1);
        hbox.add(infoLabel2);
        hbox.add(infoLabel3);
        COMMAND_USAGE_PANEL.add(hbox, BorderLayout.CENTER);
    }

    private final CommandTaskModel commandModel;

    public CommandTreeNode(final CommandTaskModel commandModel) {
        super();
        this.commandModel = commandModel;
    }

    public CommandTreeNode(
            final UUID serverUuid,
            final TypeDefLut typeDefs,
            final Command command) {
        this(new CommandTaskModel(serverUuid, typeDefs, command));
    }

    /**
     * Creates a new table entry for the task queue.
     *
     * @return The command used for the task queue table.
     */
    public CommandTask createTableEntry() {
        return new CommandTask(commandModel);
    }

    @Override
    public String toString() {
        return commandModel.getCommand().getDisplayName();
    }
}
