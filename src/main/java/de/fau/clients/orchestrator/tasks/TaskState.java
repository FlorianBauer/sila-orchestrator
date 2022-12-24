package de.fau.clients.orchestrator.tasks;

import javax.swing.ImageIcon;

/**
 * Enum to describe the current task state and supply the corresponding icon.
 */
public enum TaskState {
    NEUTRAL(new ImageIcon(TaskState.class.getResource("/icons/state-neutral.png"))),
    RUNNING(new ImageIcon(TaskState.class.getResource("/icons/state-running.png"))),
    FINISHED_SUCCESS(new ImageIcon(TaskState.class.getResource("/icons/state-finished-success.png"))),
    FINISHED_ERROR(new ImageIcon(TaskState.class.getResource("/icons/state-finished-error.png")));

    private final ImageIcon icon;

    private TaskState(final ImageIcon icon) {
        this.icon = icon;
    }

    public ImageIcon getIcon() {
        return icon;
    }
}
