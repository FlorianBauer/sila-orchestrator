package de.fau.clients.orchestrator;

import de.fau.clients.orchestrator.feature_explorer.SilaNode;
import de.fau.clients.orchestrator.feature_explorer.TypeDefLut;
import java.util.UUID;
import javax.swing.tree.DefaultMutableTreeNode;
import lombok.extern.slf4j.Slf4j;
import sila_java.library.core.models.Feature.Property;
import sila_java.library.manager.ServerManager;
import sila_java.library.manager.models.SiLACall;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fau.clients.orchestrator.feature_explorer.NodeFactory;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

@Slf4j
public class PropertyTreeNode extends DefaultMutableTreeNode {

    private static final int CONTENT_COMPONENT_IDX = 0;
    private final UUID serverId;
    private final String featureId;
    private final TypeDefLut typeDefs;
    private final Property property;
    private final JPanel panel = new JPanel();
    private final JButton refreshBtn = new JButton("Refresh");
    private boolean isPanelBuilt = false;
    private SilaNode node;
    private String lastResult = "";

    public PropertyTreeNode(
            final UUID serverId,
            final String featureId,
            final TypeDefLut typeDefs,
            final Property property) {

        super();
        this.serverId = serverId;
        this.featureId = featureId;
        this.typeDefs = typeDefs;
        this.property = property;
        this.panel.setLayout(new BoxLayout(this.panel, BoxLayout.Y_AXIS));
        this.panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(this.property.getDisplayName()),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        this.refreshBtn.addActionListener((ActionEvent evt) -> {
            refreshBtnActionPerformed();
        });

    }

    public JPanel getPanel() {
        if (!isPanelBuilt) {
            if (node == null) {
                // node is null -> show exception message
                panel.add(new JLabel(lastResult), CONTENT_COMPONENT_IDX);
            } else {
                panel.add(node.getComponent(), CONTENT_COMPONENT_IDX);
            }
            panel.add(Box.createVerticalStrut(10));
            panel.add(refreshBtn);
            isPanelBuilt = true;
        }
        return panel;
    }

    public boolean isPanelBuild() {
        return this.isPanelBuilt;
    }

    public void requestPropertyData() {
        final SiLACall.Type callType = property.getObservable().equalsIgnoreCase("yes")
                ? SiLACall.Type.OBSERVABLE_PROPERTY
                : SiLACall.Type.UNOBSERVABLE_PROPERTY;

        SiLACall call = new SiLACall(
                serverId,
                featureId,
                property.getIdentifier(),
                callType);

        boolean wasSuccessful = false;
        try {
            lastResult = ServerManager.getInstance().newCallExecutor(call).execute();
            wasSuccessful = true;
            log.info(lastResult);
        } catch (RuntimeException ex) {
            log.error(ex.getMessage());
            lastResult = ex.getMessage();
        } catch (Exception ex) {
            log.error(ex.getMessage());
            lastResult = ex.getMessage();
        }

        if (!wasSuccessful) {
            node = null;
            return;
        }

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode rootNode;
        try {
            rootNode = mapper.readTree(lastResult);
        } catch (IOException ex) {
            log.error(ex.getMessage());
            return;
        }

        node = NodeFactory.createFromJson(
                typeDefs,
                property.getDataType(),
                rootNode.get(property.getIdentifier()),
                true);
    }

    @Override
    public String toString() {
        return property.getDisplayName();
    }

    private void refreshBtnActionPerformed() {
        requestPropertyData();
        panel.remove(CONTENT_COMPONENT_IDX);
        panel.add(node.getComponent(), CONTENT_COMPONENT_IDX);
        panel.revalidate();
        panel.repaint();
    }
}
