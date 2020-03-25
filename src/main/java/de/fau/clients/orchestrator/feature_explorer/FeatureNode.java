package de.fau.clients.orchestrator.feature_explorer;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import sila_java.library.core.models.DataTypeType;
import sila_java.library.core.models.SiLAElement;

@Slf4j
public final class FeatureNode {

    private final List<SilaNode> children = new ArrayList<>();
    private final List<SiLAElement> elements;

    public FeatureNode(@NonNull final List<SiLAElement> elements) {
        this.elements = elements;
        for (final SiLAElement elem : this.elements) {
            children.add(createFromDataType(elem.getDataType()));
        }
    }

    protected static SilaNode createFromDataType(@NonNull final DataTypeType type) {
        if (type.getBasic() != null) {
            // basic type
            return BasicNode.create(type.getBasic());
        } else {
            // derived type
            if (type.getConstrained() != null) {
                final DataTypeType conType = type.getConstrained().getDataType();
                if (conType != null) {
                    if (conType.getBasic() != null) {
                        // TODO: add constaint
                        return BasicNode.create(conType.getBasic());
                    } else if (type.getConstrained().getDataType().getList() != null) {
                        return ListNode.createWithConstraint(conType.getList(),
                                type.getConstrained().getConstraints());
                    } else {
                        log.error("A Constrained type can only contain a Basic- or a List-type");
                    }
                } else {
                    log.error("Constrained type is null");
                }
            } else if (type.getList() != null) {
                return ListNode.create(type.getList());
            } else if (type.getStructure() != null) {
                // TODO: implement
            } else {
                log.error("Unknown type of DataTypeType");
            }
        }
        return null;
    }

    public String toJsonMessage() {
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
        return jsonElem.substring(0, jsonElem.length() - 2);
    }

    /**
     * Populates the given panel with GUI components representing the consisting nodes. Do not
     * populate a given panel more then once.
     *
     * @param panel The panel to add the elements.
     */
    public void populatePanel(@NonNull final JPanel panel) {
        for (int i = 0; i < elements.size(); i++) {
            panel.add(new JLabel(elements.get(i).getDisplayName()));
            panel.add(children.get(i).getComponent());
        }
    }
}
