package de.fau.clients.orchestrator;

import de.fau.clients.orchestrator.utils.VersionNumber;
import java.awt.Insets;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
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
public class FeatureInfoTreeNode extends DefaultMutableTreeNode implements Presentable {

    private static final String DOC_WIDTH = "width:550px;";
    private static final HTMLEditorKit FEAT_INFO_EDITOR_KIT;

    static {
        FEAT_INFO_EDITOR_KIT = new HTMLEditorKit();
        final StyleSheet styleSheet = FEAT_INFO_EDITOR_KIT.getStyleSheet();
        styleSheet.addRule("table {"
                + "  border-collapse:collapse;"
                + "  border-spacing:none;"
                + "  text-align:left;"
                + DOC_WIDTH
                + "}");
        styleSheet.addRule("th, td {"
                + "  border:2px solid black;"
                + "  padding:5px;"
                + "  border-spacing:none;"
                + "}");
        styleSheet.addRule("pre {"
                + "  font-family:monospace;"
                + "  font-size:10pt"
                + "}");
    }

    private final Feature feature;
    /**
     * Fully Qualified Feature Identifier string
     */
    private final String fqfi;
    private JPanel panel = null;

    public FeatureInfoTreeNode(final Feature feature) {
        this.feature = feature;
        final VersionNumber featVer = VersionNumber.parseVersionString(this.feature.getFeatureVersion());
        this.fqfi = this.feature.getOriginator()
                + "/" + this.feature.getCategory()
                + "/" + this.feature.getIdentifier()
                + "/v" + featVer.getMajorNumber();
    }

    @Override
    public JPanel getPresenter() {
        if (panel == null) {
            final StringBuilder builder = new StringBuilder(4096);
            builder.append("<html><body>")
                    .append("<h2>[")
                    .append(feature.getIdentifier())
                    .append("] – ")
                    .append(feature.getDisplayName())
                    .append("</h2><br><div style=\"text-align:left; " + DOC_WIDTH + "\">")
                    .append(feature.getDescription())
                    .append("<br><br><pre>")
                    .append(fqfi)
                    .append("<br>Category: ")
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
                    .append("</pre><br></div>");

            printProperties(builder, feature.getProperty());
            printCommands(builder, feature.getCommand());
            printMetadata(builder, feature.getMetadata());

            builder.append("</body></html>");

            final JEditorPane txtPane = new JEditorPane();
            txtPane.setContentType("text/html");
            txtPane.setEditorKit(FEAT_INFO_EDITOR_KIT);
            txtPane.setEditable(false);
            txtPane.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            txtPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
            txtPane.setMargin(new Insets(10, 20, 20, 20));
            txtPane.setText(builder.toString());

            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(txtPane);
        }
        return panel;
    }

    protected void printProperties(final StringBuilder builder, final List<Property> propList) {
        if (!propList.isEmpty()) {
            builder.append("<h3>Properties</h3>");
            for (final Property prop : propList) {
                builder.append("<table><tr><td><b>[")
                        .append(prop.getIdentifier())
                        .append("] – ")
                        .append(prop.getDisplayName())
                        .append("</b><p><br>")
                        .append(prop.getDescription())
                        .append("</p><pre>")
                        .append(fqfi)
                        .append("/Property/")
                        .append(prop.getIdentifier())
                        .append("<br>Observable: ")
                        .append(prop.getObservable())
                        .append("</pre></td></tr></table><br>");
            }
            builder.append("<br>");
        }
    }

    protected void printCommands(final StringBuilder builder, final List<Command> cmdList) {
        if (!cmdList.isEmpty()) {
            builder.append("<h3>Commands</h3>");
            for (final Command cmd : cmdList) {
                final String fqci = fqfi + "/Command/" + cmd.getIdentifier();
                builder.append("<table><tr><td><b>[")
                        .append(cmd.getIdentifier())
                        .append("] – ")
                        .append(cmd.getDisplayName())
                        .append("</b><p><br>")
                        .append(cmd.getDescription())
                        .append("</p><pre>")
                        .append(fqci)
                        .append("<br>Observable: ")
                        .append(cmd.getObservable())
                        .append("</pre></td></tr>");

                final List<SiLAElement> paramList = cmd.getParameter();
                if (!paramList.isEmpty()) {
                    builder.append("<tr><td><i>Parameter:</i><br>");
                    for (final SiLAElement param : paramList) {
                        builder.append("<br>[")
                                .append(param.getIdentifier())
                                .append("] – ")
                                .append(param.getDisplayName())
                                .append("<p>")
                                .append(param.getDescription())
                                .append("</p><pre>")
                                .append(fqci)
                                .append("/Parameter/")
                                .append(param.getIdentifier())
                                .append("</pre>");
                    }
                    builder.append("</td></tr>");
                }

                final List<SiLAElement> intermRespList = cmd.getIntermediateResponse();
                if (!intermRespList.isEmpty()) {
                    builder.append("<tr><td><i>Intermediate Response:</i><br>");
                    for (final SiLAElement intermResp : intermRespList) {
                        builder.append("<br>[")
                                .append(intermResp.getIdentifier())
                                .append("] – ")
                                .append(intermResp.getDisplayName())
                                .append("<p>")
                                .append(intermResp.getDescription())
                                .append("</p><pre>")
                                .append(fqci)
                                .append("/IntermediateResponse/")
                                .append(intermResp.getIdentifier())
                                .append("</pre>");
                    }
                    builder.append("</td></tr>");
                }

                final List<SiLAElement> respList = cmd.getResponse();
                if (!respList.isEmpty()) {
                    builder.append("<tr><td><i>Response:</i><br>");
                    for (final SiLAElement resp : respList) {
                        builder.append("<br>[")
                                .append(resp.getIdentifier())
                                .append("] – ")
                                .append(resp.getDisplayName())
                                .append("<p>")
                                .append(resp.getDescription())
                                .append("</p><pre>")
                                .append(fqci)
                                .append("/Response/")
                                .append(resp.getIdentifier())
                                .append("</pre>");
                    }
                    builder.append("</td></tr>");
                }
                builder.append("</table><br>");
            }
        }
    }

    protected void printMetadata(final StringBuilder builder, final List<Metadata> metaList) {
        if (!metaList.isEmpty()) {
            builder.append("<h3>Metadata</h3>");
            for (final Metadata meta : metaList) {
                builder.append("<tr><td><b>[")
                        .append(meta.getIdentifier())
                        .append("] – ")
                        .append(meta.getDisplayName())
                        .append("</b><p>")
                        .append(meta.getDescription())
                        .append("</p><pre>")
                        .append(fqfi)
                        .append("/Metadata/")
                        .append(meta.getIdentifier())
                        .append("</pre></td></tr>");
            }
        }
    }
}
