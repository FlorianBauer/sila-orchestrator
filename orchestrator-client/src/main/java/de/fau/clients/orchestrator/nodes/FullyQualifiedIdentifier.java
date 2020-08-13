package de.fau.clients.orchestrator.nodes;

/**
 * List of valid <code>FullyQualifiedIdentifier</code> constraint stings according to the SiLA 2
 * standard.
 */
public enum FullyQualifiedIdentifier {
    FEATURE_IDENTIFIER("FeatureIdentifier"),
    COMMAND_IDENTIFIER("CommandIdentifier"),
    COMMAND_PARAMETER_IDENTIFIER("CommandParameterIdentifier"),
    COMMAND_RESPONSE_IDENTIFIER("CommandResponseIdentifier"),
    INTERMEDIATE_COMMAND_RESPONSEIDENTIFIER("IntermediateCommandResponseIdentifier"),
    DEFINED_EXECUTION_ERROR_IDENTIFIER("DefinedExecutionErrorIdentifier"),
    PROPERTY_IDENTIFIER("PropertyIdentifier"),
    TYPE_IDENTIFIER("TypeIdentifier"),
    METADATA_IDENTIFIER("MetadataIdentifier");

    private final String identifier;

    FullyQualifiedIdentifier(final String id) {
        this.identifier = id;
    }

    @Override
    public String toString() {
        return identifier;
    }
}
