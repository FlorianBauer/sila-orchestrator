package de.fau.clients.orchestrator;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

@SuppressWarnings("serial")
public class TaskQueueTable extends JTable {

    public static final int COLUMN_TASK_ID_IDX = 0;
    public static final int COLUMN_COMMAND_IDX = 1;
    public static final int COLUMN_STATE_IDX = 2;
    public static final int COLUMN_START_TIME_IDX = 3;
    public static final int COLUMN_END_TIME_IDX = 4;
    public static final int COLUMN_DURATION_IDX = 5;
    public static final int COLUMN_RESULT_IDX = 6;
    public static final int COLUMN_SERVER_UUID_IDX = 7;

    public static final String[] COLUMN_TITLES = {
        "ID",
        "Command",
        "State",
        "Start Time",
        "End Time",
        "Duration",
        "Result",
        "Server UUID"
    };

    private final TableColumnHider tch;
    private final JPopupMenu taskQueueHeaderPopupMenu = new JPopupMenu();

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

        // hidden on default
        tch.hideColumn(COLUMN_START_TIME_IDX);
        tch.hideColumn(COLUMN_END_TIME_IDX);
        tch.hideColumn(COLUMN_RESULT_IDX);
        tch.hideColumn(COLUMN_SERVER_UUID_IDX);

        for (int i = 0; i < TaskQueueTable.COLUMN_TITLES.length; i++) {
            if (i == COLUMN_COMMAND_IDX) {
                // Do not allow the user to hide the command column.
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

    public CommandTableEntry getFromRow(int rowIdx) {
        return (CommandTableEntry) this.dataModel.getValueAt(rowIdx, COLUMN_COMMAND_IDX);
    }
}
