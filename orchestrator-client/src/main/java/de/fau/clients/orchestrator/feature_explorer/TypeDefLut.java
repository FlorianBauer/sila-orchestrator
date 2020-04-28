package de.fau.clients.orchestrator.feature_explorer;

import java.util.HashMap;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import sila_java.library.core.models.Feature;
import sila_java.library.core.models.SiLAElement;

/**
 * A look-up table for type-definitions that takes all definitions of an SiLA-Feature, creates Nodes
 * out of it and stores them in a HashMap. If the data-type gets referenced later on in other
 * commands/properties etc., the TypeIdentifier can be looked-up in the table and a copy of the
 * already generated Node can be made.
 *
 * It is not recommended to use the returned instances from this LUT directly without cloning them
 * first, since other parts of the program may also referencing/cloning from the very same node.
 */
@Slf4j
public class TypeDefLut {

    private final HashMap<String, SilaNode> typeDefMap = new HashMap<>();

    public TypeDefLut(@NonNull final Feature feature) {
        if (feature.getDataTypeDefinition() != null && !feature.getDataTypeDefinition().isEmpty()) {
            for (final SiLAElement elem : feature.getDataTypeDefinition()) {
                typeDefMap.put(
                        elem.getIdentifier(),
                        FeatureNode.createFromDataType(this, elem.getDataType())
                );
            }
        }
    }

    /**
     * Looks up a type in the Map and returns the found node. It is not recommended to use the
     * returned instances directly without cloning them first, since other parts of the program may
     * also referencing/cloning from the very same node.
     *
     * @param typeIdentifier The String to identify the type.
     * @return A node to clone from or <code>null</code>.
     */
    public SilaNode getNode(final String typeIdentifier) {
        return typeDefMap.get(typeIdentifier);
    }
}
