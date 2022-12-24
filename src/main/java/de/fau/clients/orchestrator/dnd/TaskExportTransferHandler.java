package de.fau.clients.orchestrator.dnd;

import de.fau.clients.orchestrator.tasks.QueueTask;
import java.awt.datatransfer.Transferable;
import java.util.function.Supplier;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

/**
 * A <code>TransferHandler</code> which handles the start of an <code>QueueTask</code> transfer from
 * a draggable widget.
 *
 * @see QueueTask
 * @see TaskImportTransferHandler
 * @see CommandNodeTransferHandler
 * @see TaskTransferer
 */
@SuppressWarnings("serial")
public class TaskExportTransferHandler extends TransferHandler {

    private final Supplier<QueueTask> taskSupp;

    /**
     * Constructor. Requires a task source to spawn task instances for the transfer.
     *
     * @param taskSupp The <code>QueueTask</code> supplier for the transferable tasks.
     */
    public TaskExportTransferHandler(final Supplier<QueueTask> taskSupp) {
        this.taskSupp = taskSupp;
    }

    @Override
    public int getSourceActions(JComponent comp) {
        return TransferHandler.COPY;
    }

    @Override
    protected Transferable createTransferable(JComponent comp) {
        return new TaskTransferer(taskSupp.get());
    }
}
