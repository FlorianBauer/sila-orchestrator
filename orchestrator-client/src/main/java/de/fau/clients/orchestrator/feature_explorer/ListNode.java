package de.fau.clients.orchestrator.feature_explorer;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import lombok.NonNull;
import sila_java.library.core.models.Constraints;
import sila_java.library.core.models.ListType;

final class ListNode implements SilaNode {

    private static final ImageIcon REMOVE_ICON = new ImageIcon("src/main/resources/icons/list-remove.png");
    private static final ImageIcon ADD_ICON = new ImageIcon("src/main/resources/icons/list-add.png");

    /// A panel to place additional components on.
    private final JPanel listPanel = new JPanel();
    /// The button to trigger the removal of unnecessary list elements.
    private final JButton removeBtn = new JButton("Remove", REMOVE_ICON);
    /// The button to trigger the addition of extra list elements.
    private final JButton addBtn = new JButton("Add", ADD_ICON);
    /// Prototype node to clone the list element on a add-operations.
    private final SilaNode prototype;
    /// List holding the SilaNode elements.
    private final ArrayList<SilaNode> nodeList = new ArrayList<>();
    /// Constraint object holding vaious constraints (e.g. min. and max. list elements).
    private final Constraints constraints;

    private ListNode(@NonNull final SilaNode prototype) {
        this.prototype = prototype;
        this.constraints = null;
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.PAGE_AXIS));
        listPanel.setBorder(BorderFactory.createEtchedBorder());
        nodeList.add(this.prototype.cloneNode());

        removeBtn.addActionListener((ActionEvent evt) -> {
            removeBtnActionPerformed();
        });

        addBtn.addActionListener((ActionEvent evt) -> {
            addBtnActionPerformed();
        });
    }

    private ListNode(@NonNull final SilaNode prototype, final Constraints constraints) {
        this.prototype = prototype;
        this.constraints = constraints;
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.PAGE_AXIS));
        listPanel.setBorder(BorderFactory.createEtchedBorder());

        if (constraints.getElementCount() != null) {
            for (int i = 0; i < constraints.getElementCount().intValue(); i++) {
                nodeList.add(this.prototype.cloneNode());
            }
            // everything is fixed, so registering button-listeners can be omitted
            return;
        } else if (constraints.getMinimalElementCount() != null) {
            for (int i = 0; i < constraints.getMinimalElementCount().intValue(); i++) {
                removeBtn.setEnabled(false);
                nodeList.add(this.prototype.cloneNode());
            }
        } else {
            nodeList.add(this.prototype.cloneNode());
        }

        removeBtn.addActionListener((ActionEvent evt) -> {
            removeBtnActionPerformed();
        });

        addBtn.addActionListener((ActionEvent evt) -> {
            addBtnActionPerformed();
        });
    }

    protected static ListNode create(@NonNull final ListType type) {
        SilaNode prototype = FeatureNode.createFromDataType(type.getDataType());
        return new ListNode(prototype);
    }

    protected static ListNode createWithConstraint(
            @NonNull final ListType type,
            final Constraints con) {
        SilaNode prototype = FeatureNode.createFromDataType(type.getDataType());
        return new ListNode(prototype, con);
    }

    @Override
    public ListNode cloneNode() {
        if (this.constraints != null) {
            return new ListNode(this.prototype, this.constraints);
        }
        return new ListNode(this.prototype);
    }

    @Override
    public String toJsonString() {
        String jsonElem = "";
        String child;
        for (int i = 0; i < nodeList.size(); i++) {
            child = nodeList.get(i).toJsonString();
            if (child.isEmpty()) {
                continue;
            }
            jsonElem += nodeList.get(i).toJsonString() + ", ";
        }

        if (jsonElem.isEmpty()) {
            return "";
        }
        return "[" + jsonElem.substring(0, jsonElem.length() - 2) + "]";
    }

    @Override
    public JComponent getComponent() {
        for (final SilaNode node : nodeList) {
            listPanel.add(node.getComponent());
        }

        if (constraints == null || constraints.getElementCount() == null) {
            // Adding the "Remove" and "Add"-buttons if no fixed element count constraint was given.
            Box hbox = Box.createHorizontalBox();
            hbox.add(addBtn);
            hbox.add(removeBtn);
            hbox.add(Box.createHorizontalGlue());
            listPanel.add(hbox);
        }
        return listPanel;
    }

    private void removeBtnActionPerformed() {
        if (constraints != null) {
            if (constraints.getElementCount() != null) {
                // The element count is fixed, so there is nothing to remove.
                return;
            }

            if (constraints.getMinimalElementCount() != null) {
                final int minElemCount = constraints.getMinimalElementCount().intValue();
                if (nodeList.size() > minElemCount) {
                    listPanel.remove(nodeList.size() - 1);
                    listPanel.revalidate();
                    listPanel.repaint();
                    nodeList.remove(nodeList.size() - 1);

                    if (nodeList.size() <= minElemCount) {
                        removeBtn.setEnabled(false);
                    }
                    // re-enable the "Add"-button
                    addBtn.setEnabled(true);
                }
                return;
            }
        }
        listPanel.remove(nodeList.size() - 1);
        listPanel.revalidate();
        listPanel.repaint();
        nodeList.remove(nodeList.size() - 1);

        if (nodeList.size() <= 0) {
            // nothing to remove anymore
            removeBtn.setEnabled(false);
        }
        // re-enable the "Add"-button
        addBtn.setEnabled(true);
    }

    private void addBtnActionPerformed() {
        if (constraints != null) {
            if (constraints.getElementCount() != null) {
                // The element count is fixed, so there is nothing to add.
                return;
            }

            if (constraints.getMaximalElementCount() != null) {
                // max. element constraint was given
                final int maxElemCount = constraints.getMaximalElementCount().intValue();
                if (nodeList.size() < maxElemCount) {
                    final SilaNode clone = prototype.cloneNode();
                    nodeList.add(clone);
                    listPanel.add(clone.getComponent(), nodeList.size() - 1);
                    listPanel.revalidate();
                    listPanel.repaint();

                    if (nodeList.size() >= maxElemCount) {
                        addBtn.setEnabled(false);
                    }
                    // re-enable the "Remove"-button
                    removeBtn.setEnabled(true);
                }
                return;
            }
        }
        // unlimited list
        final SilaNode clone = prototype.cloneNode();
        nodeList.add(clone);
        listPanel.add(clone.getComponent(), nodeList.size() - 1);
        listPanel.revalidate();
        listPanel.repaint();

        // re-enable the "Remove"-button
        removeBtn.setEnabled(true);
    }
}
