package de.fau.clients.orchestrator;

import static de.fau.clients.orchestrator.TaskQueueTable.*;
import de.fau.clients.orchestrator.feature_explorer.TypeDefLut;
import de.fau.clients.orchestrator.file_loader.TaskEntry;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.swing.table.DefaultTableModel;
import lombok.extern.slf4j.Slf4j;
import sila_java.library.core.models.Feature;
import sila_java.library.core.models.Feature.Command;
import sila_java.library.manager.ServerManager;
import sila_java.library.manager.models.Server;

/**
 * This class represents the underlying data-model of the <code>TaskQueueTable</code> in the GUI.
 */
@Slf4j
@SuppressWarnings("serial")
public class TaskQueueTableModel extends DefaultTableModel {

    /**
     * Identifier for signaling change events on the task state property.
     */
    public static final String TASK_STATE_PROPERTY = "taskState";
    private static final ServerManager serverManager = ServerManager.getInstance();

    /**
     * Clears the entire table. The table is empty after this operation.
     */
    public void removeAllRows() {
        for (int i = getRowCount() - 1; i >= 0; i--) {
            removeRow(i);
        }
    }

    /**
     * Imports a given task entry into the table. The corresponding server has to be available to
     * successfully load the task.
     *
     * @param entry The task entry to import.
     * @return true if import was successful, otherwise false.
     */
    public boolean importTaskEntry(final TaskEntry entry) {
        final UUID serverUuid = entry.getCommand().getServerUuid();
        final Map<UUID, Server> serverMap = serverManager.getServers();
        if (serverMap.isEmpty()) {
            log.warn("No server available.");
            return false;
        } else if (!serverMap.containsKey(serverUuid)) {
            log.warn("Server with UUID " + serverUuid.toString() + " not found.");
            return false;
        }

        final List<Feature> featureList = serverMap.get(serverUuid).getFeatures();
        for (final Feature feat : featureList) {
            if (feat.getIdentifier().equalsIgnoreCase(entry.getCommand().getFeatureId())) {
                final List<Command> commandList = feat.getCommand();
                for (final Command cmd : commandList) {
                    if (cmd.getIdentifier().equalsIgnoreCase(entry.getCommand().getCommandId())) {
                        CommandTask tableEntry = new CommandTask(serverUuid,
                                feat.getIdentifier(),
                                new TypeDefLut(feat),
                                cmd,
                                entry.getCommand().getCommandParamsAsJsonNode());

                        this.addRow(new Object[]{
                            entry.getTaskId(),
                            tableEntry,
                            tableEntry.getState(),
                            tableEntry.getStartTimeStamp(),
                            tableEntry.getEndTimeStamp(),
                            tableEntry.getDuration(),
                            tableEntry.getLastExecResult(),
                            tableEntry.getServerUuid()});
                        addStateListener(tableEntry);
                        return true;
                    }
                }
            }
        }
        log.warn("Feature not found on server.");
        return false;
    }

    /**
     * Adds the given command entry to the table and registers a change listener on the state
     * property. To import task entries from outside sources, use
     * {@link #importTaskEntry(de.fau.clients.orchestrator.file_loader.TaskEntry)}.
     *
     * @param taskId The task ID to use for this entry.
     * @param cmdEntry The command entry to add.
     *
     * @see #importTaskEntry(de.fau.clients.orchestrator.file_loader.TaskEntry)}
     */
    protected void addCommandTableEntry(int taskId, final CommandTask cmdEntry) {
        addRow(new Object[]{
            taskId,
            cmdEntry,
            cmdEntry.getState(),
            cmdEntry.getStartTimeStamp(),
            cmdEntry.getEndTimeStamp(),
            cmdEntry.getDuration(),
            cmdEntry.getLastExecResult(),
            cmdEntry.getServerUuid()});
        addStateListener(cmdEntry);
    }

    protected void addTaskEntry(int taskId, final QueueTask taskEntry) {
        addRow(new Object[]{
            taskId,
            taskEntry,
            taskEntry.getState(),
            taskEntry.getStartTimeStamp(),
            taskEntry.getEndTimeStamp(),
            taskEntry.getDuration(),
            taskEntry.getLastExecResult(),
            ""});
        addStateListener(taskEntry);
    }

    private void addStateListener(final QueueTask taskEntry) {
        taskEntry.addStatusChangeListener((PropertyChangeEvent pcEvt) -> {
            if (pcEvt.getPropertyName().equals(TASK_STATE_PROPERTY)) {
                final TaskState state = (TaskState) pcEvt.getNewValue();
                // Find the row of the changed entry. This has to be done dynamically, since 
                // the order of rows might change during runtime.
                int rowIdx = -1;
                for (int i = 0; i < getRowCount(); i++) {
                    if (getValueAt(i, TaskQueueTable.COLUMN_COMMAND_IDX).equals(taskEntry)) {
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
                    default:
                        log.warn("Unhandled state change");
                }
            }
        });
    }

    @Override
    public int getColumnCount() {
        return COLUMN_TITLES.length;
    }

    @Override
    public String getColumnName(int col) {
        return COLUMN_TITLES[col];
    }

    @Override
    public Class getColumnClass(int col) {
        switch (col) {
            case TaskQueueTable.COLUMN_TASK_ID_IDX:
                return Integer.class;
            case TaskQueueTable.COLUMN_COMMAND_IDX:
                return CommandTask.class;
            default:
                return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        // make only the task-ID editable
        return (col == COLUMN_TASK_ID_IDX);
    }
}
