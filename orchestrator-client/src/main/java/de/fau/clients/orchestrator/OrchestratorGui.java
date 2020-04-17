package de.fau.clients.orchestrator;

import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.ToolTipManager;
import javax.swing.table.DefaultTableModel;
import lombok.extern.slf4j.Slf4j;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import sila_java.library.core.models.Feature;
import sila_java.library.core.models.Feature.Command;
import sila_java.library.core.models.Feature.Property;
import sila_java.library.manager.ServerAdditionException;
import sila_java.library.manager.ServerManager;
import sila_java.library.manager.models.Server;
import javax.swing.tree.TreeSelectionModel;
import sila_java.library.core.models.Feature.Metadata;
import sila_java.library.manager.ServerFinder;

@Slf4j
public class OrchestratorGui extends javax.swing.JFrame {

    private static ServerManager serverManager;

    private void addSpecificServer() {
        String addr = serverAddressTextField.getText();
        int port;
        try {
            port = Integer.parseUnsignedInt(serverPortFormattedTextField.getText());
        } catch (NumberFormatException ex) {
            // do not accept invalid input
            return;
        } catch (Exception ex) {
            log.warn(OrchestratorGui.class.getName(), ex);
            return;
        }

        try {
            serverManager.addServer(addr, port);
        } catch (ServerAdditionException ex) {
            log.warn(OrchestratorGui.class.getName(), ex);
            return;
        }

        for (final Server server : serverManager.getServers().values()) {
            if (server.getHost().equals(addr) && server.getPort() == port) {
                addFeaturesToTree(List.of(server));
                break;
            }
        }

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

                if (feature.getCommand() != null && !feature.getCommand().isEmpty()) {
                    DefaultMutableTreeNode commandNode = new DefaultMutableTreeNode("Commands");
                    featureNode.add(commandNode);
                    for (final Command command : feature.getCommand()) {
                        final CommandTreeNode ctn = new CommandTreeNode(commandPanel,
                                server.getConfiguration().getUuid(),
                                feature.getIdentifier(),
                                command);
                        ctn.setUserObject(new FeatureTreeType(command));
                        commandNode.add(ctn);
                    }
                }

                if (feature.getProperty() != null && !feature.getProperty().isEmpty()) {
                    DefaultMutableTreeNode propertyNode = new DefaultMutableTreeNode("Properties");
                    featureNode.add(propertyNode);
                    for (final Property prop : feature.getProperty()) {
                        DefaultMutableTreeNode ptn = new DefaultMutableTreeNode();
                        ptn.setUserObject(new FeatureTreeType(prop));
                        propertyNode.add(ptn);
                    }
                }

                if (feature.getMetadata() != null && !feature.getMetadata().isEmpty()) {
                    DefaultMutableTreeNode metaNode = new DefaultMutableTreeNode("Metadata");
                    featureNode.add(metaNode);
                    for (final Metadata meta : feature.getMetadata()) {
                        DefaultMutableTreeNode mtn = new DefaultMutableTreeNode();
                        mtn.setUserObject(new FeatureTreeType(meta));
                        metaNode.add(mtn);
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
            log.error("ServerManager: ", ex);
            System.exit(1);
        }
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        addServerDialog = new javax.swing.JDialog();
        serverAddressLabel = new javax.swing.JLabel();
        serverAddressTextField = new javax.swing.JTextField();
        serverPortLabel = new javax.swing.JLabel();
        serverDialogOkBtn = new javax.swing.JButton();
        serverDialogCancelBtn = new javax.swing.JButton();
        serverPortFormattedTextField = new javax.swing.JFormattedTextField();
        aboutDialog = new javax.swing.JDialog();
        aboutLabel = new javax.swing.JLabel();
        taskQueuePopupMenu = new javax.swing.JPopupMenu();
        removeTaskFromQueueMenuItem = new javax.swing.JMenuItem();
        execRowEntryMenuItem = new javax.swing.JMenuItem();
        serverSplitPane = new javax.swing.JSplitPane();
        serverPanel = new javax.swing.JPanel();
        featureScrollPane = new javax.swing.JScrollPane();
        featureTree = new javax.swing.JTree();
        addServerBtn = new javax.swing.JButton();
        removeServerBtn = new javax.swing.JButton();
        scanServerBtn = new javax.swing.JButton();
        mainPanel = new javax.swing.JPanel();
        mainPanelSplitPane = new javax.swing.JSplitPane();
        taskQueuePanel = new javax.swing.JPanel();
        addTaskToQueueBtn = new javax.swing.JButton();
        taskQueueScrollPane = new javax.swing.JScrollPane();
        taskQueueTable = new javax.swing.JTable();
        executeAllBtn = new javax.swing.JButton();
        moveTaskUpBtn = new javax.swing.JButton();
        moveTaskDownBtn = new javax.swing.JButton();
        removeTaskFromQueueBtn = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        cutMenuItem = new javax.swing.JMenuItem();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        deleteMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        contentsMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        addServerDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addServerDialog.setTitle("Add Server");
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

        try {
            serverPortFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("#####")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
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
        aboutDialog.setMinimumSize(new java.awt.Dimension(300, 256));
        aboutDialog.setModal(true);
        aboutDialog.setName("aboutDialog"); // NOI18N
        aboutDialog.setLocationRelativeTo(null);

        aboutLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        aboutLabel.setText("<html>\n<center>\n<h1>sila-orchestrator</h1>\n<p>Copyright © 2020 Florian Bauer</p>\n</center>\n<p></p>\n<p>E-Mail: florian.bauer.dev@gmail.com</p>\n<p>License: Apache-2.0</p>\n<html>");
        aboutLabel.setName(""); // NOI18N
        aboutDialog.getContentPane().add(aboutLabel, java.awt.BorderLayout.CENTER);

        aboutDialog.getAccessibleContext().setAccessibleParent(this);

        removeTaskFromQueueMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/list-remove-16px.png"))); // NOI18N
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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SiLA Orchestrator");
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

        addServerBtn.setMnemonic('a');
        addServerBtn.setText("Add");
        addServerBtn.setPreferredSize(new java.awt.Dimension(80, 30));
        addServerBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addServerBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        serverPanel.add(addServerBtn, gridBagConstraints);

        removeServerBtn.setMnemonic('r');
        removeServerBtn.setText("Remove");
        removeServerBtn.setEnabled(false);
        removeServerBtn.setPreferredSize(new java.awt.Dimension(80, 30));
        removeServerBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeServerBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        serverPanel.add(removeServerBtn, gridBagConstraints);

        scanServerBtn.setMnemonic('s');
        scanServerBtn.setText("Scan");
        scanServerBtn.setPreferredSize(new java.awt.Dimension(80, 30));
        scanServerBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scanServerBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
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

        taskQueueTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Task #", "Icon", "Feature", "State", "Time", "Result"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                true, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        taskQueueTable.setFillsViewportHeight(true);
        taskQueueTable.setRowHeight(32);
        taskQueueTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        taskQueueTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                taskQueueTableMouseClicked(evt);
            }
        });
        taskQueueScrollPane.setViewportView(taskQueueTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        taskQueuePanel.add(taskQueueScrollPane, gridBagConstraints);

        executeAllBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/execute-all.png"))); // NOI18N
        executeAllBtn.setText("Execute All");
        executeAllBtn.setToolTipText("Execute all tasks in queue");
        executeAllBtn.setEnabled(false);
        executeAllBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                executeAllBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        taskQueuePanel.add(executeAllBtn, gridBagConstraints);

        moveTaskUpBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/move-up.png"))); // NOI18N
        moveTaskUpBtn.setToolTipText("Move selcted task one place up in the queue order");
        moveTaskUpBtn.setEnabled(false);
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

        removeTaskFromQueueBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/list-remove.png"))); // NOI18N
        removeTaskFromQueueBtn.setToolTipText("Remove selected task from queue");
        removeTaskFromQueueBtn.setEnabled(false);
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

        commandPanel.setLayout(new javax.swing.BoxLayout(commandPanel, javax.swing.BoxLayout.PAGE_AXIS));
        commandScrollPane.setViewportView(commandPanel);

        mainPanelSplitPane.setRightComponent(commandScrollPane);

        mainPanel.add(mainPanelSplitPane);

        serverSplitPane.setRightComponent(mainPanel);

        getContentPane().add(serverSplitPane, java.awt.BorderLayout.CENTER);

        fileMenu.setMnemonic('f');
        fileMenu.setText("File");

        openMenuItem.setMnemonic('o');
        openMenuItem.setText("Open");
        fileMenu.add(openMenuItem);

        saveMenuItem.setMnemonic('s');
        saveMenuItem.setText("Save");
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setMnemonic('a');
        saveAsMenuItem.setText("Save As ...");
        saveAsMenuItem.setDisplayedMnemonicIndex(5);
        fileMenu.add(saveAsMenuItem);

        exitMenuItem.setMnemonic('x');
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        editMenu.setMnemonic('e');
        editMenu.setText("Edit");

        cutMenuItem.setMnemonic('t');
        cutMenuItem.setText("Cut");
        editMenu.add(cutMenuItem);

        copyMenuItem.setMnemonic('y');
        copyMenuItem.setText("Copy");
        editMenu.add(copyMenuItem);

        pasteMenuItem.setMnemonic('p');
        pasteMenuItem.setText("Paste");
        editMenu.add(pasteMenuItem);

        deleteMenuItem.setMnemonic('d');
        deleteMenuItem.setText("Delete");
        editMenu.add(deleteMenuItem);

        menuBar.add(editMenu);

        helpMenu.setMnemonic('h');
        helpMenu.setText("Help");

        contentsMenuItem.setMnemonic('c');
        contentsMenuItem.setText("Contents");
        helpMenu.add(contentsMenuItem);

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

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        serverManager.close();
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void addServerBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addServerBtnActionPerformed
        addServerDialog.pack();
        addServerDialog.setVisible(true);
    }//GEN-LAST:event_addServerBtnActionPerformed

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

