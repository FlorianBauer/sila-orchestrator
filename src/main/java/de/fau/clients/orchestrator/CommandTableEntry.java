package de.fau.clients.orchestrator;

import de.fau.clients.orchestrator.feature_explorer.FeatureNode;
import java.awt.event.ActionEvent;
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

    private final JPanel panel = new JPanel();
    private final JButton execBtn = new JButton("Execute");

    private final UUID serverId;
    private final String featureId;
    private final Feature.Command command;
    private boolean isNodeBuild = false;
    private FeatureNode featNode = null;

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
        final SiLACall.Type callType = command.getObservable().equalsIgnoreCase("yes")
                ? SiLACall.Type.OBSERVABLE_COMMAND
                : SiLACall.Type.UNOBSERVABLE_COMMAND;

        final String jsonMsg = featNode.toJsonMessage();
        log.info("jsonMsg: " + jsonMsg);
        SiLACall call = new SiLACall(serverId,
                featureId,
                command.getIdentifier(),
                callType,
                jsonMsg
        );

        try {
            ServerManager.getInstance().newCallExecutor(call).execute();
        } catch (RuntimeException ex) {
            log.error(ex.getMessage());
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    public boolean isNodeBuild() {
        return isNodeBuild;
    }
}
