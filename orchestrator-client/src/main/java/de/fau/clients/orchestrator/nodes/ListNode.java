package de.fau.clients.orchestrator.nodes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.fau.clients.orchestrator.utils.IconProvider;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import sila_java.library.core.models.Constraints;
import sila_java.library.core.models.ListType;

/**
 * A Node representing a list consisting of other <code>SilaNode</code>s.
 *
 * @see SilaNode
 */
@Slf4j
final class ListNode extends SilaNode {

    /**
     * Look-up table for data-types defined by the corresponding SiLA-Feature.
     */
    private final TypeDefLut typeDefs;
    /**
     * List holding the SilaNode elements.
     */
    private final ArrayList<SilaNode> nodeList = new ArrayList<>();
    /**
     * Prototype node to clone and add a list item from. If the prototype is <code>null</code>, no
     * add- and remove-operations of items on the list are allowed.
     */
    private final SilaNode prototype;

    private boolean isAddAndRemoveBtnNeeded = false;
    private boolean isAddBtnEnabled = true;
    private boolean isRemoveBtnEnabled = true;

    /**
     * A panel to place additional components on.
     */
    private JPanel listPanel;
    /**
     * The button to trigger the removal of unnecessary list elements.
     */
    private JButton removeBtn;
    /**
     * The button to trigger the addition of extra list elements.
     */
    private JButton addBtn;
    /**
     * Determines wether the values hold by the components are adjustable by the user or not.
     */
    private final boolean isEditable;
    /**
     * Constraint object holding various constraints (e.g. min. and max. list elements).
     */
    private Constraints constraints;

    private ListNode(
            @NonNull final TypeDefLut typeDefs,
            final SilaNode prototype,
            boolean isEditable) {
        this.typeDefs = typeDefs;
        this.prototype = prototype;
        this.isEditable = isEditable;
        this.constraints = null;
    }

    private ListNode(
            @NonNull final TypeDefLut typeDefs,
            final SilaNode prototype,
            final Constraints constraints) {
        this.typeDefs = typeDefs;
        this.prototype = prototype;
        // constraining the list makes only sense if the list is editable in the first place
        this.isEditable = true;
        this.constraints = constraints;
    }

    protected static ListNode create(
            @NonNull final TypeDefLut typeDefs,
            @NonNull final ListType type) {
        final SilaNode prototype = NodeFactory.createFromDataType(typeDefs, type.getDataType());
        final ListNode listNode = new ListNode(typeDefs, prototype, true);
        listNode.buildNode();
        return listNode;
    }

    protected static ListNode createWithConstraint(
            @NonNull final TypeDefLut typeDefs,
            @NonNull final ListType type,
            final Constraints con,
            final JsonNode jsonNode) {
        final SilaNode prototype = NodeFactory.createFromDataType(typeDefs, type.getDataType());
        final ListNode listNode = new ListNode(typeDefs, prototype, con);
        if (jsonNode != null) {
            final boolean isEditable = listNode.isEditable;
            final Iterator<JsonNode> iter = jsonNode.elements();
            while (iter.hasNext()) {
                listNode.nodeList.add(NodeFactory.createFromJson(
                        typeDefs,
                        type.getDataType(),
                        iter.next(),
                        isEditable));
            }
        }
        listNode.buildNode();
        return listNode;
    }

    protected static ListNode createFromJson(
            @NonNull final TypeDefLut typeDefs,
            @NonNull final ListType type,
            final JsonNode jsonNode,
            boolean isEditable) {
        final SilaNode prototype = NodeFactory.createFromDataType(typeDefs, type.getDataType());
        final ListNode listNode = new ListNode(typeDefs, prototype, isEditable);
        final Iterator<JsonNode> iter = jsonNode.elements();
        while (iter.hasNext()) {
            listNode.nodeList.add(NodeFactory.createFromJson(
                    typeDefs,
                    type.getDataType(),
                    iter.next(),
                    isEditable));
        }
        listNode.buildNode();
        return listNode;
    }

