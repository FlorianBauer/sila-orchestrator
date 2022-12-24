package de.fau.clients.orchestrator.nodes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.fau.clients.orchestrator.ctx.FeatureContext;
import static de.fau.clients.orchestrator.nodes.SilaNode.jsonMapper;
import de.fau.clients.orchestrator.utils.IconProvider;
import de.fau.clients.orchestrator.utils.SilaDescriptionToolTip;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import lombok.NonNull;
import sila_java.library.core.models.SiLAElement;

/**
 * A <code>CompositNode</code> is a <code>SilaNode</code> which holds one or more child nodes. In
 * contrast to a <code>ListNode</code>, the child nodes can consist of different types.
 */
final class CompositNode extends SilaNode {

    private final FeatureContext featCtx;
    private final List<SiLAElement> elements;
    private final List<SilaNode> children;

    private CompositNode(
            final FeatureContext featCtx,
            @NonNull final List<SiLAElement> elements
    ) {
        this.featCtx = featCtx;
        this.elements = elements;
        this.children = new ArrayList<>(elements.size());
    }

    protected final static CompositNode create(
            final FeatureContext featCtx,
            @NonNull final List<SiLAElement> elements
    ) {
        final CompositNode node = new CompositNode(featCtx, elements);
        for (final SiLAElement elem : node.elements) {
            node.children.add(NodeFactory.createFromDataType(featCtx, elem.getDataType()));
        }
        return node;
    }

    protected final static CompositNode createFromJson(
            final FeatureContext featCtx,
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
                    BorderFactory.createEmptyBorder(0, 4, 0, 4)));
        }

        for (int i = 0; i < elemCount; i++) {
            final Box vBox = Box.createVerticalBox();
            vBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            vBox.setAlignmentY(JComponent.TOP_ALIGNMENT);

            final Box hBox = Box.createHorizontalBox();
            hBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            hBox.setAlignmentY(JComponent.TOP_ALIGNMENT);
            hBox.setMinimumSize(new Dimension(10, 20));
            hBox.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));

            final SiLAElement elem = elements.get(i);
            final JLabel dispLabel = new JLabel(elem.getDisplayName());
            dispLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            dispLabel.setAlignmentY(JComponent.BOTTOM_ALIGNMENT);

            final String infoDesc = elem.getDescription();
            final JLabel infoLabel = new JLabel("<html><p width=\"800\">"
                    + "– " + infoDesc
                    + "</p></html>");
            infoLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            infoLabel.setAlignmentY(JComponent.BOTTOM_ALIGNMENT);
            infoLabel.setVisible(false);

            final JToggleButton infoBtn = new JToggleButton(IconProvider.INFO_16PX.getIcon());
            infoBtn.setMaximumSize(new Dimension(18, 18));
            infoBtn.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            infoBtn.setAlignmentY(JComponent.BOTTOM_ALIGNMENT);
            infoBtn.setContentAreaFilled(false);
            infoBtn.setFocusable(false);
            infoBtn.setToolTipText(SilaDescriptionToolTip.formatToolTipString(infoDesc));
            infoBtn.addItemListener((final ItemEvent evt) -> {
                if (evt.getStateChange() == ItemEvent.SELECTED) {
                    infoLabel.setVisible(true);
                } else if (evt.getStateChange() == ItemEvent.DESELECTED) {
                    infoLabel.setVisible(false);
                }
            });

            hBox.add(dispLabel);
            hBox.add(Box.createHorizontalStrut(5));
            hBox.add(infoBtn);
            hBox.add(Box.createHorizontalStrut(5));
            hBox.add(infoLabel);
            hBox.add(Box.createHorizontalGlue());
            vBox.add(hBox);

            final SilaNode node = children.get(i);
            if (node == null) {
                continue;
            }
            final JComponent comp = node.getComponent();
            comp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            comp.setAlignmentY(JComponent.TOP_ALIGNMENT);
            vBox.add(comp);
            structBox.add(vBox);
            structBox.add(Box.createVerticalStrut(5));
        }
        return structBox;
    }
}
