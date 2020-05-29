package de.fau.clients.orchestrator.feature_explorer;

import javax.swing.JComponent;

/**
 * Interface for SiLA-DataType elements.
 */
public interface SilaNode {

    /**
     * Creates a deep-copy of the current node.
     *
     * @return A clone of the node.
     */
    SilaNode cloneNode();

    /**
     * Generates a JSON representation of the current node data.
     *
     * @return A JSON formatted string.
     */
    String toJsonString();

    /**
     * Gets the GUI controls for interaction with the data-model of the node. More complex node
     * types may return one container populated with various other components.
     *
     * @return The GUI component(s) representing the node.
     */
    JComponent getComponent();
}
