package de.fau.clients.orchestrator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fau.clients.orchestrator.nodes.TypeDefLut;
import de.fau.clients.orchestrator.queue.TaskQueueTable;
import de.fau.clients.orchestrator.tasks.DelayTask;
import de.fau.clients.orchestrator.tasks.ExecPolicy;
import de.fau.clients.orchestrator.tasks.LocalExecTask;
import de.fau.clients.orchestrator.tasks.QueueTask;
import de.fau.clients.orchestrator.tasks.TaskQueueData;
import de.fau.clients.orchestrator.tasks.TaskState;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import lombok.extern.slf4j.Slf4j;
import sila_java.library.core.models.Feature;
import sila_java.library.core.models.Feature.Command;
import sila_java.library.core.models.Feature.Property;
import sila_java.library.manager.ServerAdditionException;
import sila_java.library.manager.ServerFinder;
import sila_java.library.manager.ServerManager;
import sila_java.library.manager.models.Server;

@Slf4j
@SuppressWarnings("serial")
public class OrchestratorGui extends javax.swing.JFrame {

    private static final Image ICON_IMG = new ImageIcon("src/main/resources/icons/sila-orchestrator-16px.png").getImage();
    private static final ImageIcon START_QUEUE_EXEC_ICON = new ImageIcon("src/main/resources/icons/queue-exec-start.png");
    private static final ImageIcon STOP_QUEUE_EXEC_ICON = new ImageIcon("src/main/resources/icons/queue-exec-stop.png");
    private static final String START_QUEUE_EXEC_LABEL = "Start Execute All";
    private static final String STOP_QUEUE_EXEC_LABEL = "Stop Execute All";
    private static ServerManager serverManager;
    private static String silaOrchestratorVersion;
    private static String gitCommit;
    private static String gitCommitTimestamp;
    private static String gitRepositoryUrl;
    private final String aboutInfo = "<html>"
            + "<p>Version: <b>" + silaOrchestratorVersion + "</b></p>"
            + "<p>"
            + "Git Commit: " + gitCommit + "<br>"
            + "Timestamp: " + gitCommitTimestamp + "<br>"
            + "Repository: " + gitRepositoryUrl + "<br>"
            + "E-Mail: florian.bauer.dev@gmail.com<br>"
            + "License: Apache-2.0<br>"
            + "</p></html>";
    private final TaskQueueTable taskQueueTable = new TaskQueueTable();
    private boolean isOnExecution = false;
    private boolean wasSaved = false;
    private String outFilePath = "";
    private Thread currentlyExecutedTaskThread = null;

    private void addSpecificServer() {
        String addr = serverAddressTextField.getText();
        int port;
        try {
            port = Integer.parseUnsignedInt(serverPortFormattedTextField.getText());
        } catch (NumberFormatException ex) {
            // do not accept invalid input
            return;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return;
        }

        try {
            serverManager.addServer(addr, port);
        } catch (ServerAdditionException ex) {
            log.warn(ex.getMessage());
            return;
        } catch (Exception ex) {
            log.warn(ex.getMessage());
            return;
        }

        for (final Server server : serverManager.getServers().values()) {
            if (server.getHost().equals(addr) && server.getPort() == port) {
                addFeaturesToTree(List.of(server));
                taskQueueTable.addUuidToSelectionSet(server.getConfiguration().getUuid());
                break;
            }
        }

        featureTree.setRootVisible(false);
        addServerDialog.setVisible(false);
        addServerDialog.dispose();
    }

    private void addFeaturesToTree(final Collection<Server> serverList) {
        DefaultTreeModel model = (DefaultTreeModel) featureTree.getModel();
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) model.getRoot();
        rootNode.removeAllChildren();

