package de.fau.clients.orchestrator.ctx;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.NonNull;
import sila_java.library.manager.ServerAdditionException;
import sila_java.library.manager.ServerListener;
import sila_java.library.manager.ServerManager;
import sila_java.library.manager.models.Server;

/**
 * Singleton to manage connections and server handling.
 */
public class ConnectionManager implements AutoCloseable, ServerListener {

    private final ServerManager serverManager;
    private final Map<UUID, ServerContext> serverMap = new HashMap<>();
    private final List<ConnectionListener> connectionListenerList = new CopyOnWriteArrayList<>();

    private ConnectionManager(@NonNull final ServerManager serverManager) {
        this.serverManager = serverManager;
        this.serverManager.addServerListener(this);
    }

    public static ConnectionManager getInstance() {
        return ConnectionManagerHolder.INSTANCE;
    }

    @Deprecated
    public UUID addServer(final String host, int port) throws ServerAdditionException {
        serverManager.addServer(host, port);
        for (final Server server : serverManager.getServers().values()) {
            if (server.getHost().equals(host) && server.getPort() == port) {
                final UUID serverUuid = server.getConfiguration().getUuid();
                if (!serverMap.containsKey(serverUuid)) {
                    addServerToContext(serverUuid, server);
                }
                return serverUuid;
            }
        }
        return null;
    }

    public UUID addServer(final String host, int port, String cert) throws ServerAdditionException {
        serverManager.addServer(host, port, cert);
        for (final Server server : serverManager.getServers().values()) {
            if (server.getHost().equals(host) && server.getPort() == port) {
                final UUID serverUuid = server.getConfiguration().getUuid();
                if (!serverMap.containsKey(serverUuid)) {
                    addServerToContext(serverUuid, server);
                }
                return serverUuid;
            }
        }
        return null;
    }

    private void addServerToContext(final UUID serverUuid, final Server server) {
        final ServerContext serverCtx = new ServerContext(this, server);
        serverMap.put(serverUuid, serverCtx);
        connectionListenerList.forEach(listener -> listener.onServerConnectionAdded(serverCtx));
    }

    public void reconnectServer(@NonNull final UUID serverUuid) throws ServerAdditionException {
        final ServerContext serverCtx = serverMap.get(serverUuid);
        if (serverCtx != null) {
            final Server server = serverCtx.getServer();
            serverManager.addServer(
                    server.getHost(),
                    server.getPort(),
                    server.getCertificateAuthority());
        }
    }

    public void removeServer(@NonNull final UUID serverUuid) {
        final ServerContext serverCtx = serverMap.get(serverUuid);
        if (serverCtx != null) {
            serverManager.removeServer(serverUuid);
        }
    }

    public ServerContext getServerCtx(@NonNull final UUID serverUuid) {
        return serverMap.get(serverUuid);
    }

    public Collection<ServerContext> getServerCtxList() {
        return serverMap.values();
    }

    public ServerManager getServerManager() {
        return serverManager;
    }

    public void scanNetwork() {
        serverManager.getDiscovery().scanNetwork();
    }

    public void setServerName(@NonNull final UUID serverUuid, @NonNull final String newServerName) {
        serverManager.setServerName(serverUuid, newServerName);
    }

    public void clear() {
        serverMap.clear();
        serverManager.clear();
    }

    public void addServerListener(@NonNull final ServerListener siLAServerListener) {
        serverManager.addServerListener(siLAServerListener);
    }

    public void removeServerListener(@NonNull final ServerListener siLAServerListener) {
        serverManager.removeServerListener(siLAServerListener);
    }

    public void addConnectionListener(@NonNull final ConnectionListener conListener) {
        connectionListenerList.add(conListener);
    }

    public void removeConnectionListener(@NonNull final ConnectionListener conListener) {
        connectionListenerList.remove(conListener);
    }

    @Override
    public void close() {
        serverManager.close();
    }

    @Override
    public void onServerChange(UUID uuid, Server server) {
        final ServerContext serverCtx = serverMap.get(uuid);
        if (serverCtx != null) {
            connectionListenerList.forEach(listener -> listener.onServerConnectionChanged(serverCtx));
        }
    }

    @Override
    public void onServerAdded(UUID uuid, Server server) {
        final ServerContext serverCtx = serverMap.get(uuid);
        if (serverCtx != null) {
            serverCtx.getServer().setStatus(Server.Status.ONLINE);
            connectionListenerList.forEach(listener -> listener.onServerConnectionChanged(serverCtx));
        } else {
            addServerToContext(uuid, server);
        }
    }

    @Override
    public void onServerRemoved(UUID uuid, Server server) {
        final ServerContext serverCtx = serverMap.get(uuid);
        if (serverCtx != null) {
            serverCtx.getServer().setStatus(Server.Status.OFFLINE);
            connectionListenerList.forEach(listener -> listener.onServerConnectionChanged(serverCtx));
        }
    }

    @Override
    public void onServerAdditionFail(String host, int port, String reason) {
        // todo display error
    }

    private static class ConnectionManagerHolder {

        private static final ServerManager serverManager = ServerManager.getInstance();
        private static final ConnectionManager INSTANCE = new ConnectionManager(serverManager);
    }
}
