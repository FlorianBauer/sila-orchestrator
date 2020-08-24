package de.fau.clients.orchestrator;

import static de.fau.clients.orchestrator.nodes.BasicNodeFactory.MAX_HEIGHT;
import de.fau.clients.orchestrator.utils.DocumentLengthFilter;
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
import javax.swing.text.AbstractDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import sila_java.library.manager.models.Server;
import sila_java.library.manager.models.Server.Status;
import sila_java.library.server_base.config.ServerConfiguration;

@SuppressWarnings("serial")
public class ServerTreeNode extends DefaultMutableTreeNode implements Presentable {

    private static final int MAX_NAME_LEN = 255;
    private static final int MAX_WIDTH = 1024;
    private static final Dimension MAX_DIM = new Dimension(MAX_WIDTH, MAX_HEIGHT);
    private final Server server;
    private JPanel panel = null;
    private JTextField serverNameTextField = null;
    private JButton applyNewServerNameBtn = null;

    public ServerTreeNode(final Server server) {
        this.server = server;
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
        if (panel == null) {
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
            ((AbstractDocument) serverNameTextField.getDocument())
                    .setDocumentFilter(new DocumentLengthFilter(MAX_NAME_LEN));

            applyNewServerNameBtn = new JButton("Apply New Server Name");
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
            final JTextField hostTextField = new JTextField(server.getHost());
            hostTextField.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            hostTextField.setMaximumSize(MAX_DIM);
            hostTextField.setEditable(false);
            panel.add(hostTextField);
            panel.add(Box.createVerticalStrut(10));

            panel.add(new JLabel("Port"));
            final JTextField portTextField = new JTextField(server.getPort().toString());
            portTextField.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            portTextField.setMaximumSize(MAX_DIM);
            portTextField.setEditable(false);
            panel.add(portTextField);
            panel.add(Box.createVerticalStrut(10));

            panel.add(new JLabel("Status"));
            final String statusText = (server.getStatus() == Status.ONLINE) ? "Online" : "Offline";
            final JTextField statusTextField = new JTextField(statusText);
            statusTextField.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            statusTextField.setMaximumSize(MAX_DIM);
            statusTextField.setEditable(false);
            panel.add(statusTextField);
            panel.add(Box.createVerticalStrut(10));

            panel.add(new JLabel("Joined"));
            final JTextField joinedTextField = new JTextField(server.getJoined().toInstant().toString());
            joinedTextField.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            joinedTextField.setMaximumSize(MAX_DIM);
            joinedTextField.setEditable(false);
            panel.add(joinedTextField);
            panel.add(Box.createVerticalStrut(10));

            panel.add(new JLabel("Negotiation Type"));
            final JTextField negoTypeTextField = new JTextField(server.getNegotiationType().toString());
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
                    + "<br></html>";
            final JTextPane serverInfoTextPane = new JTextPane();
            serverInfoTextPane.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            serverInfoTextPane.setEditable(false);
            serverInfoTextPane.setContentType("text/html");
            serverInfoTextPane.setText(htmlInfoString);
            serverInfoTextPane.putClientProperty(javax.swing.JTextPane.HONOR_DISPLAY_PROPERTIES, true);
            panel.add(serverInfoTextPane);
            panel.add(Box.createVerticalStrut(10));
        }
        return panel;
    }

    private void changeServerName() {
        String selectedServerName = serverNameTextField.getText();
        if (selectedServerName.equals(server.getConfiguration().getName())) {
            return;
        }

        if (selectedServerName.length() > MAX_NAME_LEN) {
            selectedServerName = selectedServerName.substring(0, MAX_NAME_LEN);
        }
        ServerConfiguration config = server.getConfiguration().withName(selectedServerName);
        server.setConfiguration(config);
    }
}
