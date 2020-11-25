package de.fau.clients.orchestrator.ctx;

/**
 * Listener for connection states within a ServerContext. Contrary to the SiLA ServerListener
 * provided by the ServerManager, this listener is only handling the state change from/to
 * online/offline and providing a ServerContext object for a better data handling.
 *
 * @see ServerContext
 */
public interface ConnectionListener {

    /**
     * Method which gets invoked when server connection state has changed (online/offline).
     *
     * @param serverCtx The server context which was affected by the change.
     */
    void onServerConnectionChanged(final ServerContext serverCtx);
}
