package de.fau.clients.orchestrator.tasks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fau.clients.orchestrator.feature_explorer.TypeDefLut;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import sila_java.library.core.models.Feature;
import sila_java.library.manager.models.Server;

/**
 *
 */
@Slf4j
@JsonPropertyOrder({"serverUuid", "featureId", "commandId", "commandParams"})
public class CommandTaskModel {

    static private final ObjectMapper mapper = new ObjectMapper();
    private boolean isValid = false;
    private UUID serverUuid;
    private final String featureId;
    private TypeDefLut typeDefs = null;
    private final String commandId;
    private Feature.Command command = null;
    private String cmdParamsAsString = "";
    private JsonNode cmdParamsAsJsonNode = null;

    public CommandTaskModel(
            final UUID serverUuid,
            final TypeDefLut typeDefs,
            final Feature.Command command) {
        this.serverUuid = serverUuid;
        this.typeDefs = typeDefs;
        this.command = command;
        this.featureId = this.typeDefs.getFeatureId();
        this.commandId = this.command.getIdentifier();
        this.isValid = true;
    }

    public CommandTaskModel(
            final UUID serverUuid,
            final TypeDefLut typeDefs,
            final Feature.Command command,
            final String commandParams) {
        this(serverUuid, typeDefs, command);
        this.cmdParamsAsString = commandParams;
        this.cmdParamsAsJsonNode = mapper.valueToTree(commandParams);
    }

    @JsonCreator
    public CommandTaskModel(
            @JsonProperty("serverUuid") final UUID serverUuid,
            @JsonProperty("featureId") final String featureId,
            @JsonProperty("commandId") final String commandId) {
        this.serverUuid = serverUuid;
        this.featureId = featureId;
        this.commandId = commandId;
        this.isValid = false;
    }

    public boolean importFromIdentifiers(final Map<UUID, Server> serverMap) {
        if (serverMap.isEmpty()) {
            log.warn("No server available.");
            this.isValid = false;
            return false;
        } else if (!serverMap.containsKey(serverUuid)) {
            log.warn("Server with UUID " + serverUuid.toString() + " not found.");
            this.isValid = false;
            return false;
        }

        final List<Feature> featureList = serverMap.get(serverUuid).getFeatures();
        for (final Feature feat : featureList) {
            if (feat.getIdentifier().equalsIgnoreCase(featureId)) {
                final List<Feature.Command> commandList = feat.getCommand();
                for (final Feature.Command cmd : commandList) {
                    if (cmd.getIdentifier().equalsIgnoreCase(commandId)) {
                        this.typeDefs = new TypeDefLut(feat);
                        this.command = cmd;
                        this.isValid = true;
                        return true;
                    }
                }
            }
        }
        log.warn("Feature not found on server.");
        this.isValid = false;
        return false;
    }

    @JsonIgnore
    public boolean isValid() {
        return isValid;
    }

    @JsonGetter("serverUuid")
    public String getServerUuidAsString() {
        return serverUuid.toString();
    }

    @JsonIgnore
    public UUID getServerUuid() {
        return serverUuid;
    }

    @JsonIgnore
    public Feature.Command getCommand() {
        return command;
    }

    public String getFeatureId() {
        return featureId;
    }

    public String getCommandId() {
        return commandId;
    }

    @JsonRawValue
    public String getCommandParams() {
        return cmdParamsAsString;
    }

    @JsonIgnore
    public JsonNode getCommandParamsAsJsonNode() {
        return cmdParamsAsJsonNode;
    }

    @JsonSetter
    public void setCommandParams(final Map<String, Object> jsonMap) {
        this.cmdParamsAsJsonNode = mapper.valueToTree(jsonMap);
    }

    @JsonIgnore
    public void setCommandParamsFromString(final String cmdParams) {
        this.cmdParamsAsString = cmdParams;
    }

    @Override
    public String toString() {
        return "(" + serverUuid + ", "
                + featureId + ", "
                + commandId + ", "
                + cmdParamsAsJsonNode + ")";
    }

    @JsonIgnore
    TypeDefLut getTypeDefs() {
        return this.typeDefs;
    }
}
