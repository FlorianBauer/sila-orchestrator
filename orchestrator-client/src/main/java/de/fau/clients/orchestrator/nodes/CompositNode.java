package de.fau.clients.orchestrator.nodes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.fau.clients.orchestrator.ctx.FeatureContext;
import static de.fau.clients.orchestrator.nodes.SilaNode.jsonMapper;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import sila_java.library.core.models.SiLAElement;

/**
 * A <code>CompositNode</code> is a <code>SilaNode</code> which holds one or more child nodes. In
 * contrast to a <code>ListNode</code>, the child nodes can consist of different types.
 */
@Slf4j
final class CompositNode extends SilaNode {

    private final FeatureContext featCtx;
    private final List<SiLAElement> elements;
    private final List<SilaNode> children;

    private CompositNode(
            @NonNull final FeatureContext featCtx,
            @NonNull final List<SiLAElement> elements
    ) {
        this.featCtx = featCtx;
        this.elements = elements;
        this.children = new ArrayList<>(elements.size());
    }

    protected final static CompositNode create(
            @NonNull final FeatureContext featCtx,
            @NonNull final List<SiLAElement> elements
    ) {
        final CompositNode node = new CompositNode(featCtx, elements);
        for (final SiLAElement elem : node.elements) {
            node.children.add(NodeFactory.createFromDataType(featCtx, elem.getDataType()));
        }
        return node;
    }

    protected final static CompositNode createFromJson(
            @NonNull final FeatureContext featCtx,
            @NonNull final List<SiLAElement> elements,
            @NonNull final JsonNode jsonNode,
            boolean isEditable
    ) {
        final CompositNode node = new CompositNode(featCtx, elements);
        for (final SiLAElement elem : node.elements) {
            node.children.add(NodeFactory.createFromJson(
                    featCtx,
                    elem.getDataType(),
                    jsonNode.get(elem.getIdentifier()),
                    isEditable));
        }
        return node;
    }

    @Override
    public SilaNode cloneNode() {
        return CompositNode.create(this.featCtx, this.elements);
    }

    @Override
    public JsonNode toJson() {
        final ObjectNode objNode = jsonMapper.createObjectNode();
        for (int i = 0; i < elements.size(); i++) {
            final JsonNode child = children.get(i).toJson();
            if (child.isEmpty()) {
                continue;
            }
            objNode.set(elements.get(i).getIdentifier(), child);
        }
        return objNode;
    }

    @Override
    public JComponent getComponent() {
        final int elemCount = elements.size();
        final Box structBox = Box.createVerticalBox();
        structBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        structBox.setAlignmentY(JComponent.TOP_ALIGNMENT);

        if (elemCount >= 2) {
            structBox.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEtchedBorder(),
                    BorderFactory.createEmptyBorder(4, 4, 0, 4)));
        }

        for (int i = 0; i < elemCount; i++) {
            final Box vBox = Box.createVerticalBox();
            vBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            vBox.setAlignmentY(JComponent.TOP_ALIGNMENT);
            vBox.add(new JLabel(elements.get(i).getDisplayName()));
            SilaNode node = children.get(i);
            if (node == null) {
                continue;
            }
            final JComponent comp = node.getComponent();
            comp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            comp.setAlignmentY(JComponent.TOP_ALIGNMENT);
            vBox.add(comp);
            structBox.add(vBox);
            structBox.add(Box.createVerticalStrut(10));
        }
        return structBox;
    }
}
