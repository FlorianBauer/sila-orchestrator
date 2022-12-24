package de.fau.clients.orchestrator.nodes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.swing.JComponent;

/**
 * Abstract base class for SiLA-DataType elements.
 */
public abstract class SilaNode {

    /**
     * The mapper used to create <code>JsonNode</code>s.
     */
    protected static final ObjectMapper jsonMapper = new ObjectMapper();

    /**
     * Creates a deep-copy of the current node.
     *
     * @return A clone of the node.
     */
    protected abstract SilaNode cloneNode();

    /**
     * Generates a JSON Node out of the current SiLA node data.
     *
     * @return A JsonNode.
     */
    public abstract JsonNode toJson();

    /**
     * Gets the GUI controls for interaction with the data-model of the node. More complex node
     * types may return one container populated with various other components.
     *
     * @return The GUI component(s) representing the node.
     */
    public abstract JComponent getComponent();

    /**
     * Generates a JSON String out of the current SiLA node data.
     *
     * @return A JSON formatted string.
     */
    public String toJsonString() {
        return toJson().toString();
    }
}
