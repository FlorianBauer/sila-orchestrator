package de.fau.clients.orchestrator;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public abstract class QueueTask implements Runnable {

    public static final ImageIcon EXECUTE_ICON = new ImageIcon("src/main/resources/icons/execute.png");
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
    protected TaskState state = TaskState.NEUTRAL;

    /**
     * Gets the parameters of this task as JSON string. The set content is defined by the concrete
     * task implementation.
     *
     * @return The parameters as JSON string.
     */
    abstract public String getTaskParamsAsJson();

    /**
     * Gets a JPanel for the parameter-view. This mechanism should be used to populate a panel with
     * controls, which allows the user to set the parameters* of an task. A returned
     * <code>null</code> value sets the parameter-view empty.
     *
     * (* The correct term would be "argument" but "parameter" is used to stay coherent with the
     * SiLA-standard.)
     *
     * @return The panel to set task parameters or <code>null</code>.
     */
    public JPanel getPanel() {
        return null;
    }

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
        return state;
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
