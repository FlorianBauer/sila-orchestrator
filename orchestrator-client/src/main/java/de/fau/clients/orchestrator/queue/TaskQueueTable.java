package de.fau.clients.orchestrator.queue;

import de.fau.clients.orchestrator.dnd.TaskImportTransferHandler;
import de.fau.clients.orchestrator.tasks.CommandTask;
import de.fau.clients.orchestrator.tasks.ConnectionStatus;
import de.fau.clients.orchestrator.tasks.ExecPolicy;
import de.fau.clients.orchestrator.tasks.QueueTask;
import de.fau.clients.orchestrator.tasks.TaskState;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.UUID;
import javax.swing.AbstractCellEditor;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.DropMode;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import lombok.extern.slf4j.Slf4j;
import sila_java.library.manager.ServerListener;
import sila_java.library.manager.models.Server;

/**
 * Table component responsible for managing queue-tasks with all their properties.
 */
@Slf4j
@SuppressWarnings("serial")
public final class TaskQueueTable extends JTable implements ServerListener {

    public static final int COLUMN_TASK_ID_IDX = 0;
    public static final int COLUMN_CONNECTION_STATUS_IDX = 1;
    public static final int COLUMN_TASK_INSTANCE_IDX = 2;
    public static final int COLUMN_SERVER_UUID_IDX = 3;
    public static final int COLUMN_EXEC_POLICY_IDX = 4;
    public static final int COLUMN_STATE_IDX = 5;
    public static final int COLUMN_START_TIME_IDX = 6;
    public static final int COLUMN_END_TIME_IDX = 7;
    public static final int COLUMN_DURATION_IDX = 8;
    public static final int COLUMN_RESULT_IDX = 9;

    public static final String[] COLUMN_TITLES = {
        "ID",
        "Connection",
        "Task",
        "Server UUID",
        "Policy",
        "State",
        "Start Time",
        "End Time",
        "Duration",
        "Result"
    };

    private static final JLabel EMPTY_LABEL = new JLabel(" - ");
    private static final int INIT_TASK_ID = 1;
    private static int taskId = INIT_TASK_ID;
    private final TableColumnHider tch;
    private final JPopupMenu taskQueueHeaderPopupMenu = new JPopupMenu();
    private final JCheckBoxMenuItem[] headerItems = new JCheckBoxMenuItem[COLUMN_TITLES.length];

    /**
     * Set to keep track of available SiLA server.
     */
    private final HashSet<UUID> serverUuidSet = new HashSet<>();
    private final JComboBox<UUID> uuidComboBox = new JComboBox<>();

    /**
     * Widget to set the execution policy for the tasks.
     */
    private final JComboBox<ExecPolicy> policyComboBox = new JComboBox<>(ExecPolicy.values());

    /**
     * Set to track task IDs and ensure uniqueness.
     */
    private final HashSet<Integer> taskIdSet = new HashSet<>();
    private JScrollPane paramsPane = null;

