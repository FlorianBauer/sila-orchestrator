package de.fau.clients.orchestrator.queue;

import de.fau.clients.orchestrator.tasks.CommandTask;
import de.fau.clients.orchestrator.tasks.ConnectionStatus;
import de.fau.clients.orchestrator.tasks.ExecPolicy;
import de.fau.clients.orchestrator.tasks.QueueTask;
import de.fau.clients.orchestrator.tasks.TaskState;
import java.beans.PropertyChangeEvent;
import javax.swing.table.DefaultTableModel;
import lombok.extern.slf4j.Slf4j;

/**
 * This class represents the underlying data-model of the <code>TaskQueueTable</code>.
 *
 * @see TaskQueueTable
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
            getRowCount() + 1,
            taskId,
            cmdTask.getConnectionStatus(),
            cmdTask,
            cmdTask.getServerUuid(),
            pol,
            cmdTask.getState(),
            cmdTask.getStartTimeStamp(),
            cmdTask.getEndTimeStamp(),
            cmdTask.getDuration(),
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
                    index + 1,
                    taskId,
                    cmdTask.getConnectionStatus(),
                    cmdTask,
                    cmdTask.getServerUuid(),
                    pol,
                    cmdTask.getState(),
                    cmdTask.getStartTimeStamp(),
                    cmdTask.getEndTimeStamp(),
                    cmdTask.getDuration(),
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
            getRowCount() + 1,
            taskId,
            task.getConnectionStatus(),
            task,
            "",
            pol,
            task.getState(),
            task.getStartTimeStamp(),
            task.getEndTimeStamp(),
            task.getDuration(),
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
                    index + 1,
                    taskId,
                    task.getConnectionStatus(),
                    task,
                    "",
                    pol,
                    task.getState(),
                    task.getStartTimeStamp(),
                    task.getEndTimeStamp(),
                    task.getDuration(),
                    task.getLastExecResult()});
        addStateListener(task);
    }

    protected void resetTaskStates() {
        for (int i = 0; i < getRowCount(); i++) {
            setValueAt(TaskState.NEUTRAL, i, Column.STATE);
            setValueAt("-", i, Column.DURATION);
            setValueAt("-", i, Column.END_TIME);
            setValueAt("-", i, Column.START_TIME);
            setValueAt("", i, Column.RESULT);
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
                    if (getValueAt(i, Column.TASK_INSTANCE).equals(taskEntry)) {
                        rowIdx = i;
                        break;
                    }
                }

                if (rowIdx == -1) {
                    log.error("Could not find entry in table");
                    return;
                }
                setValueAt(state, rowIdx, Column.STATE);
                switch (state) {
                    case RUNNING:
                        setValueAt(taskEntry.getStartTimeStamp(), rowIdx, Column.START_TIME);
                        setValueAt(taskEntry.getState(), rowIdx, Column.STATE);
                        break;
                    case FINISHED_SUCCESS:
                    case FINISHED_ERROR:
                        setValueAt(taskEntry.getState(), rowIdx, Column.STATE);
                        setValueAt(taskEntry.getLastExecResult(), rowIdx, Column.RESULT);
                        setValueAt(taskEntry.getEndTimeStamp(), rowIdx, Column.END_TIME);
                        setValueAt(taskEntry.getDuration(), rowIdx, Column.DURATION);
                        break;
                    case NEUTRAL:
                        setValueAt(taskEntry.getState(), rowIdx, Column.STATE);
                        break;
                    default:
                        log.warn("Unhandled state change");
                }
            }
        });
    }

    @Override
    public int getColumnCount() {
        return Column.size();
    }

    @Override
    public String getColumnName(int col) {
        return Column.values()[col].title;
    }

    @Override
    public Class getColumnClass(int col) {
        switch (Column.values()[col]) {
            case ROW_NR:
            case TASK_ID:
                return Integer.class;
            case TASK_INSTANCE:
                return CommandTask.class;
            case CONNECTION_STATUS:
                return ConnectionStatus.class;
            case STATE:
                return TaskState.class;
            default:
                return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        // make the task-ID and the server-UUID column editable
        switch (Column.values()[col]) {
            case TASK_ID:
            case EXEC_POLICY:
                return true;
            case SERVER_UUID:
                if (getValueAt(row, Column.TASK_INSTANCE) instanceof CommandTask) {
                    return true;
                }
                break;
            case RESULT:
                final String result = (String) getValueAt(row, Column.RESULT);
                if (!result.isEmpty()) {
                    return true;
                }
                return false;
            default:
                break;
        }
        return false;
    }

    public Object getValueAt(int row, final Column col) {
        return getValueAt(row, col.ordinal());
    }

    public void setValueAt(final Object obj, int row, final Column col) {
        setValueAt(obj, row, col.ordinal());
    }

    /**
     * Sets the current numbering on all row entries above the given start index. Use only after a
     * row was inserted, deleted or moved.
     *
     * @param startIdx The row index to start the numbering update from.
     */
    private void setRowNumbering(int startIdx) {
        for (int i = startIdx; i < getRowCount(); i++) {
            setValueAt(i + 1, i, Column.ROW_NR);
        }
    }

    @Override
    public void fireTableRowsInserted(int firstRow, int lastRow) {
        super.fireTableRowsInserted(firstRow, lastRow);
        setRowNumbering(firstRow);
    }

    @Override
    public void fireTableRowsDeleted(int firstRow, int lastRow) {
        super.fireTableRowsDeleted(firstRow, lastRow);
        setRowNumbering(firstRow);
    }

    @Override
    public void fireTableRowsUpdated(int firstRow, int lastRow) {
        super.fireTableRowsUpdated(firstRow, lastRow);
        setRowNumbering(firstRow);
    }
}