        for (final Server server : serverList) {
            DefaultMutableTreeNode serverNode = new DefaultMutableTreeNode();
            serverNode.setUserObject(new FeatureTreeType(server));
            rootNode.add(serverNode);

            for (final Feature feature : server.getFeatures()) {
                DefaultMutableTreeNode featureNode = new DefaultMutableTreeNode();
                featureNode.setUserObject(new FeatureTreeType(feature));
                serverNode.add(featureNode);

                final TypeDefLut typeDefs = new TypeDefLut(feature);
                if (feature.getProperty() != null && !feature.getProperty().isEmpty()) {
                    DefaultMutableTreeNode propertyNode = new DefaultMutableTreeNode("Properties");
                    featureNode.add(propertyNode);
                    for (final Property prop : feature.getProperty()) {
                        final PropertyTreeNode ptn = new PropertyTreeNode(
                                server.getConfiguration().getUuid(),
                                feature.getIdentifier(),
                                typeDefs,
                                prop);
                        ptn.setUserObject(new FeatureTreeType(prop));
                        propertyNode.add(ptn);
                    }
                }

                if (feature.getCommand() != null && !feature.getCommand().isEmpty()) {
                    DefaultMutableTreeNode commandNode = new DefaultMutableTreeNode("Commands");
                    featureNode.add(commandNode);
                    for (final Command command : feature.getCommand()) {
                        final CommandTreeNode ctn = new CommandTreeNode(
                                server.getConfiguration().getUuid(),
                                typeDefs,
                                command);
                        ctn.setUserObject(new FeatureTreeType(command));
                        commandNode.add(ctn);
                    }
                }
            }
        }
        model.reload();
        // expand all nodes in the tree
        for (int i = 0; i < featureTree.getRowCount(); i++) {
            featureTree.expandRow(i);
        }
    }

    /**
     * Creates new form OrchestratorGui
     */
    public OrchestratorGui() {
        try {
            serverManager = ServerManager.getInstance();
        } catch (Exception ex) {
            log.error(ex.getMessage());
            System.exit(1);
        }
        initComponents();
        initTaskQueueTable();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        addServerDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addServerDialog.setTitle("Add Server");
        addServerDialog.setIconImage(ICON_IMG);
        addServerDialog.setModal(true);
        addServerDialog.setName("addServerDialog"); // NOI18N
        addServerDialog.setPreferredSize(new java.awt.Dimension(300, 200));
        addServerDialog.setSize(new java.awt.Dimension(300, 200));
        addServerDialog.setLocationRelativeTo(null);
        java.awt.GridBagLayout addServerDialogLayout = new java.awt.GridBagLayout();
        addServerDialogLayout.columnWidths = new int[] {2};
        addServerDialogLayout.rowHeights = new int[] {5};
        addServerDialogLayout.columnWeights = new double[] {0.5, 0.5};
        addServerDialog.getContentPane().setLayout(addServerDialogLayout);

        serverAddressLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        serverAddressLabel.setText("Server Address:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        addServerDialog.getContentPane().add(serverAddressLabel, gridBagConstraints);

        serverAddressTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverAddressTextFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        addServerDialog.getContentPane().add(serverAddressTextField, gridBagConstraints);
        serverAddressTextField.getAccessibleContext().setAccessibleParent(this);

        serverPortLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        serverPortLabel.setText("Server Port:");
        serverPortLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        addServerDialog.getContentPane().add(serverPortLabel, gridBagConstraints);

        serverDialogOkBtn.setMnemonic('o');
        serverDialogOkBtn.setText("Ok");
        serverDialogOkBtn.setPreferredSize(new java.awt.Dimension(80, 30));
        serverDialogOkBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverDialogOkBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 5);
        addServerDialog.getContentPane().add(serverDialogOkBtn, gridBagConstraints);

        serverDialogCancelBtn.setMnemonic('c');
        serverDialogCancelBtn.setText("Cancel");
        serverDialogCancelBtn.setPreferredSize(new java.awt.Dimension(80, 30));
        serverDialogCancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverDialogCancelBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 10);
        addServerDialog.getContentPane().add(serverDialogCancelBtn, gridBagConstraints);

        final NumberFormatter formatter = new NumberFormatter(new DecimalFormat("#0"));
        formatter.setMinimum(0);
        formatter.setMaximum(99999);
        formatter.setAllowsInvalid(false);
        serverPortFormattedTextField.setFormatterFactory(new DefaultFormatterFactory(formatter));
        serverPortFormattedTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverPortFormattedTextFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        addServerDialog.getContentPane().add(serverPortFormattedTextField, gridBagConstraints);

        addServerDialog.getAccessibleContext().setAccessibleParent(this);

        aboutDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        aboutDialog.setTitle("About");
        aboutDialog.setAlwaysOnTop(true);
        aboutDialog.setIconImage(ICON_IMG);
        aboutDialog.setMinimumSize(new java.awt.Dimension(300, 256));
        aboutDialog.setModal(true);
        aboutDialog.setName("aboutDialog"); // NOI18N
        aboutDialog.setResizable(false);
        aboutDialog.setLocationRelativeTo(null);
        java.awt.GridBagLayout aboutDialogLayout = new java.awt.GridBagLayout();
        aboutDialogLayout.columnWidths = new int[] {1};
        aboutDialogLayout.rowHeights = new int[] {3};
        aboutDialog.getContentPane().setLayout(aboutDialogLayout);

        aboutLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        aboutLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/sila-orchestrator-128px.png"))); // NOI18N
        aboutLabel.setText("<html><h1>sila-orchestrator</h1<p>Copyright © 2020 Florian Bauer</p></html>"); // NOI18N
        aboutLabel.setAlignmentX(0.5F);
        aboutLabel.setIconTextGap(32);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        aboutDialog.getContentPane().add(aboutLabel, gridBagConstraints);

        aboutInfoTextPane.setEditable(false);
        aboutInfoTextPane.setContentType("text/html"); // NOI18N
        aboutInfoTextPane.setText(aboutInfo);
        aboutInfoTextPane.setMargin(new java.awt.Insets(15, 15, 15, 15));
        aboutInfoTextPane.putClientProperty(javax.swing.JTextPane.HONOR_DISPLAY_PROPERTIES, true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(15, 15, 15, 15);
        aboutDialog.getContentPane().add(aboutInfoTextPane, gridBagConstraints);

        aboutDialogCloseBtn.setText("Close");
        aboutDialogCloseBtn.setMargin(new java.awt.Insets(5, 15, 5, 15));
        aboutDialogCloseBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutDialogCloseBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 5, 15);
        aboutDialog.getContentPane().add(aboutDialogCloseBtn, gridBagConstraints);

        aboutDialog.getAccessibleContext().setAccessibleParent(this);

        removeTaskFromQueueMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/task-remove-16px.png"))); // NOI18N
        removeTaskFromQueueMenuItem.setMnemonic('r');
        removeTaskFromQueueMenuItem.setText("Remove Entry");
        removeTaskFromQueueMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeTaskFromQueue(evt);
            }
        });
        taskQueuePopupMenu.add(removeTaskFromQueueMenuItem);

        execRowEntryMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/execute-16px.png"))); // NOI18N
        execRowEntryMenuItem.setMnemonic('x');
        execRowEntryMenuItem.setText("Execute Entry");
        execRowEntryMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                execRowEntryMenuItemActionPerformed(evt);
            }
        });
        taskQueuePopupMenu.add(execRowEntryMenuItem);

        fileSaveAsChooser.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        fileSaveAsChooser.setDialogTitle("Save");
        fileSaveAsChooser.setFileSelectionMode(javax.swing.JFileChooser.FILES_AND_DIRECTORIES);

        fileOpenChooser.setFileFilter(new SiloFileFilter());

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SiLA Orchestrator");
        setIconImage(ICON_IMG);
        setLocationByPlatform(true);
        setPreferredSize(new java.awt.Dimension(1200, 600));
        setSize(new java.awt.Dimension(0, 0));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        serverSplitPane.setContinuousLayout(true);

        serverPanel.setPreferredSize(new java.awt.Dimension(384, 220));
        java.awt.GridBagLayout jPanel1Layout = new java.awt.GridBagLayout();
        jPanel1Layout.columnWidths = new int[] {3};
        jPanel1Layout.rowHeights = new int[] {2};
        serverPanel.setLayout(jPanel1Layout);

        ToolTipManager.sharedInstance().registerComponent(featureTree);
        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("No Server Available");
        featureTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        featureTree.setCellRenderer(new FeatureTreeRenderer());
        featureTree.setRowHeight(-1);
        featureTree.setVisibleRowCount(10);
        featureTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        featureTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                featureTreeValueChanged(evt);
            }
        });
        featureScrollPane.setViewportView(featureTree);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        serverPanel.add(featureScrollPane, gridBagConstraints);

        addServerBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/server-add.png"))); // NOI18N
        addServerBtn.setMnemonic('a');
        addServerBtn.setText("Add");
        addServerBtn.setToolTipText("Adds a SiLA server with a given IP address and port.");
        addServerBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addServerActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        serverPanel.add(addServerBtn, gridBagConstraints);

        scanServerBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/network-scan.png"))); // NOI18N
        scanServerBtn.setMnemonic('c');
        scanServerBtn.setText("Scan");
        scanServerBtn.setToolTipText("Scans the network for discoverable SiLA servers.");
        scanServerBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scanNetworkActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.8;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        serverPanel.add(scanServerBtn, gridBagConstraints);

        serverSplitPane.setLeftComponent(serverPanel);

        mainPanel.setPreferredSize(new java.awt.Dimension(512, 409));
        mainPanel.setLayout(new javax.swing.BoxLayout(mainPanel, javax.swing.BoxLayout.PAGE_AXIS));

        mainPanelSplitPane.setDividerLocation(300);
        mainPanelSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        mainPanelSplitPane.setContinuousLayout(true);

        java.awt.GridBagLayout taskQueuePanelLayout = new java.awt.GridBagLayout();
        taskQueuePanelLayout.columnWidths = new int[] {3};
        taskQueuePanelLayout.rowHeights = new int[] {3};
        taskQueuePanel.setLayout(taskQueuePanelLayout);

        addTaskToQueueBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/add-entry.png"))); // NOI18N
        addTaskToQueueBtn.setToolTipText("Add command to task-queue");
        addTaskToQueueBtn.setEnabled(false);
        addTaskToQueueBtn.setMargin(new java.awt.Insets(4, 2, 4, 2));
        addTaskToQueueBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTaskToQueueBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        taskQueuePanel.add(addTaskToQueueBtn, gridBagConstraints);

        executeAllBtn.setIcon(START_QUEUE_EXEC_ICON);
        executeAllBtn.setText(START_QUEUE_EXEC_LABEL);
        executeAllBtn.setToolTipText("Execute all tasks in queue");
        executeAllBtn.setEnabled(false);
        executeAllBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                executeAllActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        taskQueuePanel.add(executeAllBtn, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        taskQueuePanel.add(taskQueueScrollPane, gridBagConstraints);

        moveTaskUpBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/move-up.png"))); // NOI18N
        moveTaskUpBtn.setToolTipText("Move selcted task one place up in the queue order");
        moveTaskUpBtn.setEnabled(false);
        moveTaskUpBtn.setMargin(new java.awt.Insets(4, 0, 4, 0));
        moveTaskUpBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveTaskUpBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 0.5;
        taskQueuePanel.add(moveTaskUpBtn, gridBagConstraints);

        moveTaskDownBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/move-down.png"))); // NOI18N
        moveTaskDownBtn.setToolTipText("Move selected task one place down in the queue order");
        moveTaskDownBtn.setEnabled(false);
        moveTaskDownBtn.setMargin(new java.awt.Insets(4, 0, 4, 0));
        moveTaskDownBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveTaskDownBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 0.5;
        taskQueuePanel.add(moveTaskDownBtn, gridBagConstraints);

        removeTaskFromQueueBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/task-remove.png"))); // NOI18N
        removeTaskFromQueueBtn.setToolTipText("Remove selected task from queue");
        removeTaskFromQueueBtn.setEnabled(false);
        removeTaskFromQueueBtn.setMargin(new java.awt.Insets(2, 0, 2, 0));
        removeTaskFromQueueBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeTaskFromQueue(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        taskQueuePanel.add(removeTaskFromQueueBtn, gridBagConstraints);

        mainPanelSplitPane.setLeftComponent(taskQueuePanel);
        mainPanelSplitPane.setRightComponent(commandScrollPane);

        mainPanel.add(mainPanelSplitPane);

        serverSplitPane.setRightComponent(mainPanel);

        getContentPane().add(serverSplitPane, java.awt.BorderLayout.CENTER);

        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        openFileBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/document-open.png"))); // NOI18N
        openFileBtn.setText("Open");
        openFileBtn.setToolTipText("Opens a *.silo file.");
        openFileBtn.setFocusable(false);
        openFileBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        openFileBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        openFileBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openFileActionPerformed(evt);
            }
        });
        toolBar.add(openFileBtn);

        saveFileBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/document-save.png"))); // NOI18N
        saveFileBtn.setText("Save");
        saveFileBtn.setToolTipText("Saves a current queue into a *.silo file.");
        saveFileBtn.setEnabled(false);
        saveFileBtn.setFocusable(false);
        saveFileBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        saveFileBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        saveFileBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveFileActionPerformed(evt);
            }
        });
        toolBar.add(saveFileBtn);

        toolBarSeparator.setSeparatorSize(new java.awt.Dimension(24, 12));
        toolBar.add(toolBarSeparator);

        addDelayBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/delay-add.png"))); // NOI18N
        addDelayBtn.setText("Add Delay");
        addDelayBtn.setToolTipText("Adds a delay in the task queue.");
        addDelayBtn.setFocusable(false);
        addDelayBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addDelayBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addDelayBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addDelayTaskActionPerformed(evt);
            }
        });
        toolBar.add(addDelayBtn);

        addLocalExecBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/exec-add.png"))); // NOI18N
        addLocalExecBtn.setText("Add Exec");
        addLocalExecBtn.setToolTipText("Adds a local executable in the task queue.");
        addLocalExecBtn.setFocusable(false);
        addLocalExecBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addLocalExecBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addLocalExecBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addLocalExecTaskActionPerformed(evt);
            }
        });
        toolBar.add(addLocalExecBtn);

        clearQueueBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/queue-clear.png"))); // NOI18N
        clearQueueBtn.setText("Clear Queue");
        clearQueueBtn.setToolTipText("Removes all entries from the current task queue.");
        clearQueueBtn.setEnabled(false);
        clearQueueBtn.setFocusable(false);
        clearQueueBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        clearQueueBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        clearQueueBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearQueueActionPerformed(evt);
            }
        });
        toolBar.add(clearQueueBtn);

        getContentPane().add(toolBar, java.awt.BorderLayout.PAGE_START);

        fileMenu.setMnemonic('f');
        fileMenu.setText("File");

        openMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        openMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/document-open-16px.png"))); // NOI18N
        openMenuItem.setMnemonic('o');
        openMenuItem.setText("Open");
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openFileActionPerformed(evt);
            }
        });
        fileMenu.add(openMenuItem);

        saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        saveMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/document-save-16px.png"))); // NOI18N
        saveMenuItem.setMnemonic('s');
        saveMenuItem.setText("Save");
        saveMenuItem.setEnabled(false);
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveFileActionPerformed(evt);
            }
        });
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setMnemonic('a');
        saveAsMenuItem.setText("Save As ...");
        saveAsMenuItem.setDisplayedMnemonicIndex(5);
        saveAsMenuItem.setEnabled(false);
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsActionPerformed(evt);
            }
        });
        fileMenu.add(saveAsMenuItem);

        exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        exitMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/application-exit-16px.png"))); // NOI18N
        exitMenuItem.setMnemonic('x');
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        serverMenu.setMnemonic('v');
        serverMenu.setText("Server");

        addServerMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/server-add-16px.png"))); // NOI18N
        addServerMenuItem.setMnemonic('a');
        addServerMenuItem.setText("Add Server");
        addServerMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addServerActionPerformed(evt);
            }
        });
        serverMenu.add(addServerMenuItem);

        scanNetworkMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/network-scan-16px.png"))); // NOI18N
        scanNetworkMenuItem.setMnemonic('c');
        scanNetworkMenuItem.setText("Scan Network");
        scanNetworkMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scanNetworkActionPerformed(evt);
            }
        });
        serverMenu.add(scanNetworkMenuItem);

        menuBar.add(serverMenu);

        tasksMenu.setMnemonic('t');
        tasksMenu.setText("Tasks");

        clearQueueMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/queue-clear-16px.png"))); // NOI18N
        clearQueueMenuItem.setMnemonic('c');
        clearQueueMenuItem.setText("Clear Queue");
        clearQueueMenuItem.setToolTipText("Clears all entries from the current task queue.");
        clearQueueMenuItem.setEnabled(false);
        clearQueueMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearQueueActionPerformed(evt);
            }
        });
        tasksMenu.add(clearQueueMenuItem);

        executeAllMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/queue-exec-start-16px.png"))); // NOI18N
        executeAllMenuItem.setMnemonic('e');
        executeAllMenuItem.setText("Execute All");
        executeAllMenuItem.setToolTipText("Start executing the current task queue.");
        executeAllMenuItem.setEnabled(false);
        executeAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                executeAllActionPerformed(evt);
            }
        });
        tasksMenu.add(executeAllMenuItem);

        addDelayTaskMenuItem.setText("Add Delay");
        addDelayTaskMenuItem.setToolTipText("Add a delay to the task queue.");
        addDelayTaskMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addDelayTaskActionPerformed(evt);
            }
        });
        tasksMenu.add(addDelayTaskMenuItem);

        addLocalExecTaskMenuItem.setText("Add Executable");
        addLocalExecTaskMenuItem.setToolTipText("Adds a starter for a local executable or a script.");
        addLocalExecTaskMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addLocalExecTaskActionPerformed(evt);
            }
        });
        tasksMenu.add(addLocalExecTaskMenuItem);

        menuBar.add(tasksMenu);

        helpMenu.setMnemonic('h');
        helpMenu.setText("Help");

        aboutMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/sila-orchestrator-16px.png"))); // NOI18N
        aboutMenuItem.setMnemonic('a');
        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void initTaskQueueTable() {
        taskQueueTable.setServerManager(serverManager);
        taskQueueTable.setParamsPane(commandScrollPane);
        taskQueueTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                taskQueueTableMouseClicked(evt);
            }
        });
        taskQueueScrollPane.setViewportView(taskQueueTable);
    }

    private void taskQueueTableMouseClicked(final MouseEvent evt) {
        int selectedRowIdx = taskQueueTable.getSelectedRow();
        if (selectedRowIdx < 0) {
            return;
        }

        // show popup-menu on right-click
        if (evt.getButton() == MouseEvent.BUTTON3) {
            taskQueuePopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
            return;
        }

        final QueueTask entry = taskQueueTable.getTaskFromRow(selectedRowIdx);
        if (entry == null) {
            return;
        }

        int rowCount = taskQueueTable.getRowCount();
        if (rowCount > 1) {
            moveTaskUpBtn.setEnabled(selectedRowIdx > 0);
            moveTaskDownBtn.setEnabled(selectedRowIdx < rowCount - 1);
        } else {
            moveTaskUpBtn.setEnabled(false);
            moveTaskDownBtn.setEnabled(false);
        }

        final boolean isTaskRemoveEnabled = (rowCount > 0);
        removeTaskFromQueueBtn.setEnabled(isTaskRemoveEnabled);
        removeTaskFromQueueMenuItem.setEnabled(isTaskRemoveEnabled);
        commandScrollPane.setViewportView(entry.getPanel());
    }

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        serverManager.close();
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void addServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addServerActionPerformed
        addServerDialog.pack();
        addServerDialog.setVisible(true);
    }//GEN-LAST:event_addServerActionPerformed

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        aboutDialog.pack();
        aboutDialog.setVisible(true);
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    private void serverDialogCancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverDialogCancelBtnActionPerformed
        addServerDialog.setVisible(false);
        addServerDialog.dispose();
    }//GEN-LAST:event_serverDialogCancelBtnActionPerformed

    private void serverDialogOkBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverDialogOkBtnActionPerformed
        addSpecificServer();
    }//GEN-LAST:event_serverDialogOkBtnActionPerformed

    /**
     * Scans the network for available SiLA-Servers which are enabled for discovery. This method is
     * not thread safe. The scan-routine runs in a dedicated thread just to prevent the GUI from
     * freezing while scanning.
     */
    private void scanNetworkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scanNetworkActionPerformed
        scanServerBtn.setEnabled(false);
        final Runnable scan = () -> {
            serverManager.getDiscovery().scanNetwork();
            final List<Server> serverList = ServerFinder.filterBy(ServerFinder.Filter.status(Server.Status.ONLINE)).find();
            final boolean isTreeRootVisible;
            if (!serverList.isEmpty()) {
                // hide the "No Server Available" string.
                isTreeRootVisible = false;
                addFeaturesToTree(serverList);
                for (final Server server : serverList) {
                    taskQueueTable.addUuidToSelectionSet(server.getConfiguration().getUuid());
                }
            } else {
                // show the "No Server Available" string.
                isTreeRootVisible = true;
            }

            // update components in the GUI thread
            SwingUtilities.invokeLater(() -> {
                featureTree.setRootVisible(isTreeRootVisible);
                scanServerBtn.setEnabled(true);
            });
        };
        new Thread(scan).start();
    }//GEN-LAST:event_scanNetworkActionPerformed

    private void serverPortFormattedTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverPortFormattedTextFieldActionPerformed
        addSpecificServer();
    }//GEN-LAST:event_serverPortFormattedTextFieldActionPerformed

    private void serverAddressTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverAddressTextFieldActionPerformed
        // set cursor to the next text field when enter was pressed
        serverPortFormattedTextField.requestFocusInWindow();
    }//GEN-LAST:event_serverAddressTextFieldActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        serverManager.close();
    }//GEN-LAST:event_formWindowClosing

    private void featureTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_featureTreeValueChanged
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) featureTree.getLastSelectedPathComponent();
        if (node == null) {
            return;
        }

        boolean isAddBtnToEnable = false;
        JComponent viewportView = null;
        if (node.isLeaf()) {
            if (node instanceof CommandTreeNode) {
                isAddBtnToEnable = true;
            } else if (node instanceof PropertyTreeNode) {
                PropertyTreeNode propNode = (PropertyTreeNode) node;
                propNode.requestPropertyData();
                viewportView = propNode.getPanel();
            }
        }
        addTaskToQueueBtn.setEnabled(isAddBtnToEnable);
        commandScrollPane.setViewportView(viewportView);
    }//GEN-LAST:event_featureTreeValueChanged

    private void addTaskToQueueBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTaskToQueueBtnActionPerformed
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) featureTree.getLastSelectedPathComponent();
        if (node == null) {
            return;
        }

        if (node.isLeaf()) {
            if (node instanceof CommandTreeNode) {
                final CommandTreeNode cmdNode = (CommandTreeNode) node;
                // use the selected node to create a new table entry.
                taskQueueTable.addCommandTask(cmdNode.createTableEntry());
                enableTaskQueueOperations();
            }
        }
    }//GEN-LAST:event_addTaskToQueueBtnActionPerformed

    /**
     * Starts/stops the execution of all entries in the task queue. If the execution is stopped and
     * a task is currently running, the active task gets 2 seconds time to complete before an
     * interrupt is signaled, which causes a <code>InterruptedException</code> inside the thread.
     *
     * @param evt The fired event.
     */
    private void executeAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_executeAllActionPerformed
        if (isOnExecution) {
            // exectuion is already running, so stop it
            executeAllBtn.setEnabled(false);
            isOnExecution = false;
            log.info("Aborted queue execution by user.");
            /* Use a dedicated thread for the abortion process, since the user can pile up events by 
               spamming the button due to the delay inside the cancellation routine. */
            final Runnable abortRunner = () -> {
                // give the executing thread 2 seconds time to finish before sending an interrupt
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    log.error(ex.getMessage());
                }
                if (currentlyExecutedTaskThread != null
                        && !currentlyExecutedTaskThread.isInterrupted()) {
                    currentlyExecutedTaskThread.interrupt();
                }

                SwingUtilities.invokeLater(() -> {
                    executeAllBtn.setEnabled(true);
                });
            };
            new Thread(abortRunner).start();
            return;
        }

        executeAllBtn.setIcon(STOP_QUEUE_EXEC_ICON);
        executeAllBtn.setText(STOP_QUEUE_EXEC_LABEL);
        executeAllMenuItem.setEnabled(false);
        isOnExecution = true;

        final Runnable queueRunner = () -> {
            for (int i = 0; i < taskQueueTable.getRowCount(); i++) {
                if (!isOnExecution) {
                    break;
                }

                final QueueTask task = taskQueueTable.getTaskFromRow(i);
                currentlyExecutedTaskThread = new Thread(task);
                currentlyExecutedTaskThread.start();
                try {
                    currentlyExecutedTaskThread.join();
                } catch (InterruptedException ex) {
                    log.error(ex.getMessage());
                }

                if (task.getState() == TaskState.FINISHED_ERROR) {
                    // apply execution policy
                    if (taskQueueTable.getTaskPolicyFromRow(i) == ExecPolicy.HALT_AFTER_ERROR) {
                        break;
                    }
                }
            }
            currentlyExecutedTaskThread = null;

            SwingUtilities.invokeLater(() -> {
                executeAllBtn.setIcon(START_QUEUE_EXEC_ICON);
                executeAllBtn.setText(START_QUEUE_EXEC_LABEL);
                executeAllMenuItem.setEnabled(true);
                executeAllBtn.setEnabled(true);
            });
            isOnExecution = false;
        };
        new Thread(queueRunner).start();
    }//GEN-LAST:event_executeAllActionPerformed

    private void execRowEntryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_execRowEntryMenuItemActionPerformed
        int selectedRowIdx = taskQueueTable.getSelectedRow();
        if (selectedRowIdx < 0) {
            return;
        }
        final QueueTask entry = taskQueueTable.getTaskFromRow(selectedRowIdx);
        new Thread(entry).start();
    }//GEN-LAST:event_execRowEntryMenuItemActionPerformed

    private void moveTaskUpBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveTaskUpBtnActionPerformed
        int selectedRowIdx = taskQueueTable.getSelectedRow();
        if (selectedRowIdx < 0) {
            return;
        } else if (selectedRowIdx <= 1) {
            moveTaskUpBtn.setEnabled(false);
        }
        taskQueueTable.moveRow(selectedRowIdx, selectedRowIdx - 1);
        taskQueueTable.changeSelection(selectedRowIdx - 1, TaskQueueTable.COLUMN_TASK_ID_IDX, false, false);
        moveTaskDownBtn.setEnabled(true);
    }//GEN-LAST:event_moveTaskUpBtnActionPerformed

    private void moveTaskDownBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveTaskDownBtnActionPerformed
        int selectedRowIdx = taskQueueTable.getSelectedRow();
        int rowCount = taskQueueTable.getRowCount();
        if (selectedRowIdx < 0 || selectedRowIdx >= rowCount - 1) {
            return;
        } else if (selectedRowIdx >= rowCount - 2) {
            moveTaskDownBtn.setEnabled(false);
        }
        taskQueueTable.moveRow(selectedRowIdx, selectedRowIdx + 1);
        taskQueueTable.changeSelection(selectedRowIdx + 1, TaskQueueTable.COLUMN_TASK_ID_IDX, false, false);
        moveTaskUpBtn.setEnabled(true);
    }//GEN-LAST:event_moveTaskDownBtnActionPerformed

    private void removeTaskFromQueue(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeTaskFromQueue
        int selectedRowIdx = taskQueueTable.getSelectedRow();
        if (selectedRowIdx < 0) {
            return;
        }
        moveTaskUpBtn.setEnabled(false);
        moveTaskDownBtn.setEnabled(false);
        removeTaskFromQueueBtn.setEnabled(false);
        removeTaskFromQueueMenuItem.setEnabled(false);
        taskQueueTable.removeRow(selectedRowIdx);
        if (taskQueueTable.getRowCount() <= 0) {
            // task queue is now empty
            disableTaskQueueOperations();
        }
        commandScrollPane.setViewportView(null);
    }//GEN-LAST:event_removeTaskFromQueue

    private void saveFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveFileActionPerformed
        if (!wasSaved || outFilePath.isEmpty()) {
            saveAsActionPerformed(evt);
        } else {
            // TODO: give the user some kind of notificatin that the file was saved
            String outData = getSaveData();
            if (outData.isEmpty()) {
                log.warn("Empty save!");
                return;
            }

            try {
                Files.writeString(Paths.get(outFilePath), outData, StandardCharsets.UTF_8);
                log.info("Saved " + outFilePath);
            } catch (IOException ex) {
                log.error(ex.getMessage());
            }
        }
    }//GEN-LAST:event_saveFileActionPerformed

    private void saveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsActionPerformed
        String outData = getSaveData();
        if (outData.isEmpty()) {
            log.warn("Empty save!");
            return;
        }

        fileSaveAsChooser.setSelectedFile(new File(LocalDate.now().toString() + ".silo"));
        int retVal = fileSaveAsChooser.showSaveDialog(this);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File outFile = fileSaveAsChooser.getSelectedFile();
            outFilePath = outFile.getAbsolutePath();
            File tmpFile = new File(outFilePath);
            int userDesition = JOptionPane.OK_OPTION;
            if (tmpFile.exists() && tmpFile.isFile()) {
                userDesition = JOptionPane.showConfirmDialog(this,
                        "File \"" + tmpFile.getName() + "\" already exists in \""
                        + tmpFile.getParent() + "\"!\n"
                        + "Do you want to overwrite the existing file?",
                        null,
                        JOptionPane.INFORMATION_MESSAGE,
                        JOptionPane.YES_NO_CANCEL_OPTION);
            }

            if (userDesition == JOptionPane.OK_OPTION) {
                try {
                    Files.writeString(Paths.get(outFilePath), outData, StandardCharsets.UTF_8);
                    wasSaved = true;
                    log.info("Saved as file " + outFilePath);
                } catch (IOException ex) {
                    log.error(ex.getMessage());
                }
            }
        }
    }//GEN-LAST:event_saveAsActionPerformed

    private void openFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openFileActionPerformed
        int retVal = fileOpenChooser.showOpenDialog(this);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            final File file = fileOpenChooser.getSelectedFile();
            log.info("Opend file: " + file.getAbsolutePath());
            ObjectMapper mapper = new ObjectMapper();
            TaskQueueData tqd = null;
            try {
                tqd = mapper.readValue(file, TaskQueueData.class);
            } catch (IOException ex) {
                log.error(ex.getMessage());
            }

            if (tqd != null) {
                clearQueueActionPerformed(evt);
                log.info("Silo-file version: " + tqd.getSiloFileVersion());
                tqd.importToTaskQueue(taskQueueTable, serverManager.getServers());
                enableTaskQueueOperations();
            }
        } else {
            log.warn("File access cancelled by user.");
        }
    }//GEN-LAST:event_openFileActionPerformed

    private void clearQueueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearQueueActionPerformed
        disableTaskQueueOperations();
        commandScrollPane.setViewportView(null);
        taskQueueTable.clearTable();
    }//GEN-LAST:event_clearQueueActionPerformed

    private void aboutDialogCloseBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutDialogCloseBtnActionPerformed
        aboutDialog.setVisible(false);
        aboutDialog.dispose();
    }//GEN-LAST:event_aboutDialogCloseBtnActionPerformed

    /**
     * Adds a delay task to the task queue.
     *
     * @param evt The fired event.
     */
    private void addDelayTaskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addDelayTaskActionPerformed
        taskQueueTable.addTask(new DelayTask());
        enableTaskQueueOperations();
    }//GEN-LAST:event_addDelayTaskActionPerformed

    private void addLocalExecTaskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addLocalExecTaskActionPerformed
        taskQueueTable.addTask(new LocalExecTask());
        enableTaskQueueOperations();
    }//GEN-LAST:event_addLocalExecTaskActionPerformed

    /**
     * Enables all the GUI controls which actions can be applied on entries in the task queue. This
     * function is to enable user interaction after the task queue was set to a valid state (e.g.
     * queue is not empty anymore).
     */
    private void enableTaskQueueOperations() {
        executeAllBtn.setEnabled(true);
        executeAllMenuItem.setEnabled(true);
        clearQueueMenuItem.setEnabled(true);
        clearQueueBtn.setEnabled(true);
        saveFileBtn.setEnabled(true);
        saveMenuItem.setEnabled(true);
        saveAsMenuItem.setEnabled(true);
    }

    /**
     * Disables all the GUI controls which actions relay on entries in the task queue. This function
     * is supposed to be used when the table is empty or in an locked state and actions, like saving
     * or executing tasks, make no sense for the user.
     */
    private void disableTaskQueueOperations() {
        executeAllBtn.setEnabled(false);
        executeAllMenuItem.setEnabled(false);
        clearQueueMenuItem.setEnabled(false);
        clearQueueBtn.setEnabled(false);
        saveFileBtn.setEnabled(false);
        saveMenuItem.setEnabled(false);
        saveAsMenuItem.setEnabled(false);
        moveTaskUpBtn.setEnabled(false);
        moveTaskDownBtn.setEnabled(false);
    }

    private String getSaveData() {
        String outData = "";
        ObjectMapper mapper = new ObjectMapper();
        TaskQueueData tqd = TaskQueueData.createFromTaskQueue(taskQueueTable);
        try {
            outData = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(tqd);
        } catch (JsonProcessingException ex) {
            log.error(ex.getMessage());
        }
        return outData;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        final Properties properties = new Properties();
        try {
            // retrieve version info from the maven git plug-in
            properties.load(OrchestratorGui.class.getClassLoader().getResourceAsStream("git.properties"));
        } catch (IOException ex) {
            log.error(ex.getMessage());
            System.exit(1);
        }

        OrchestratorGui.silaOrchestratorVersion = properties.getProperty("git.build.version")
                + "-" + properties.getProperty("git.commit.id.abbrev");
        OrchestratorGui.gitCommit = properties.getProperty("git.commit.id");
        OrchestratorGui.gitCommitTimestamp = properties.getProperty("git.commit.time");
        OrchestratorGui.gitRepositoryUrl = properties.getProperty("git.remote.origin.url");

        if (args.length > 0) {
            // arguments were set, so we handel erverything in command line and ditch the GUI stuff
            for (final String arg : args) {
                if (arg.equalsIgnoreCase("-h") || arg.equalsIgnoreCase("--help")) {
                    System.out.println("Usage: java -jar orchestrator-client-exec.jar [args]"
                            + "\n -h, --help"
                            + "\n\t Print this help message."
                            + "\n -v, --version"
                            + "\n\t Print the version number."
                            + "\n --about, --info"
                            + "\n\t Print some general information about this software.");
                } else if (arg.equalsIgnoreCase("-v") || arg.equalsIgnoreCase("--version")) {
                    System.out.println(silaOrchestratorVersion);
                } else if (arg.equalsIgnoreCase("--about") || arg.equalsIgnoreCase("--info")) {
                    System.out.println("sila-orchestrator"
                            + "\n Copyright © 2020 Florian Bauer"
                            + "\n Version: " + silaOrchestratorVersion
                            + "\n Git Commit: " + gitCommit
                            + "\n Timestamp: " + gitCommitTimestamp
                            + "\n Git Repository: " + gitRepositoryUrl
                            + "\n E-Mail: florian.bauer.dev@gmail.com"
                            + "\n License: Apache-2.0");
                }
            }
            System.exit(0);
        }

        String laf = "Nimbus";
        final String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows")) {
            laf = "Windows";
        } else if (osName.startsWith("Linux")) {
            laf = "GTK+";
        }

        try {
            for (final UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (info.getName().equals(laf)) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException
                | InstantiationException
                | IllegalAccessException
                | javax.swing.UnsupportedLookAndFeelException ex) {
            log.error(ex.getMessage());
        }

        // Create and display the form
        SwingUtilities.invokeLater(() -> {
            new OrchestratorGui().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final javax.swing.JDialog aboutDialog = new javax.swing.JDialog();
    private final javax.swing.JButton aboutDialogCloseBtn = new javax.swing.JButton();
    private final javax.swing.JTextPane aboutInfoTextPane = new javax.swing.JTextPane();
    private final javax.swing.JLabel aboutLabel = new javax.swing.JLabel();
    private final javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
    private final javax.swing.JButton addDelayBtn = new javax.swing.JButton();
    private final javax.swing.JMenuItem addDelayTaskMenuItem = new javax.swing.JMenuItem();
    private final javax.swing.JButton addLocalExecBtn = new javax.swing.JButton();
    private final javax.swing.JMenuItem addLocalExecTaskMenuItem = new javax.swing.JMenuItem();
    private final javax.swing.JButton addServerBtn = new javax.swing.JButton();
    private final javax.swing.JDialog addServerDialog = new javax.swing.JDialog();
    private final javax.swing.JMenuItem addServerMenuItem = new javax.swing.JMenuItem();
    private final javax.swing.JButton addTaskToQueueBtn = new javax.swing.JButton();
    private final javax.swing.JButton clearQueueBtn = new javax.swing.JButton();
    private final javax.swing.JMenuItem clearQueueMenuItem = new javax.swing.JMenuItem();
    private final javax.swing.JScrollPane commandScrollPane = new javax.swing.JScrollPane();
    private final javax.swing.JMenuItem execRowEntryMenuItem = new javax.swing.JMenuItem();
    private final javax.swing.JButton executeAllBtn = new javax.swing.JButton();
    private final javax.swing.JMenuItem executeAllMenuItem = new javax.swing.JMenuItem();
    private final javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
    private final javax.swing.JScrollPane featureScrollPane = new javax.swing.JScrollPane();
    private final javax.swing.JTree featureTree = new javax.swing.JTree();
    private final javax.swing.JMenu fileMenu = new javax.swing.JMenu();
    private final javax.swing.JFileChooser fileOpenChooser = new javax.swing.JFileChooser();
    private final javax.swing.JFileChooser fileSaveAsChooser = new javax.swing.JFileChooser();
    private final javax.swing.JMenu helpMenu = new javax.swing.JMenu();
    private final javax.swing.JPanel mainPanel = new javax.swing.JPanel();
    private final javax.swing.JSplitPane mainPanelSplitPane = new javax.swing.JSplitPane();
    private final javax.swing.JMenuBar menuBar = new javax.swing.JMenuBar();
    private final javax.swing.JButton moveTaskDownBtn = new javax.swing.JButton();
    private final javax.swing.JButton moveTaskUpBtn = new javax.swing.JButton();
    private final javax.swing.JButton openFileBtn = new javax.swing.JButton();
    private final javax.swing.JMenuItem openMenuItem = new javax.swing.JMenuItem();
    private final javax.swing.JButton removeTaskFromQueueBtn = new javax.swing.JButton();
    private final javax.swing.JMenuItem removeTaskFromQueueMenuItem = new javax.swing.JMenuItem();
    private final javax.swing.JMenuItem saveAsMenuItem = new javax.swing.JMenuItem();
    private final javax.swing.JButton saveFileBtn = new javax.swing.JButton();
    private final javax.swing.JMenuItem saveMenuItem = new javax.swing.JMenuItem();
    private final javax.swing.JMenuItem scanNetworkMenuItem = new javax.swing.JMenuItem();
    private final javax.swing.JButton scanServerBtn = new javax.swing.JButton();
    private final javax.swing.JLabel serverAddressLabel = new javax.swing.JLabel();
    private final javax.swing.JTextField serverAddressTextField = new javax.swing.JTextField();
    private final javax.swing.JButton serverDialogCancelBtn = new javax.swing.JButton();
    private final javax.swing.JButton serverDialogOkBtn = new javax.swing.JButton();
    private final javax.swing.JMenu serverMenu = new javax.swing.JMenu();
    private final javax.swing.JPanel serverPanel = new javax.swing.JPanel();
    private final javax.swing.JFormattedTextField serverPortFormattedTextField = new javax.swing.JFormattedTextField();
    private final javax.swing.JLabel serverPortLabel = new javax.swing.JLabel();
    private final javax.swing.JSplitPane serverSplitPane = new javax.swing.JSplitPane();
    private final javax.swing.JPanel taskQueuePanel = new javax.swing.JPanel();
    private final javax.swing.JPopupMenu taskQueuePopupMenu = new javax.swing.JPopupMenu();
    private final javax.swing.JScrollPane taskQueueScrollPane = new javax.swing.JScrollPane();
    private final javax.swing.JMenu tasksMenu = new javax.swing.JMenu();
    private final javax.swing.JToolBar toolBar = new javax.swing.JToolBar();
    private final javax.swing.JToolBar.Separator toolBarSeparator = new javax.swing.JToolBar.Separator();
    // End of variables declaration//GEN-END:variables
}
