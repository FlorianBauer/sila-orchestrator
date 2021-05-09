package de.fau.clients.orchestrator.queue;

import de.fau.clients.orchestrator.ctx.ConnectionListener;
import de.fau.clients.orchestrator.ctx.ServerContext;
import de.fau.clients.orchestrator.dnd.TaskImportTransferHandler;
import de.fau.clients.orchestrator.tasks.CommandTask;
import de.fau.clients.orchestrator.tasks.ConnectionStatus;
import de.fau.clients.orchestrator.tasks.ExecPolicy;
import de.fau.clients.orchestrator.tasks.QueueTask;
import de.fau.clients.orchestrator.tasks.TaskState;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.UUID;
import javax.swing.DefaultCellEditor;
import javax.swing.DropMode;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * Table component responsible for managing queue-tasks with all their properties.
 */
@SuppressWarnings("serial")
public final class TaskQueueTable extends JTable implements ConnectionListener {

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

    public static final JLabel EMPTY_LABEL = new JLabel(" - ");
    private static final int INIT_TASK_ID = 1;
    private static int genericTaskId = INIT_TASK_ID;
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
        columnModel.getColumn(COLUMN_START_TIME_IDX).setPreferredWidth(170);
        columnModel.getColumn(COLUMN_END_TIME_IDX).setPreferredWidth(170);

        final TableColumn connectionStatusColumn = columnModel.getColumn(COLUMN_CONNECTION_STATUS_IDX);
        connectionStatusColumn.setMaxWidth(48);
        connectionStatusColumn.setCellRenderer(new ConnectionStatusCellRenderer());

        final TableColumn taskIdColumn = columnModel.getColumn(COLUMN_TASK_ID_IDX);
        taskIdColumn.setPreferredWidth(48);
        taskIdColumn.setMaxWidth(64);
        taskIdColumn.setCellEditor(new TaskIdCellEditor(taskIdSet));

        final TableColumn uuidColumn = columnModel.getColumn(COLUMN_SERVER_UUID_IDX);
        uuidColumn.setCellRenderer(new UuidCellRenderer());
        uuidColumn.setCellEditor(new DefaultCellEditor(uuidComboBox));
        uuidComboBox.addActionListener(evt -> {
            changeTaskUuidActionPerformed();
        });

        final TableColumn taskStateColumn = columnModel.getColumn(COLUMN_STATE_IDX);
        taskStateColumn.setMaxWidth(48);
        taskStateColumn.setCellRenderer(new TaskStateCellRenderer());

        final TableColumn resultColumn = columnModel.getColumn(COLUMN_RESULT_IDX);
        resultColumn.setMaxWidth(64);
        // Set the editor and renderer for the result cell to view the returned response.
        resultColumn.setCellRenderer(new ResponseResultCellEditor(this));
        resultColumn.setCellEditor(new ResponseResultCellEditor(this));

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
        if (isEditing()) {
            // abort editing before purging the entries
            getCellEditor().stopCellEditing();
        }
        ((TaskQueueTableModel) dataModel).setRowCount(0);
        genericTaskId = INIT_TASK_ID;
        taskIdSet.clear();
    }

    public void removeRow(int rowIdx) {
        if (isEditing()) {
            // abort editing before removing the row
            getCellEditor().stopCellEditing();
        }

        final int taskId;
        try {
            taskId = Integer.parseInt(dataModel.getValueAt(rowIdx, COLUMN_TASK_ID_IDX).toString());
        } catch (final NumberFormatException ex) {
            System.err.println("Failed to parse task ID. Can not delete entry.");
            return;
        }
        taskIdSet.remove(taskId);
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
        if (isEditing()) {
            // abort editing before reseting the states
            getCellEditor().stopCellEditing();
        }
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
        int rowIdx = tqtModel.getRowCount();
        tqtModel.addTask(generateAndRegisterTaskId(), task, ExecPolicy.HALT_AFTER_ERROR);
        selectionModel.setSelectionInterval(rowIdx, rowIdx);
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
        selectionModel.setSelectionInterval(idx, idx);
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
     * Exports the entire content of the current task queue table including runtime and state
     * information as CSV-table.
     *
     * @param exportStr The StringBuilder instance to append the queue table contents.
     */
    public void exportTableContentsAsCsv(final StringBuilder exportStr) {
        final char sep = ';'; // use semicolon as separator
        for (final String title : COLUMN_TITLES) {
            exportStr.append(title);
            exportStr.append(sep);
        }
        exportStr.append("\n");

        for (int i = 0; i < this.getRowCount(); i++) {
            for (int j = COLUMN_TASK_ID_IDX; j <= COLUMN_DURATION_IDX; j++) {
                exportStr.append(dataModel.getValueAt(i, j).toString());
                exportStr.append(sep);
            }
            exportStr.append("\"");
            exportStr.append(dataModel.getValueAt(i, COLUMN_RESULT_IDX).toString()
                    .replaceAll("\\s{2,}", " "));  // remove whitespace chains in the result string
            exportStr.append("\"");
            exportStr.append(sep);
            exportStr.append("\n");
        }
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
            boolean wasChangeSuccess = task.changeServerByUuid(serverUuid);
            if (wasChangeSuccess) {
                dataModel.setValueAt(ConnectionStatus.ONLINE,
                        editingRow,
                        COLUMN_CONNECTION_STATUS_IDX);
            } else {
                dataModel.setValueAt(ConnectionStatus.OFFLINE,
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
        while (taskIdSet.contains(genericTaskId)) {
            genericTaskId++;
        }
        taskIdSet.add(genericTaskId);
        return genericTaskId;
    }

    /**
     * Function to update the connection symbols in the queue table according to the changed server
     * state (online/offline).
     *
     * @param serverCtx The changed server context.
     */
    private void updateConnectionStateOfQueueEntries(final ServerContext serverCtx) {
        for (int i = 0; i < getRowCount(); i++) {
            final Object obj = dataModel.getValueAt(i, COLUMN_SERVER_UUID_IDX);

            if (obj instanceof UUID) {
                UUID taskUuid = (UUID) obj;
                if (taskUuid.compareTo(serverCtx.getServerUuid()) == 0) {
                    if (serverCtx.isOnline()) {
                        final CommandTask task = (CommandTask) dataModel.getValueAt(i,
                                COLUMN_TASK_INSTANCE_IDX);
                        task.changeServerByCtx(serverCtx);
                        dataModel.setValueAt(ConnectionStatus.ONLINE,
                                i,
                                COLUMN_CONNECTION_STATUS_IDX);
                    } else {
                        dataModel.setValueAt(ConnectionStatus.OFFLINE,
                                i,
                                COLUMN_CONNECTION_STATUS_IDX);
                    }
                }
            }
        }
    }

    /**
     * Listener function which gets invoked when the server connection state gets changed.
     *
     * @param serverCtx The changed server context.
     */
    @Override
    public void onServerConnectionChanged(final ServerContext serverCtx) {
        updateConnectionStateOfQueueEntries(serverCtx);
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

    private final class ConnectionStatusCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int col
        ) {
            final ConnectionStatus status = (value != null) ? (ConnectionStatus) value : ConnectionStatus.NEUTRAL;
            this.setIcon(status.getIcon());
            this.setToolTipText(status.toString());
            return this;
        }
    }

    private final class TaskStateCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int col
        ) {
            final TaskState state = (value != null) ? (TaskState) value : TaskState.NEUTRAL;
            this.setIcon(state.getIcon());
            this.setToolTipText(state.toString());
            return this;
        }
    }
}
