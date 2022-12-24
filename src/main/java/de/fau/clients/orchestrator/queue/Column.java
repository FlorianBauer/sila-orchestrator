package de.fau.clients.orchestrator.queue;

/**
 * Enum defining the table columns of the queue. The <code>ordinal()</code> function can be used to
 * query the index of an column (e.g. <code>int idx = Column.TASK_ID.ordinal(); // = 1 </code>).
 */
public enum Column {
    ROW_NR("Nr.", true),
    TASK_ID("ID"),
    CONNECTION_STATUS("Connection"),
    TASK_INSTANCE("Task"),
    SERVER_UUID("Server UUID", true),
    EXEC_POLICY("Policy"),
    STATE("State"),
    START_TIME("Start Time", true),
    END_TIME("End Time", true),
    DURATION("Duration"),
    RESULT("Result");

    public final String title;
    public final boolean isHiddenOnDefault;

    private Column(final String columnTitle) {
        this.title = columnTitle;
        this.isHiddenOnDefault = false;
    }

    private Column(final String columnTitle, boolean isHiddenOnDefault) {
        this.title = columnTitle;
        this.isHiddenOnDefault = isHiddenOnDefault;
    }

    @Override
    public String toString() {
        return this.title;
    }

    /**
     * The size of columns.
     *
     * @return The total number of columns.
     */
    public static int size() {
        return Column.values().length;
    }
}
