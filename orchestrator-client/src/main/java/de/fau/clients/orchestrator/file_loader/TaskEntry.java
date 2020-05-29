package de.fau.clients.orchestrator.file_loader;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 *
 */
@JsonPropertyOrder({"taskId", "command"})
public class TaskEntry {

    private final int taskId;
    private final CommandEntry command;

    @JsonCreator
    public TaskEntry(
            @JsonProperty("taskId") int taskId,
            @JsonProperty("command") CommandEntry command) {

        this.taskId = taskId;
        this.command = command;
    }

    public int getTaskId() {
        return taskId;
    }

    public CommandEntry getCommand() {
        return command;
    }

    @Override
    public String toString() {
        return "(" + taskId + ", " + command + ")";
    }
}
