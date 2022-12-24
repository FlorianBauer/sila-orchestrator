package de.fau.clients.orchestrator.tasks;

import de.fau.clients.orchestrator.nodes.MaxDim;
import java.time.OffsetDateTime;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import lombok.extern.slf4j.Slf4j;

/**
 * Task which only sleeps the given amount of time to delay the execution of the next task in the
 * queue.
 */
@Slf4j
public class DelayTask extends QueueTask {

    private final DelayTaskModel delayModel;
    private boolean isPanelBuilt = false;
    private JPanel panel = null;
    private JButton execBtn = null;
    private SpinnerNumberModel minModel = null;
    private SpinnerNumberModel secModel = null;
    private SpinnerNumberModel milliModel = null;

    public DelayTask() {
        delayModel = new DelayTaskModel();
    }

    public DelayTask(DelayTaskModel timerModel) {
        this.delayModel = timerModel;
    }

    public DelayTask(long delayInMillisec) {
        delayModel = new DelayTaskModel(delayInMillisec);
    }

    @Override
    public DelayTaskModel getCurrentTaskModel() {
        if (isPanelBuilt) {
            int min = minModel.getNumber().intValue();
            int sec = secModel.getNumber().intValue();
            int milli = milliModel.getNumber().intValue();
            delayModel.setDelayFromMinSecMilli(min, sec, milli);
        }
        return delayModel;
    }

    public long getDelayInMilisec() {
        return delayModel.getDelayInMillisec();
    }

    public void setDelayInMilisec(long delayInMillisec) {
        delayModel.setDelayInMillisec(delayInMillisec);
    }

    @Override
    public String toString() {
        return "Delay";
    }

    /**
     * Presenter which holds widgets to adjust the minutes, seconds, and milliseconds properties.
     *
     * @return A <code>JPanel</code> populated with widgets for delay adjustments.
     */
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

            int[] delay = delayModel.getDelayAsMinSecMilli();
            minModel = new SpinnerNumberModel(delay[0], 0, 1440, 1);
            secModel = new SpinnerNumberModel(delay[1], 0, 59, 1);
            milliModel = new SpinnerNumberModel(delay[2], 0, 999, 100);
            final JSpinner minSpinner = new JSpinner(minModel);
            minSpinner.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            minSpinner.setMaximumSize(MaxDim.DATE_TIME_SPINNER.getDim());
            final JSpinner secSpinner = new JSpinner(secModel);
            secSpinner.setEditor(new JSpinner.NumberEditor(secSpinner, "00"));
            secSpinner.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            secSpinner.setMaximumSize(MaxDim.DATE_TIME_SPINNER.getDim());
            final JSpinner milliSpinner = new JSpinner(milliModel);
            milliSpinner.setEditor(new JSpinner.NumberEditor(milliSpinner, "000"));
            milliSpinner.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            milliSpinner.setMaximumSize(MaxDim.DATE_TIME_SPINNER.getDim());

            final Box hBox = Box.createHorizontalBox();
            hBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            final Box vBoxMin = Box.createVerticalBox();
            vBoxMin.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            vBoxMin.add(new JLabel("Minutes"));
            vBoxMin.add(minSpinner);
            hBox.add(vBoxMin);
            final JLabel sepColon = new JLabel(" : ");
            sepColon.setAlignmentY(JComponent.TOP_ALIGNMENT);
            hBox.add(sepColon);
            final Box vBoxSec = Box.createVerticalBox();
            vBoxSec.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            vBoxSec.add(new JLabel("Seconds"));
            vBoxSec.add(secSpinner);
            hBox.add(vBoxSec);
            final JLabel sepDot = new JLabel(" . ");
            sepDot.setAlignmentY(JComponent.TOP_ALIGNMENT);
            hBox.add(sepDot);
            final Box vBoxMilli = Box.createVerticalBox();
            vBoxMilli.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            vBoxMilli.add(new JLabel("Milliseconds"));
            vBoxMilli.add(milliSpinner);
            hBox.add(vBoxMilli);

            panel.add(hBox);
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

    @Override
    public void run() {
        if (isPanelBuilt) {
            execBtn.setEnabled(false);
            int min = minModel.getNumber().intValue();
            int sec = secModel.getNumber().intValue();
            int milli = milliModel.getNumber().intValue();
            delayModel.setDelayFromMinSecMilli(min, sec, milli);
        }

        startTimeStamp = OffsetDateTime.now();
        TaskState oldState = taskState;
        taskState = TaskState.RUNNING;
        stateChanges.firePropertyChange(TASK_STATE_PROPERTY, oldState, taskState);
        oldState = taskState;

        boolean wasCanceled = false;
        try {
            Thread.sleep(delayModel.getDelayInMillisec());
        } catch (final InterruptedException ex) {
            wasCanceled = true;
        }

        taskState = (wasCanceled) ? TaskState.FINISHED_ERROR : TaskState.FINISHED_SUCCESS;
        endTimeStamp = OffsetDateTime.now();
        stateChanges.firePropertyChange(TASK_STATE_PROPERTY, oldState, taskState);
        if (isPanelBuilt) {
            execBtn.setEnabled(true);
        }
    }
}
