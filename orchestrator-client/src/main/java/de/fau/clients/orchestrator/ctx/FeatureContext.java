package de.fau.clients.orchestrator.ctx;

import de.fau.clients.orchestrator.utils.VersionNumber;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.NonNull;
import sila_java.library.core.models.DataTypeType;
import sila_java.library.core.models.Feature;
import sila_java.library.core.models.SiLAElement;

/**
 * The context of a SiLA feature allowing traversal trough the corresponding members as well as the
 * lower server context level.
 */
public class FeatureContext implements FullyQualifiedIdentifieable {

    private final ServerContext serverCtx;
    private final Feature feature;
    private final boolean isCoreFeature;
    private final Map<String, CommandContext> commandMap = new HashMap<>();
    private final Map<String, PropertyContext> propertyMap = new HashMap<>();
    private final Map<String, MetadataContext> metadataMap = new HashMap<>();
    private final Map<String, DataTypeType> dataTypeTypeMap = new HashMap<>();

    protected FeatureContext(
            @NonNull final ServerContext serverCtx,
            @NonNull final Feature feature,
            boolean isCoreFeature
    ) {
        this.serverCtx = serverCtx;
        this.feature = feature;
        this.isCoreFeature = isCoreFeature;

        for (final Feature.Command cmd : feature.getCommand()) {
            commandMap.put(cmd.getIdentifier(), new CommandContext(this, cmd));
        }
        for (final Feature.Property prop : feature.getProperty()) {
            propertyMap.put(prop.getIdentifier(), new PropertyContext(this, prop));
        }
        for (final Feature.Metadata meta : feature.getMetadata()) {
            metadataMap.put(meta.getIdentifier(), new MetadataContext(this, meta));
        }
        for (final SiLAElement elem : feature.getDataTypeDefinition()) {
            dataTypeTypeMap.put(elem.getIdentifier(), elem.getDataType());
        }
    }

    public ServerContext getServerCtx() {
        return serverCtx;
    }

    public UUID getServerUuid() {
        return serverCtx.getServerUuid();
    }

    public Feature getFeature() {
        return feature;
    }

    public String getFeatureId() {
        return feature.getIdentifier();
    }

    public boolean isCoreFeature() {
        return isCoreFeature;
    }

    public CommandContext getCommandCtx(@NonNull final String commandIdentifier) {
        return commandMap.get(commandIdentifier);
    }

    public DataTypeType getElement(@NonNull final String dataTypeTypeIdentifier) {
        return dataTypeTypeMap.get(dataTypeTypeIdentifier);
    }

    public Collection<CommandContext> getCommandCtxList() {
        return commandMap.values();
    }

    public Collection<PropertyContext> getPropertyCtxList() {
        return propertyMap.values();
    }

    public Collection<MetadataContext> getMetadataCtxList() {
        return metadataMap.values();
    }

    @Override
    public String getFullyQualifiedIdentifier() {
        final VersionNumber featVer = VersionNumber.parseVersionString(feature.getFeatureVersion());
        return feature.getOriginator()
                + "/" + feature.getCategory()
                + "/" + feature.getIdentifier()
                + "/v" + featVer.getMajorNumber();
    }
}
