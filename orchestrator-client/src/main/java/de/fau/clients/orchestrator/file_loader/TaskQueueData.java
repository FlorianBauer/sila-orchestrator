package de.fau.clients.orchestrator.file_loader;

import com.fasterxml.jackson.annotation.*;
import de.fau.clients.orchestrator.CommandTask;
import de.fau.clients.orchestrator.TaskQueueTable;
import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;

/**
 * Class responsible for importing and exporting the task-queue from/to a JSON-file.
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

    /**
     * Creates a <code>TaskQueueData</code> object and populates it with the task-entry data for
     * serialization in JSON.
     *
     * @param queue The queue instance to extract the data from.
     * @return A populated <code>TaskQueueData</code> object for JSON serialization.
     */
    public static TaskQueueData createFromTaskQueue(final TaskQueueTable queue) {
        final DefaultTableModel model = (DefaultTableModel) queue.getModel();
        final int rows = model.getRowCount();
        final TaskQueueData data = new TaskQueueData();
        data.tasks = new ArrayList<>(rows);
        int taskId;
        CommandTask tableEntry;
        CommandEntry cmdEntry;
        for (int i = 0; i < rows; i++) {
            taskId = (int) model.getValueAt(i, TaskQueueTable.COLUMN_TASK_ID_IDX);
            tableEntry = (CommandTask) model.getValueAt(i, TaskQueueTable.COLUMN_COMMAND_IDX);
            cmdEntry = new CommandEntry(
                    tableEntry.getServerUuid(),
                    tableEntry.getFeatureId(),
                    tableEntry.getCommandId(),
                    tableEntry.getTaskParamsAsJson());
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
