package de.fau.clients.orchestrator;

import de.fau.clients.orchestrator.dnd.TaskExportTransferHandler;
import de.fau.clients.orchestrator.queue.TaskQueueData;
import de.fau.clients.orchestrator.queue.TaskQueueTable;
import de.fau.clients.orchestrator.tasks.DelayTask;
import de.fau.clients.orchestrator.tasks.ExecPolicy;
import de.fau.clients.orchestrator.tasks.LocalExecTask;
import de.fau.clients.orchestrator.tasks.QueueTask;
import de.fau.clients.orchestrator.tasks.TaskState;
import de.fau.clients.orchestrator.tree.CommandTreeNode;
import de.fau.clients.orchestrator.tree.ServerFeatureTree;
import de.fau.clients.orchestrator.utils.IconProvider;
import de.fau.clients.orchestrator.utils.SiloFileFilter;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import lombok.extern.slf4j.Slf4j;
import sila_java.library.manager.ServerFinder;
import sila_java.library.manager.ServerManager;
import sila_java.library.manager.models.Server;

/**
 * The main GUI window and execution entry point of the client. It is advised to use the NetBeans
 * GUI designer to make changes on its layout.
 */
@Slf4j
@SuppressWarnings("serial")
public class OrchestratorGui extends javax.swing.JFrame {

    private static final Image ICON_IMG = IconProvider.SILA_ORCHESTRATOR_16PX.getIcon().getImage();
    private static final String START_QUEUE_EXEC_LABEL = "Start Execute All";
    private static final String STOP_QUEUE_EXEC_LABEL = "Stop Execute All";
    private static final String COPYRIGHT_NOTICE = "Copyright © 2020 Florian Bauer";
    private static final String NO_ERROR_STR = "<No Error>";
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
    private final ServerFeatureTree serverFeatureTree = new ServerFeatureTree();
    private boolean isOnExecution = false;
    private boolean wasSaved = false;
    private Path outFilePath = null;
    private Thread currentlyExecutedTaskThread = null;

    private void addSpecificServer() {
        String addr = serverAddressTextField.getText();
        int port;
        try {
            port = Integer.parseUnsignedInt(serverPortFormattedTextField.getText());
        } catch (NumberFormatException ex) {
            serverAddErrorEditorPane.setText("Invalid port number. Possible range [1024..65535]");
            return;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return;
        }

        try {
            serverManager.addServer(addr, port);
        } catch (Exception ex) {
            log.warn(ex.getMessage());
            serverAddErrorEditorPane.setText(ex.getMessage());
            return;
        }

        for (final Server server : serverManager.getServers().values()) {
            if (server.getHost().equals(addr) && server.getPort() == port) {
                serverFeatureTree.addServersToTree(serverManager, List.of(server));
                taskQueueTable.addUuidToSelectionSet(server.getConfiguration().getUuid());
                break;
            }
        }

        serverAddErrorEditorPane.setText(NO_ERROR_STR);
        serverFeatureTree.setRootVisible(false);
        addServerDialog.setVisible(false);
        addServerDialog.dispose();
    }

