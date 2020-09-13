package de.fau.clients.orchestrator.nodes;

import com.fasterxml.jackson.databind.JsonNode;
import de.fau.clients.orchestrator.utils.ImagePanel;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JLabel;
import sila_java.library.core.models.BasicType;
import sila_java.library.core.models.Constraints;

/**
 * A Factory for <code>ConstraintBasicNode</code>s.
 *
 * @see ConstraintBasicNode
 */
public class ConstraintBasicNodeFactory {

    private static final URL IMAGE_MISSING = ConstraintBasicNode.class.getResource("/icons/document-missing-64px.png");

    private ConstraintBasicNodeFactory() {
        throw new UnsupportedOperationException("Instantiation not allowed.");
    }

    protected static BasicNode createConstrainedBinaryTypeFromJson(
            final Constraints constraints,
            final JsonNode jsonNode) {
        final JComponent comp;
        final Supplier<String> supp;
        final Constraints.ContentType contentType = constraints.getContentType();
        if (contentType != null) {
            if (contentType.getType().equalsIgnoreCase("image")) {
                final String subtype = contentType.getSubtype();
                if (subtype.equalsIgnoreCase("jpeg")
                        || subtype.equalsIgnoreCase("png")
                        || subtype.equalsIgnoreCase("bmp")
                        || subtype.equalsIgnoreCase("gif")) {
                    BufferedImage img = null;
                    try {
                        if (jsonNode != null) {
                            img = ImageIO.read(new ByteArrayInputStream(jsonNode.binaryValue()));
                        } else {
                            img = ImageIO.read(IMAGE_MISSING);
                        }
                    } catch (IOException ex) {
                        System.err.println(ex.getMessage());
                    }

                    if (img != null) {
                        comp = new ImagePanel(img);
                        final BufferedImage tmpImg = img;
                        supp = () -> {
                            final ByteArrayOutputStream os = new ByteArrayOutputStream();
                            boolean hasWriter;
                            try {
                                hasWriter = ImageIO.write(tmpImg,
                                        subtype.toLowerCase(),
                                        Base64.getEncoder().wrap(os));
                                os.close();
                            } catch (IOException ex) {
                                return ex.getMessage();
                            }
                            if (hasWriter) {
                                return os.toString(StandardCharsets.UTF_8);
                            } else {
                                return "No ImageWriter found.";
                            }
                        };
                        return new ConstraintBasicNode(BasicType.BINARY, comp, supp, constraints);
                    }
                }
            }
        }
        comp = new JLabel("Unknown constrained binary type");
        supp = () -> ("not implemented 04");
        return new ConstraintBasicNode(BasicType.BINARY, comp, supp, constraints);
    }
}
