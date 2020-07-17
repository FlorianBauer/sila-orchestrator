package de.fau.clients.orchestrator.tasks;

public enum TaskState {
    READY,
    SCHEDULED,
    RUNNING,
    FINISHED_SUCCESS,
    FINISHED_ERROR,
    SKIPPED,
    OFFLINE
}
