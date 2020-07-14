package de.fau.clients.orchestrator.tasks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class to wrap an task-entry from the queue into a format for de-/serialization.
 */
@JsonPropertyOrder({"taskId", "taskModel", "taskPolicy"})
public final class TaskEntry {

    public int taskId;
    public final TaskModel taskModel;
    public ExecPolicy taskPolicy;

    @JsonCreator
    public TaskEntry(
            @JsonProperty("taskId") int taskId,
            @JsonProperty("taskModel") final TaskModel taskModel,
            @JsonProperty("taskPolicy") final ExecPolicy taskPolicy) {
        this.taskId = taskId;
        this.taskModel = taskModel;
        this.taskPolicy = taskPolicy;
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

    public ExecPolicy getTaskPolicy() {
        return taskPolicy;
    }

    public void setTaskPolicy(ExecPolicy policy) {
        this.taskPolicy = policy;
    }
}
