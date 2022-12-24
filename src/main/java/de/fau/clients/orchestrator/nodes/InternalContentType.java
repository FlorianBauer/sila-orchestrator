package de.fau.clients.orchestrator.nodes;

/**
 * Enum to internally distinguish between (un-)supported MIME types.
 */
public enum InternalContentType {
    /**
     * No support for the given content type.
     */
    UNSUPPORTED,
    /**
     * Unknown content type.
     */
    UNKNOWN,
    /**
     * Supported image type.
     */
    IMAGE,
    /**
     * Supported text type.
     */
    TEXT,
    TEXT_XML,
}
