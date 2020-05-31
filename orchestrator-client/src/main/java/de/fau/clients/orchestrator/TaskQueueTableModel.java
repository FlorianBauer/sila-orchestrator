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
 *
 */
@Slf4j
@SuppressWarnings("serial")
public class TaskQueueTableModel extends DefaultTableModel {

    private static final ServerManager serverManager = ServerManager.getInstance();

    public void importEntry(final TaskEntry entry) {
        final UUID serverUuid = UUID.fromString(entry.getCommand().getServerId());
        final Map<UUID, Server> serverMap = serverManager.getServers();
        if (serverMap.isEmpty()) {
            log.warn("No server available.");
            return;
        } else if (!serverMap.containsKey(serverUuid)) {
            log.warn("Server with UUID " + serverUuid.toString() + " not found.");
            return;
        }

        final List<Feature> featureList = serverMap.get(serverUuid).getFeatures();
        for (final Feature feat : featureList) {
            if (feat.getIdentifier().equalsIgnoreCase(entry.getCommand().getFeatureId())) {
                final List<Command> commandList = feat.getCommand();
                for (final Command cmd : commandList) {
                    if (cmd.getIdentifier().equalsIgnoreCase(entry.getCommand().getCommandId())) {
                        CommandTableEntry tableEntry = new CommandTableEntry(serverUuid,
                                feat.getIdentifier(),
                                new TypeDefLut(feat),
                                cmd,
                                entry.getCommand().getCommandParamsAsJsonNode());

                        this.addRow(new Object[]{
                            entry.getTaskId(),
                            tableEntry,
                            tableEntry.getState(),
                            tableEntry.getStartTimeStamp(),
                            tableEntry.getLastExecResult()});
                        addListener(tableEntry);
                        log.info("Row added");
                        return;
                    }
                }
            }
        }
        log.warn("Feature not found on server.");
    }

    public void addCommandTableEntry(int taskId, final CommandTableEntry cmdEntry) {
        this.addRow(new Object[]{
            taskId,
            cmdEntry,
            cmdEntry.getState(),
            cmdEntry.getStartTimeStamp(),
            cmdEntry.getLastExecResult()
        });

        addListener(cmdEntry);
    }

    private void addListener(final CommandTableEntry cmdEntry) {
        cmdEntry.addStatusChangeListener((PropertyChangeEvent pcEvt) -> {
            if (pcEvt.getPropertyName().equals("taskState")) {
                final TaskState state = (TaskState) pcEvt.getNewValue();
                // Find the row of the changed entry. This has to be done dynamically, since 
                // the order of rows might change during runtime.
                int rowIdx = -1;
                for (int i = 0; i < this.getRowCount(); i++) {
                    if (this.getValueAt(i, TaskQueueTable.COLUMN_COMMAND_IDX).equals(cmdEntry)) {
                        rowIdx = i;
                        break;
                    }
                }

                if (rowIdx == -1) {
                    log.error("Could not find entry in table");
                    return;
                }
                this.setValueAt(state, rowIdx, TaskQueueTable.COLUMN_STATE_IDX);
                switch (state) {
                    case RUNNING:
                        this.setValueAt(cmdEntry.getStartTimeStamp(), rowIdx, TaskQueueTable.COLUMN_START_TIME_IDX);
                        break;
                    case FINISHED_SUCCESS:
                    case FINISHED_ERROR:
                        this.setValueAt(cmdEntry.getLastExecResult(), rowIdx, TaskQueueTable.COLUMN_RESULT_IDX);
                        break;
                    default:
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
        return getValueAt(0, col).getClass();
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        // make only the task-ID editable
        return (col == COLUMN_TASK_ID_IDX);
    }
}
