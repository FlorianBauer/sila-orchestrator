package de.fau.clients.orchestrator.tasks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fau.clients.orchestrator.nodes.TypeDefLut;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import sila_java.library.core.models.Feature;
import sila_java.library.manager.models.Server;

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

    static private final ObjectMapper mapper = new ObjectMapper();
    private boolean isValid = false;
    private UUID serverUuid;
    private final String featureId;
    private TypeDefLut typeDefs = null;
    private final String commandId;
    private Feature.Command command = null;
    private String cmdParamsAsString = null;
    private JsonNode cmdParamsAsJsonNode = null;

    public CommandTaskModel(
            final UUID serverUuid,
            final TypeDefLut typeDefs,
            final Feature.Command command
    ) {
        this.serverUuid = serverUuid;
        this.typeDefs = typeDefs;
        this.command = command;
        this.featureId = this.typeDefs.getFeatureId();
        this.commandId = this.command.getIdentifier();
        this.isValid = true;
    }

    @JsonCreator
    public CommandTaskModel(
            @JsonProperty("serverUuid") final UUID serverUuid,
            @JsonProperty("featureId") final String featureId,
            @JsonProperty("commandId") final String commandId
    ) {
        this.serverUuid = serverUuid;
        this.featureId = featureId;
        this.commandId = commandId;
        this.isValid = false;
    }

    /**
     * Tries to acquire the desired SiLA command instance by using only the given identifier.
     * Therefore a Map of currently available servers has to be provided.
     *
     * @param serverMap The Map with the currently available SiLA servers.
     * @return <code>true</code> on success otherwise <code>false</code>.
     */
    public boolean importFromIdentifiers(final Map<UUID, Server> serverMap) {
        if (serverMap.isEmpty()) {
            log.warn("No server available for " + commandId + ".");
            this.isValid = false;
            return false;
        } else if (!serverMap.containsKey(serverUuid)) {
            log.warn("Server with UUID " + serverUuid.toString() + " not found.");
            this.isValid = false;
            return false;
        }

        final Server server = serverMap.get(serverUuid);
        for (final Feature feat : server.getFeatures()) {
            if (feat.getIdentifier().equalsIgnoreCase(featureId)) {
                final List<Feature.Command> commandList = feat.getCommand();
                for (final Feature.Command cmd : commandList) {
                    if (cmd.getIdentifier().equalsIgnoreCase(commandId)) {
                        this.typeDefs = new TypeDefLut(server, feat);
                        this.command = cmd;
                        this.isValid = true;
                        return true;
                    }
                }
            }
        }
        log.warn("Feature " + featureId + " for " + commandId + " not found on server.");
        this.isValid = false;
        return false;
    }

    /**
     * Sets the server instance where this command model is assigned to. There may be more than one
     * valid server instances available for a given SiLA command or none at all. This function
     * allows to re-assign the command to a different instance and sets the <code>isValid</code>
     * field accordingly. The UUID is not altered since a server may be offline.
     *
     * @param server The server instance for this command or <code>null</code> to mark as invalid
     * (offline).
     * @see #isValid()
     * @see #setServerUuid(java.util.UUID)
     */
    @JsonIgnore
    public void setServerInstance(final Server server) {
        if (server == null) {
            log.warn("No server available for " + commandId + ".");
            this.isValid = false;
            return;
        }

        for (final Feature feat : server.getFeatures()) {
            if (feat.getIdentifier().equalsIgnoreCase(featureId)) {
                final List<Feature.Command> commandList = feat.getCommand();
                for (final Feature.Command cmd : commandList) {
                    if (cmd.getIdentifier().equalsIgnoreCase(commandId)) {
                        this.typeDefs = new TypeDefLut(server, feat);
                        this.command = cmd;
                        this.isValid = true;
                        return;
                    }
                }
            }
        }
        log.warn("Feature " + featureId + " for " + commandId + " not found on server.");
        this.isValid = false;
    }

    /**
     * Indicates if the command is currently in an valid state and ready to be executed.
     *
     * @return <code>true</code> if the command is valid or <code>false</code> otherwise.
     */
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

    public void setServerUuid(UUID serverUuid) {
        this.serverUuid = serverUuid;
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
        cmdParamsAsJsonNode = mapper.valueToTree(jsonMap);
        cmdParamsAsString = cmdParamsAsJsonNode.toString();
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
