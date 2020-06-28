package de.fau.clients.orchestrator.file_loader;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import de.fau.clients.orchestrator.tasks.CommandTaskModel;

/**
 *
 */
@JsonPropertyOrder({"taskId", "command"})
public class TaskEntry {

    private final int taskId;
    private final CommandTaskModel command;

    @JsonCreator
    public TaskEntry(
            @JsonProperty("taskId") int taskId,
            @JsonProperty("command") final CommandTaskModel command) {
        this.taskId = taskId;
        this.command = command;
    }

    public int getTaskId() {
        return taskId;
    }

    public CommandTaskModel getCommand() {
        return command;
    }

    @Override
    public String toString() {
        return "(" + taskId + ", " + command + ")";
    }
}
