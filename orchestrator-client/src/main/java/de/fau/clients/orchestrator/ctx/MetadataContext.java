package de.fau.clients.orchestrator.ctx;

import lombok.NonNull;
import sila_java.library.core.models.Feature;

public class MetadataContext implements FullyQualifiedIdentifieable {

    private final FeatureContext featureCtx;
    private final Feature.Metadata metadata;

    protected MetadataContext(
            @NonNull final FeatureContext featureCtx,
            @NonNull final Feature.Metadata metadata
    ) {
        this.featureCtx = featureCtx;
        this.metadata = metadata;
    }

    public FeatureContext getFeatureCtx() {
        return featureCtx;
    }

    public Feature.Metadata getMetadata() {
        return metadata;
    }

    @Override
    public String getFullyQualifiedIdentifier() {
        return featureCtx.getFullyQualifiedIdentifier() + "/Metadata/" + metadata.getIdentifier();
    }
}
