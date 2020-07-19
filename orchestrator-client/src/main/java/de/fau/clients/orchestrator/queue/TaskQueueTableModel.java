package de.fau.clients.orchestrator.queue;

import de.fau.clients.orchestrator.tasks.CommandTask;
import de.fau.clients.orchestrator.tasks.ExecPolicy;
import de.fau.clients.orchestrator.tasks.QueueTask;
import de.fau.clients.orchestrator.tasks.TaskState;
import java.beans.PropertyChangeEvent;
import javax.swing.table.DefaultTableModel;
import lombok.extern.slf4j.Slf4j;

/**
 * This class represents the underlying data-model of the <code>TaskQueueTable</code> in the GUI.
 */
@Slf4j
@SuppressWarnings("serial")
class TaskQueueTableModel extends DefaultTableModel {

    /**
     * Adds the given command entry to the table and registers a change listener on the state
     * property.
     *
     * @param taskId The task ID to use for this entry.
     * @param cmdEntry The command entry to add.
     * @param policy The execution policy to add or <code>null</code> for default value
     * <code>ExecPolicy.HALT_AFTER_ERROR</code>.
     *
     * @see ExecPolicy
     */
    protected void addCommandTableEntry(
            int taskId,
            final CommandTask cmdEntry,
            final ExecPolicy policy) {
        final ExecPolicy pol = (policy != null) ? policy : ExecPolicy.HALT_AFTER_ERROR;
        addRow(new Object[]{
            taskId,
            cmdEntry,
            cmdEntry.getState(),
            cmdEntry.getServerUuid(),
            cmdEntry.getStartTimeStamp(),
            cmdEntry.getEndTimeStamp(),
            cmdEntry.getDuration(),
            cmdEntry.getLastExecResult(),
            pol});
        addStateListener(cmdEntry);
    }

    protected void addTaskEntry(
            int taskId,
            final QueueTask taskEntry,
            final ExecPolicy policy) {
        final ExecPolicy pol = (policy != null) ? policy : ExecPolicy.HALT_AFTER_ERROR;
        addRow(new Object[]{
            taskId,
            taskEntry,
            taskEntry.getState(),
            "",
            taskEntry.getStartTimeStamp(),
            taskEntry.getEndTimeStamp(),
            taskEntry.getDuration(),
            taskEntry.getLastExecResult(),
            pol});
        addStateListener(taskEntry);
    }

    private void addStateListener(final QueueTask taskEntry) {
        taskEntry.addStatusChangeListener((PropertyChangeEvent pcEvt) -> {
            if (pcEvt.getPropertyName().equals(QueueTask.TASK_STATE_PROPERTY)) {
                final TaskState state = (TaskState) pcEvt.getNewValue();
                // Find the row of the changed entry. This has to be done dynamically, since 
                // the order of rows might change during runtime.
                int rowIdx = -1;
                for (int i = 0; i < getRowCount(); i++) {
                    if (getValueAt(i, TaskQueueTable.COLUMN_TASK_INSTANCE_IDX).equals(taskEntry)) {
                        rowIdx = i;
                        break;
                    }
                }

                if (rowIdx == -1) {
                    log.error("Could not find entry in table");
                    return;
                }
                setValueAt(state, rowIdx, TaskQueueTable.COLUMN_STATE_IDX);
                switch (state) {
                    case RUNNING:
                        setValueAt(taskEntry.getStartTimeStamp(), rowIdx, TaskQueueTable.COLUMN_START_TIME_IDX);
                        break;
                    case FINISHED_SUCCESS:
                    case FINISHED_ERROR:
                        setValueAt(taskEntry.getLastExecResult(), rowIdx, TaskQueueTable.COLUMN_RESULT_IDX);
                        setValueAt(taskEntry.getEndTimeStamp(), rowIdx, TaskQueueTable.COLUMN_END_TIME_IDX);
                        setValueAt(taskEntry.getDuration(), rowIdx, TaskQueueTable.COLUMN_DURATION_IDX);
                        break;
                    case SKIPPED:
                    case OFFLINE:
                        break;
                    default:
                        log.warn("Unhandled state change");
                }
            }
        });
    }

    @Override
    public int getColumnCount() {
        return TaskQueueTable.COLUMN_TITLES.length;
    }

    @Override
    public String getColumnName(int col) {
        return TaskQueueTable.COLUMN_TITLES[col];
    }

    @Override
    public Class getColumnClass(int col) {
        switch (col) {
            case TaskQueueTable.COLUMN_TASK_ID_IDX:
                return Integer.class;
            case TaskQueueTable.COLUMN_TASK_INSTANCE_IDX:
                return CommandTask.class;
            default:
                return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        // make the task-ID and the server-UUID column editable
        switch (col) {
            case TaskQueueTable.COLUMN_TASK_ID_IDX:
            case TaskQueueTable.COLUMN_RESULT_IDX:
            case TaskQueueTable.COLUMN_EXEC_POLICY_IDX:
                return true;
            case TaskQueueTable.COLUMN_SERVER_UUID_IDX:
                if (getValueAt(row, TaskQueueTable.COLUMN_TASK_INSTANCE_IDX) instanceof CommandTask) {
                    return true;
                }
                break;
            default:
                break;
        }

        return false;
    }
}