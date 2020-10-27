package de.fau.clients.orchestrator.queue;

import de.fau.clients.orchestrator.tasks.CommandTask;
import de.fau.clients.orchestrator.tasks.ExecPolicy;
import de.fau.clients.orchestrator.tasks.QueueTask;
import de.fau.clients.orchestrator.tasks.TaskState;
import java.beans.PropertyChangeEvent;
import javax.swing.Icon;
import javax.swing.table.DefaultTableModel;
import lombok.extern.slf4j.Slf4j;

/**
 * This class represents the underlying data-model of the <code>TaskQueueTable</code> in the GUI.
 */
@Slf4j
@SuppressWarnings("serial")
class TaskQueueTableModel extends DefaultTableModel {

    /**
     * Adds the given command task at the end of the table and registers a change listener on the
     * state property.
     *
     * @param taskId The task ID to use for this entry.
     * @param cmdTask The command task to add.
     * @param policy The execution policy to add or <code>null</code> for default value
     * <code>ExecPolicy.HALT_AFTER_ERROR</code>.
     *
     * @see ExecPolicy
     */
    protected void addCommandTask(
            int taskId,
            final CommandTask cmdTask,
            final ExecPolicy policy
    ) {
        final ExecPolicy pol = (policy != null) ? policy : ExecPolicy.HALT_AFTER_ERROR;
        addRow(new Object[]{
            taskId,
            cmdTask,
            cmdTask.getConnectionStatus().getIcon(),
            cmdTask.getServerUuid(),
            cmdTask.getState().getIcon(),
            cmdTask.getStartTimeStamp(),
            cmdTask.getEndTimeStamp(),
            cmdTask.getDuration(),
            pol,
            cmdTask.getLastExecResult()});
        addStateListener(cmdTask);
    }

    /**
     * Inserts a command task at the given row index of the table and registers a change listener on
     * the state property.
     *
     * @param index The row index of the table where to insert the task.
     * @param taskId The task ID to use for this entry.
     * @param cmdTask The command task to add.
     * @param policy The execution policy to add or <code>null</code> for default value
     * <code>ExecPolicy.HALT_AFTER_ERROR</code>.
     *
     * @see ExecPolicy
     * @see #addTask()
     * @see #addCommandTask()
     */
    protected void insertCommandTask(
            int index,
            int taskId,
            final CommandTask cmdTask,
            final ExecPolicy policy
    ) {
        final ExecPolicy pol = (policy != null) ? policy : ExecPolicy.HALT_AFTER_ERROR;
        insertRow(index,
                new Object[]{
                    taskId,
                    cmdTask,
                    cmdTask.getConnectionStatus().getIcon(),
                    cmdTask.getServerUuid(),
                    cmdTask.getState().getIcon(),
                    cmdTask.getStartTimeStamp(),
                    cmdTask.getEndTimeStamp(),
                    cmdTask.getDuration(),
                    pol,
                    cmdTask.getLastExecResult()});
        addStateListener(cmdTask);
    }

    protected void addTask(
            int taskId,
            final QueueTask task,
            final ExecPolicy policy
    ) {
        final ExecPolicy pol = (policy != null) ? policy : ExecPolicy.HALT_AFTER_ERROR;
        addRow(new Object[]{
            taskId,
            task,
            task.getConnectionStatus().getIcon(),
            "",
            task.getState().getIcon(),
            task.getStartTimeStamp(),
            task.getEndTimeStamp(),
            task.getDuration(),
            pol,
            task.getLastExecResult()});
        addStateListener(task);
    }

    protected void insertTask(
            int index,
            int taskId,
            final QueueTask task,
            final ExecPolicy policy
    ) {
        final ExecPolicy pol = (policy != null) ? policy : ExecPolicy.HALT_AFTER_ERROR;
        insertRow(index,
                new Object[]{
                    taskId,
                    task,
                    task.getConnectionStatus().getIcon(),
                    "",
                    task.getState().getIcon(),
                    task.getStartTimeStamp(),
                    task.getEndTimeStamp(),
                    task.getDuration(),
                    pol,
                    task.getLastExecResult()});
        addStateListener(task);
    }

    protected void resetTaskStates() {
        for (int i = 0; i < getRowCount(); i++) {
            setValueAt(TaskState.NEUTRAL.getIcon(), i, TaskQueueTable.COLUMN_STATE_IDX);
            setValueAt("-", i, TaskQueueTable.COLUMN_DURATION_IDX);
            setValueAt("-", i, TaskQueueTable.COLUMN_END_TIME_IDX);
            setValueAt("-", i, TaskQueueTable.COLUMN_START_TIME_IDX);
            setValueAt("", i, TaskQueueTable.COLUMN_RESULT_IDX);
        }
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
                        setValueAt(taskEntry.getState().getIcon(), rowIdx, TaskQueueTable.COLUMN_STATE_IDX);
                        break;
                    case FINISHED_SUCCESS:
                    case FINISHED_ERROR:
                        setValueAt(taskEntry.getState().getIcon(), rowIdx, TaskQueueTable.COLUMN_STATE_IDX);
                        setValueAt(taskEntry.getLastExecResult(), rowIdx, TaskQueueTable.COLUMN_RESULT_IDX);
                        setValueAt(taskEntry.getEndTimeStamp(), rowIdx, TaskQueueTable.COLUMN_END_TIME_IDX);
                        setValueAt(taskEntry.getDuration(), rowIdx, TaskQueueTable.COLUMN_DURATION_IDX);
                        break;
                    case NEUTRAL:
                        setValueAt(taskEntry.getState().getIcon(), rowIdx, TaskQueueTable.COLUMN_STATE_IDX);
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
            case TaskQueueTable.COLUMN_CONNECTION_STATUS_IDX:
            case TaskQueueTable.COLUMN_STATE_IDX:
                return Icon.class;
            default:
                return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        // make the task-ID and the server-UUID column editable
        switch (col) {
            case TaskQueueTable.COLUMN_TASK_ID_IDX:
            case TaskQueueTable.COLUMN_EXEC_POLICY_IDX:
                return true;
            case TaskQueueTable.COLUMN_SERVER_UUID_IDX:
                if (getValueAt(row, TaskQueueTable.COLUMN_TASK_INSTANCE_IDX) instanceof CommandTask) {
                    return true;
                }
                break;
            case TaskQueueTable.COLUMN_RESULT_IDX:
                final String result = (String) getValueAt(row, TaskQueueTable.COLUMN_RESULT_IDX);
                if (!result.isEmpty()) {
                    return true;
                }
                return false;
            default:
                break;
        }
        return false;
    }
}
