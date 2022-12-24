package de.fau.clients.orchestrator.tasks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * A model class holding the data for a <code>CommandTask</code>. The data of a task is separated
 * from the task itself for reasons of encapsulation and serialization.
 *
 * @see CommandTask
 * @see TaskModel
 */
@Slf4j
@Getter
@ToString
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"serverUuid", "fullyQualifiedFeatureIdentifier", "commandId", "commandParams"})
public class CommandTaskModel extends TaskModel {

    private final String fullyQualifiedFeatureIdentifier;
    private final String commandId;

    @Setter
    private UUID serverUuid;
    @Setter
    private JsonNode commandParams = null;

    /**
     *
     * @param serverUuid the Server UUID
     * @param fullyQualifiedFeatureIdentifier the Fully Qualified Feature Identifier
     * @param commandId the command identifier
     */
    @JsonCreator
    public CommandTaskModel(
            @JsonProperty("serverUuid") final UUID serverUuid,
            @JsonProperty("fullyQualifiedFeatureIdentifier") final String fullyQualifiedFeatureIdentifier,
            @JsonProperty("commandId") final String commandId
    ) {
        this.serverUuid = serverUuid;
        this.fullyQualifiedFeatureIdentifier = fullyQualifiedFeatureIdentifier;
        this.commandId = commandId;
    }

}
