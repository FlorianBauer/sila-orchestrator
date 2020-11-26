package de.fau.clients.orchestrator.utils;

import javax.swing.ImageIcon;

/**
 * The central place for all available icons.
 *
 * Usage example: <code>new JLabel iconLabel(IconProvider.DOCUMENT_MISSING.getIcon());</code>
 */
public enum IconProvider {
    ADD_ENTRY(new ImageIcon(IconProvider.class.getResource("/icons/add-entry.png"))),
    APPLICATION_EXIT(new ImageIcon(IconProvider.class.getResource("/icons/application-exit-16px.png"))),
    COMMAND(new ImageIcon(IconProvider.class.getResource("/icons/command.png"))),
    DELAY_ADD(new ImageIcon(IconProvider.class.getResource("/icons/delay-add.png"))),
    DOCUMENT_MISSING(new ImageIcon(IconProvider.class.getResource("/icons/document-missing-64px.png"))),
    DOCUMENT_OPEN(new ImageIcon(IconProvider.class.getResource("/icons/document-open.png"))),
    DOCUMENT_OPEN_16PX(new ImageIcon(IconProvider.class.getResource("/icons/document-open-16px.png"))),
    DOCUMENT_SAVE(new ImageIcon(IconProvider.class.getResource("/icons/document-save.png"))),
    DOCUMENT_SAVE_16PX(new ImageIcon(IconProvider.class.getResource("/icons/document-save-16px.png"))),
    EXEC_ADD(new ImageIcon(IconProvider.class.getResource("/icons/exec-add.png"))),
    EXECUTE(new ImageIcon(IconProvider.class.getResource("/icons/execute.png"))),
    EXECUTE_16PX(new ImageIcon(IconProvider.class.getResource("/icons/execute-16px.png"))),
    LIST_ADD(new ImageIcon(IconProvider.class.getResource("/icons/list-add.png"))),
    LIST_REMOVE(new ImageIcon(IconProvider.class.getResource("/icons/list-remove.png"))),
    MOVE_DOWN(new ImageIcon(IconProvider.class.getResource("/icons/move-down.png"))),
    MOVE_UP(new ImageIcon(IconProvider.class.getResource("/icons/move-up.png"))),
    NETWORK_SCAN(new ImageIcon(IconProvider.class.getResource("/icons/network-scan.png"))),
    NETWORK_SCAN_16PX(new ImageIcon(IconProvider.class.getResource("/icons/network-scan-16px.png"))),
    PROPERTY(new ImageIcon(IconProvider.class.getResource("/icons/property.png"))),
    QUEUE_CLEAR(new ImageIcon(IconProvider.class.getResource("/icons/queue-clear.png"))),
    QUEUE_CLEAR_16PX(new ImageIcon(IconProvider.class.getResource("/icons/queue-clear-16px.png"))),
    QUEUE_EXEC_START(new ImageIcon(IconProvider.class.getResource("/icons/queue-exec-start.png"))),
    QUEUE_EXEC_START_16PX(new ImageIcon(IconProvider.class.getResource("/icons/queue-exec-start-16px.png"))),
    QUEUE_EXEC_STOP(new ImageIcon(IconProvider.class.getResource("/icons/queue-exec-stop.png"))),
    QUEUE_EXPORT(new ImageIcon(IconProvider.class.getResource("/icons/queue-export.png"))),
    REFRESH(new ImageIcon(IconProvider.class.getResource("/icons/refresh.png"))),
    SERVER_ADD(new ImageIcon(IconProvider.class.getResource("/icons/server-add.png"))),
    SERVER_ADD_16PX(new ImageIcon(IconProvider.class.getResource("/icons/server-add-16px.png"))),
    SERVER_OFFLINE(new ImageIcon(IconProvider.class.getResource("/icons/server-offline.png"))),
    SERVER_ONLINE(new ImageIcon(IconProvider.class.getResource("/icons/server-online.png"))),
    SILA_FEATURE(new ImageIcon(IconProvider.class.getResource("/icons/sila-feature.png"))),
    SILA_FEATURE_CORE(new ImageIcon(IconProvider.class.getResource("/icons/sila-feature-core.png"))),
    SILA_ORCHESTRATOR_128PX(new ImageIcon(IconProvider.class.getResource("/icons/sila-orchestrator-128px.png"))),
    SILA_ORCHESTRATOR_16PX(new ImageIcon(IconProvider.class.getResource("/icons/sila-orchestrator-16px.png"))),
    STATUS_OK(new ImageIcon(IconProvider.class.getResource("/icons/status-ok.png"))),
    STATUS_WARNING(new ImageIcon(IconProvider.class.getResource("/icons/status-warning.png"))),
    TASK_REMOVE(new ImageIcon(IconProvider.class.getResource("/icons/task-remove.png"))),
    TASK_REMOVE_16PX(new ImageIcon(IconProvider.class.getResource("/icons/task-remove-16px.png")));

    private final ImageIcon icon;

    private IconProvider(final ImageIcon icon) {
        this.icon = icon;
    }

    public ImageIcon getIcon() {
        return icon;
    }
}
