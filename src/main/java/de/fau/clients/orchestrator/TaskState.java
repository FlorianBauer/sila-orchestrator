package de.fau.clients.orchestrator;

public enum TaskState {
    NEUTRAL,
    SCHEDULED,
    RUNNING,
    FINISHED_SUCCESS,
    FINISHED_ERROR,
    SKIPPED
}
