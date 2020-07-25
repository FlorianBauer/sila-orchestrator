package de.fau.clients.orchestrator.dnd;

import de.fau.clients.orchestrator.queue.TaskQueueTable;
import de.fau.clients.orchestrator.tasks.CommandTask;
import de.fau.clients.orchestrator.tasks.QueueTask;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.JTable;
import javax.swing.TransferHandler;

/**
 * A <code>TransferHandler</code> which handles the import of an <code>QueueTask</code> into the
 * registered <code>TaskQueueTable</code> after a successful drop operation.
 *
 * @see QueueTask
 * @see TaskQueueTable
 * @see CommandNodeTransferHandler
 * @see TaskExportTransferHandler
 * @see TaskTransferer
 */
@SuppressWarnings("serial")
public class TaskImportTransferHandler extends TransferHandler {

    @Override
    public boolean canImport(TransferSupport support) {
        // only support drops
        if (!support.isDrop()) {
            return false;
        }

        // only support QueueTasks
        if (!support.isDataFlavorSupported(TaskTransferer.queueTaskFlavor)) {
            return false;
        }

        // only support copy operations
        boolean isCopySupported = ((COPY & support.getSourceDropActions()) == COPY);
        if (!isCopySupported) {
            return false;
        }

        support.setDropAction(COPY);
        return true;
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        final Transferable trans = support.getTransferable();
        QueueTask task;
        try {
            task = (QueueTask) trans.getTransferData(TaskTransferer.queueTaskFlavor);
        } catch (UnsupportedFlavorException | IOException ex) {
            return false;
        }

        final JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();
        int dropRowIdx = dl.getRow();
        final TaskQueueTable table = (TaskQueueTable) support.getComponent();
        if (task instanceof CommandTask) {
            table.insertCommandTask(dropRowIdx, (CommandTask) task);
        } else {
            table.insertTask(dropRowIdx, task);
        }
        return true;
    }
}
