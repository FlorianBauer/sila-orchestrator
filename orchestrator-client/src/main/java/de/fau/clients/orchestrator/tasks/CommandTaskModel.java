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
import sila_java.library.manager.ServerManager;
import sila_java.library.manager.models.Server;

/**
 *
 */
@Slf4j
@JsonPropertyOrder({"serverUuid", "featureId", "commandId", "commandParams"})
public class CommandTaskModel {

    static private final ObjectMapper mapper = new ObjectMapper();
    private UUID serverUuid;
    private TypeDefLut typeDefs;
    private Feature.Command command;
    private String cmdParamsAsString = "";
    private JsonNode cmdParamsAsJsonNode;

    public CommandTaskModel(
            final UUID serverUuid,
            final TypeDefLut typeDefs,
            final Feature.Command command) {
        this.serverUuid = serverUuid;
        this.typeDefs = typeDefs;
        this.command = command;
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
    public static CommandTaskModel importFromIdentifiers(
            @JsonProperty("serverUuid") final UUID serverUuid,
            @JsonProperty("featureId") final String featureId,
            @JsonProperty("commandId") final String commandId) {

        final Map<UUID, Server> serverMap = ServerManager.getInstance().getServers();
        if (serverMap.isEmpty()) {
            log.warn("No server available.");
            return null;
        } else if (!serverMap.containsKey(serverUuid)) {
            log.warn("Server with UUID " + serverUuid.toString() + " not found.");
            return null;
        }

        final List<Feature> featureList = serverMap.get(serverUuid).getFeatures();
        for (final Feature feat : featureList) {
            if (feat.getIdentifier().equalsIgnoreCase(featureId)) {
                final List<Feature.Command> commandList = feat.getCommand();
                for (final Feature.Command cmd : commandList) {
                    if (cmd.getIdentifier().equalsIgnoreCase(commandId)) {
                        return new CommandTaskModel(serverUuid, new TypeDefLut(feat), cmd);
                    }
                }
            }
        }
        log.warn("Feature not found on server.");
        return null;
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
        return typeDefs.getFeatureId();
    }

    public String getCommandId() {
        return command.getIdentifier();
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
                + typeDefs.getFeatureId() + ", "
                + command.getIdentifier() + ", "
                + cmdParamsAsJsonNode + ")";
    }

    @JsonIgnore
    TypeDefLut getTypeDefs() {
        return this.typeDefs;
    }
}
