package de.fau.clients.orchestrator.file_loader;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;

/**
 *
 */
@JsonPropertyOrder({"serverUuid", "featureId", "commandId", "commandParams"})
public class CommandEntry {

    static private final ObjectMapper mapper = new ObjectMapper();
    private final UUID serverUuid;
    private final String featureId;
    private final String commandId;
    private String commandParams = "";
    @JsonIgnore
    private JsonNode commandNode;

    @JsonCreator
    public CommandEntry(
            @JsonProperty("serverUuid") String serverUuid,
            @JsonProperty("featureId") String featureId,
            @JsonProperty("commandId") String commandId) {
        this.serverUuid = UUID.fromString(serverUuid);
        this.featureId = featureId;
        this.commandId = commandId;
    }

    public CommandEntry(
            String serverUuid,
            String featureId,
            String commandId,
            String commandParams) {
        this.serverUuid = UUID.fromString(serverUuid);
        this.featureId = featureId;
        this.commandId = commandId;
        this.commandParams = commandParams;
        this.commandNode = mapper.valueToTree(commandParams);
    }

    @JsonGetter("serverUuid")
    public String getServerUuidAsString() {
        return serverUuid.toString();
    }

    @JsonIgnore
    public UUID getServerUuid() {
        return serverUuid;
    }

    public String getFeatureId() {
        return featureId;
    }

    public String getCommandId() {
        return commandId;
    }

    @JsonRawValue
    public String getCommandParams() {
        return commandParams;
    }

    @JsonIgnore
    public JsonNode getCommandParamsAsJsonNode() {
        return commandNode;
    }

    @JsonSetter
    public void setCommandParams(Map<String, Object> jsonMap) {
        this.commandNode = mapper.valueToTree(jsonMap);
    }

    @Override
    public String toString() {
        return "(" + serverUuid + ", " + featureId + ", " + commandId + ", " + commandNode + ")";
    }
}
