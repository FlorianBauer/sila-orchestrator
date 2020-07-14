package de.fau.clients.orchestrator.tasks;

/**
 * The execution policy which defines the proceeding behavior of the queue run-through after each
 * task.
 */
public enum ExecPolicy {
    /**
     * Queue stops after task finished with an error.
     */
    HALT_AFTER_ERROR,
    /**
     * Queue proceeds further with the next task even if the task finished with an error.
     */
    PROCEED_AFTER_ERROR
}
