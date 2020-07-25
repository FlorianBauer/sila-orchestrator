package de.fau.clients.orchestrator.dnd;

import de.fau.clients.orchestrator.CommandTreeNode;
import java.awt.datatransfer.Transferable;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * A transfer handler to enable Drag and Drop of <code>CommandNode</code>s from the feature tree
 * into the task queue.
 *
 * @see CommandTreeNode
 * @see TaskExportTransferHandler
 * @see TaskImportTransferHandler
 * @see TaskTransferer
 */
@SuppressWarnings("serial")
public class CommandNodeTransferHandler extends TransferHandler {

    @Override
    public int getSourceActions(JComponent comp) {
        return TransferHandler.COPY;
    }

    @Override
    protected Transferable createTransferable(JComponent comp) {
        final JTree tree = (JTree) comp;
        if (tree == null) {
            return null;
        }

        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node == null) {
            return null;
        }

        if (node.isLeaf()) {
            if (node instanceof CommandTreeNode) {
                final CommandTreeNode cmdNode = (CommandTreeNode) node;
                return new TaskTransferer(cmdNode.createTableEntry());
            }
        }
        return null;
    }
}