    private void scanServerBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scanServerBtnActionPerformed
        serverManager.getDiscovery().scanNetwork();
        final List<Server> serverList = ServerFinder.filterBy(ServerFinder.Filter.status(Server.Status.ONLINE)).find();
        if (!serverList.isEmpty()) {
            // hide the "No Server Available" string.
            featureTree.setRootVisible(false);
            addFeaturesToTree(serverList);
        } else {
            // show the "No Server Available" string.
            featureTree.setRootVisible(true);
        }
    }//GEN-LAST:event_scanServerBtnActionPerformed

    private void removeServerBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeServerBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_removeServerBtnActionPerformed

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

        if (node.isLeaf()) {
            addTaskToQueueBtn.setEnabled(true);
        } else {
            addTaskToQueueBtn.setEnabled(false);
        }
    }//GEN-LAST:event_featureTreeValueChanged

    private void addTaskToQueueBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTaskToQueueBtnActionPerformed
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) featureTree.getLastSelectedPathComponent();
        if (node == null) {
            return;
        }

        if (node.isLeaf()) {
            CommandTreeNode cmdNode = (CommandTreeNode) node;
            // use the selected node to create a new table entry.
            CommandTableEntry cmdEntry = cmdNode.createTableEntry();
            DefaultTableModel model = (DefaultTableModel) taskQueueTable.getModel();
            final int row = model.getRowCount();
            model.addRow(new Object[]{
                row + 1,
                new ImageIcon("src/main/resources/icons/32px/command.png"),
                cmdEntry,
                cmdEntry.getState(),
                cmdEntry.getStartTimeStamp(),
                "-"
            });

            cmdEntry.addStatusChangeListener((PropertyChangeEvent pcEvt) -> {
                if (pcEvt.getPropertyName().equals("taskState")) {
                    final TaskState state = (TaskState) pcEvt.getNewValue();
                    // Find the row of the changed entry. This has to be done dynamically, since 
                    // the order of rows might change during runtime.
                    int rowIdx = -1;
                    for (int i = 0; i < model.getRowCount(); i++) {
                        if (model.getDataVector().elementAt(i).get(2).equals(cmdEntry)) {
                            rowIdx = i;
                            break;
                        }
                    }

                    if (rowIdx == -1) {
                        log.error("Could not find entry in table");
                        return;
                    }
                    model.setValueAt(state, rowIdx, 3);
                    switch (state) {
                        case RUNNING:
                            model.setValueAt(cmdEntry.getStartTimeStamp(), rowIdx, 4);
                            break;
                        case FINISHED_SUCCESS:
                        case FINISHED_ERROR:
                            model.setValueAt(cmdEntry.getLastExecResult(), rowIdx, 5);
                            break;
                        default:
                    }
                }
            });
            executeAllBtn.setEnabled(true);
        } else {
            commandPanel.removeAll();
        }
        commandPanel.revalidate();
        commandPanel.repaint();
    }//GEN-LAST:event_addTaskToQueueBtnActionPerformed

    private void taskQueueTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_taskQueueTableMouseClicked
        int selectedRowIdx = taskQueueTable.getSelectedRow();
        if (selectedRowIdx < 0) {
            return;
        }

        // show popup-menu on right-click
        if (evt.getButton() == MouseEvent.BUTTON3) {
            taskQueuePopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
            return;
        }

        DefaultTableModel model = (DefaultTableModel) taskQueueTable.getModel();
        CommandTableEntry entry = (CommandTableEntry) model.getValueAt(selectedRowIdx, 2);
        if (entry == null) {
            return;
        }

        int rowCount = model.getRowCount();
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

        if (!entry.isNodeBuild()) {
            entry.buildCommandPanel();
        }
        entry.showCommandPanel(commandScrollPane);
    }//GEN-LAST:event_taskQueueTableMouseClicked

    private void executeAllBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_executeAllBtnActionPerformed
        executeAllBtn.setEnabled(false);

        Runnable queueRunner = () -> {
            Thread entryThread;
            CommandTableEntry entry;
            final DefaultTableModel model = (DefaultTableModel) taskQueueTable.getModel();
            for (int i = 0; i < taskQueueTable.getRowCount(); i++) {
                entry = (CommandTableEntry) model.getValueAt(i, 2);

                if (!entry.isNodeBuild()) {
                    // create entry with default values, if not done so far by the user
                    entry.buildCommandPanel();
                }
                entryThread = new Thread(entry);
                entryThread.start();
                try {
                    entryThread.join();
                } catch (InterruptedException ex) {
                    log.error(ex.getMessage());
                }
            }
            executeAllBtn.setEnabled(true);
        };
        new Thread(queueRunner).start();
    }//GEN-LAST:event_executeAllBtnActionPerformed

    private void execRowEntryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_execRowEntryMenuItemActionPerformed
        int selectedRowIdx = taskQueueTable.getSelectedRow();
        if (selectedRowIdx < 0) {
            return;
        }
        DefaultTableModel model = (DefaultTableModel) taskQueueTable.getModel();
        CommandTableEntry entry = (CommandTableEntry) model.getValueAt(selectedRowIdx, 2);
        new Thread(entry).start();
    }//GEN-LAST:event_execRowEntryMenuItemActionPerformed

    private void moveTaskUpBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveTaskUpBtnActionPerformed
        int selectedRowIdx = taskQueueTable.getSelectedRow();
        if (selectedRowIdx < 0) {
            return;
        } else if (selectedRowIdx <= 1) {
            moveTaskUpBtn.setEnabled(false);
        }
        DefaultTableModel model = (DefaultTableModel) taskQueueTable.getModel();
        model.moveRow(selectedRowIdx, selectedRowIdx, selectedRowIdx - 1);
        taskQueueTable.changeSelection(selectedRowIdx - 1, 0, false, false);
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
        DefaultTableModel model = (DefaultTableModel) taskQueueTable.getModel();
        model.moveRow(selectedRowIdx, selectedRowIdx, selectedRowIdx + 1);
        taskQueueTable.changeSelection(selectedRowIdx + 1, 0, false, false);
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
        DefaultTableModel model = (DefaultTableModel) taskQueueTable.getModel();
        model.removeRow(selectedRowIdx);
        commandPanel.removeAll();
        commandPanel.revalidate();
        commandPanel.repaint();
    }//GEN-LAST:event_removeTaskFromQueue

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        // Set the GTK+ look and feel
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("GTK+".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException
                | InstantiationException
                | IllegalAccessException
                | javax.swing.UnsupportedLookAndFeelException ex) {
            log.error(OrchestratorGui.class.getName(), ex);
        }
        //</editor-fold>

        // Create and display the form
        java.awt.EventQueue.invokeLater(() -> {
            new OrchestratorGui().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog aboutDialog;
    private javax.swing.JLabel aboutLabel;
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JButton addServerBtn;
    private javax.swing.JDialog addServerDialog;
    private javax.swing.JButton addTaskToQueueBtn;
    private final javax.swing.JPanel commandPanel = new javax.swing.JPanel();
    private final javax.swing.JScrollPane commandScrollPane = new javax.swing.JScrollPane();
    private javax.swing.JMenuItem contentsMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem execRowEntryMenuItem;
    private javax.swing.JButton executeAllBtn;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JScrollPane featureScrollPane;
    private javax.swing.JTree featureTree;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JSplitPane mainPanelSplitPane;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JButton moveTaskDownBtn;
    private javax.swing.JButton moveTaskUpBtn;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JButton removeServerBtn;
    private javax.swing.JButton removeTaskFromQueueBtn;
    private javax.swing.JMenuItem removeTaskFromQueueMenuItem;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JButton scanServerBtn;
    private javax.swing.JLabel serverAddressLabel;
    private javax.swing.JTextField serverAddressTextField;
    private javax.swing.JButton serverDialogCancelBtn;
    private javax.swing.JButton serverDialogOkBtn;
    private javax.swing.JPanel serverPanel;
    private javax.swing.JFormattedTextField serverPortFormattedTextField;
    private javax.swing.JLabel serverPortLabel;
    private javax.swing.JSplitPane serverSplitPane;
    private javax.swing.JPanel taskQueuePanel;
    private javax.swing.JPopupMenu taskQueuePopupMenu;
    private javax.swing.JScrollPane taskQueueScrollPane;
    private javax.swing.JTable taskQueueTable;
    // End of variables declaration//GEN-END:variables
}
