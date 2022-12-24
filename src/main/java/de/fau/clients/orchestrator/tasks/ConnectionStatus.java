package de.fau.clients.orchestrator.tasks;

import javax.swing.ImageIcon;

/**
 * Enum to describe the current connection status of an task and supply the corresponding icon.
 */
public enum ConnectionStatus {
    NEUTRAL(new ImageIcon(ConnectionStatus.class.getResource("/icons/task-neutral.png"))),
    OFFLINE(new ImageIcon(ConnectionStatus.class.getResource("/icons/task-offline.png"))),
    ONLINE(new ImageIcon(ConnectionStatus.class.getResource("/icons/task-online.png")));

    private final ImageIcon icon;

    private ConnectionStatus(final ImageIcon icon) {
        this.icon = icon;
    }

    public ImageIcon getIcon() {
        return icon;
    }
}
