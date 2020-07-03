package de.fau.clients.orchestrator;

import de.fau.clients.orchestrator.tasks.CommandTask;
import de.fau.clients.orchestrator.tasks.QueueTask;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import sila_java.library.manager.ServerManager;
import sila_java.library.manager.models.Server;

@SuppressWarnings("serial")
public class TaskQueueTable extends JTable {

    public static final int COLUMN_TASK_ID_IDX = 0;
    public static final int COLUMN_TASK_INSTANCE_IDX = 1;
    public static final int COLUMN_STATE_IDX = 2;
    public static final int COLUMN_START_TIME_IDX = 3;
    public static final int COLUMN_END_TIME_IDX = 4;
    public static final int COLUMN_DURATION_IDX = 5;
    public static final int COLUMN_RESULT_IDX = 6;
    public static final int COLUMN_SERVER_UUID_IDX = 7;

    public static final String[] COLUMN_TITLES = {
        "ID",
        "Task",
        "State",
        "Start Time",
        "End Time",
        "Duration",
        "Result",
        "Server UUID"
    };

    private static ServerManager serverManager = null;
    private final TableColumnHider tch;
    private final JPopupMenu taskQueueHeaderPopupMenu = new JPopupMenu();
    private final HashSet<UUID> serverUuidSet = new HashSet<>();
    private final JComboBox<UUID> comboBox = new JComboBox<>();
    private JScrollPane paramsPane = null;

    public TaskQueueTable() {
        super(new TaskQueueTableModel());
        this.setFillsViewportHeight(true);
        this.setRowHeight(32);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tch = new TableColumnHider(columnModel, COLUMN_TITLES);
        final TableColumn taskColumn = columnModel.getColumn(COLUMN_TASK_ID_IDX);
        taskColumn.setPreferredWidth(40);
        taskColumn.setMaxWidth(80);
        columnModel.getColumn(COLUMN_START_TIME_IDX).setPreferredWidth(170);
        columnModel.getColumn(COLUMN_END_TIME_IDX).setPreferredWidth(170);

        columnModel.getColumn(COLUMN_SERVER_UUID_IDX).setCellEditor(new DefaultCellEditor(comboBox));
        comboBox.addActionListener(evt -> {
            changeTaskUuidActionPerformed();
        });

        // hidden on default
        tch.hideColumn(COLUMN_START_TIME_IDX);
        tch.hideColumn(COLUMN_END_TIME_IDX);
        tch.hideColumn(COLUMN_RESULT_IDX);
        tch.hideColumn(COLUMN_SERVER_UUID_IDX);

        for (int i = 0; i < TaskQueueTable.COLUMN_TITLES.length; i++) {
            if (i == COLUMN_TASK_INSTANCE_IDX) {
                // Do not allow the user to hide the task column.
                continue;
            }

            final int colIdx = i;
            final JCheckBoxMenuItem item = new JCheckBoxMenuItem();
            final boolean isChecked;
            if (colIdx == COLUMN_START_TIME_IDX
                    || colIdx == COLUMN_END_TIME_IDX
                    || colIdx == COLUMN_RESULT_IDX
                    || colIdx == COLUMN_SERVER_UUID_IDX) {
                // uncheck hidden columns
                isChecked = false;
            } else {
                isChecked = true;
            }
            item.setSelected(isChecked);
            item.setText(COLUMN_TITLES[colIdx]);
            item.addActionListener(evt -> {
                if (item.isSelected()) {
                    tch.showColumn(colIdx);
                } else {
                    tch.hideColumn(colIdx);
                }
            });
            taskQueueHeaderPopupMenu.add(item);
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

    @Override
    public TaskQueueTableModel getModel() {
        return (TaskQueueTableModel) this.dataModel;
    }

    public QueueTask getTaskFromRow(int rowIdx) {
        return (QueueTask) dataModel.getValueAt(rowIdx, COLUMN_TASK_INSTANCE_IDX);
    }

    /**
     * Adds the given command task to the queue table.
     *
     * @param taskId The task ID to use for this entry.
     * @param cmdTask The command task to add.
     */
    public void addCommandTask(int taskId, final CommandTask cmdTask) {
        addUuidToSelectionSet(cmdTask.getServerUuid());
        final TaskQueueTableModel tqtModel = (TaskQueueTableModel) dataModel;
        tqtModel.addCommandTableEntry(taskId, cmdTask);
    }

    public void setServerManager(ServerManager manager) {
        TaskQueueTable.serverManager = manager;
    }

    public void setParamsPane(final JScrollPane pane) {
        this.paramsPane = pane;
    }

    /**
     * Adds the given queue task to the table.
     *
     * @param taskId The task ID to use for this entry.
     * @param task The queue task to add.
     */
    public void addTask(int taskId, final QueueTask task) {
        final TaskQueueTableModel tqtModel = (TaskQueueTableModel) dataModel;
        tqtModel.addTaskEntry(taskId, task);
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
            comboBox.addItem(serverUuid);
        }
    }

    private void changeTaskUuidActionPerformed() {
        if (editingRow >= 0) {
            if (serverManager != null) {
                final UUID serverUuid = (UUID) comboBox.getSelectedItem();
                final Map<UUID, Server> onlineServer = serverManager.getServers();
                final CommandTask task = (CommandTask) dataModel.getValueAt(editingRow, COLUMN_TASK_INSTANCE_IDX);
                task.changeServer(serverUuid, onlineServer.get(serverUuid));
                dataModel.setValueAt(task.getState(), editingRow, COLUMN_STATE_IDX);
                if (paramsPane != null) {
                    // update the parameter panel if available
                    paramsPane.setViewportView(task.getPanel());
                }
            }
        }
    }
}
