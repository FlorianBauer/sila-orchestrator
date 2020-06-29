package de.fau.clients.orchestrator.tasks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class to wrap an task-entry from the queue into a format for de-/serialization.
 */
@JsonPropertyOrder({"taskId", "taskModel"})
public final class TaskEntry {

    public int taskId;
    public final TaskModel taskModel;

    @JsonCreator
    public TaskEntry(
            @JsonProperty("taskId") int taskId,
            @JsonProperty("taskModel") final TaskModel taskModel) {
        this.taskId = taskId;
        this.taskModel = taskModel;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int id) {
        this.taskId = id;
    }

    public TaskModel getTaskModel() {
        return taskModel;
    }
}
