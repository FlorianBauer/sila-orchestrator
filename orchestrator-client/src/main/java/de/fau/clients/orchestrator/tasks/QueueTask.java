package de.fau.clients.orchestrator.tasks;

import de.fau.clients.orchestrator.Presentable;
import de.fau.clients.orchestrator.utils.IconProvider;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.ImageIcon;

/**
 * The abstract class each task has to extend to become manageable by the task-queue table.
 *
 * @see de.fau.clients.orchestrator.queue.TaskQueueTable
 */
public abstract class QueueTask implements Runnable, Presentable {

    public static final ImageIcon EXECUTE_ICON = IconProvider.EXECUTE.getIcon();
    /**
     * Identifier for signaling change events on the task state property.
     */
    public static final String TASK_STATE_PROPERTY = "taskState";
    /**
     * Use a "ISO 8601-ish" date-time representation.
     */
    protected static final DateTimeFormatter TIME_STAMP_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS");
    protected final PropertyChangeSupport stateChanges = new PropertyChangeSupport(this);
    protected OffsetDateTime startTimeStamp = null;
    protected OffsetDateTime endTimeStamp = null;
    protected String lastExecResult = "";
    protected ConnectionStatus conStatus = ConnectionStatus.NEUTRAL;
    protected TaskState taskState = TaskState.NEUTRAL;

    /**
     * Gets the current <code>TaskModel</code> by collecting the set parameters form the view and
     * storing them in the data-model. The purpose of this function is to update the model before
     * returning it (e.g. before exporting it into a file etc.).
     *
     * @return The task model with the current parameters.
     */
    abstract public TaskModel getCurrentTaskModel();

    /**
     * Gets the timestamp of the execution start. The start-time thereby should be set at the begin
     * of the concrete <code>run()</code> method like
     * <code>startTimeStamp = OffsetDateTime.now();</code>.
     *
     * @return The timestamp as String or "-" if no execution was done so far.
     */
    public String getStartTimeStamp() {
        if (startTimeStamp != null) {
            return startTimeStamp.format(TIME_STAMP_FORMAT);
        }
        return "-";
    }

    /**
     * Gets the timestamp of the execution end. The end-time thereby should be set at the end of the
     * concrete <code>run()</code> method like <code>endTimeStamp = OffsetDateTime.now();</code>.
     *
     * @return The timestamp as String or "-" if no execution was done so far.
     */
    public String getEndTimeStamp() {
        if (endTimeStamp != null) {
            return endTimeStamp.format(TIME_STAMP_FORMAT);
        }
        return "-";
    }

    /**
     * Gets the duration time of the last execution.
     *
     * @return The duration as String or "-" if no execution was done so far.
     */
    public String getDuration() {
        if (startTimeStamp != null && endTimeStamp != null) {
            final Duration dur = Duration.between(
                    startTimeStamp.toLocalDateTime(),
                    endTimeStamp.toLocalDateTime());

            return String.format("%d:%02d:%02d.%03d",
                    dur.toHoursPart(),
                    dur.toMinutesPart(),
                    dur.toSecondsPart(),
                    dur.toMillisPart());
        }
        return "-";
    }

    /**
     * Gets the result of the last execution. The result value usually gets overwritten on each
     * execution.
     *
     * @return The last result as String or an empty String if no result was available.
     */
    public String getLastExecResult() {
        return lastExecResult;
    }

    /**
     * Gets the current connection status to the corresponding server. If the task has no server
     * requirement, the default NEUTRAL state is returned.
     *
     * @return The connection status.
     */
    public ConnectionStatus getConnectionStatus() {
        return conStatus;
    }

    /**
     * Gets the current state of this task.
     *
     * The implementing <code>run()</code> method should thereby set the state accordingly like e.g. <code>
     * TaskState oldState = state;
     * state = TaskState.RUNNING;
     * stateChanges.firePropertyChange(TASK_STATE_PROPERTY, oldState, state);
     * oldState = state;
     * </code>
     *
     * @return The current state.
     * @see TaskState
     * @see #addStatusChangeListener(java.beans.PropertyChangeListener)
     */
    public TaskState getState() {
        return taskState;
    }

    /**
     * Adds a Listener which gets notified when the <code>TaskState</code> changes. Therefore the
     * signaled in the implementation of the <code>run()</code> method by firing the changed state
     * like e.g.<code>
     * stateChanges.firePropertyChange(TASK_STATE_PROPERTY, oldState, newState);
     * </code>
     *
     * @param listener The listener which gets notified when the TaskeState changes.
     */
    public void addStatusChangeListener(PropertyChangeListener listener) {
        stateChanges.addPropertyChangeListener(listener);
    }
}
