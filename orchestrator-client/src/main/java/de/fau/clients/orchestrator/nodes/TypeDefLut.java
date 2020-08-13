package de.fau.clients.orchestrator.nodes;

import java.util.HashMap;
import lombok.NonNull;
import sila_java.library.core.models.DataTypeType;
import sila_java.library.core.models.Feature;
import sila_java.library.core.models.SiLAElement;
import sila_java.library.manager.models.Server;

/**
 * Look-up-table for type-definitions of a given SiLA feature. This mapping is necessary since
 * various SiLA elements can reference to a data type defined outside of their own scope.
 */
public final class TypeDefLut {

    private final Server server;
    private final Feature feature;
    private final HashMap<String, DataTypeType> typeDefElemMap = new HashMap<>();

    public TypeDefLut(@NonNull final Server server, @NonNull final Feature feature) {
        this.server = server;
        this.feature = feature;
        if (feature.getDataTypeDefinition() != null && !feature.getDataTypeDefinition().isEmpty()) {
            for (final SiLAElement elem : feature.getDataTypeDefinition()) {
                typeDefElemMap.put(elem.getIdentifier(), elem.getDataType());
            }
        }
    }

    public Server getServer() {
        return server;
    }

    public String getFeatureId() {
        return feature.getIdentifier();
    }

    public Feature getFeature() {
        return feature;
    }

    public DataTypeType getElement(final String typeIdentifier) {
        return typeDefElemMap.get(typeIdentifier);
    }
}
