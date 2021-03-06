package de.fau.clients.orchestrator.utils;

import javax.swing.JComponent;
import javax.swing.JToolTip;

/**
 * Custom ToolTip class for a better distinction between application tool-tips and SiLA element
 * descriptions.
 */
@SuppressWarnings("serial")
public class SilaDescriptionToolTip extends JToolTip {

    public SilaDescriptionToolTip(final JComponent comp) {
        setFont(getFont().deriveFont(15.0f));
        setComponent(comp);
    }
}