    public TaskQueueTable() {
        super(new TaskQueueTableModel());
        this.setFillsViewportHeight(true);
        this.setRowHeight(32);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.setRowSelectionAllowed(true);
        this.setColumnSelectionAllowed(false);
        this.setTransferHandler(new TaskImportTransferHandler());
        this.setDragEnabled(false);
        this.setDropMode(DropMode.INSERT_ROWS);
        tch = new TableColumnHider(columnModel, COLUMN_TITLES);
        columnModel.getColumn(COLUMN_CONNECTION_STATUS_IDX).setMaxWidth(48);
        columnModel.getColumn(COLUMN_STATE_IDX).setMaxWidth(48);
        columnModel.getColumn(COLUMN_START_TIME_IDX).setPreferredWidth(170);
        columnModel.getColumn(COLUMN_END_TIME_IDX).setPreferredWidth(170);

        final TableColumn taskIdColumn = columnModel.getColumn(COLUMN_TASK_ID_IDX);
        taskIdColumn.setPreferredWidth(48);
        taskIdColumn.setMaxWidth(64);
        taskIdColumn.setCellEditor(new TaskIdCellEditor(new TaskIdVerifier()));

        final TableColumn uuidColumn = columnModel.getColumn(COLUMN_SERVER_UUID_IDX);
        uuidColumn.setCellRenderer(new UuidCellRenderer());
        uuidColumn.setCellEditor(new DefaultCellEditor(uuidComboBox));
        uuidComboBox.addActionListener(evt -> {
            changeTaskUuidActionPerformed();
        });

        final TableColumn resultColumn = columnModel.getColumn(COLUMN_RESULT_IDX);
        resultColumn.setMaxWidth(52);
        // Set the editor and renderer for the result cell to view the returned response.
        resultColumn.setCellRenderer(new ResultCellEditor());
        resultColumn.setCellEditor(new ResultCellEditor());

        final TableColumn policyColumn = columnModel.getColumn(COLUMN_EXEC_POLICY_IDX);
        policyColumn.setCellRenderer(new ExecPolicyCellRenderer());
        policyColumn.setCellEditor(new DefaultCellEditor(policyComboBox));
        policyComboBox.addActionListener(evt -> {
            changeTaskPolicyActionPerformed();
        });

        // hidden on default
        tch.hideColumn(COLUMN_SERVER_UUID_IDX);
        tch.hideColumn(COLUMN_START_TIME_IDX);
        tch.hideColumn(COLUMN_END_TIME_IDX);

        for (int i = 0; i < TaskQueueTable.COLUMN_TITLES.length; i++) {
            if (i == COLUMN_TASK_INSTANCE_IDX) {
                // Do not allow the user to hide the task column.
                continue;
            }

            final int colIdx = i;
            final boolean isChecked;
            if (colIdx == COLUMN_SERVER_UUID_IDX
                    || colIdx == COLUMN_START_TIME_IDX
                    || colIdx == COLUMN_END_TIME_IDX) {
                // uncheck hidden columns
                isChecked = false;
            } else {
                isChecked = true;
            }
            headerItems[colIdx] = new JCheckBoxMenuItem();
            headerItems[colIdx].setSelected(isChecked);
            headerItems[colIdx].setText(COLUMN_TITLES[colIdx]);
            headerItems[colIdx].addActionListener(evt -> {
                if (headerItems[colIdx].isSelected()) {
                    tch.showColumn(colIdx);
                } else {
                    tch.hideColumn(colIdx);
                }
            });
            taskQueueHeaderPopupMenu.add(headerItems[i]);
        }

        this.tableHeader.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                // show popup-menu on right-click
                if (evt.getButton() == MouseEvent.BUTTON3) {
                    taskQueueHeaderPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        });
    }

    /**
     * Clears all entries in the entire table. Current cell-editing operations get canceled and all
     * used task IDs are released as well.
     */
    public void clearTable() {
        final TableCellEditor ce = getCellEditor();
        if (ce != null) {
            // abort editing before purging the entries
            ce.stopCellEditing();
        }
        ((TaskQueueTableModel) dataModel).setRowCount(0);
        taskId = INIT_TASK_ID;
        taskIdSet.clear();
    }

    public void removeRow(int rowIdx) {
        taskIdSet.remove((int) dataModel.getValueAt(rowIdx, COLUMN_TASK_ID_IDX));
        ((TaskQueueTableModel) dataModel).removeRow(rowIdx);
    }

    public void moveRow(int sourceRowIdx, int targetRowIdx) {
        ((TaskQueueTableModel) dataModel).moveRow(sourceRowIdx, sourceRowIdx, targetRowIdx);
    }

    public void setParamsPane(final JScrollPane pane) {
        this.paramsPane = pane;
    }

    public int getTaskIdFromRow(int rowIdx) {
        return Integer.parseInt(dataModel.getValueAt(rowIdx, COLUMN_TASK_ID_IDX).toString());
    }

    public QueueTask getTaskFromRow(int rowIdx) {
        return (QueueTask) dataModel.getValueAt(rowIdx, COLUMN_TASK_INSTANCE_IDX);
    }

    public ExecPolicy getTaskPolicyFromRow(int rowIdx) {
        return (ExecPolicy) dataModel.getValueAt(rowIdx, COLUMN_EXEC_POLICY_IDX);
    }

    /**
     * Resets the run-time states of every task in the queue. The the following fields get set to
     * their default values:
     * <ul>
     * <li>state icon</li>
     * <li>start-time</li>
     * <li>end-time</li>
     * <li>duration</li>
     * <li>result</li>
     * </ul>
     */
    public void resetAllTaskStates() {
        ((TaskQueueTableModel) dataModel).resetTaskStates();
    }

    /**
     * Adds the given command task to the queue table and sets the selection focus on the added
     * item.
     *
     * @param cmdTask The command task to add.
     *
     * @see CommandTask
     */
    public void addCommandTask(final CommandTask cmdTask) {
        addUuidToSelectionSet(cmdTask.getServerUuid());
        final TaskQueueTableModel tqtModel = (TaskQueueTableModel) dataModel;
        int rowIdx = tqtModel.getRowCount();
        tqtModel.addCommandTask(generateAndRegisterTaskId(),
                cmdTask,
                ExecPolicy.HALT_AFTER_ERROR);
        selectionModel.setSelectionInterval(rowIdx, rowIdx);
    }

    /**
     * Inserts the given command task into the queue table at the given row index and sets the
     * selection focus on the added item.
     *
     * @param rowIdx The row index position in the table where to insert the task.
     * @param cmdTask The command task to add.
     *
     * @see CommandTask
     */
    public void insertCommandTask(int rowIdx, final CommandTask cmdTask) {
        addUuidToSelectionSet(cmdTask.getServerUuid());
        final TaskQueueTableModel tqtModel = (TaskQueueTableModel) dataModel;
        tqtModel.insertCommandTask(rowIdx,
                generateAndRegisterTaskId(),
                cmdTask,
                ExecPolicy.HALT_AFTER_ERROR);
        selectionModel.setSelectionInterval(rowIdx, rowIdx);
    }

    /**
     * Adds the given command task to the queue table.
     *
     * @param taskId The task ID to use for this entry.
     * @param cmdTask The command task to add.
     * @param policy The execution policy.
     *
     * @see CommandTask
     * @see #addCommandTask
     */
    public void addCommandTaskWithId(
            int taskId,
            final CommandTask cmdTask,
            final ExecPolicy policy
    ) {
        final int uniqueId;
        if (checkAndRegisterTaskId(taskId)) {
            uniqueId = taskId;
        } else {
            uniqueId = generateAndRegisterTaskId();
        }
        addUuidToSelectionSet(cmdTask.getServerUuid());
        final TaskQueueTableModel tqtModel = (TaskQueueTableModel) dataModel;
        tqtModel.addCommandTask(uniqueId, cmdTask, policy);
    }

    /**
     * Adds the given queue task to the table. The execution policy is set to
     * <code>ExecPolicy.HALT_AFTER_ERROR</code> on default.
     *
     * @param task The queue task to add.
     *
     * @see QueueTask
     */
    public void addTask(final QueueTask task) {
        final TaskQueueTableModel tqtModel = (TaskQueueTableModel) dataModel;
        tqtModel.addTask(generateAndRegisterTaskId(), task, ExecPolicy.HALT_AFTER_ERROR);
    }

    /**
     * Inserts the given command task into the queue table at the given row index. The execution
     * policy is set to <code>ExecPolicy.HALT_AFTER_ERROR</code> on default.
     *
     * @param idx The row index position in the table where to insert the task.
     * @param task The queue task to add.
     *
     * @see QueueTask
     */
    public void insertTask(int idx, final QueueTask task) {
        final TaskQueueTableModel tqtModel = (TaskQueueTableModel) dataModel;
        tqtModel.insertTask(idx, generateAndRegisterTaskId(), task, ExecPolicy.HALT_AFTER_ERROR);
    }

    /**
     * Adds the given queue task to the table.
     *
     * @param taskId The task ID to use for this entry.
     * @param task The queue task to add.
     * @param policy The execution policy.
     *
     * @see QueueTask
     */
    public void addTaskWithId(int taskId, final QueueTask task, final ExecPolicy policy) {
        final int uniqueId;
        if (checkAndRegisterTaskId(taskId)) {
            uniqueId = taskId;
        } else {
            uniqueId = generateAndRegisterTaskId();
        }
        final TaskQueueTableModel tqtModel = (TaskQueueTableModel) dataModel;
        tqtModel.addTask(uniqueId, task, policy);
    }

    /**
     * Adds the given UUID to the selection set. This allows to re-assign a task to another UUID
     * listed in the set.
     *
     * @param serverUuid The server UUID to add to the selection set.
     */
    public void addUuidToSelectionSet(UUID serverUuid) {
        if (!serverUuidSet.contains(serverUuid)) {
            serverUuidSet.add(serverUuid);
            uuidComboBox.addItem(serverUuid);
        }
    }

    public void showColumn(int columnIdx) {
        if (columnIdx >= 0 && columnIdx < COLUMN_TITLES.length) {
            tch.showColumn(columnIdx);
            headerItems[columnIdx].setSelected(true);
        }
    }

    public void hideColumn(int columnIdx) {
        if (columnIdx >= 0 && columnIdx < COLUMN_TITLES.length) {
            tch.hideColumn(columnIdx);
            headerItems[columnIdx].setSelected(false);
        }
    }

    public JPopupMenu getColumnHeaderPopupMenu() {
        return this.taskQueueHeaderPopupMenu;
    }

    /**
     * Changes the server UUID of the given task to the UUID in the corresponding ComboBox of the
     * same row. Invalid UUIDs are allowed and the connection status icon changes accordingly as
     * well as the presenter.
     */
    private void changeTaskUuidActionPerformed() {
        if (editingRow >= 0) {
            final Object taskObj = dataModel.getValueAt(editingRow, COLUMN_TASK_INSTANCE_IDX);
            if (!(taskObj instanceof CommandTask)) {
                return;
            }
            final CommandTask task = (CommandTask) taskObj;
            final UUID serverUuid = (UUID) uuidComboBox.getSelectedItem();
            boolean wasChangeSuccess = task.changeServer(serverUuid);
            if (wasChangeSuccess) {
                dataModel.setValueAt(ConnectionStatus.ONLINE.getIcon(),
                        editingRow,
                        COLUMN_CONNECTION_STATUS_IDX);
            } else {
                dataModel.setValueAt(ConnectionStatus.OFFLINE.getIcon(),
                        editingRow,
                        COLUMN_CONNECTION_STATUS_IDX);
            }

            if (paramsPane != null) {
                // update the parameter panel if available
                paramsPane.setViewportView(task.getPresenter());
            }
        }
    }

    private void changeTaskPolicyActionPerformed() {
        if (editingRow >= 0) {
            final ExecPolicy policy = (ExecPolicy) policyComboBox.getSelectedItem();
            dataModel.setValueAt(policy, editingRow, COLUMN_EXEC_POLICY_IDX);
        }
    }

    private boolean checkAndRegisterTaskId(int taskId) {
        if (!taskIdSet.contains(taskId)) {
            taskIdSet.add(taskId);
            return true;
        }
        return false;
    }

    private int generateAndRegisterTaskId() {
        while (taskIdSet.contains(taskId)) {
            taskId++;
        }
        taskIdSet.add(taskId);
        return taskId;
    }

    /**
     * Listener for server status (online/offline) which changes the symbols in the queue table
     * accordingly.
     *
     * @param uuid The UUID of the changing server.
     * @param server The changing server instance.
     */
    @Override
    public void onServerChange(UUID uuid, Server server) {
        for (int i = 0; i < getRowCount(); i++) {
            final Object obj = dataModel.getValueAt(i, COLUMN_SERVER_UUID_IDX);
            if (obj instanceof UUID) {
                UUID taskUuid = (UUID) obj;
                if (taskUuid.compareTo(uuid) == 0) {
                    if (server.getStatus() == Server.Status.ONLINE) {
                        dataModel.setValueAt(ConnectionStatus.ONLINE.getIcon(),
                                i,
                                COLUMN_CONNECTION_STATUS_IDX);
                    } else {
                        dataModel.setValueAt(ConnectionStatus.OFFLINE.getIcon(),
                                i,
                                COLUMN_CONNECTION_STATUS_IDX);
                    }
                }
            }
        }
    }

    /**
     * Custom cell editor for task IDs.
     */
    private class TaskIdCellEditor extends DefaultCellEditor {

        /**
         * Verifier for ID uniqueness.
         */
        final InputVerifier verifier;
        /**
         * Initial ID before editing process begun.
         */
        String oldTaskId;

        public TaskIdCellEditor(InputVerifier verifier) {
            super(new JTextField());
            this.verifier = verifier;
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table,
                Object value,
                boolean isSelected,
                int row,
                int column
        ) {
            final JTextField tf = (JTextField) super.getTableCellEditorComponent(
                    table,
                    value,
                    isSelected,
                    row,
                    column);
            if (value != null) {
                oldTaskId = value.toString();
                tf.setText(oldTaskId);
            }
            return tf;
        }

        @Override
        public boolean stopCellEditing() {
            String value = getCellEditorValue().toString();
            if (value.isBlank() || value.equals(oldTaskId)) {
                cancelCellEditing();
                return true;
            }

            final boolean isValid = verifier.verify(editorComponent) && super.stopCellEditing();
            if (isValid) {
                try {
                    taskIdSet.add(Integer.parseInt(value));
                    taskIdSet.remove(Integer.parseInt(oldTaskId));
                } catch (final NumberFormatException ex) {
                    return false;
                }
            }
            return isValid;
        }
    }

    /**
     * Verifier to check if the edited task ID from the user is unique.
     */
    private class TaskIdVerifier extends InputVerifier {

        @Override
        public boolean verify(JComponent input) {
            final String text = ((JTextField) input).getText();
            int newTaskId;
            try {
                newTaskId = Integer.parseInt(text);
            } catch (final NumberFormatException ex) {
                return false;
            }

            if (newTaskId > 0) {
                if (!taskIdSet.contains(newTaskId)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * A custom cell renderer for displaying UUID objects in the table. This renderer shows the UUID
     * inside a <code>JComboBox</code> for the sole purpose of signaling the user a editable cell.
     */
    private static final class UuidCellRenderer implements TableCellRenderer {

        private final JComboBox<String> comboBox = new JComboBox<>();

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            if (value != null) {
                if (table.isCellEditable(row, column)) {
                    comboBox.getModel().setSelectedItem(value.toString());
                    return comboBox;
                }
            }
            return EMPTY_LABEL;
        }
    }

    /**
     * A custom cell renderer for displaying <code>ExecPolicy</code> objects in the table. This
     * renderer shows the <code>ExecPolicy</code> inside a <code>JComboBox</code> for the sole
     * purpose of signaling the user a editable cell.
     */
    private static final class ExecPolicyCellRenderer implements TableCellRenderer {

        private final JComboBox<String> comboBox = new JComboBox<>();

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            if (value != null) {
                comboBox.getModel().setSelectedItem(value.toString());
                return comboBox;
            }
            return EMPTY_LABEL;
        }
    }

    /**
     * Nested class to render/view the latest results provided by the command response.
     */
    private final class ResultCellEditor extends AbstractCellEditor implements TableCellEditor,
            TableCellRenderer {

        private static final int POPUP_WIDTH = 300;
        private final Dimension buttonDim = new Dimension(60, 24);
        private final JButton expandBtn;
        private final ButtonGroup btnGroup = new ButtonGroup();
        private final JToggleButton rawViewBtn;
        private final JToggleButton nodeViewBtn;
        private final JPopupMenu editorPopup;
        private final JEditorPane editorPane;
        private Object value;

        public ResultCellEditor() {
            editorPane = new JEditorPane();
            editorPane.setEditable(false);

            final JScrollPane scrollPane = new JScrollPane(editorPane);
            scrollPane.setAlignmentY(LEFT_ALIGNMENT);

            rawViewBtn = new JToggleButton("Raw");
            rawViewBtn.setAlignmentY(RIGHT_ALIGNMENT);
            rawViewBtn.setPreferredSize(buttonDim);
            rawViewBtn.addActionListener((final ActionEvent evt) -> {
                final QueueTask task = getTaskFromRow(editingRow);
                editorPane.setText(task.getLastExecResult());
                scrollPane.setViewportView(editorPane);
            });
            nodeViewBtn = new JToggleButton("Node");
            nodeViewBtn.setAlignmentY(RIGHT_ALIGNMENT);
            nodeViewBtn.setPreferredSize(buttonDim);
            nodeViewBtn.addActionListener((final ActionEvent evt) -> {
                final QueueTask queueTask = getTaskFromRow(editingRow);
                if (!(queueTask instanceof CommandTask)) {
                    return;
                }
                final CommandTask task = (CommandTask) queueTask;
                scrollPane.setViewportView(task.getResultPresenter());
            });

            btnGroup.add(rawViewBtn);
            btnGroup.add(nodeViewBtn);

            final Box btnBox = Box.createHorizontalBox();
            btnBox.setAlignmentY(RIGHT_ALIGNMENT);
            btnBox.add(Box.createHorizontalGlue());
            btnBox.add(rawViewBtn);
            btnBox.add(nodeViewBtn);

            editorPopup = new JPopupMenu();
            editorPopup.setAlignmentY(LEFT_ALIGNMENT);
            editorPopup.setPreferredSize(new Dimension(POPUP_WIDTH, 256));
            editorPopup.add(btnBox);
            editorPopup.add(scrollPane);

            expandBtn = new JButton("[...]");
            expandBtn.setAlignmentY(RIGHT_ALIGNMENT);
            expandBtn.addActionListener((final ActionEvent evt) -> {
                final QueueTask queueTask = getTaskFromRow(editingRow);
                if (!(queueTask instanceof CommandTask)) {
                    return;
                }
                final CommandTask task = (CommandTask) queueTask;
                if (task.getState() != TaskState.FINISHED_SUCCESS) {
                    rawViewBtn.setSelected(true);
                    nodeViewBtn.setEnabled(false);
                    editorPane.setText(task.getLastExecResult());
                    scrollPane.setViewportView(editorPane);
                } else {
                    nodeViewBtn.setEnabled(true);
                    nodeViewBtn.setSelected(true);
                    scrollPane.setViewportView(task.getResultPresenter());
                }
                editorPopup.show(expandBtn,
                        -(POPUP_WIDTH - expandBtn.getWidth()),
                        expandBtn.getHeight());
            });
        }

        @Override
        public Object getCellEditorValue() {
            return value;
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table,
                Object value,
                boolean isSelected,
                int row,
                int column
        ) {
            this.value = value;
            return expandBtn;
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            if (!value.toString().isEmpty()) {
                return expandBtn;
            }
            return EMPTY_LABEL;
        }
    }
}
