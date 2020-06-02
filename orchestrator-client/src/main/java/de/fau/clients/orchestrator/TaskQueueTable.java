package de.fau.clients.orchestrator;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

@SuppressWarnings("serial")
public class TaskQueueTable extends JTable {

    public static final int COLUMN_TASK_ID_IDX = 0;
    public static final int COLUMN_COMMAND_IDX = 1;
    public static final int COLUMN_STATE_IDX = 2;
    public static final int COLUMN_START_TIME_IDX = 3;
    public static final int COLUMN_RESULT_IDX = 4;

    public static final String[] COLUMN_TITLES = {
        "Task ID",
        "Command",
        "State",
        "Start Time",
        "Result"
    };

    private final TableColumnHider tch;

    public TaskQueueTable() {
        super(new TaskQueueTableModel());
        this.setFillsViewportHeight(true);
        this.setRowHeight(32);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tch = new TableColumnHider(columnModel, COLUMN_TITLES);
        final TableColumn taskColumn = columnModel.getColumn(COLUMN_TASK_ID_IDX);
        taskColumn.setPreferredWidth(60);
        taskColumn.setMaxWidth(200);
        final TableColumn startTimeColumn = columnModel.getColumn(COLUMN_START_TIME_IDX);
        startTimeColumn.setPreferredWidth(160);
        startTimeColumn.setMaxWidth(300);
    }

    @Override
    public TaskQueueTableModel getModel() {
        return (TaskQueueTableModel) this.dataModel;
    }

    public CommandTableEntry getFromRow(int rowIdx) {
        return (CommandTableEntry) this.dataModel.getValueAt(rowIdx, COLUMN_COMMAND_IDX);
    }

    public void showColumn(int columnIdx) {
        tch.showColumn(columnIdx);
    }

    public void hideColumn(int columnIdx) {
        tch.hideColumn(columnIdx);
    }
}
