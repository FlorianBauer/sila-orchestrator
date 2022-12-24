package de.fau.clients.orchestrator.tasks;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
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

    /**
     * Gets the command and argument strings as a list for the usage within the
     * <code>ProcessBuilder</code>. Sections enclosed in <code>"</code> or <code>'</code>-Symbols
     * getting escaped.
     *
     * @return A List with the command at the first element and the corresponding arguments in the
     * rest of the list.
     */
    @JsonIgnore
    public List<String> getExecWithArgsAsList() {
        final ArrayList<String> slices = new ArrayList<>();
        final String[] escParts = exec.split("[\"\']");
        for (int i = 0; i < escParts.length; i++) {
            if (i % 2 == 0) {
                slices.addAll(List.of(escParts[i].split(" ")));
            } else {
                slices.add(escParts[i]);
            }
        }
        return slices;
    }

    public int getExpRetVal() {
        return expRetVal;
    }

    public void setExpRetVal(int expRetVal) {
        this.expRetVal = expRetVal;
    }
}
