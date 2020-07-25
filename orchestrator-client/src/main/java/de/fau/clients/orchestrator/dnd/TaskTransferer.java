package de.fau.clients.orchestrator.dnd;

import de.fau.clients.orchestrator.tasks.QueueTask;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

/**
 * A transferable class which holds a <code>QueueTask</code> object during a Drag and Drop
 * operation.
 *
 * @see QueueTask
 */
class TaskTransferer implements Transferable {

    /**
     * The Mime type string for <code>QueueTask</code>s.
     */
    static final String QUEUE_TASK_MIME_TYPE = DataFlavor.javaJVMLocalObjectMimeType + ";class=\""
            + de.fau.clients.orchestrator.tasks.QueueTask.class.getName() + "\"";

    /**
     * The <code>DataFavor</code> for <code>QueueTask</code>s.
     */
    public static final DataFlavor queueTaskFlavor = new DataFlavor(QUEUE_TASK_MIME_TYPE, "QueueTask");
    public static final DataFlavor[] flavors = {queueTaskFlavor};

    /**
     * The <code>QueueTask</code> object which gets transferred.
     */
    protected final QueueTask task;

    /**
     * Constructor. Creates a transferable object with the given task as payload.
     *
     * @param task The <code>QueueTask</code> to transfer.
     */
    public TaskTransferer(final QueueTask task) {
        this.task = task;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
        return task;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return queueTaskFlavor.equals(flavor);
    }
}
