package de.fau.clients.orchestrator.nodes;

import java.awt.Dimension;

/**
 * The maximum Dimension definitions for different widget types.
 */
public enum MaxDim {
    TEXT_FIELD(4096),
    NUMERIC_SPINNER(160),
    DATE_TIME_SPINNER(160),
    TIMESTAMP_SPINNER(240);

    private static final int MAX_HEIGHT = 42;
    private final Dimension dim;

    private MaxDim(int maxWidth) {
        this.dim = new Dimension(maxWidth, MAX_HEIGHT);
    }

    public Dimension getDim() {
        return this.dim;
    }
}
