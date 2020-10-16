package de.fau.clients.orchestrator.ctx;

import lombok.NonNull;
import sila_java.library.core.models.Feature;

public class CommandContext implements FullyQualifiedIdentifieable {

    private final FeatureContext featureCtx;
    private final Feature.Command command;

    protected CommandContext(
            @NonNull final FeatureContext featureCtx,
            @NonNull final Feature.Command command
    ) {
        this.featureCtx = featureCtx;
        this.command = command;
    }

    public FeatureContext getFeatureCtx() {
        return featureCtx;
    }

    public Feature.Command getCommand() {
        return command;
    }

    @Override
    public String getFullyQualifiedIdentifier() {
        return featureCtx.getFullyQualifiedIdentifier() + "/Command/" + command.getIdentifier();
    }
}
