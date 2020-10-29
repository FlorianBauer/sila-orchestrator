package de.fau.clients.orchestrator.tasks;

import de.fau.clients.orchestrator.nodes.MaxDim;
import static de.fau.clients.orchestrator.tasks.QueueTask.TASK_STATE_PROPERTY;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.OffsetDateTime;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;
import lombok.extern.slf4j.Slf4j;

/**
 * Task for local execution of programs or scripts on the host system.
 */
@Slf4j
public class LocalExecTask extends QueueTask {

    private final LocalExecTaskModel execTaskModel;
    private int exitValue = -1;
    private boolean isPanelBuilt = false;
    private JPanel panel = null;
    private JTextField execTextField = null;
    private JFormattedTextField expRetValTextField = null;
    private JButton execBtn = null;

    public LocalExecTask() {
        execTaskModel = new LocalExecTaskModel();
    }

    /**
     * Constructor for a local execution task.
     *
     * @param exec The executable without arguments.
     */
    public LocalExecTask(final String exec) {
        execTaskModel = new LocalExecTaskModel(exec);
    }

    public LocalExecTask(final LocalExecTaskModel execTaskModel) {
        this.execTaskModel = execTaskModel;
    }

    @Override
    public TaskModel getCurrentTaskModel() {
        if (isPanelBuilt) {
            execTaskModel.setExec(execTextField.getText());
            execTaskModel.setExpRetVal((int) expRetValTextField.getValue());
        }
        return execTaskModel;
    }

    @Override
    public void run() {
        if (isPanelBuilt) {
            execBtn.setEnabled(false);
            execTaskModel.setExec(execTextField.getText());
            execTaskModel.setExpRetVal((int) expRetValTextField.getValue());
        }

        startTimeStamp = OffsetDateTime.now();
        TaskState oldState = taskState;
        taskState = TaskState.RUNNING;
        stateChanges.firePropertyChange(TASK_STATE_PROPERTY, oldState, taskState);
        oldState = taskState;

        final ProcessBuilder pb = new ProcessBuilder(execTaskModel.getExecWithArgsAsList());
        try {
            Process proc = pb.start();
            exitValue = proc.waitFor();
        } catch (IOException ex) {
            log.error(ex.getMessage());
            exitValue = 2;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        taskState = (exitValue == execTaskModel.getExpRetVal())
                ? TaskState.FINISHED_SUCCESS
                : TaskState.FINISHED_ERROR;
        endTimeStamp = OffsetDateTime.now();
        stateChanges.firePropertyChange(TASK_STATE_PROPERTY, oldState, taskState);
        if (isPanelBuilt) {
            execBtn.setEnabled(true);
        }
    }

    @Override
    public String toString() {
        return "Local Executable";
    }

    @Override
    public JPanel getPresenter() {
        if (!isPanelBuilt) {
            panel = new JPanel();
            panel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(this.toString()),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)));
            panel.setFocusCycleRoot(true);

            final Box vBox = Box.createVerticalBox();
            vBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            final JLabel execCmdLabel = new JLabel("Exec Command");
            execCmdLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            vBox.add(execCmdLabel);
            execTextField = new JTextField(execTaskModel.getExec());
            execTextField.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            execTextField.setMaximumSize(MaxDim.TEXT_FIELD.getDim());
            vBox.add(execTextField);
            vBox.add(Box.createVerticalStrut(10));
            final JLabel expRetValLabel = new JLabel("Expected Return Value");
            expRetValLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            vBox.add(expRetValLabel);
            final NumberFormatter formatter = new NumberFormatter(new DecimalFormat("0"));
            formatter.setValueClass(Integer.class);
            formatter.setMinimum(Integer.MIN_VALUE);
            formatter.setMaximum(Integer.MAX_VALUE);
            formatter.setAllowsInvalid(true);
            expRetValTextField = new JFormattedTextField(formatter);
            expRetValTextField.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            expRetValTextField.setMaximumSize(MaxDim.NUMERIC_SPINNER.getDim());
            expRetValTextField.setValue(execTaskModel.getExpRetVal());
            vBox.add(expRetValTextField);
            panel.add(vBox);
            panel.add(Box.createVerticalStrut(20));
            execBtn = new JButton("Execute", EXECUTE_ICON);
            execBtn.addActionListener((evt) -> {
                new Thread(this).start();
            });
            panel.add(execBtn);
            isPanelBuilt = true;
        }
        return panel;
    }
}
