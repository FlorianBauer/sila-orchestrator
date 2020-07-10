package de.fau.clients.orchestrator.queue;

import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles the visibility of the columns in a <code>TableColumnModel</code> of an
 * <code>JTable</code>.
 */
@Slf4j
class TableColumnHider {

    private final TableColumnModel columnModel;
    private final String[] columnTitles;
    private final TableColumn[] hiddenColumnLut;

    public TableColumnHider(final TableColumnModel columnModel, final String[] columnTitles) {
        if (columnModel.getColumnCount() != columnTitles.length) {
            throw new IllegalArgumentException("Column count missmatch between model an titles.");
        }
        this.columnModel = columnModel;
        this.columnTitles = columnTitles;
        this.hiddenColumnLut = new TableColumn[this.columnModel.getColumnCount()];
    }

    /**
     * Makes the given column in the table invisible.
     *
     * @param columnIdx The index of the column.
     */
    public void hideColumn(int columnIdx) {
        if (columnIdx >= 0 && columnIdx < columnTitles.length) {
            final int idx = columnModel.getColumnIndex(columnTitles[columnIdx]);
            final TableColumn column = columnModel.getColumn(idx);
            hiddenColumnLut[columnIdx] = column;
            columnModel.removeColumn(column);
        }
    }

    /**
     * Makes the given column in the table visible.
     *
     * @param columnIdx The index of the column.
     */
    public void showColumn(int columnIdx) {
        if (columnIdx >= 0 && columnIdx < columnTitles.length) {
            final TableColumn column = hiddenColumnLut[columnIdx];
            if (column != null) {
                columnModel.addColumn(column);
                hiddenColumnLut[columnIdx] = null;
                int lastColumn = columnModel.getColumnCount() - 1;
                if (columnIdx < lastColumn) {
                    columnModel.moveColumn(lastColumn, columnIdx);
                }
            }
        }
    }
}
