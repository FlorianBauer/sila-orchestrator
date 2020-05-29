package de.fau.clients.orchestrator;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class TaskQueue extends JTable {

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

    public TaskQueue() {
        super(new TaskQueueTableModel());
        columnModel.getColumn(COLUMN_TASK_ID_IDX).setPreferredWidth(40);
        columnModel.getColumn(COLUMN_COMMAND_IDX).setPreferredWidth(180);
    }

    @SuppressWarnings("serial")
    private static class TaskQueueTableModel extends DefaultTableModel {

        @Override
        public int getColumnCount() {
            return COLUMN_TITLES.length;
        }

        @Override
        public String getColumnName(int col) {
            return COLUMN_TITLES[col];
        }

        @Override
        public Class getColumnClass(int col) {
            switch (col) {
                case COLUMN_TASK_ID_IDX: // 0
                    return Integer.class;
                case COLUMN_COMMAND_IDX: // 1
                    return CommandTableEntry.class;
                case COLUMN_START_TIME_IDX: // 3
                case COLUMN_RESULT_IDX: // 4
                    return String.class;
                default:
                    return Object.class;
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            // make only the task-ID editable
            return (col == COLUMN_TASK_ID_IDX);
        }
    }
}
