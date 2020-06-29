package de.fau.clients.orchestrator.tasks;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The data model for delay-tasks.
 *
 * @see DelayTask
 */
public class DelayTaskModel extends TaskModel {

    /**
     * The delay. Initial value is 1 sec.
     */
    private long delayInMillisec = 1000L;

    public DelayTaskModel() {
    }

    public DelayTaskModel(long delayInMillisec) {
        if (delayInMillisec < 0) {
            throw new IllegalArgumentException("value must not be negative");
        }
        this.delayInMillisec = delayInMillisec;
    }

    public long getDelayInMillisec() {
        return delayInMillisec;
    }

    public void setDelayInMillisec(long delayInMillisec) {
        if (delayInMillisec < 0) {
            throw new IllegalArgumentException("value must not be negative");
        }
        this.delayInMillisec = delayInMillisec;
    }

    @JsonIgnore
    public void setDelayFromMinSecMilli(int min, int sec, int milli) {
        setDelayInMillisec(min * 60L * 1000L + sec * 1000L + milli);
    }

    @JsonIgnore
    public int[] getDelayAsMinSecMilli() {
        int min = (int) delayInMillisec / (60 * 1000);
        int sec = (int) (delayInMillisec / 1000) % 60;
        int milli = (int) delayInMillisec % 1000;
        return new int[]{min, sec, milli};
    }
}
