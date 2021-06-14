package de.fau.clients.orchestrator.tasks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fau.clients.orchestrator.ctx.CommandContext;
import de.fau.clients.orchestrator.ctx.ConnectionManager;
import de.fau.clients.orchestrator.ctx.FeatureContext;
import de.fau.clients.orchestrator.ctx.ServerContext;
import de.fau.clients.orchestrator.nodes.NodeFactory;
import de.fau.clients.orchestrator.nodes.SilaNode;
import java.awt.event.ActionEvent;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import sila_java.library.core.models.SiLAElement;
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

    private static final ConnectionManager manager = ConnectionManager.getInstance();
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private final CommandTaskModel commandModel;
    private CommandContext cmdCtx = null;
    private boolean isCommandValid = false;
    private boolean isPanelBuilt = false;
    private JPanel panel = null;
    private JButton execBtn = null;
    private boolean isNodeBuilt = false;
    private SilaNode cmdNode = null;

    public CommandTask(@NonNull final CommandTaskModel commandModel) {
        this.commandModel = commandModel;
        final ServerContext serverCtx = manager.getServerCtx(this.commandModel.getServerUuid());
        if (serverCtx == null) {
            conStatus = ConnectionStatus.OFFLINE;
            return;
        }

        isCommandValid = tryToSetServerInstance(serverCtx);
        if (isCommandValid) {
            if (serverCtx.isOnline()) {
                conStatus = ConnectionStatus.ONLINE;
                return;
            }
        }
        conStatus = ConnectionStatus.OFFLINE;
    }

    public CommandTask(@NonNull final CommandContext commandCtx) {
        this.cmdCtx = commandCtx;
        isCommandValid = true;
        final FeatureContext featCtx = commandCtx.getFeatureCtx();
        this.commandModel = new CommandTaskModel(
                featCtx.getServerUuid(),
                featCtx.getFeatureId(),
                this.cmdCtx.getCommand().getIdentifier());

        if (featCtx.getServerCtx().isOnline()) {
            conStatus = ConnectionStatus.ONLINE;
        } else {
            conStatus = ConnectionStatus.OFFLINE;
        }
    }

    /**
     * Tries to acquire the desired command context by using only the given identifier stored in the
     * command model. Therefore, a server context has to be provided. The UUID of the model itself
     * is not altered since the server may be temporarily offline.
     *
     * @param serverCtx The server context to get the command from or <code>null</code>.
     * @return <code>true</code> on success otherwise <code>false</code>.
     */
    private boolean tryToSetServerInstance(final ServerContext serverCtx) {
        if (serverCtx == null) {
            log.warn("Server with UUID " + commandModel.getServerUuid() + " not found.");
            return false;
        }

        final FeatureContext featCtx = serverCtx.getFeatureCtx(commandModel.getFeatureId());
        if (featCtx != null) {
            final CommandContext tmpCmdCtx = featCtx.getCommandCtx(commandModel.getCommandId());
            if (tmpCmdCtx != null) {
                this.cmdCtx = tmpCmdCtx;
                return true;
            }
        }
        log.warn("Feature " + commandModel.getFeatureId() + " for " + commandModel.getCommandId()
                + " not found on server.");
        return false;
    }

    /**
     * Gets the current <code>CommandTaskModel</code> by collecting the set parameters form the
     * presenter and stores them in the data-model.
     *
     * @return The task-model of the command with the current parameters.
     * @see CommandTaskModel
     */
    @Override
    public TaskModel getCurrentTaskModel() {
        if (cmdNode != null) {
            commandModel.setCommandParams(cmdNode.toJson());
        }
        return commandModel;
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
     * Build all GUI components in the Panel to allow user-interaction with the command parameters.
     *
     * @return A populated <code>JPanel</code> or <code>null</code> on error.
     */
    @Override
    public JPanel getPresenter() {
        if (!isCommandValid) {
            return null;
        }

        if (!isPanelBuilt) {
            if (!isNodeBuilt) {
                boolean wasSuccessful = buildNode();
                if (!wasSuccessful) {
                    return null;
                }
            }

            panel = new JPanel();
            panel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(cmdCtx.getCommand().getDisplayName()),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)));
            panel.setFocusCycleRoot(true);

            if (cmdNode != null) {
                final JComponent comp = cmdNode.getComponent();
                // always remove border decoration when in first layer
                comp.setBorder(BorderFactory.createEmptyBorder());
                panel.add(comp);
            }

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
     * Creates the Presenter to view the latest result within a widget component.
     *
     * @return The JComponent presenting the results or <code>null</code> on error or empty result.
     */
    public JComponent getResultPresenter() {

        if (lastExecResult.isEmpty()) {
            return null;
        }

        final JsonNode results;
        try {
            results = jsonMapper.readTree(lastExecResult);
        } catch (final JsonProcessingException ex) {
            log.error(ex.getMessage());
            return null;
        }

        if (results.isEmpty()) {
            return null;
        }

        final List<SiLAElement> params = cmdCtx.getCommand().getResponse();
        final SilaNode silaNode = NodeFactory.createFromElementsWithJson(
                cmdCtx.getFeatureCtx(),
                params,
                results,
                false);
        final JComponent comp = silaNode.getComponent();
        comp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return comp;
    }

    /**
     * Builds up the <code>SilaNode</code>. This method shall only be called once an must be used
     * before proceeding any actions with the internal <code>cmdNode</code>.
     */
    private boolean buildNode() {
        if (isCommandValid) {
            final List<SiLAElement> params = cmdCtx.getCommand().getParameter();
            if (params.isEmpty()) {
                isNodeBuilt = true;
                return true;
            }

            final JsonNode cmdParams = commandModel.getCommandParams();
            if (cmdParams == null) {
                cmdNode = NodeFactory.createFromElements(cmdCtx.getFeatureCtx(), params);
            } else {
                cmdNode = NodeFactory.createFromElementsWithJson(
                        cmdCtx.getFeatureCtx(),
                        params,
                        cmdParams,
                        true);
            }
            isNodeBuilt = true;
            return true;
        }
        log.warn("Could not build Node for " + commandModel.getCommandId() + ".");
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

    /**
     * Changes the server instance of this task by the given UUID. The UUID gets changed in the
     * model even if no valid server instance for the command could be found.
     *
     * @param serverUuid The server UUID to change.
     * @return <code>true</code> if the server change was successful and the server instance is
     * currently online, otherwise <code>false</code>.
     */
    public boolean changeServerByUuid(final UUID serverUuid) {
        commandModel.setServerUuid(serverUuid);
        final ServerContext serverCtx = manager.getServerCtx(serverUuid);
        isCommandValid = tryToSetServerInstance(serverCtx);
        if (isCommandValid) {
            return serverCtx.isOnline();
        }
        return isCommandValid;
    }

    /**
     * Changes the server instance of this task by the context.
     *
     * @param serverCtx The server context to change this task to.
     */
    public void changeServerByCtx(final ServerContext serverCtx) {
        isCommandValid = tryToSetServerInstance(serverCtx);
    }

    /**
     * The actual action which is performed on execution. The overwritten <code>Runnable</code>
     * interface allows the execution routine to be run in its own, dedicated thread without
     * blocking the entire GUI. Multiple instances of this routine shall not be executed at the same
     * time, since the parallel usage of the involved GUI-components is not synchronized and
     * therefore not thread-safe.
     *
     * To start the routine in the current Thread use <code>this.run()</code>. To create a separate
     * process use <code>new Thread(this).start</code>.
     */
    @Override
    public void run() {
        TaskState oldState = taskState;
        if (!isNodeBuilt) {
            buildNode();
        }

        if (!isCommandValid) {
            lastExecResult = "Error: Offline or invalid server instance.";
            taskState = TaskState.FINISHED_ERROR;
            stateChanges.firePropertyChange(TASK_STATE_PROPERTY, oldState, taskState);
            return;
        }

        if (isPanelBuilt) {
            execBtn.setEnabled(false);
        }
        final SiLACall.Type callType = cmdCtx.getCommand().getObservable().equalsIgnoreCase("yes")
                ? SiLACall.Type.OBSERVABLE_COMMAND
                : SiLACall.Type.UNOBSERVABLE_COMMAND;

        startTimeStamp = OffsetDateTime.now();
        taskState = TaskState.RUNNING;
        stateChanges.firePropertyChange(TASK_STATE_PROPERTY, oldState, taskState);
        oldState = taskState;

        String jsonParams = "";
        if (cmdNode != null) {
            jsonParams = cmdNode.toJsonString();
        }

        final SiLACall call;
        if (jsonParams.isEmpty()) {
            call = new SiLACall(commandModel.getServerUuid(),
                    commandModel.getFeatureId(),
                    commandModel.getCommandId(),
                    callType);
        } else {
            call = new SiLACall(commandModel.getServerUuid(),
                    commandModel.getFeatureId(),
                    commandModel.getCommandId(),
                    callType,
                    jsonParams);
        }

        try {
            lastExecResult = manager.getServerManager().newCallExecutor(call).execute();
            taskState = TaskState.FINISHED_SUCCESS;
        } catch (final Exception ex) {
            System.err.println(ex.getMessage());
            lastExecResult = ex.getMessage();
            taskState = TaskState.FINISHED_ERROR;
        }
        endTimeStamp = OffsetDateTime.now();
        stateChanges.firePropertyChange(TASK_STATE_PROPERTY, oldState, taskState);

        if (isPanelBuilt) {
            execBtn.setEnabled(true);
        }
    }
}
