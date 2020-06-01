package de.fau.clients.orchestrator.file_loader;

import com.fasterxml.jackson.annotation.*;
import de.fau.clients.orchestrator.CommandTableEntry;
import de.fau.clients.orchestrator.TaskQueueTable;
import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;

/**
 * Class responsible to importing and exporting the task-queue from/to a JSON-file.
 */
@JsonPropertyOrder({"siloFileVersion", "tasks"})
public class TaskQueueData {

    /**
     * Save-file version identifier to allow managing compatibility with potential older or future
     * releases.
     */
    public static final String SILO_FILE_VERSION = "1.0.0";
    private ArrayList<TaskEntry> tasks = null;
    private String versionOfLoadedFile = "";

    public static TaskQueueData createFromTaskQueue(final TaskQueueTable queue) {
        TaskQueueData data = new TaskQueueData();

        final DefaultTableModel model = (DefaultTableModel) queue.getModel();
        final int rows = model.getRowCount();
        data.tasks = new ArrayList<>(rows);
        int taskId;
        CommandTableEntry tableEntry;
        CommandEntry cmdEntry;
        for (int i = 0; i < rows; i++) {
            taskId = (int) model.getValueAt(i, TaskQueueTable.COLUMN_TASK_ID_IDX);
            tableEntry = (CommandTableEntry) model.getValueAt(i, TaskQueueTable.COLUMN_COMMAND_IDX);
            cmdEntry = new CommandEntry(
                    tableEntry.getServerId(),
                    tableEntry.getFeatureId(),
                    tableEntry.getCommandId(),
                    tableEntry.getCommandParams());
            data.tasks.add(new TaskEntry(taskId, cmdEntry));
        }

        return data;
    }

    public ArrayList<TaskEntry> getTasks() {
        return tasks;
    }

    public void setTasks(ArrayList<TaskEntry> tasks) {
        if (!versionOfLoadedFile.equalsIgnoreCase(SILO_FILE_VERSION)) {
            // TODO: add version check
        }

        this.tasks = tasks;
    }

    public String getSiloFileVersion() {
        return SILO_FILE_VERSION;
    }

    public void setSiloFileVersion(String version) {
        this.versionOfLoadedFile = version;
    }
}
