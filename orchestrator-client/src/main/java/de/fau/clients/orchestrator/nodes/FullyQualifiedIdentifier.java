package de.fau.clients.orchestrator.nodes;

/**
 * List of all valid <code>FullyQualifiedIdentifier</code> types according to the SiLA 2 standard.
 */
public enum FullyQualifiedIdentifier {
    FEATURE_IDENTIFIER("FeatureIdentifier", 4),
    COMMAND_IDENTIFIER("CommandIdentifier", 6),
    COMMAND_PARAMETER_IDENTIFIER("CommandParameterIdentifier", 8),
    COMMAND_RESPONSE_IDENTIFIER("CommandResponseIdentifier", 8),
    INTERMEDIATE_COMMAND_RESPONSEIDENTIFIER("IntermediateCommandResponseIdentifier", 8),
    DEFINED_EXECUTION_ERROR_IDENTIFIER("DefinedExecutionErrorIdentifier", 6),
    PROPERTY_IDENTIFIER("PropertyIdentifier", 6),
    TYPE_IDENTIFIER("TypeIdentifier", 6),
    METADATA_IDENTIFIER("MetadataIdentifier", 6);

    private final String identifier;
    private final int sectionCount;

    FullyQualifiedIdentifier(final String id, final int sections) {
        this.identifier = id;
        this.sectionCount = sections;
    }

    public static FullyQualifiedIdentifier fromString(final String identifier) {
        for (FullyQualifiedIdentifier b : FullyQualifiedIdentifier.values()) {
            if (b.identifier.equalsIgnoreCase(identifier)) {
                return b;
            }
        }
        throw new RuntimeException("Invalid enum identifier " + identifier);
    }

    /**
     * Gets the number of sections in the Fully Qualified Identifier URI string.
     *
     * For example: A <code>FeatureIdentifier</code> URI string like
     * <code>org.silastandard/core/SiLAService/v1</code> consists of 4 parts. A
     * <code>CommandIdentifier</code> URI string like
     * <code>org.silastandard/core/SiLAService/v1/Command/GetFeatureDefinition</code> consists of 6
     * parts.
     *
     * @return The number of sections in the Fully Qualified Identifier URI string.
     */
    public int getSectionCount() {
        return this.sectionCount;
    }

    @Override
    public String toString() {
        return this.identifier;
    }
}
