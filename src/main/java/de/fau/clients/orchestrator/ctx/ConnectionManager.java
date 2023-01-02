package de.fau.clients.orchestrator.ctx;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.NonNull;
import sila_java.library.manager.server_management.ServerConnectionException;
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

    /**
     * Connect to a server using a trusted certificate or through an unsecure connection if
     * {@link ServerManager#setAllowUnsecureConnection(boolean)} has been set to true. Note that
     * setting this to true is deprecated and not allowed by the SiLA Standard and should only be
     * used for test purposes.
     *
     * @param host the server host
     * @param port the server host
     * @return the server UUID if connection is successful
     * @throws ServerConnectionException if unable to connect to the server or is not a valid SiLA
     * Server
     */
    public UUID addServer(final String host, int port) throws Exception {
        // method is marked as deprecated but is not, sila_java 0.11.0 will fix this problem
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
        throw new Exception("Adding server failed for unknown reason (01).");
    }

    /**
     * Connect to a server using a untrusted (self-signed) certificate or through an unsecure
     * connection if {@link ServerManager#setAllowUnsecureConnection(boolean)} has been set to true.
     * Note that setting this to true is deprecated and not allowed by the SiLA Standard and should
     * only be used for test purposes.
     *
     * @param host the server host
     * @param port the server host
     * @param cert the server untrusted (self-signed) certificate
     * @return the server UUID if connection is successful
     * @throws ServerConnectionException if unable to connect to the server or is not a valid SiLA
     * Server
     */
    public UUID addServer(final String host, int port, String cert) throws Exception {
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
        throw new Exception("Adding server failed for unknown reason (02).");
    }

    private void addServerToContext(final UUID serverUuid, final Server server) {
        final ServerContext serverCtx = new ServerContext(this, server);
        serverMap.put(serverUuid, serverCtx);
        connectionListenerList.forEach(listener -> listener.onServerConnectionAdded(serverCtx));
    }

    public void reconnectServer(@NonNull final UUID serverUuid) throws ServerConnectionException {
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
