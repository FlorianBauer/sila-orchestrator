package de.fau.clients.orchestrator.queue;

import java.awt.Component;
import java.util.HashSet;
import javax.swing.DefaultCellEditor;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import lombok.NonNull;

/**
 * Custom cell editor for task IDs.
 */
@SuppressWarnings("serial")
final class TaskIdCellEditor extends DefaultCellEditor {

    /**
     * Verifier for ID uniqueness.
     */
    private final InputVerifier verifier = new TaskIdVerifier();

    /**
     * HashSet holding the task IDs to chose from.
     */
    private final HashSet<Integer> taskIdSet;

    /**
     * Initial ID before the editing process has begun.
     */
    private String oldTaskId;

    public TaskIdCellEditor(@NonNull final HashSet<Integer> taskIdSet) {
        super(new JTextField());
        this.taskIdSet = taskIdSet;
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
                cancelCellEditing();
            }
        } else {
            // Don't accept the value and leave the cell editor.
            cancelCellEditing();
        }
        return true;
    }

    /**
     * Verifier to check if the edited task ID from the user is unique.
     */
    private final class TaskIdVerifier extends InputVerifier {

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
}