    @Override
    public ListNode cloneNode() {
        if (constraints != null) {
            return new ListNode(typeDefs, prototype, constraints);
        }
        return new ListNode(typeDefs, prototype, isEditable);
    }

    @Override
    public JsonNode toJson() {
        final ArrayNode arrayNode = jsonMapper.createObjectNode().arrayNode(nodeList.size());
        for (int i = 0; i < nodeList.size(); i++) {
            final JsonNode child = nodeList.get(i).toJson();
            if (child.isEmpty()) {
                continue;
            }
            arrayNode.add(child);
        }
        return arrayNode;
    }

    @Override
    public JComponent getComponent() {
        if (listPanel == null) {
            listPanel = new JPanel();
            listPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.PAGE_AXIS));
            listPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEtchedBorder(),
                    BorderFactory.createEmptyBorder(4, 16, 4, 4)));

            for (final SilaNode node : nodeList) {
                listPanel.add(node.getComponent());
            }

            if (isAddAndRemoveBtnNeeded) {
                addBtn = new JButton("Add", IconProvider.LIST_ADD.getIcon());
                addBtn.setEnabled(isAddBtnEnabled);
                addBtn.addActionListener((ActionEvent evt) -> {
                    addBtnActionPerformed();
                });

                removeBtn = new JButton("Remove", IconProvider.LIST_REMOVE.getIcon());
                removeBtn.setEnabled(isRemoveBtnEnabled);
                removeBtn.addActionListener((ActionEvent evt) -> {
                    removeBtnActionPerformed();
                });

                final Box hbox = Box.createHorizontalBox();
                hbox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
                hbox.add(addBtn);
                hbox.add(removeBtn);
                listPanel.add(hbox);
            }
        }
        return listPanel;
    }

    /**
     * Builds the Node by filling the list with default items and setting the states of the item
     * add/remove buttons accordingly. This function shall only be called once.
     */
    private void buildNode() {
        if (isEditable && prototype != null) {
            if (constraints != null) {
                if (constraints.getElementCount() != null) {
                    for (int i = nodeList.size(); i < constraints.getElementCount().intValue(); i++) {
                        nodeList.add(prototype.cloneNode());
                    }
                    // everything is fixed, so registering button-listeners can be omitted
                } else {
                    if (constraints.getMinimalElementCount() != null) {
                        final int elemSize = nodeList.size();
                        final int minSize = constraints.getMinimalElementCount().intValue();
                        for (int i = elemSize; i < minSize; i++) {
                            nodeList.add(prototype.cloneNode());
                        }
                        isRemoveBtnEnabled = (elemSize > minSize);
                    }
                    if (constraints.getMaximalElementCount() != null) {
                        if (nodeList.size() >= constraints.getMaximalElementCount().intValue()) {
                            isAddBtnEnabled = false;
                        }
                        if (nodeList.isEmpty()) {
                            nodeList.add(prototype.cloneNode());
                        }
                    }
                    isAddAndRemoveBtnNeeded = true;
                }
            } else {
                if (nodeList.isEmpty()) {
                    nodeList.add(prototype.cloneNode());
                }
                isAddAndRemoveBtnNeeded = true;
            }
        }
    }

    /**
     * Removes the last item of the current list.
     */
    private void removeBtnActionPerformed() {
        if (constraints != null) {
            if (constraints.getElementCount() != null) {
                // The element count is fixed, so there is nothing to remove.
                log.error("Removal of list-item was called on an absolute constraint list.");
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

    /**
     * Appends a new item to the current list. This is done by cloning one element form the given
     * prototype and inserting it at the and of the list.
     */
    private void addBtnActionPerformed() {
        if (constraints != null) {
            if (constraints.getElementCount() != null) {
                // The element count is fixed, so there is nothing to add.
                log.error("Adding of list-item was called on an absolute constraint list.");
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
