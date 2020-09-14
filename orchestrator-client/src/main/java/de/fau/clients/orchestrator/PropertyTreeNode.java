package de.fau.clients.orchestrator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fau.clients.orchestrator.nodes.NodeFactory;
import de.fau.clients.orchestrator.nodes.SilaNode;
import de.fau.clients.orchestrator.nodes.TypeDefLut;
import de.fau.clients.orchestrator.utils.IconProvider;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.UUID;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import lombok.extern.slf4j.Slf4j;
import sila_java.library.core.models.Feature.Property;
import sila_java.library.manager.ServerManager;
import sila_java.library.manager.models.SiLACall;

/**
 * Representation of a SiLA Property in the Feature tree.
 */
@Slf4j
@SuppressWarnings("serial")
public class PropertyTreeNode extends DefaultMutableTreeNode implements Presentable {

    /**
     * Index to place and update the contents of the panel.
     */
    private static final int CONTENT_COMPONENT_IDX = 0;
    private final UUID serverUuid;
    private final String featureId;
    private final TypeDefLut typeDefs;
    private final Property property;
    private JPanel panel;
    private JButton refreshBtn;
    private SilaNode node;
    private String lastResult = "";

    /**
     * Constructor.
     *
     * @param serverUuid The current UUID of the server where this SiLA Property belongs to.
     * @param featureId The SiLA Feature identifier of this Property.
     * @param typeDefs The Look-Up-Table of the data types from the Feature domain.
     * @param property The actual SiLA Property.
     */
    public PropertyTreeNode(
            final UUID serverUuid,
            final String featureId,
            final TypeDefLut typeDefs,
            final Property property) {
        this.serverUuid = serverUuid;
        this.featureId = featureId;
        this.typeDefs = typeDefs;
        this.property = property;
    }

    /**
     * Gets a <code>JPanel</code> populated with widgets viewing the current SiLA Property.
     *
     * @return A <code>JPanel</code> representing the SiLA Property.
     */
    public JPanel getPresenter() {
        if (panel == null) {
            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(property.getDisplayName()),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)));
            refreshBtn = new JButton("Refresh", IconProvider.REFRESH.getIcon());
            refreshBtn.addActionListener((ActionEvent evt) -> {
                refreshBtnActionPerformed();
            });
        } else {
            panel.removeAll();
        }

        if (node != null) {
            panel.add(node.getComponent(), CONTENT_COMPONENT_IDX);
        } else {
            // node is null -> show exception message
            panel.add(new JLabel(lastResult), CONTENT_COMPONENT_IDX);
        }
        panel.add(Box.createVerticalStrut(10));
        panel.add(refreshBtn);
        return panel;
    }

    /**
     * Request the current SiLA Property data form the server and updates the internal
     * <code>SilaNode</code>.
     */
    public void requestPropertyData() {
        final SiLACall.Type callType = property.getObservable().equalsIgnoreCase("yes")
                ? SiLACall.Type.OBSERVABLE_PROPERTY
                : SiLACall.Type.UNOBSERVABLE_PROPERTY;

        final SiLACall call = new SiLACall(
                serverUuid,
                featureId,
                property.getIdentifier(),
                callType);

        boolean wasSuccessful = false;
        try {
            lastResult = ServerManager.getInstance().newCallExecutor(call).execute();
            wasSuccessful = true;
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
                false);
    }

    @Override
    public String toString() {
        return property.getDisplayName();
    }

    /**
     * Requests the current state of the SiLA Property form the server and updates the view of the
     * GUI components. The internal panel has to be constructed before using this function.
     */
    private void refreshBtnActionPerformed() {
        requestPropertyData();
        panel.remove(CONTENT_COMPONENT_IDX);
        if (node != null) {
            panel.add(node.getComponent(), CONTENT_COMPONENT_IDX);
        } else {
            panel.add(new JLabel(lastResult), CONTENT_COMPONENT_IDX);
        }
        panel.revalidate();
        panel.repaint();
    }
}
