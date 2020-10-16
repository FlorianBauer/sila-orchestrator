package de.fau.clients.orchestrator.tasks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * A model class holding the data for a <code>CommandTask</code>. The data of a task is separated
 * from the task itself for reasons of encapsulation and serialization.
 *
 * @see CommandTask
 * @see TaskModel
 */
@Slf4j
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"serverUuid", "featureId", "commandId", "commandParams"})
public class CommandTaskModel extends TaskModel {

    private UUID serverUuid;
    private final String featureId;
    private final String commandId;
    private JsonNode commandParams = null;

    @JsonCreator
    public CommandTaskModel(
            @JsonProperty("serverUuid") final UUID serverUuid,
            @JsonProperty("featureId") final String featureId,
            @JsonProperty("commandId") final String commandId
    ) {
        this.serverUuid = serverUuid;
        this.featureId = featureId;
        this.commandId = commandId;
    }

    public UUID getServerUuid() {
        return serverUuid;
    }

    public void setServerUuid(@NonNull final UUID serverUuid) {
        this.serverUuid = serverUuid;
    }

    public String getFeatureId() {
        return featureId;
    }

    public String getCommandId() {
        return commandId;
    }

    public JsonNode getCommandParams() {
        return commandParams;
    }

    public void setCommandParams(@NonNull final JsonNode jsonNode) {
        this.commandParams = jsonNode;
    }

    @Override
    public String toString() {
        return "(" + serverUuid + ", "
                + featureId + ", "
                + commandId + ", "
                + commandParams + ")";
    }
}
