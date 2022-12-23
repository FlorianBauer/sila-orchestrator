package de.fau.clients.orchestrator.ctx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import sila_java.library.core.models.Feature;
import sila_java.library.manager.models.Server;

@Slf4j
public class ServerContext {

    private static final int MAX_NAME_LEN = 255;
    private static final String CATEGORY_CORE = "core";
    private final ConnectionManager connectionManager;
    private final Server server;
    private final Map<String, FeatureContext> featureMap = new HashMap<>();

    public ServerContext(
            @NonNull final ConnectionManager connectionManager,
            @NonNull final Server server
    ) {
        this.connectionManager = connectionManager;
        this.server = server;
        for (final Feature feat : this.server.getFeatures()) {
            final boolean isCore = feat.getCategory().startsWith(CATEGORY_CORE);
            final FeatureContext featCtx = new FeatureContext(this, feat, isCore);
            featureMap.put(featCtx.getFullyQualifiedIdentifier(), featCtx);
        }
    }

    public Server getServer() {
        return server;
    }

    public UUID getServerUuid() {
        return server.getConfiguration().getUuid();
    }

    public boolean isOnline() {
        return (server.getStatus() == Server.Status.ONLINE);
    }

    public boolean isConnectionServerInitiated() {
        return (server.getConnectionType() == Server.ConnectionType.SERVER_INITIATED);
    }

    public FeatureContext getFeatureCtx(@NonNull final String fullyQualifiedFeatureIdentifier) {
        return featureMap.get(fullyQualifiedFeatureIdentifier);
    }

    public Collection<FeatureContext> getFeatureCtxList() {
        return featureMap.values();
    }

    public List<FeatureContext> getFeatureCtxSortedList() {
        final ArrayList<FeatureContext> sortedList = new ArrayList<>(featureMap.values());
        Collections.sort(sortedList);
        return sortedList;
    }

    public boolean isServerNameValid(final String newServerName) {
        if (newServerName.isBlank()) {
            return false;
        }

        if (newServerName.equals(server.getConfiguration().getName())) {
            return false;
        }

        if (newServerName.length() > MAX_NAME_LEN) {
            return false;
        }

        if (server.getStatus() != Server.Status.ONLINE) {
            return false;
        }
        return true;
    }

    /**
     * Changes the server name without validation checks.
     *
     * @param newServerName The new name of this server instance.
     *
     * @see #isServerNameValid
     */
    public void changeServerName(@NonNull final String newServerName) {
        try {
            connectionManager.setServerName(getServerUuid(), newServerName);
        } catch (final Exception ex) {
            log.warn("Could not set server name to '" + newServerName + "'");
        }
    }
}
