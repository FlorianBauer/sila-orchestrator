package de.fau.clients.orchestrator.ctx;

import de.fau.clients.orchestrator.utils.VersionNumber;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
public class FeatureContext implements FullyQualifiedIdentifieable, Comparable<FeatureContext> {

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

    public String getDisplayName() {
        return feature.getDisplayName();
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

    public List<CommandContext> getCommandCtxSortedList() {
        final ArrayList<CommandContext> sortedList = new ArrayList<>(commandMap.values());
        Collections.sort(sortedList);
        return sortedList;
    }

    public List<PropertyContext> getPropertyCtxSortedList() {
        final ArrayList<PropertyContext> sortedList = new ArrayList<>(propertyMap.values());
        Collections.sort(sortedList);
        return sortedList;
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

    /**
     * Comparator function for sorting. The first criteria for ordering is done by the (non-) core
     * feature category, the second is the lexicographical order of the display name.
     *
     * @param other The other `FeatureContext` to compare against.
     * @return A negative integer on an lower rank, zero on equality and a positive integer on an
     * higher rank as the compared object.
     */
    @Override
    public int compareTo(final FeatureContext other) {
        int selfRank = this.isCoreFeature ? 1 : -1;
        int otherRank = other.isCoreFeature ? 1 : -1;

        if (selfRank != otherRank) {
            // Fist, sort after core features (to place core features always at the end) ...
            return selfRank - otherRank;
        }
        // ... then sort after the display name.
        return this.getDisplayName().compareTo(other.getDisplayName());
    }

    @Override
    public String toString() {
        return getFullyQualifiedIdentifier();
    }
}
