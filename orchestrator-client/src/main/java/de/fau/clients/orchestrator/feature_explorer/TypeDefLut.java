package de.fau.clients.orchestrator.feature_explorer;

import java.util.HashMap;
import lombok.NonNull;
import sila_java.library.core.models.DataTypeType;
import sila_java.library.core.models.Feature;
import sila_java.library.core.models.SiLAElement;

/**
 * Look-up-table for type-definitions of a given SiLA feature. This mapping is necessary since
 * various SiLA elements can reference to a data type defined outside of their own scope.
 */
public final class TypeDefLut {

    private final String featureId;
    private final HashMap<String, DataTypeType> typeDefElemMap = new HashMap<>();

    public TypeDefLut(@NonNull final Feature feature) {
        this.featureId = feature.getIdentifier();
        if (feature.getDataTypeDefinition() != null && !feature.getDataTypeDefinition().isEmpty()) {
            for (final SiLAElement elem : feature.getDataTypeDefinition()) {
                typeDefElemMap.put(elem.getIdentifier(), elem.getDataType());
            }
        }
    }

    public String getFeatureId() {
        return featureId;
    }

    public DataTypeType getElement(final String typeIdentifier) {
        return typeDefElemMap.get(typeIdentifier);
    }
}
