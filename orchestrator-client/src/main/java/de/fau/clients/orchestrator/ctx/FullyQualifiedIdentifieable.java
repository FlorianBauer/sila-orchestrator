package de.fau.clients.orchestrator.ctx;

/**
 * Interface to for implementing a Fully Qualified Identifier as required by the SiLA standard.
 */
public interface FullyQualifiedIdentifieable {

    /**
     * Gets the Fully Qualified Identifier
     *
     * @return The FQI as string.
     */
    String getFullyQualifiedIdentifier();
}
