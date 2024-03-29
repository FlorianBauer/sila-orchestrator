package de.fau.clients.orchestrator.ctx;

import lombok.NonNull;
import sila_java.library.core.models.Feature;

public class PropertyContext implements FullyQualifiedIdentifieable, Comparable<PropertyContext> {

    private final FeatureContext featureCtx;
    private final Feature.Property property;

    protected PropertyContext(
            @NonNull final FeatureContext featureCtx,
            @NonNull final Feature.Property property
    ) {
        this.featureCtx = featureCtx;
        this.property = property;
    }

    public FeatureContext getFeatureCtx() {
        return featureCtx;
    }

    public Feature.Property getProperty() {
        return property;
    }

    public String getDisplayName() {
        return property.getDisplayName();
    }

    @Override
    public String getFullyQualifiedIdentifier() {
        return featureCtx.getFullyQualifiedIdentifier() + "/Property/" + property.getIdentifier();
    }

    @Override
    public int compareTo(final PropertyContext other) {
        return this.getDisplayName().compareTo(other.getDisplayName());
    }

    @Override
    public String toString() {
        return getFullyQualifiedIdentifier();
    }
}
