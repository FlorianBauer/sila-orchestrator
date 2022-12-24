package de.fau.clients.orchestrator;

import javax.swing.JPanel;

/**
 * The Presenter interface. Enables implementing classes to view their contents on a
 * <code>JPanel</code> with populated widgets representing those class properties.
 */
public interface Presentable {

    /**
     * Gets the Presenter as a <code>JPanel</code> populated with widgets which representing the
     * class implementing the <code>Presentable</code> interface.
     *
     * @return A populated <code>JPanel</code> or <code>null</code>.
     */
    public JPanel getPresenter();
}
