package de.fau.clients.orchestrator;

import de.fau.clients.orchestrator.feature_explorer.FeatureNode;
import lombok.extern.slf4j.Slf4j;
import java.awt.event.ActionEvent;
import java.util.UUID;
import javax.swing.JButton;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JPanel;
import sila_java.library.core.models.Feature.Command;
import sila_java.library.manager.ServerManager;
import sila_java.library.manager.models.SiLACall;

@Slf4j
public class CommandTreeNode extends DefaultMutableTreeNode {

    private final JPanel panel;
    private final UUID serverId;
    private final String featureId;
    private final Command command;
    FeatureNode featNode;

    public CommandTreeNode(final JPanel panel, final UUID serverId, final String featureId, final Command command) {
        super();
        this.panel = panel;
        this.serverId = serverId;
        this.featureId = featureId;
        this.command = command;
    }

    @Override
    public String toString() {
        return command.getDisplayName();
    }

    public void buildCommandPanel() {
        panel.removeAll();

        featNode = new FeatureNode(command.getParameter());
        featNode.populatePanel(panel);

        JButton execBtn = new JButton("Execute");
        execBtn.addActionListener((ActionEvent evt) -> {
            executeCommandBtnActionPerformed(evt);
        });

        panel.add(execBtn);
        panel.revalidate();
        panel.repaint();
    }

    private void executeCommandBtnActionPerformed(ActionEvent evt) {
        final SiLACall.Type callType = command.getObservable().equalsIgnoreCase("yes")
                ? SiLACall.Type.OBSERVABLE_COMMAND
                : SiLACall.Type.UNOBSERVABLE_COMMAND;

        final String jsonMsg = "{" + featNode.toJsonMessage() + "}";
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
}
