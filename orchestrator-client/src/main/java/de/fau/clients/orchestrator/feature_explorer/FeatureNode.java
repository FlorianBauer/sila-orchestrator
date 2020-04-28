package de.fau.clients.orchestrator.feature_explorer;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import sila_java.library.core.models.DataTypeType;
import sila_java.library.core.models.SiLAElement;

@Slf4j
public final class FeatureNode implements SilaNode {

    private final TypeDefLut typeDefs;
    private final List<SilaNode> children = new ArrayList<>();
    private final List<SiLAElement> elements;

    public FeatureNode(
            @NonNull final TypeDefLut typeDefs,
            @NonNull final List<SiLAElement> elements) {

        this.typeDefs = typeDefs;
        this.elements = elements;
        for (final SiLAElement elem : this.elements) {
            children.add(createFromDataType(typeDefs, elem.getDataType()));
        }
    }

    protected static SilaNode createFromDataType(
            @NonNull final TypeDefLut typeDefs,
            @NonNull final DataTypeType type) {

        if (type.getBasic() != null) {
            // basic type
            return BasicNode.create(type.getBasic());
        } else {
            // derived type
            if (type.getConstrained() != null) {
                final DataTypeType conType = type.getConstrained().getDataType();
                if (conType != null) {
                    if (conType.getBasic() != null) {
                        return BasicNode.createWithConstraint(conType.getBasic(),
                                type.getConstrained().getConstraints());
                    } else if (type.getConstrained().getDataType().getList() != null) {
                        return ListNode.createWithConstraint(typeDefs, conType.getList(),
                                type.getConstrained().getConstraints());
                    } else {
                        log.error("A Constrained type can only contain a Basic- or a List-type");
                    }
                } else {
                    log.error("Constrained type is null");
                }
            } else if (type.getList() != null) {
                return ListNode.create(typeDefs, type.getList());
            } else if (type.getStructure() != null) {
                return new FeatureNode(typeDefs, type.getStructure().getElement());
            } else if (type.getDataTypeIdentifier() != null) {
                return new DefTypeNode(typeDefs, type.getDataTypeIdentifier());
            } else {
                log.error("Unknown type of DataTypeType");
            }
        }
        return null;
    }

    @Override
    public SilaNode cloneNode() {
        return new FeatureNode(this.typeDefs, this.elements);
    }

    @Override
    public String toJsonString() {
        String jsonElem = "";
        String child;

        for (int i = 0; i < elements.size(); i++) {
            child = children.get(i).toJsonString();
            if (child.isEmpty()) {
                continue;
            }
            jsonElem += "\"" + elements.get(i).getIdentifier() + "\":" + child + ", ";
        }

        if (jsonElem.isEmpty()) {
            return "";
        }
        return "{" + jsonElem.substring(0, jsonElem.length() - 2) + "}";
    }

    @Override
    public JComponent getComponent() {
        Box vbox = Box.createVerticalBox();
        for (int i = 0; i < elements.size(); i++) {
            vbox.add(new JLabel(elements.get(i).getDisplayName()));
            SilaNode node = children.get(i);
            if (node == null) {
                continue;
            }
            JComponent comp = node.getComponent();
            comp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            vbox.add(comp);
            vbox.add(Box.createVerticalStrut(10));
        }
        return vbox;
    }
}
