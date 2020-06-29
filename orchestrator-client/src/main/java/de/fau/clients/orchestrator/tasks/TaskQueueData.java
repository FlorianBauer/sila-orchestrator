package de.fau.clients.orchestrator.tasks;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import de.fau.clients.orchestrator.TaskQueueTable;
import de.fau.clients.orchestrator.TaskQueueTableModel;
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
     * Creates a <code>TaskQueueData</code> object and populates it with the taskModel-entry data
     * for serialization in JSON.
     *
     * @param queueModel The queue model instance to extract the data from.
     * @return A populated <code>TaskQueueData</code> object for JSON serialization.
     */
    public static TaskQueueData createFromTaskQueue(final TaskQueueTableModel queueModel) {
        final int rows = queueModel.getRowCount();
        final TaskQueueData data = new TaskQueueData();
        data.tasks = new ArrayList<>(rows);
        int taskId;
        QueueTask tableEntry;
        for (int i = 0; i < rows; i++) {
            taskId = (int) queueModel.getValueAt(i, TaskQueueTable.COLUMN_TASK_ID_IDX);
            tableEntry = (QueueTask) queueModel.getValueAt(i, TaskQueueTable.COLUMN_COMMAND_IDX);
            data.tasks.add(new TaskEntry(taskId, tableEntry.getCurrentTaskModel()));
        }
        return data;
    }

    public void importToTaskQueue(final TaskQueueTableModel queue, final Map<UUID, Server> serverMap) {
        for (final TaskEntry entry : this.tasks) {
            final TaskModel taskModel = entry.getTaskModel();
            if (taskModel instanceof CommandTaskModel) {
                final CommandTaskModel ctm = (CommandTaskModel) taskModel;
                ctm.importFromIdentifiers(serverMap);
                queue.addCommandTableEntry(entry.taskId, new CommandTask(ctm));
            } else if (taskModel instanceof DelayTaskModel) {
                final DelayTaskModel dtm = (DelayTaskModel) taskModel;
                queue.addTaskEntry(entry.taskId, new DelayTask(dtm));
            } else {
                log.warn("Unknow TaskModel instance found. Task import omitted.");
            }
        }
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
