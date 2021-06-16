package de.fau.clients.orchestrator.queue;

import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * Handles the visibility of the columns in a <code>TableColumnModel</code> of an
 * <code>JTable</code>. The indices of the shown/hidden columns and the defined column-enums are
 * different and the actual mapping has to be resolved by using the internal look-up table (LUT).
 */
final class TableColumnHider {

    private final TableColumnModel columnModel;
    private final TableColumn[] hiddenColumnLut = new TableColumn[Column.size()];

    /**
     * Constructor. Sets the column model and hides the flagged entries. Ensure the column model is
     * completely initialized before constructing this class.
     *
     * @param columnModel The fully initialized column model of an table.
     */
    public TableColumnHider(final TableColumnModel columnModel) {
        this.columnModel = columnModel;

        for (final Column col : Column.values()) {
            if (col.isHiddenOnDefault) {
                this.hideColumn(col);
            }
        }
    }

    /**
     * Makes the given column invisible in the table.
     *
     * @param col The column to hide.
     */
    public void hideColumn(final Column col) {
        final int modelIdx = columnModel.getColumnIndex(col.title);
        final TableColumn column = columnModel.getColumn(modelIdx);
        hiddenColumnLut[col.ordinal()] = column;
        columnModel.removeColumn(column);
    }

    /**
     * Makes the given column visible in the table.
     *
     * @param col The column to show.
     */
    public void showColumn(final Column col) {
        final int idx = col.ordinal();
        final TableColumn column = hiddenColumnLut[idx];
        if (column != null) {
            columnModel.addColumn(column);
            hiddenColumnLut[idx] = null;
            int lastColumn = columnModel.getColumnCount() - 1;
            if (idx < lastColumn) {
                columnModel.moveColumn(lastColumn, idx);
            }
        }
    }
}
