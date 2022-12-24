package de.fau.clients.orchestrator.queue;

import de.fau.clients.orchestrator.tasks.CommandTask;
import de.fau.clients.orchestrator.tasks.QueueTask;
import de.fau.clients.orchestrator.tasks.TaskState;
import java.awt.Component;
import static java.awt.Component.LEFT_ALIGNMENT;
import static java.awt.Component.RIGHT_ALIGNMENT;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.AbstractCellEditor;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import lombok.NonNull;

/**
 * Class to render/view the latest response results provided by the command response.
 */
@SuppressWarnings("serial")
final class ResponseResultCellEditor extends AbstractCellEditor implements TableCellEditor,
        TableCellRenderer {

    private static final int POPUP_WINDOW_WIDTH = 450;
    private static final int POPUP_WINDOW_HEIGHT = 350;
    private final Dimension buttonDim = new Dimension(60, 24);
    private final JButton expandBtn;
    private final ButtonGroup btnGroup = new ButtonGroup();
    private final JToggleButton rawViewBtn;
    private final JToggleButton nodeViewBtn;
    private final JPopupMenu editorPopup;
    private final JEditorPane editorPane;
    private final TaskQueueTable parentTable;
    private Object value;

    public ResponseResultCellEditor(@NonNull final TaskQueueTable table) {
        parentTable = table;
        editorPane = new JEditorPane();
        editorPane.setEditable(false);

        final JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setAlignmentY(LEFT_ALIGNMENT);

        rawViewBtn = new JToggleButton("Raw");
        rawViewBtn.setAlignmentY(RIGHT_ALIGNMENT);
        rawViewBtn.setPreferredSize(buttonDim);
        rawViewBtn.addActionListener((final ActionEvent evt) -> {
            final QueueTask task = parentTable.getTaskFromRow(parentTable.getEditingRow());
            editorPane.setText(task.getLastExecResult());
            scrollPane.setViewportView(editorPane);
        });
        nodeViewBtn = new JToggleButton("View");
        nodeViewBtn.setAlignmentY(RIGHT_ALIGNMENT);
        nodeViewBtn.setPreferredSize(buttonDim);
        nodeViewBtn.addActionListener((final ActionEvent evt) -> {
            final QueueTask queueTask = parentTable.getTaskFromRow(parentTable.getEditingRow());
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
        editorPopup.setPreferredSize(new Dimension(POPUP_WINDOW_WIDTH, POPUP_WINDOW_HEIGHT));
        editorPopup.add(btnBox);
        editorPopup.add(scrollPane);

        expandBtn = new JButton("[...]");
        expandBtn.setAlignmentY(RIGHT_ALIGNMENT);
        expandBtn.addActionListener((final ActionEvent evt) -> {
            final QueueTask queueTask = parentTable.getTaskFromRow(parentTable.getEditingRow());
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
                    -(POPUP_WINDOW_WIDTH - expandBtn.getWidth()),
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
        if (isResultValueEmpty(this.value)) {
            return TaskQueueTable.EMPTY_LABEL;
        }
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
        if (isResultValueEmpty(value)) {
            return TaskQueueTable.EMPTY_LABEL;
        }
        return expandBtn;
    }

    /**
     * Checks whether an result value is empty or not.
     *
     * @param resValue The string-object that may contain a JSON message.
     * @return true on empty strings and empty JSON messages (e.g. `{ }`), otherwise false.
     */
    static boolean isResultValueEmpty(final Object resValue) {
        if (resValue == null) {
            return true;
        }

        final String resStr = resValue.toString().strip();
        if (resStr.isEmpty()) {
            return true;
        }

        // Check against empty JSON messages (e.g. `{ }`).
        if (resStr.matches("^\\{\\s*\\}$")) {
            return true;
        }
        return false;
    }
}
