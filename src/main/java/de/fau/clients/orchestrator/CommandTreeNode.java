package de.fau.clients.orchestrator;

import lombok.extern.slf4j.Slf4j;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import sila_java.library.core.models.DataTypeType;
import sila_java.library.core.models.Feature.Command;
import sila_java.library.core.models.SiLAElement;

@Slf4j
public class CommandTreeNode extends DefaultMutableTreeNode {

    static final Dimension MAX_SILA_BASIC_TYPE_ELEM_DIM = new Dimension(8192, 32);
    private final Command command;
    private final JPanel panel;

    public CommandTreeNode(final JPanel panel, final Command command) {
        super();
        this.panel = panel;
        this.command = command;
    }

    @Override
    public String toString() {
        return command.getDisplayName();
    }

    public void buildCommandPanel() {
        panel.removeAll();
        for (SiLAElement elem : command.getParameter()) {
            if (elem.getDataType() != null) {
                panel.add(new JLabel(elem.getDisplayName()));
                if (elem.getDataType().getBasic() != null) {
                    // SiLA basic type
                    introspectDataType(panel, elem.getDataType());
                } else {
                    // SiLA derived type
                    String tail;
                    if (elem.getDataType().getConstrained() != null) {
                        tail = "\n\t* getDataTypeConstrained: " + elem.getDataType().getConstrained().toString();
                        elem.getDataType().getConstrained().getDataType();
                    } else if (elem.getDataType().getList() != null) {
                        tail = "\n\t* getDataTypeConstrained: " + elem.getDataType().getList().toString();
                    } else if (elem.getDataType().getStructure() != null) {
                        tail = "\n\t* getDataTypeConstrained: " + elem.getDataType().getStructure().toString();
                    } else {
                        // this is illegal by SiLA standard
                        log.error("SiLAElement: invalid SiLA derived type");
                        tail = "null";
                    }
                    log.info(tail);
                    introspectDataType(panel, elem.getDataType());
                }
            } else {
                // TODO: This should not be possible. -> Throw error.
                log.error("SiLAElement: getDataType() = null");
            }
        }
    }

    private void introspectDataType(JPanel comPanel, final DataTypeType type) {
        if (type != null) {
            if (type.getBasic() != null) {
                // basic type
                log.info("BasicType: " + type.getBasic().name());
                switch (type.getBasic()) {
                    case BINARY:
                        // TODO: implement
                        break;
                    case BOOLEAN:
                        // TODO: implement
                        break;
                    case DATE:
                        // TODO: implement
                        break;
                    case INTEGER:
                        JSpinner intSpinner = new JSpinner();
                        intSpinner.setModel(new SpinnerNumberModel());
                        comPanel.add(intSpinner);
                        break;
                    case REAL:
                        JSpinner realSpinner = new JSpinner();
                        realSpinner.setModel(new SpinnerNumberModel());
                        comPanel.add(realSpinner);
                        break;
                    case STRING:
                        JTextField strField = new JTextField();
                        strField.setMaximumSize(MAX_SILA_BASIC_TYPE_ELEM_DIM);
                        comPanel.add(strField);
                        break;
                    case TIME:
                        // TODO: implement
                        break;
                    case TIMESTAMP:
                        // TODO: implement
                        break;
                    case ANY:
                    default:
                        // TODO: implement
                        log.warn("No handling for ANY type implemented");
                }
            } else {
                // derived type
                if (type.getConstrained() != null) {
                    log.info("cons + " + type.getConstrained().getConstraints().getMaximalElementCount());
                    introspectDataType(comPanel, type.getConstrained().getDataType());
                } else if (type.getList() != null) {
                    log.info("list + ");
                    introspectDataType(comPanel, type.getList().getDataType());
                    // TODO: add buttons to extend the list
                } else if (type.getStructure() != null) {
                    log.info("structure ");
                    // TODO
                } else {
                    log.info("???");
                }
            }
        }
    }
}
