package de.fau.clients.orchestrator.tree;

import de.fau.clients.orchestrator.Presentable;
import de.fau.clients.orchestrator.nodes.MaxDim;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.UUID;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import lombok.extern.slf4j.Slf4j;
import sila_java.library.manager.ServerManager;
import sila_java.library.manager.models.Server;
import sila_java.library.manager.models.Server.Status;

@Slf4j
@SuppressWarnings("serial")
public class ServerTreeNode extends DefaultMutableTreeNode implements Presentable {

    private static final int MAX_NAME_LEN = 255;
    private static final Dimension MAX_DIM = MaxDim.TEXT_FIELD.getDim();
    private final Server server;
    private final ServerManager manager;
    private final UUID serverUuid;
    private boolean wasBuilt = false;
    private JPanel panel = null;
    private JTextField serverNameTextField = null;
    private JButton applyNewServerNameBtn = null;
    private JTextField hostTextField = null;
    private JTextField portTextField = null;
    private JTextField statusTextField = null;
    private JTextField joinedTextField = null;
    private JTextField negoTypeTextField = null;

    public ServerTreeNode(final ServerManager manager, final UUID serverUuid) {
        this.manager = manager;
        this.serverUuid = serverUuid;
        this.server = manager.getServers().get(serverUuid);
    }

    public String getServerLabel() {
        return "<html><p>" + server.getConfiguration().getName() + "</p>"
                + "<p>UUID: " + server.getConfiguration().getUuid().toString() + "</p>"
                + "<p>Addr: " + server.getHostAndPort().toString() + "</p>"
                + "</html>";
    }

    public String getDescription() {
        if (server.getStatus() == Status.ONLINE) {
            return "Joined on " + server.getJoined().toInstant();
        } else {
            return "Offline";
        }
    }

    public UUID getServerUuid() {
        return server.getConfiguration().getUuid();
    }

    @Override
    public JPanel getPresenter() {
        if (!wasBuilt) {
            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("Server Details"),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)));

            panel.add(new JLabel("Server Name"));
            serverNameTextField = new JTextField(server.getConfiguration().getName());
            serverNameTextField.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            serverNameTextField.setMaximumSize(MAX_DIM);
            serverNameTextField.setEditable(true);
            serverNameTextField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent de) {
                    validateAndEnableServerNameChange();
                }

                @Override
                public void removeUpdate(DocumentEvent de) {
                    validateAndEnableServerNameChange();
                }

                @Override
                public void changedUpdate(DocumentEvent de) {
                    // not needed on plain-text fields
                }
            });

            applyNewServerNameBtn = new JButton("Apply Change");
            applyNewServerNameBtn.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            applyNewServerNameBtn.setEnabled(false);
            applyNewServerNameBtn.addActionListener((ActionEvent evt) -> {
                changeServerName();
            });

            panel.add(serverNameTextField);
            panel.add(applyNewServerNameBtn);
            panel.add(Box.createVerticalStrut(10));

            panel.add(new JLabel("UUID"));
            final JTextField uuidTextField = new JTextField(server.getConfiguration().getUuid().toString());
            uuidTextField.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            uuidTextField.setMaximumSize(MAX_DIM);
            uuidTextField.setEditable(false);
            panel.add(uuidTextField);
            panel.add(Box.createVerticalStrut(10));

            panel.add(new JLabel("Host"));
            hostTextField = new JTextField(server.getHost());
            hostTextField.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            hostTextField.setMaximumSize(MAX_DIM);
            hostTextField.setEditable(false);
            panel.add(hostTextField);
            panel.add(Box.createVerticalStrut(10));

            panel.add(new JLabel("Port"));
            portTextField = new JTextField(server.getPort().toString());
            portTextField.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            portTextField.setMaximumSize(MAX_DIM);
            portTextField.setEditable(false);
            panel.add(portTextField);
            panel.add(Box.createVerticalStrut(10));

            panel.add(new JLabel("Status"));
            final String statusText = (server.getStatus() == Status.ONLINE) ? "Online" : "Offline";
            statusTextField = new JTextField(statusText);
            statusTextField.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            statusTextField.setMaximumSize(MAX_DIM);
            statusTextField.setEditable(false);
            panel.add(statusTextField);
            panel.add(Box.createVerticalStrut(10));

            panel.add(new JLabel("Joined"));
            joinedTextField = new JTextField(server.getJoined().toInstant().toString());
            joinedTextField.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            joinedTextField.setMaximumSize(MAX_DIM);
            joinedTextField.setEditable(false);
            panel.add(joinedTextField);
            panel.add(Box.createVerticalStrut(10));

            panel.add(new JLabel("Negotiation Type"));
            negoTypeTextField = new JTextField(server.getNegotiationType().toString());
            negoTypeTextField.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            negoTypeTextField.setMaximumSize(MAX_DIM);
            negoTypeTextField.setEditable(false);
            panel.add(negoTypeTextField);
            panel.add(Box.createVerticalStrut(10));

            panel.add(new JLabel("General Info"));
            final String htmlInfoString = "<html><p>"
                    + "Description: " + server.getInformation().getDescription() + "<br>"
                    + "Type: " + server.getInformation().getType() + "<br>"
                    + "Vendor URL: " + server.getInformation().getVendorURL() + "<br>"
                    + "Version: " + server.getInformation().getVersion() + "<br>"
                    + "<br></p></html>";
            final JTextPane serverInfoTextPane = new JTextPane();
            serverInfoTextPane.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            serverInfoTextPane.setEditable(false);
            serverInfoTextPane.setContentType("text/html");
            serverInfoTextPane.setText(htmlInfoString);
            serverInfoTextPane.putClientProperty(javax.swing.JTextPane.HONOR_DISPLAY_PROPERTIES, true);
            panel.add(serverInfoTextPane);
            panel.add(Box.createVerticalStrut(10));
            wasBuilt = true;
        } else {
            // just update the widgets with changeable content
            serverNameTextField.setText(server.getConfiguration().getName());
            hostTextField.setText(server.getHost());
            portTextField.setText(server.getPort().toString());
            statusTextField.setText((server.getStatus() == Status.ONLINE) ? "Online" : "Offline");
            joinedTextField.setText(server.getJoined().toInstant().toString());
            negoTypeTextField.setText(server.getNegotiationType().toString());
        }
        return panel;
    }

    private boolean validateServerName(final String changedName) {
        if (changedName.isBlank()) {
            return false;
        }

        if (changedName.equals(server.getConfiguration().getName())) {
            return false;
        }

        if (changedName.length() > MAX_NAME_LEN) {
            return false;
        }

        if (server.getStatus() != Status.ONLINE) {
            return false;
        }
        return true;
    }

    /**
     * Validates the server name and dis-/enables the "Apply Change"-button accordingly.
     */
    private void validateAndEnableServerNameChange() {
        final String text = serverNameTextField.getText();
        final boolean isValid = validateServerName(text);

        if (applyNewServerNameBtn.isEnabled() != isValid) {
            applyNewServerNameBtn.setEnabled(isValid);
        }
    }

    /**
     * Changes the server name without validation checks and disables the "Apply Change"-button.
     */
    private void changeServerName() {
        final String newServerName = serverNameTextField.getText();
        try {
            manager.setServerName(serverUuid, newServerName);
        } catch (Exception ex) {
            log.warn("Could not set server name to '" + newServerName + "'");
            return;
        }
        applyNewServerNameBtn.setEnabled(false);
    }
}