    /**
     * Creates new form OrchestratorGui
     */
    public OrchestratorGui() {
        initComponents();
        initTaskQueueTable();
        initServerTree();
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
        addServerDialog.setAlwaysOnTop(true);
        addServerDialog.setIconImage(ICON_IMG);
        addServerDialog.setPreferredSize(new java.awt.Dimension(400, 280));
        addServerDialog.setResizable(false);
        addServerDialog.setLocationRelativeTo(null);
        java.awt.GridBagLayout addServerDialogLayout = new java.awt.GridBagLayout();
        addServerDialogLayout.columnWidths = new int[] {2};
        addServerDialogLayout.rowHeights = new int[] {6};
        addServerDialogLayout.columnWeights = new double[] {0.5, 0.5};
        addServerDialog.getContentPane().setLayout(addServerDialogLayout);

        serverAddressLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        serverAddressLabel.setText("Server Address");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        addServerDialog.getContentPane().add(serverAddressLabel, gridBagConstraints);

        serverAddressTextField.setToolTipText("e.g. localhost, 192.168.0.2");
        serverAddressTextField.setPreferredSize(new java.awt.Dimension(64, 32));
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
        serverPortLabel.setText("Server Port");
        serverPortLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        addServerDialog.getContentPane().add(serverPortLabel, gridBagConstraints);

        serverPortFormattedTextField.setToolTipText("e.g. 50052, 55001 ");
        serverPortFormattedTextField.setPreferredSize(new java.awt.Dimension(64, 32));
        final NumberFormatter formatter = new NumberFormatter(new DecimalFormat("#0"));
        formatter.setMinimum(1024);
        formatter.setMaximum(65535);
        formatter.setAllowsInvalid(true);
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
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        addServerDialog.getContentPane().add(serverPortFormattedTextField, gridBagConstraints);

        serverAddErrorScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        serverAddErrorScrollPane.setEnabled(false);
        serverAddErrorScrollPane.setFocusable(false);
        serverAddErrorScrollPane.setMinimumSize(new java.awt.Dimension(64, 48));
        serverAddErrorScrollPane.setPreferredSize(new java.awt.Dimension(64, 52));

        serverAddErrorEditorPane.setEditable(false);
        serverAddErrorEditorPane.setText(NO_ERROR_STR);
        serverAddErrorEditorPane.setEnabled(false);
        serverAddErrorEditorPane.setFocusable(false);
        serverAddErrorEditorPane.setMargin(new java.awt.Insets(4, 4, 4, 24));
        serverAddErrorScrollPane.setViewportView(serverAddErrorEditorPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        addServerDialog.getContentPane().add(serverAddErrorScrollPane, gridBagConstraints);

        serverDialogOkBtn.setMnemonic('o');
        serverDialogOkBtn.setText("Ok");
        serverDialogOkBtn.setPreferredSize(new java.awt.Dimension(80, 42));
        serverDialogOkBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverDialogOkBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 5);
        addServerDialog.getContentPane().add(serverDialogOkBtn, gridBagConstraints);

        serverDialogCancelBtn.setMnemonic('c');
        serverDialogCancelBtn.setText("Cancel");
        serverDialogCancelBtn.setPreferredSize(new java.awt.Dimension(80, 42));
        serverDialogCancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverDialogCancelBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 10);
        addServerDialog.getContentPane().add(serverDialogCancelBtn, gridBagConstraints);

        addServerDialog.getAccessibleContext().setAccessibleParent(this);

        aboutDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        aboutDialog.setTitle("About");
        aboutDialog.setAlwaysOnTop(true);
        aboutDialog.setIconImage(ICON_IMG);
        aboutDialog.setMinimumSize(new java.awt.Dimension(300, 256));
        aboutDialog.setModal(true);
        aboutDialog.setResizable(false);
        aboutDialog.setLocationRelativeTo(null);
        java.awt.GridBagLayout aboutDialogLayout = new java.awt.GridBagLayout();
        aboutDialogLayout.columnWidths = new int[] {1};
        aboutDialogLayout.rowHeights = new int[] {3};
        aboutDialog.getContentPane().setLayout(aboutDialogLayout);

        aboutLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        aboutLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/sila-orchestrator-128px.png"))); // NOI18N
        aboutLabel.setText("<html><h1>sila-orchestrator</h1<p>" + COPYRIGHT_NOTICE + "</p></html>");
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
        removeTaskFromQueueMenuItem.setEnabled(false);
        removeTaskFromQueueMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeTaskFromQueue(evt);
            }
        });
        taskQueuePopupMenu.add(removeTaskFromQueueMenuItem);

        execRowEntryMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/execute-16px.png"))); // NOI18N
        execRowEntryMenuItem.setMnemonic('x');
        execRowEntryMenuItem.setText("Execute Entry");
        execRowEntryMenuItem.setEnabled(false);
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
        jPanel1Layout.columnWidths = new int[] {2};
        jPanel1Layout.rowHeights = new int[] {2};
        serverPanel.setLayout(jPanel1Layout);

        serverTreeScrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 2, 0));
        serverTreeScrollPane.setViewportView(serverFeatureTree);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        serverPanel.add(serverTreeScrollPane, gridBagConstraints);

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
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
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
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.8;
        serverPanel.add(scanServerBtn, gridBagConstraints);

        serverSplitPane.setLeftComponent(serverPanel);

        mainPanel.setPreferredSize(new java.awt.Dimension(512, 409));
        mainPanel.setLayout(new javax.swing.BoxLayout(mainPanel, javax.swing.BoxLayout.PAGE_AXIS));

        mainPanelSplitPane.setDividerLocation(300);
        mainPanelSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        mainPanelSplitPane.setContinuousLayout(true);

        java.awt.GridBagLayout taskQueuePanelLayout = new java.awt.GridBagLayout();
        taskQueuePanelLayout.columnWidths = new int[] {3};
        taskQueuePanelLayout.rowHeights = new int[] {4};
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
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        taskQueuePanel.add(addTaskToQueueBtn, gridBagConstraints);

        executeAllBtn.setIcon(IconProvider.QUEUE_EXEC_START.getIcon());
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
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        taskQueuePanel.add(executeAllBtn, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        taskQueuePanel.add(taskQueueScrollPane, gridBagConstraints);

        showOrHideTableColumnBtn.setText("..."); // NOI18N
        showOrHideTableColumnBtn.setToolTipText("Show/hide table columns.");
        showOrHideTableColumnBtn.setMaximumSize(new java.awt.Dimension(64, 24));
        showOrHideTableColumnBtn.setMinimumSize(new java.awt.Dimension(36, 24));
        showOrHideTableColumnBtn.setPreferredSize(new java.awt.Dimension(36, 24));
        showOrHideTableColumnBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showOrHideTableColumnBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        taskQueuePanel.add(showOrHideTableColumnBtn, gridBagConstraints);

        moveTaskUpBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/move-up.png"))); // NOI18N
        moveTaskUpBtn.setToolTipText("Move selcted task one place up in the queue order");
        moveTaskUpBtn.setEnabled(false);
        moveTaskUpBtn.setMaximumSize(new java.awt.Dimension(64, 1024));
        moveTaskUpBtn.setMinimumSize(new java.awt.Dimension(36, 36));
        moveTaskUpBtn.setPreferredSize(new java.awt.Dimension(36, 48));
        moveTaskUpBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveTaskUpBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 0.5;
        taskQueuePanel.add(moveTaskUpBtn, gridBagConstraints);

        moveTaskDownBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/move-down.png"))); // NOI18N
        moveTaskDownBtn.setToolTipText("Move selected task one place down in the queue order");
        moveTaskDownBtn.setEnabled(false);
        moveTaskDownBtn.setMaximumSize(new java.awt.Dimension(64, 1024));
        moveTaskDownBtn.setMinimumSize(new java.awt.Dimension(36, 36));
        moveTaskDownBtn.setPreferredSize(new java.awt.Dimension(36, 48));
        moveTaskDownBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveTaskDownBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 0.5;
        taskQueuePanel.add(moveTaskDownBtn, gridBagConstraints);

        removeTaskFromQueueBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/task-remove.png"))); // NOI18N
        removeTaskFromQueueBtn.setToolTipText("Remove selected task from queue");
        removeTaskFromQueueBtn.setEnabled(false);
        removeTaskFromQueueBtn.setMaximumSize(new java.awt.Dimension(64, 38));
        removeTaskFromQueueBtn.setMinimumSize(new java.awt.Dimension(36, 24));
        removeTaskFromQueueBtn.setPreferredSize(new java.awt.Dimension(36, 32));
        removeTaskFromQueueBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeTaskFromQueue(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        taskQueuePanel.add(removeTaskFromQueueBtn, gridBagConstraints);

        mainPanelSplitPane.setLeftComponent(taskQueuePanel);
        mainPanelSplitPane.setRightComponent(presenterScrollPane);

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

        toolBarSeparator.setSeparatorSize(new java.awt.Dimension(24, 32));
        toolBar.add(toolBarSeparator);

        addDelayBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/delay-add.png"))); // NOI18N
        addDelayBtn.setText("Add Delay");
        addDelayBtn.setToolTipText("Adds a delay in the task queue.");
        addDelayBtn.setFocusable(false);
        addDelayBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addDelayBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addDelayBtn.setTransferHandler(new TaskExportTransferHandler(() -> (new DelayTask())));
        addDelayBtn.setDropTarget(null);
        addDelayBtn.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                addTaskBtnMouseDragged(evt);
            }
        });
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
        addLocalExecBtn.setTransferHandler(new TaskExportTransferHandler(() -> (new LocalExecTask())));
        addLocalExecBtn.setDropTarget(null);
        addLocalExecBtn.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                addTaskBtnMouseDragged(evt);
            }
        });
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
        taskQueueTable.setParamsPane(presenterScrollPane);
        taskQueueTable.setComponentPopupMenu(taskQueuePopupMenu);
        taskQueueScrollPane.setViewportView(taskQueueTable);
        taskQueueTable.getModel().addTableModelListener((TableModelEvent evt) -> {
            int evtType = evt.getType();
            if (evtType == TableModelEvent.INSERT) {
                final TableModel model = (TableModel) evt.getSource();
                if (model.getRowCount() == 1) {
                    enableTaskQueueOperations();
                }
            } else if (evtType == TableModelEvent.DELETE) {
                final TableModel model = (TableModel) evt.getSource();
                if (model.getRowCount() <= 0) {
                    disableTaskQueueOperations();
                    presenterScrollPane.setViewportView(null);
                }
            }
        });
        taskQueueTable.getSelectionModel().addListSelectionListener((ListSelectionEvent lse) -> {
            if (lse.getValueIsAdjusting()) {
                return;
            }
            viewSelectedTask();
        });
        taskQueueTable.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "removeTask");
        taskQueueTable.getActionMap().put("removeTask", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                removeTaskFromQueue(evt);
            }
        });
        serverManager.addServerListener(taskQueueTable.getServerChangeListener());
    }

    private void initServerTree() {
        serverManager.addServerListener(serverFeatureTree.getServerChangeListener());
        serverFeatureTree.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (evt.isTemporary()) {
                    return;
                }
                // only refresh view when the taskQueuTable lost the focus
                if (evt.getOppositeComponent() == taskQueueTable) {
                    taskQueueTable.getSelectionModel().clearSelection();
                    presenterScrollPane.setViewportView(serverFeatureTree.getPresenter());
                }
            }
        });
        serverFeatureTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent evt) {
                boolean isAddTaskBtnToEnable = false;
                if (evt.getPath().getLastPathComponent() instanceof CommandTreeNode) {
                    isAddTaskBtnToEnable = true;
                }
                addTaskToQueueBtn.setEnabled(isAddTaskBtnToEnable);
                presenterScrollPane.setViewportView(serverFeatureTree.getPresenter());
            }
        });
    }

    /**
     * Loads the presenter of the selected task-queue entry into the context sensitive view panel
     * and sets the state of the affected GUI controls accordingly.
     */
    private void viewSelectedTask() {
        int selectedRowIdx = taskQueueTable.getSelectedRow();
        if (selectedRowIdx < 0) {
            return;
        }
        if (!serverFeatureTree.isSelectionEmpty()) {
            serverFeatureTree.getSelectionModel().clearSelection();
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
        execRowEntryMenuItem.setEnabled(isTaskRemoveEnabled);
        final QueueTask entry = taskQueueTable.getTaskFromRow(selectedRowIdx);
        if (entry == null) {
            return;
        }
        addTaskToQueueBtn.setEnabled(false);
        presenterScrollPane.setViewportView(entry.getPresenter());
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
     * Scans the network for available SiLA-Servers which are enabled for discovery. The
     * scan-routine runs in a dedicated thread to avoid freezing while scanning. The synchronization
     * with the involved GUI components has to be done with
     * <code>SwingUtilities.invokeLater(() -> { ... });</code> to grant thread safety.
     */
    private void scanNetworkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scanNetworkActionPerformed
        scanServerBtn.setEnabled(false);
        final DefaultTreeModel model = (DefaultTreeModel) serverFeatureTree.getModel();
        final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) model.getRoot();
        rootNode.removeAllChildren();
        serverManager.clear();

        final Runnable scan = () -> {
            serverManager.getDiscovery().scanNetwork();
            final List<Server> serverList = ServerFinder.filterBy(ServerFinder.Filter.status(Server.Status.ONLINE)).find();
            final boolean isTreeRootVisible;
            if (!serverList.isEmpty()) {
                // hide the "No Server Available" string.
                isTreeRootVisible = false;
                serverFeatureTree.addServersToTree(serverManager, serverList);
                for (final Server server : serverList) {
                    taskQueueTable.addUuidToSelectionSet(server.getConfiguration().getUuid());
                }
            } else {
                // show the "No Server Available" string.
                isTreeRootVisible = true;
                model.reload();
            }

            // update components in the GUI thread
            SwingUtilities.invokeLater(() -> {
                serverFeatureTree.setRootVisible(isTreeRootVisible);
                serverFeatureTree.setEnabled(!isTreeRootVisible);
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

    private void addTaskToQueueBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTaskToQueueBtnActionPerformed
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) serverFeatureTree.getLastSelectedPathComponent();
        if (node == null) {
            return;
        }

        if (node.isLeaf()) {
            if (node instanceof CommandTreeNode) {
                final CommandTreeNode cmdNode = (CommandTreeNode) node;
                // use the selected node to create a new table entry.
                taskQueueTable.addCommandTask(cmdNode.createTableEntry());
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

        executeAllBtn.setIcon(IconProvider.QUEUE_EXEC_STOP.getIcon());
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
                executeAllBtn.setIcon(IconProvider.QUEUE_EXEC_START.getIcon());
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
        execRowEntryMenuItem.setEnabled(false);
        if (taskQueueTable.isEditing()) {
            // enforce to stop editing before removing the row
            taskQueueTable.getCellEditor().stopCellEditing();
        }
        taskQueueTable.removeRow(selectedRowIdx);
    }//GEN-LAST:event_removeTaskFromQueue

    private void saveFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveFileActionPerformed
        if (!wasSaved || outFilePath == null) {
            saveAsActionPerformed(evt);
        } else {
            // TODO: give the user some kind of notification that the file was saved
            final TaskQueueData tqd = TaskQueueData.createFromTaskQueue(taskQueueTable);
            try {
                TaskQueueData.writeToFile(outFilePath, tqd);
                log.info("Saved " + outFilePath);
            } catch (IOException ex) {
                log.error(ex.getMessage());
            }
        }
    }//GEN-LAST:event_saveFileActionPerformed

    private void saveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsActionPerformed
        fileSaveAsChooser.setSelectedFile(new File(LocalDate.now().toString() + ".silo"));
        int retVal = fileSaveAsChooser.showSaveDialog(this);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            final Path outPath = Paths.get(fileSaveAsChooser.getSelectedFile().getAbsolutePath());
            outFilePath = outPath;
            int userDesition = JOptionPane.OK_OPTION;
            if (Files.exists(outPath)) {
                userDesition = JOptionPane.showConfirmDialog(this,
                        "File \"" + outPath.getFileName() + "\" already exists in \""
                        + outPath.getParent() + "\"!\n"
                        + "Do you want to overwrite the existing file?",
                        "Overwrite File",
                        JOptionPane.INFORMATION_MESSAGE,
                        JOptionPane.YES_NO_CANCEL_OPTION);
            }

            if (userDesition == JOptionPane.OK_OPTION) {
                TaskQueueData tqd = TaskQueueData.createFromTaskQueue(taskQueueTable);
                try {
                    TaskQueueData.writeToFile(outPath, tqd);
                    wasSaved = true;
                    log.info("Saved as file " + outPath);
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

            StringBuilder outMsg = new StringBuilder();
            final TaskQueueData tqd = TaskQueueData.createFromFile(file.getAbsolutePath(), outMsg);
            if (tqd == null) {
                if (!outMsg.toString().isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            outMsg,
                            "Import Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                return;
            }
            clearQueueActionPerformed(evt);
            tqd.importToTaskQueue(taskQueueTable, serverManager.getServers());
        }
    }//GEN-LAST:event_openFileActionPerformed

    private void clearQueueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearQueueActionPerformed
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
    }//GEN-LAST:event_addDelayTaskActionPerformed

    private void addLocalExecTaskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addLocalExecTaskActionPerformed
        taskQueueTable.addTask(new LocalExecTask());
    }//GEN-LAST:event_addLocalExecTaskActionPerformed

    private void addTaskBtnMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addTaskBtnMouseDragged
        JComponent comp = (JComponent) evt.getSource();
        TransferHandler handler = comp.getTransferHandler();
        handler.exportAsDrag(comp, evt, TransferHandler.COPY);
    }//GEN-LAST:event_addTaskBtnMouseDragged

    private void showOrHideTableColumnBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showOrHideTableColumnBtnActionPerformed
        final JPopupMenu columnPopupMenu = taskQueueTable.getColumnHeaderPopupMenu();
        if (!columnPopupMenu.isVisible()) {
            columnPopupMenu.show(showOrHideTableColumnBtn, 0, showOrHideTableColumnBtn.getHeight());
        } else {
            columnPopupMenu.setVisible(false);
        }
    }//GEN-LAST:event_showOrHideTableColumnBtnActionPerformed

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
        removeTaskFromQueueBtn.setEnabled(false);
        removeTaskFromQueueMenuItem.setEnabled(false);
        execRowEntryMenuItem.setEnabled(false);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        final Properties properties = new Properties();
        try {
            // retrieve version info from the maven git plug-in
            properties.load(OrchestratorGui.class.getClassLoader().getResourceAsStream("git.properties"));
            serverManager = ServerManager.getInstance();
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
            for (int i = 0; i < args.length; i++) {
                final String arg = args[i];
                if (arg.equalsIgnoreCase("-h") || arg.equalsIgnoreCase("--help")) {
                    System.out.println("Usage: java -jar orchestrator-client-exec.jar [args]"
                            + "\n -h, --help"
                            + "\n\t Print this help message."
                            + "\n -v, --version"
                            + "\n\t Print the version number."
                            + "\n --about, --info"
                            + "\n\t Print some general information about this software."
                            + "\n -x <silo file>, --execute <silo file>"
                            + "\n\t Loads and executes the provided silo file.");
                } else if (arg.equalsIgnoreCase("-v") || arg.equalsIgnoreCase("--version")) {
                    System.out.println(silaOrchestratorVersion);
                } else if (arg.equalsIgnoreCase("--about") || arg.equalsIgnoreCase("--info")) {
                    System.out.println("sila-orchestrator"
                            + "\n " + COPYRIGHT_NOTICE
                            + "\n Version: " + silaOrchestratorVersion
                            + "\n Git Commit: " + gitCommit
                            + "\n Timestamp: " + gitCommitTimestamp
                            + "\n Git Repository: " + gitRepositoryUrl
                            + "\n E-Mail: florian.bauer.dev@gmail.com"
                            + "\n License: Apache-2.0");
                } else if (arg.equalsIgnoreCase("-x") || arg.equalsIgnoreCase("--execute")) {
                    if (i + 1 < args.length) {
                        final String siloFile = args[i + 1];
                        final StringBuilder outMsg = new StringBuilder();
                        final TaskQueueData tcd = TaskQueueData.createFromFile(siloFile, outMsg);
                        if (tcd != null) {
                            TaskQueueTable tqt = new TaskQueueTable();
                            tcd.importToTaskQueue(tqt, serverManager.getServers());
                            for (int j = 0; j < tqt.getRowCount(); j++) {
                                final QueueTask task = tqt.getTaskFromRow(j);
                                task.run();
                                if (task.getState() != TaskState.FINISHED_SUCCESS) {
                                    // apply execution policy
                                    if (tqt.getTaskPolicyFromRow(i) == ExecPolicy.HALT_AFTER_ERROR) {
                                        System.out.println("Halted after task #" + j
                                                + " \"" + task.toString()
                                                + "\" with state " + task.getState().toString()
                                                + " at " + task.getEndTimeStamp() + ".");
                                        break;
                                    }
                                } else {
                                    System.out.println("Finished task #" + j
                                            + " \"" + task.toString()
                                            + "\" with state " + task.getState().toString()
                                            + " at " + task.getEndTimeStamp() + ".");
                                }
                            }
                        } else {
                            System.err.println(outMsg);
                        }
                        i++;
                    } else {
                        System.err.println("Path to silo file is missing.");
                    }
                } else {
                    System.err.println("Unknown argument \"" + arg + "\".");
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
    private final javax.swing.JMenuItem execRowEntryMenuItem = new javax.swing.JMenuItem();
    private final javax.swing.JButton executeAllBtn = new javax.swing.JButton();
    private final javax.swing.JMenuItem executeAllMenuItem = new javax.swing.JMenuItem();
    private final javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
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
    private final javax.swing.JScrollPane presenterScrollPane = new javax.swing.JScrollPane();
    private final javax.swing.JButton removeTaskFromQueueBtn = new javax.swing.JButton();
    private final javax.swing.JMenuItem removeTaskFromQueueMenuItem = new javax.swing.JMenuItem();
    private final javax.swing.JMenuItem saveAsMenuItem = new javax.swing.JMenuItem();
    private final javax.swing.JButton saveFileBtn = new javax.swing.JButton();
    private final javax.swing.JMenuItem saveMenuItem = new javax.swing.JMenuItem();
    private final javax.swing.JMenuItem scanNetworkMenuItem = new javax.swing.JMenuItem();
    private final javax.swing.JButton scanServerBtn = new javax.swing.JButton();
    private final javax.swing.JEditorPane serverAddErrorEditorPane = new javax.swing.JEditorPane();
    private final javax.swing.JScrollPane serverAddErrorScrollPane = new javax.swing.JScrollPane();
    private final javax.swing.JLabel serverAddressLabel = new javax.swing.JLabel();
    private final javax.swing.JTextField serverAddressTextField = new javax.swing.JTextField();
    private final javax.swing.JButton serverDialogCancelBtn = new javax.swing.JButton();
    private final javax.swing.JButton serverDialogOkBtn = new javax.swing.JButton();
    private final javax.swing.JMenu serverMenu = new javax.swing.JMenu();
    private final javax.swing.JPanel serverPanel = new javax.swing.JPanel();
    private final javax.swing.JFormattedTextField serverPortFormattedTextField = new javax.swing.JFormattedTextField();
    private final javax.swing.JLabel serverPortLabel = new javax.swing.JLabel();
    private final javax.swing.JSplitPane serverSplitPane = new javax.swing.JSplitPane();
    private final javax.swing.JScrollPane serverTreeScrollPane = new javax.swing.JScrollPane();
    private final javax.swing.JButton showOrHideTableColumnBtn = new javax.swing.JButton();
    private final javax.swing.JPanel taskQueuePanel = new javax.swing.JPanel();
    private final javax.swing.JPopupMenu taskQueuePopupMenu = new javax.swing.JPopupMenu();
    private final javax.swing.JScrollPane taskQueueScrollPane = new javax.swing.JScrollPane();
    private final javax.swing.JMenu tasksMenu = new javax.swing.JMenu();
    private final javax.swing.JToolBar toolBar = new javax.swing.JToolBar();
    private final javax.swing.JToolBar.Separator toolBarSeparator = new javax.swing.JToolBar.Separator();
    // End of variables declaration//GEN-END:variables
}
