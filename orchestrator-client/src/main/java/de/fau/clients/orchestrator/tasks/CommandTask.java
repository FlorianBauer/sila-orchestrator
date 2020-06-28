package de.fau.clients.orchestrator.tasks;

import com.fasterxml.jackson.databind.JsonNode;
import de.fau.clients.orchestrator.feature_explorer.NodeFactory;
import de.fau.clients.orchestrator.feature_explorer.SilaNode;
import de.fau.clients.orchestrator.feature_explorer.TypeDefLut;
import java.awt.event.ActionEvent;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
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
public class CommandTask extends QueueTask {

    private final CommandTaskModel commandModel;
    private boolean isPanelBuilt = false;
    private JPanel panel = null;
    private JButton execBtn = null;
    private SilaNode cmdNode = null;

    public CommandTask(
            final UUID serverUuid,
            final TypeDefLut typeDefs,
            final Feature.Command command) {
        commandModel = new CommandTaskModel(serverUuid, typeDefs, command);
        if (!commandModel.isValid()) {
            state = TaskState.OFFLINE;
        }
    }

    public CommandTask(final CommandTaskModel commandModel) {
        this.commandModel = commandModel;
        if (!this.commandModel.isValid()) {
            state = TaskState.OFFLINE;
        }
    }

    /**
     * Gets the current <code>ComandTaskModel</code> by collecting the set parameters form the view
     * and stores them in the data-model. The purpose of this function is to update the model before
     * exporting it (e.g. into a file).
     *
     * @return The task model with the current parameters.
     * @see CommandTaskModel
     */
    public CommandTaskModel getCurrentComandTaskModel() {
        if (commandModel.isValid() && cmdNode != null) {
            commandModel.setCommandParamsFromString(cmdNode.toJsonString());
            return commandModel;
        }
        return null;
    }

    /**
     * Overwritten <code>toString()</code> function to use the SiLA identifier to label this
     * component.
     *
     * @return The SiLA display name
     */
    @Override
    public String toString() {
        return commandModel.getCommandId();
    }

    /**
     * Build all GUI components in the Panel to allow user-interaction with the commands parameter.
     *
     * @return A populated JPanel.
     */
    @Override
    public JPanel getPanel() {
        if (!isPanelBuilt) {
            if (cmdNode == null) {
                boolean wasSuccessful = buildNode();
                if (!wasSuccessful) {
                    return null;
                }
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
    private boolean buildNode() {
        if (commandModel.isValid()) {
            final List<SiLAElement> params = commandModel.getCommand().getParameter();
            if (params.isEmpty()) {
                log.warn("Parameter list for command is empty.");
                return false;
            }

            JsonNode cmdParams = commandModel.getCommandParamsAsJsonNode();
            if (cmdParams == null) {
                cmdNode = NodeFactory.createFromElements(commandModel.getTypeDefs(), params);
            } else {
                cmdNode = NodeFactory.createFromElementsWithJson(commandModel.getTypeDefs(), params, cmdParams);
            }
            return true;
        }
        return false;
    }

    /**
     * Action which gets performed when the "Execute"-Button in the command-panel gets triggered.
     * The actual executed routine is located in the overwritten <code>run()</code> method and is
     * executed in a dedicated thread.
     */
    private void executeCommandBtnActionPerformed() {
        // instead of `this.run()`, start in new thread to avoid blocking the GUI
        new Thread(this).start();
    }

    public UUID getServerUuid() {
        return commandModel.getServerUuid();
    }

    public String getFeatureId() {
        return commandModel.getFeatureId();
    }

    public String getCommandId() {
        return commandModel.getCommandId();
    }

    @Override
    public String getTaskParamsAsJson() {
        if (cmdNode == null) {
            boolean wasSuccessful = buildNode();
            if (wasSuccessful) {
                return cmdNode.toJsonString();
            }
        }
        return "";
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
        if (!commandModel.isValid()) {
            TaskState oldState = state;
            state = TaskState.OFFLINE;
            stateChanges.firePropertyChange(TASK_STATE_PROPERTY, oldState, state);
            return;
        }

        if (cmdNode == null) {
            boolean wasSuccessful = buildNode();
            if (!wasSuccessful) {
                return;
            }
        }

        if (isPanelBuilt) {
            execBtn.setEnabled(false);
        }
        final SiLACall.Type callType = commandModel.getCommand().getObservable().equalsIgnoreCase("yes")
                ? SiLACall.Type.OBSERVABLE_COMMAND
                : SiLACall.Type.UNOBSERVABLE_COMMAND;

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
        TaskState oldState = state;
        state = TaskState.RUNNING;
        stateChanges.firePropertyChange(TASK_STATE_PROPERTY, oldState, state);
        oldState = state;

        SiLACall call = new SiLACall(commandModel.getServerUuid(),
                commandModel.getFeatureId(),
                commandModel.getCommandId(),
                callType,
                jsonParams
        );

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
        stateChanges.firePropertyChange(TASK_STATE_PROPERTY, oldState, state);

        if (isPanelBuilt) {
            execBtn.setEnabled(true);
        }
    }
}