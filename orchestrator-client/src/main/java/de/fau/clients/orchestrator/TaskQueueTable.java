package de.fau.clients.orchestrator;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;

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

    public TaskQueueTable() {
        super(new TaskQueueTableModel());
        this.setFillsViewportHeight(true);
        this.setRowHeight(32);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        columnModel.getColumn(COLUMN_TASK_ID_IDX).setPreferredWidth(40);
        columnModel.getColumn(COLUMN_COMMAND_IDX).setPreferredWidth(180);
    }

    @Override
    public TaskQueueTableModel getModel() {
        return (TaskQueueTableModel) this.dataModel;
    }
}
