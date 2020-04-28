/**
 * Class which represents a SiLA-Command entry in the JTable of the task-queue.
 *
 * This class implements the `Runnable`-interface which allows the execution of the corresponding
 * SiLA-Command in a dedicated thread. Due to its GUI-Components however, thread-safety
 * for parallel usage is not given.
 */
package de.fau.clients.orchestrator;

import de.fau.clients.orchestrator.feature_explorer.FeatureNode;
import de.fau.clients.orchestrator.feature_explorer.TypeDefLut;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import lombok.extern.slf4j.Slf4j;
import sila_java.library.core.models.Feature;
import sila_java.library.core.models.SiLAElement;
import sila_java.library.manager.ServerManager;
import sila_java.library.manager.models.SiLACall;

@Slf4j
public class CommandTableEntry implements Runnable {

    /// Use a "ISO 8601-ish" date-time representation.
    private static final DateTimeFormatter TIME_STAMP_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS");
    private static final ImageIcon EXECUTE_ICON = new ImageIcon("src/main/resources/icons/execute.png");
    private final JPanel panel = new JPanel();
    private final JButton execBtn = new JButton("Execute", EXECUTE_ICON);
    private final UUID serverId;
    private final String featureId;
    private final TypeDefLut typeDefs;
    private final Feature.Command command;
    private final PropertyChangeSupport stateChanges = new PropertyChangeSupport(this);
    private boolean isNodeBuild = false;
    private FeatureNode featNode = null;
    private OffsetDateTime startTimeStamp = null;
    private OffsetDateTime endTimeStamp = null;
    private String execResult = "";
    private TaskState state = TaskState.NEUTRAL;

    public CommandTableEntry(
            final UUID serverId,
            final String featureId,
            final TypeDefLut typeDefs,
            final Feature.Command command) {

        this.serverId = serverId;
        this.featureId = featureId;
        this.typeDefs = typeDefs;
        this.command = command;
        this.panel.setLayout(new BoxLayout(this.panel, BoxLayout.Y_AXIS));
        this.panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        execBtn.addActionListener((ActionEvent evt) -> {
            executeCommandBtnActionPerformed();
        });
    }

    /**
     * Overwritten `toString()` function to use the SiLA display name to label this component.
     *
     * @return The SiLA display name
     */
    @Override
    public String toString() {
        return command.getDisplayName();
    }

    /**
     * Build all GUI components in the Panel to allow user-interaction with the commands parameter.
     */
    public void buildCommandPanel() {
        if (isNodeBuild == false) {
            isNodeBuild = true;

            if (featNode == null) {
                final List<SiLAElement> params = command.getParameter();
                if (params.isEmpty()) {
                    return;
                }
                featNode = new FeatureNode(typeDefs, params);
                panel.add(featNode.getComponent());
            }
            panel.add(Box.createVerticalStrut(10));
            panel.add(execBtn);
        } else {
            log.warn("Multiple calls to buildCommandPanel() are discouraged.");
        }
    }

    /**
     * Embeds the build-up Panel in the given ScrollPane and draws its contents.
     *
     * @param scrollPane The ScrollPane to embed the Panel.
     */
    public void showCommandPanel(final JScrollPane scrollPane) {
        scrollPane.setViewportView(panel);
    }

    /**
     * Returns wether the Node was previously build or not.
     *
     * @return true if the Node is already build-up, otherwise false.
     */
    public boolean isNodeBuild() {
        return isNodeBuild;
    }

    public String getStartTimeStamp() {
        if (startTimeStamp != null) {
            return startTimeStamp.format(TIME_STAMP_FORMAT);
        }
        return "-";
    }

    public String getEndTimeStamp() {
        if (endTimeStamp != null) {
            return endTimeStamp.format(TIME_STAMP_FORMAT);
        }
        return "-";
    }

    /**
     * Gets the result of the last execution. The result-value gets overwritten on each execution.
     *
     * @return The last result as String.
     */
    public String getLastExecResult() {
        return execResult;
    }

    public TaskState getState() {
        return state;
    }

    /**
     * Adds a listener which gets notified when the TaskState changes.
     *
     * @param listener The listener which gets notified when the TaskeState changes.
     */
    public void addStatusChangeListener(PropertyChangeListener listener) {
        stateChanges.addPropertyChangeListener(listener);
    }

    /**
     * The actual action which is performed on execution. The overwritten Runnable interface allows
     * the execution routine to be run in its own, dedicated thread without blocking the entire GUI.
     * Multiple instances of this routine shall not be executed at the same time, since the parallel
     * usage of the involved GUI-components is not synchronized and therefore not thread-safe.
     *
     * To start the routine in the current Thread use `this.run()`. To create a separate process use
     * `new Thread(this).start`.
     */
    @Override
    public void run() {
        execBtn.setEnabled(false);
        final SiLACall.Type callType = command.getObservable().equalsIgnoreCase("yes")
                ? SiLACall.Type.OBSERVABLE_COMMAND
                : SiLACall.Type.UNOBSERVABLE_COMMAND;

        final String jsonMsg = featNode.toJsonString();
        if (jsonMsg.isEmpty()) {
            log.warn("jsonMsg is empty. Execution was skipped.");
            execBtn.setEnabled(true);
            return;
        }
        log.info("jsonMsg: " + jsonMsg);

        startTimeStamp = OffsetDateTime.now();
        final TaskState tmpState = state;
        state = TaskState.RUNNING;
        stateChanges.firePropertyChange("taskState", tmpState, state);

        SiLACall call = new SiLACall(serverId,
                featureId,
                command.getIdentifier(),
                callType,
                jsonMsg
        );

        final TaskState oldState = state;
        try {
            execResult = ServerManager.getInstance().newCallExecutor(call).execute();
            state = TaskState.FINISHED_SUCCESS;
        } catch (RuntimeException ex) {
            log.error(ex.getMessage());
            execResult = ex.getMessage();
            state = TaskState.FINISHED_ERROR;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            execResult = ex.getMessage();
            state = TaskState.FINISHED_ERROR;
        }
        endTimeStamp = OffsetDateTime.now();
        execBtn.setEnabled(true);
        stateChanges.firePropertyChange("taskState", oldState, state);
    }

    /**
     * Action which gets performed when the "Execute"-Button in the command-panel gets triggered.
     * The actual executed routine is located in the overwritten `run()`-method and is executed in a
     * dedicated thread.
     */
    public void executeCommandBtnActionPerformed() {
        // instead of `this.run()`, start in new thread to avoid blocking the GUI
        new Thread(this).start();
    }
}
