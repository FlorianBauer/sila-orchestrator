package de.fau.clients.orchestrator.ctx;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.NonNull;
import sila_java.library.manager.ServerAdditionException;
import sila_java.library.manager.ServerFinder;
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

    public UUID addServer(final String host, int port) throws ServerAdditionException {
        serverManager.addServer(host, port);
        for (final Server server : serverManager.getServers().values()) {
            if (server.getHost().equals(host) && server.getPort() == port) {
                final UUID serverUuid = server.getConfiguration().getUuid();
                if (!serverMap.containsKey(serverUuid)) {
                    final ServerContext serverCtx = new ServerContext(this, server);
                    serverMap.put(serverUuid, serverCtx);
                    connectionListenerList.forEach(listener
                            -> listener.onServerConnectionChanged(serverCtx));
                }
                return serverUuid;
            }
        }
        return null;
    }

    public void removeServer(@NonNull final UUID serverUuid) {
        final ServerContext serverCtx = serverMap.get(serverUuid);
        if (serverCtx != null) {
            serverMap.remove(serverUuid);
            serverManager.removeServer(serverUuid);
            connectionListenerList.forEach(listener
                    -> listener.onServerConnectionChanged(serverCtx));
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
        final List<Server> serverList = ServerFinder
                .filterBy(ServerFinder.Filter.status(Server.Status.ONLINE))
                .find();
        for (final Server server : serverList) {
            final UUID serverUuid = server.getConfiguration().getUuid();
            if (!serverMap.containsKey(serverUuid)) {
                final ServerContext serverCtx = new ServerContext(this, server);
                serverMap.put(serverUuid, serverCtx);
                connectionListenerList.forEach(listener
                        -> listener.onServerConnectionChanged(serverCtx));
            }
        }
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
            connectionListenerList.forEach(listener
                    -> listener.onServerConnectionChanged(serverCtx));
        }
    }

    private static class ConnectionManagerHolder {

        private static final ServerManager serverManager = ServerManager.getInstance();
        private static final ConnectionManager INSTANCE = new ConnectionManager(serverManager);
    }
}
