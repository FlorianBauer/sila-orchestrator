package de.fau.clients.orchestrator;

import java.awt.Insets;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import sila_java.library.core.models.Feature;
import sila_java.library.core.models.Feature.Command;
import sila_java.library.core.models.Feature.Metadata;
import sila_java.library.core.models.Feature.Property;
import sila_java.library.core.models.SiLAElement;

/**
 * Views information and details about the corresponding SiLA feature.
 */
@SuppressWarnings("serial")
public class FeatureTreeNode extends DefaultMutableTreeNode implements Presentable {

    private static final String PADDING_LEFT_40PX_STYLE = "style=\"padding-left:40px;\"";
    private static final String STYLE = "<style>"
            + "table {"
            + "  border-collapse:collapse;"
            + "  border-spacing:none;"
            + "  text-align:left;"
            + "}"
            + "th, td {"
            + "  border:2px solid black;"
            + "  padding:5px;"
            + "  border-spacing:none;"
            + "}"
            + "p {"
            + "  padding-left:20px;"
            + "  text-align:left;"
            + "  width:500px;"
            + "}"
            + "</style> ";
    private final Feature feature;
    private JPanel panel = null;

    public FeatureTreeNode(final Feature feature) {
        this.feature = feature;
    }

    @Override
    public JPanel getPresenter() {
        if (panel == null) {
            final StringBuilder builder = new StringBuilder(4096);
            builder.append("<html>")
                    .append(STYLE)
                    .append("<h1>")
                    .append(feature.getDisplayName())
                    .append(" – [")
                    .append(feature.getIdentifier())
                    .append("]</h1><br><div style=\"text-align:left; width:500px;\">")
                    .append(feature.getDescription())
                    .append("<br><br>Category: ")
                    .append(feature.getCategory())
                    .append("<br>Feature Version: ")
                    .append(feature.getFeatureVersion())
                    .append("<br>Locale: ")
                    .append(feature.getLocale())
                    .append("<br>Maturity Level: ")
                    .append(feature.getMaturityLevel())
                    .append("<br>Originator: ")
                    .append(feature.getOriginator())
                    .append("<br>SiLA2Version: ")
                    .append(feature.getSiLA2Version())
                    .append("<br><br></div>");

            final List<Property> propList = feature.getProperty();
            if (!propList.isEmpty()) {
                builder.append("<h2>Properties</h2><table>");
                for (final Property prop : propList) {
                    builder.append("<tr><td><b>")
                            .append(prop.getDisplayName())
                            .append(" – [")
                            .append(prop.getIdentifier())
                            .append("]</b><p>")
                            .append(prop.getDescription())
                            .append("</p></td></tr>");
                }
                builder.append("</table><br>");
            }

            final List<Command> cmdList = feature.getCommand();
            if (!cmdList.isEmpty()) {
                builder.append("<h2>Commands</h2>");
                for (final Command cmd : cmdList) {
                    builder.append("<table><tr><td><b>")
                            .append(cmd.getDisplayName())
                            .append(" – [")
                            .append(cmd.getIdentifier())
                            .append("]</b><p>")
                            .append(cmd.getDescription())
                            .append("</p></td></tr>");
                    final List<SiLAElement> paramList = cmd.getParameter();
                    if (!paramList.isEmpty()) {
                        builder.append("<tr><td><i>Parameter:</i>");
                        for (final SiLAElement param : paramList) {
                            builder.append("<p>")
                                    .append(param.getDisplayName())
                                    .append(" – [")
                                    .append(param.getIdentifier())
                                    .append("]</p><p " + PADDING_LEFT_40PX_STYLE + ">")
                                    .append(param.getDescription())
                                    .append("</p>");
                        }
                        builder.append("</td></tr>");
                    }

                    final List<SiLAElement> respList = cmd.getResponse();
                    if (!respList.isEmpty()) {
                        builder.append("<tr><td><i>Response:</i>");
                        for (final SiLAElement resp : respList) {
                            builder.append("<p>")
                                    .append(resp.getDisplayName())
                                    .append(" – [")
                                    .append(resp.getIdentifier())
                                    .append("]</p><p " + PADDING_LEFT_40PX_STYLE + ">")
                                    .append(resp.getDescription())
                                    .append("</p>");
                        }
                        builder.append("</td></tr>");
                    }
                    builder.append("</table><br>");
                }
            }

            final List<Metadata> metaList = feature.getMetadata();
            if (!metaList.isEmpty()) {
                builder.append("<h2>Metadata</h2>");
                for (final Metadata meta : metaList) {
                    builder.append("<tr><td><b>")
                            .append(meta.getDisplayName())
                            .append(" – [").append(meta.getIdentifier())
                            .append("]</b><p>")
                            .append(meta.getDescription())
                            .append("</p></td></tr>");
                }
            }
            builder.append("</html>");

            final JEditorPane txtPane = new JEditorPane();
            txtPane.setContentType("text/html");
            txtPane.setEditable(false);
            txtPane.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            txtPane.putClientProperty(javax.swing.JTextPane.HONOR_DISPLAY_PROPERTIES, true);
            txtPane.setMargin(new Insets(20, 20, 20, 20));
            txtPane.setText(builder.toString());

            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(txtPane);
        }
        return panel;
    }
}
