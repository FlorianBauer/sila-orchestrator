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
        this.exec = exec;
    }

    public String getExec() {
        return exec;
    }

    public void setExec(final String exec) {
        this.exec = exec;
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
