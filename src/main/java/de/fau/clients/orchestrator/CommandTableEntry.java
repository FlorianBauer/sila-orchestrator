package de.fau.clients.orchestrator;

import de.fau.clients.orchestrator.feature_explorer.FeatureNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import lombok.extern.slf4j.Slf4j;
import sila_java.library.core.models.Feature;
import sila_java.library.core.models.SiLAElement;
import sila_java.library.manager.ServerManager;
import sila_java.library.manager.models.SiLACall;

@Slf4j
public class CommandTableEntry {

    /// Use a "ISO 8601-ish" date-time representation.
    private static final DateTimeFormatter timeStampFromat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final JPanel panel = new JPanel();
    private final JButton execBtn = new JButton("Execute");
    private final UUID serverId;
    private final String featureId;
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
            final Feature.Command command) {

        this.serverId = serverId;
        this.featureId = featureId;
        this.command = command;
        this.panel.setLayout(new BoxLayout(this.panel, BoxLayout.Y_AXIS));
    }

    @Override
    public String toString() {
        return command.getDisplayName();
    }

    public void buildCommandPanel() {
        if (isNodeBuild == false) {
            isNodeBuild = true;

            if (featNode == null) {
                final List<SiLAElement> params = command.getParameter();
                if (params.isEmpty()) {
                    return;
                }
                featNode = new FeatureNode(params);
                featNode.populatePanel(panel);
            }

            execBtn.addActionListener((ActionEvent evt) -> {
                executeCommandBtnActionPerformed(evt);
            });
            panel.add(execBtn);
        } else {
            log.warn("Multiple calls to buildCommandPanel() are discouraged.");
        }
    }

    public void showCommandPanel(final JScrollPane scrollPane) {
        scrollPane.setViewportView(panel);
        panel.revalidate();
        panel.repaint();
    }

    private void executeCommandBtnActionPerformed(ActionEvent evt) {
        execBtn.setEnabled(false);
        final SiLACall.Type callType = command.getObservable().equalsIgnoreCase("yes")
                ? SiLACall.Type.OBSERVABLE_COMMAND
                : SiLACall.Type.UNOBSERVABLE_COMMAND;

        final String jsonMsg = featNode.toJsonMessage();
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

        // run in dedicated thread to avoid blocking the GUI during execution
        Runnable task = () -> {
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
        };
        new Thread(task).start();
    }

    public boolean isNodeBuild() {
        return isNodeBuild;
    }

    public String getStartTimeStamp() {
        if (startTimeStamp != null) {
            return startTimeStamp.format(timeStampFromat);
        }
        return "-";
    }

    public String getEndTimeStamp() {
        if (endTimeStamp != null) {
            return endTimeStamp.format(timeStampFromat);
        }
        return "-";
    }

    public String getLastExecResult() {
        return execResult;
    }

    public TaskState getState() {
        return state;
    }

    public void addStatusChangeListener(PropertyChangeListener listener) {
        stateChanges.addPropertyChangeListener(listener);
    }
}
