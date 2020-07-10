package de.fau.clients.orchestrator.tasks;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import de.fau.clients.orchestrator.queue.TaskQueueTable;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import sila_java.library.manager.models.Server;

/**
 * Class responsible for importing and exporting the task-queue from/to a JSON-file.
 */
@Slf4j
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
     * Creates a <code>TaskQueueData</code> object and populates it with the task-model data for
     * serialization in JSON.
     *
     * @param queue The task queue to extract the data from.
     * @return A populated <code>TaskQueueData</code> object for JSON serialization.
     */
    public static TaskQueueData createFromTaskQueue(final TaskQueueTable queue) {
        final int rows = queue.getRowCount();
        final TaskQueueData data = new TaskQueueData();
        data.tasks = new ArrayList<>(rows);
        int taskId;
        QueueTask tableEntry;
        for (int i = 0; i < rows; i++) {
            taskId = queue.getTaskIdFromRow(i);
            tableEntry = queue.getTaskFromRow(i);
            data.tasks.add(new TaskEntry(taskId, tableEntry.getCurrentTaskModel()));
        }
        return data;
    }

    /**
     * Imports the data (tasks) hold by this instance into the given task queue.
     *
     * @param queue The task queue to import the data.
     * @param serverMap A current map with available server.
     */
    public void importToTaskQueue(final TaskQueueTable queue, final Map<UUID, Server> serverMap) {
        for (final TaskEntry entry : this.tasks) {
            final TaskModel taskModel = entry.getTaskModel();
            if (taskModel instanceof CommandTaskModel) {
                final CommandTaskModel ctm = (CommandTaskModel) taskModel;
                ctm.importFromIdentifiers(serverMap);
                queue.addCommandTaskWithId(entry.taskId, new CommandTask(ctm));
            } else if (taskModel instanceof DelayTaskModel) {
                final DelayTaskModel dtm = (DelayTaskModel) taskModel;
                queue.addTaskWithId(entry.taskId, new DelayTask(dtm));
            } else {
                log.warn("Unknow TaskModel instance found. Task import omitted.");
            }
        }
        queue.showColumn(TaskQueueTable.COLUMN_SERVER_UUID_IDX);
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
