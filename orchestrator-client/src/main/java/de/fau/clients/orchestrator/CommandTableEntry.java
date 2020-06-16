package de.fau.clients.orchestrator;

import com.fasterxml.jackson.databind.JsonNode;
import de.fau.clients.orchestrator.feature_explorer.NodeFactory;
import de.fau.clients.orchestrator.feature_explorer.SilaNode;
import de.fau.clients.orchestrator.feature_explorer.TypeDefLut;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.Duration;
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
import lombok.extern.slf4j.Slf4j;
import sila_java.library.core.models.Feature;
import sila_java.library.core.models.SiLAElement;
import sila_java.library.manager.ServerManager;
import sila_java.library.manager.models.SiLACall;

/**
 * Class which represents a SiLA-Command entry in the the task-queue.
 *
 * This class implements the <code>Runnable</code>-interface which allows the execution of the
 * corresponding SiLA-Command in a dedicated thread. Due to its GUI components however,
 * thread-safety for parallel usage is not given.
 */
@Slf4j
public class CommandTableEntry implements Runnable {

    /// Use a "ISO 8601-ish" date-time representation.
    private static final DateTimeFormatter TIME_STAMP_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS");
    private static final ImageIcon EXECUTE_ICON = new ImageIcon("src/main/resources/icons/execute.png");
    private JPanel panel = null;
    private JButton execBtn = null;
    private final UUID serverUuid;
    private final String featureId;
    private final TypeDefLut typeDefs;
    private final Feature.Command command;
    private final PropertyChangeSupport stateChanges = new PropertyChangeSupport(this);
    private boolean isPanelBuilt = false;
    private SilaNode cmdNode = null;
    private JsonNode cmdParams = null;
    private OffsetDateTime startTimeStamp = null;
    private OffsetDateTime endTimeStamp = null;
    private String lastExecResult = "";
    private TaskState state = TaskState.NEUTRAL;

    public CommandTableEntry(
            final UUID serverUuid,
            final String featureId,
            final TypeDefLut typeDefs,
            final Feature.Command command) {
        this.serverUuid = serverUuid;
        this.featureId = featureId;
        this.typeDefs = typeDefs;
        this.command = command;
    }

    public CommandTableEntry(
            final UUID serverId,
            final String featureId,
            final TypeDefLut typeDefs,
            final Feature.Command command,
            final JsonNode cmdParams) {
        this.serverUuid = serverId;
        this.featureId = featureId;
        this.typeDefs = typeDefs;
        this.command = command;
        this.cmdParams = cmdParams;
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
     *
     * @return A populated JPanel.
     */
    public JPanel getPanel() {
        if (!isPanelBuilt) {
            if (cmdNode == null) {
                buildNode();
            }
            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            panel.add(cmdNode.getComponent());
            panel.add(Box.createVerticalStrut(10));
            execBtn = new JButton("Execute", EXECUTE_ICON);
            execBtn.addActionListener((ActionEvent evt) -> {
                executeCommandBtnActionPerformed();
            });
            panel.add(execBtn);
            isPanelBuilt = true;
        }
        return panel;
    }

    /**
     * Builds up the <code>SilaNode</code>. This method shall only be called once an must be used
     * before proceeding any actions with the internal <code>cmdNode</code>.
     */
    private void buildNode() {
        final List<SiLAElement> params = command.getParameter();
        if (params.isEmpty()) {
            log.warn("Parameter list for command is empty.");
            return;
        }

        if (cmdParams == null) {
            cmdNode = NodeFactory.createFromElements(typeDefs, params);
        } else {
            cmdNode = NodeFactory.createFromElementsWithJson(typeDefs, params, cmdParams);
        }
    }

    public UUID getServerUuid() {
        return serverUuid;
    }

    public String getFeatureId() {
        return featureId;
    }

    public String getCommandId() {
        return command.getIdentifier();
    }

    public String getCommandParams() {
        if (cmdNode == null) {
            buildNode();
        }
        return cmdNode.toJsonString();
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
     * Gets the result of the last execution. The result-value gets overwritten on each execution.
     *
     * @return The last result as String or empty string on if no result was available.
     */
    public String getLastExecResult() {
        return lastExecResult;
    }

    public TaskState getState() {
        return state;
    }

    /**
     * Adds a Listener which gets notified when the TaskState changes.
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
        if (isPanelBuilt) {
            execBtn.setEnabled(false);
        }
        final SiLACall.Type callType = command.getObservable().equalsIgnoreCase("yes")
                ? SiLACall.Type.OBSERVABLE_COMMAND
                : SiLACall.Type.UNOBSERVABLE_COMMAND;

        if (cmdNode == null) {
            buildNode();
        }
        final String jsonParams = cmdNode.toJsonString();
        if (jsonParams.isEmpty()) {
            log.warn("jsonMsg is empty. Execution was skipped.");
            if (isPanelBuilt) {
                execBtn.setEnabled(true);
            }
            return;
        }
        log.info("Command parameters as JSON: " + jsonParams);

        startTimeStamp = OffsetDateTime.now();
        final TaskState tmpState = state;
        state = TaskState.RUNNING;
        stateChanges.firePropertyChange(TaskQueueTableModel.TASK_STATE_PROPERTY, tmpState, state);

        SiLACall call = new SiLACall(serverUuid,
                featureId,
                command.getIdentifier(),
                callType,
                jsonParams
        );

        final TaskState oldState = state;
        try {
            lastExecResult = ServerManager.getInstance().newCallExecutor(call).execute();
            state = TaskState.FINISHED_SUCCESS;
        } catch (RuntimeException ex) {
            log.error(ex.getMessage());
            lastExecResult = ex.getMessage();
            state = TaskState.FINISHED_ERROR;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            lastExecResult = ex.getMessage();
            state = TaskState.FINISHED_ERROR;
        }
        endTimeStamp = OffsetDateTime.now();
        if (isPanelBuilt) {
            execBtn.setEnabled(true);
        }
        stateChanges.firePropertyChange(TaskQueueTableModel.TASK_STATE_PROPERTY, oldState, state);
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
