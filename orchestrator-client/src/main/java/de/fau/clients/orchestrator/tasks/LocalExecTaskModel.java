package de.fau.clients.orchestrator.tasks;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

/**
 * The underlying data model of an <code>LocalExecTask</code>.
 *
 * @see LocalExecTask
 */
public class LocalExecTaskModel extends TaskModel {

    private String exec = "";
    private int expRetVal = 0;

    public LocalExecTaskModel() {
    }

    public LocalExecTaskModel(final String exec) {
        setExec(exec);
    }

    /**
     * Get the string containing the command and all arguments for execution.
     *
     * @return The command and argument string or a empty string if not set.
     */
    public String getExec() {
        return exec;
    }

    /**
     * Sets the command and its arguments as string for the execution. Leading and trailing
     * whitespaces getting removed.
     *
     * @param exec The command and argument string.
     */
    public void setExec(final String exec) {
        this.exec = exec.strip();
    }

    @JsonIgnore
    public List<String> getExecWithArgsAsList() {
        return List.of(exec.split(" "));
    }

    public int getExpRetVal() {
        return expRetVal;
    }

    public void setExpRetVal(int expRetVal) {
        this.expRetVal = expRetVal;
    }
}
