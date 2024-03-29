package de.fau.clients.orchestrator.tree;

import de.fau.clients.orchestrator.ctx.CommandContext;
import de.fau.clients.orchestrator.tasks.CommandTask;
import de.fau.clients.orchestrator.utils.IconProvider;
import java.awt.BorderLayout;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import lombok.NonNull;

@SuppressWarnings("serial")
public class CommandTreeNode extends DefaultMutableTreeNode {

    public static final JPanel COMMAND_USAGE_PANEL;

    static {
        COMMAND_USAGE_PANEL = new JPanel();
        final JLabel infoLabel1 = new JLabel("Right-click and  ");
        infoLabel1.setEnabled(false);
        final JLabel infoLabel2 = new JLabel(IconProvider.QUEUE_ADD_TASK_16PX.getIcon());
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

    private final CommandContext commandCtx;

    public CommandTreeNode(@NonNull final CommandContext commandCtx) {
        this.commandCtx = commandCtx;
    }

    /**
     * Creates a new table entry for the task queue.
     *
     * @return The command used for the task queue table.
     */
    public CommandTask createTableEntry() {
        return new CommandTask(commandCtx);
    }

    @Override
    public String toString() {
        return commandCtx.getCommand().getDisplayName();
    }
}
