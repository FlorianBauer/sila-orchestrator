package de.fau.clients.orchestrator.tasks;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import de.fau.clients.orchestrator.queue.TaskQueueTable;
import de.fau.clients.orchestrator.utils.VersionNumber;
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
    public static final VersionNumber SILO_FILE_VERSION = new VersionNumber(1, 0, 0);
    private VersionNumber loadedFile = null;
    private ArrayList<TaskEntry> tasks = null;

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
        for (int i = 0; i < rows; i++) {
            data.tasks.add(new TaskEntry(queue.getTaskIdFromRow(i),
                    queue.getTaskFromRow(i).getCurrentTaskModel(),
                    queue.getTaskPolicyFromRow(i)));
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
                queue.addCommandTaskWithId(entry.taskId, new CommandTask(ctm), entry.taskPolicy);
            } else if (taskModel instanceof DelayTaskModel) {
                final DelayTaskModel dtm = (DelayTaskModel) taskModel;
                queue.addTaskWithId(entry.taskId, new DelayTask(dtm), entry.taskPolicy);
            } else if (taskModel instanceof LocalExecTaskModel) {
                final LocalExecTaskModel letm = (LocalExecTaskModel) taskModel;
                queue.addTaskWithId(entry.taskId, new LocalExecTask(letm), entry.taskPolicy);
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
        this.tasks = tasks;
    }

    @JsonGetter("siloFileVersion")
    public String getSupportedSiloFileVersion() {
        return SILO_FILE_VERSION.toString();
    }

    @JsonSetter("siloFileVersion")
    public void setSiloFileVersion(final String version) {
        loadedFile = VersionNumber.parseVersionString(version);
    }

    @JsonIgnore
    public VersionNumber getLoadedSiloFileVersion() {
        return loadedFile;
    }
}
