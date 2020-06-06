package de.fau.clients.orchestrator.feature_explorer;

import com.fasterxml.jackson.databind.JsonNode;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
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
    private final JButton removeBtn;
    /// The button to trigger the addition of extra list elements.
    private final JButton addBtn;
    /// The look-up table for type definitions of this SiLA-feature.
    private final TypeDefLut typeDefs;
    /// List holding the SilaNode elements.
    private final ArrayList<SilaNode> nodeList = new ArrayList<>();
    /// Prototype node to clone the list element on a add-operations.
    private final SilaNode prototype;
    /// Constraint object holding vaious constraints (e.g. min. and max. list elements).
    private Constraints constraints;
    private final boolean isEditable;

    private ListNode(@NonNull final TypeDefLut typeDefs, @NonNull final SilaNode prototype) {
        this.typeDefs = typeDefs;
        this.prototype = prototype;
        this.constraints = null;
        this.isEditable = true;
        removeBtn = new JButton("Remove", REMOVE_ICON);
        addBtn = new JButton("Add", ADD_ICON);
        listPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.PAGE_AXIS));
        listPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(4, 16, 4, 4))
        );
        nodeList.add(this.prototype.cloneNode());

        removeBtn.addActionListener((ActionEvent evt) -> {
            removeBtnActionPerformed();
        });

        addBtn.addActionListener((ActionEvent evt) -> {
            addBtnActionPerformed();
        });
    }

    private ListNode(@NonNull final TypeDefLut typeDefs, boolean isEditable) {
        this.typeDefs = typeDefs;
        this.prototype = null;
        this.constraints = null;
        this.isEditable = isEditable;
        if (!this.isEditable) {
            removeBtn = null;
            addBtn = null;
        } else {
            removeBtn = new JButton("Remove", REMOVE_ICON);
            addBtn = new JButton("Add", ADD_ICON);
            // FIXME: create prototype and register ActionListeners
        }
        listPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.PAGE_AXIS));
        listPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(4, 16, 4, 4))
        );
    }

    private ListNode(TypeDefLut typeDefs, @NonNull final SilaNode prototype, final Constraints constraints) {
        this.typeDefs = typeDefs;
        this.prototype = prototype;
        this.constraints = constraints;
        this.isEditable = true;
        removeBtn = new JButton("Remove", REMOVE_ICON);
        addBtn = new JButton("Add", ADD_ICON);
        listPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.PAGE_AXIS));
        listPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(4, 16, 4, 4))
        );

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

    protected static ListNode create(
            @NonNull final TypeDefLut typeDefs,
            @NonNull final ListType type) {

        final SilaNode prototype = NodeFactory.createFromDataType(typeDefs, type.getDataType());
        return new ListNode(typeDefs, prototype);
    }

    protected static ListNode createWithConstraint(
            @NonNull final TypeDefLut typeDefs,
            @NonNull final ListType type,
            final Constraints con) {

        final SilaNode prototype = NodeFactory.createFromDataType(typeDefs, type.getDataType());
        return new ListNode(typeDefs, prototype, con);
    }

    protected static ListNode createFromJson(
            @NonNull final TypeDefLut typeDefs,
            @NonNull final ListType type,
            final JsonNode jsonNode,
            boolean isEditable) {

        final ListNode listNode = new ListNode(typeDefs, isEditable);
        final Iterator<JsonNode> iter = jsonNode.elements();
        while (iter.hasNext()) {
            listNode.nodeList.add(NodeFactory.createFromJson(
                    typeDefs,
                    type.getDataType(),
                    iter.next(),
                    isEditable));
        }
        return listNode;
    }

    @Override
    public ListNode cloneNode() {
        if (this.constraints != null) {
            return new ListNode(this.typeDefs, this.prototype, this.constraints);
        }

        if (!this.isEditable) {
            return new ListNode(this.typeDefs, true);
        }
        return new ListNode(this.typeDefs, this.prototype);
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

        if ((constraints == null || constraints.getElementCount() == null) && isEditable) {
            // Adding the "Remove" and "Add"-buttons if no fixed element count constraint was given.
            Box hbox = Box.createHorizontalBox();
            hbox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
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
