package de.fau.clients.orchestrator.feature_explorer;

import java.util.HashMap;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import sila_java.library.core.models.DataTypeType;
import sila_java.library.core.models.Feature;
import sila_java.library.core.models.SiLAElement;

@Slf4j
public final class TypeDefLut {

    private final HashMap<String, DataTypeType> typeDefElemMap = new HashMap<>();

    public TypeDefLut(@NonNull final Feature feature) {
        if (feature.getDataTypeDefinition() != null && !feature.getDataTypeDefinition().isEmpty()) {
            for (final SiLAElement elem : feature.getDataTypeDefinition()) {
                typeDefElemMap.put(elem.getIdentifier(), elem.getDataType());
            }
        }
    }

    public DataTypeType getElement(final String typeIdentifier) {
        return typeDefElemMap.get(typeIdentifier);
    }
}
