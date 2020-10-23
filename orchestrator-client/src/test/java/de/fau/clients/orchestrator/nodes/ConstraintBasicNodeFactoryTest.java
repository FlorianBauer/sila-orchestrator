package de.fau.clients.orchestrator.nodes;

import de.fau.clients.orchestrator.utils.ImagePanel;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import sila_java.library.core.models.BasicType;
import sila_java.library.core.models.Constraints;

public class ConstraintBasicNodeFactoryTest {

    @Test
    void create() {
        try {
            ConstraintBasicNodeFactory.create(null, BasicType.BOOLEAN, null, null);
            fail("NullPointerException was expected but not thrown.");
        } catch (NullPointerException ex) {
        } catch (Exception ex) {
            fail("Only a NullPointer was expected.");
        }

        Constraints con = new Constraints();
        try {
            ConstraintBasicNodeFactory.create(null, null, con, null);
            fail("NullPointerException was expected but not thrown.");
        } catch (NullPointerException ex) {
        } catch (Exception ex) {
            fail("Only a NullPointer was expected.");
        }

        ConstraintBasicNode act = ConstraintBasicNodeFactory.create(null, BasicType.BINARY, con, null);
        assertEquals(BasicType.BINARY, act.getType());
        assertEquals("", act.getValue());
        assertEquals(JEditorPane.class, act.getComponent().getClass());
    }

    @Test
    public void createConstrainedBinaryType() {
        try {
            ConstraintBasicNode act = ConstraintBasicNodeFactory.createConstrainedBinaryType(null, "".getBytes());
            fail("NullPointerException was expected but not thrown.");
        } catch (NullPointerException ex) {
        } catch (Exception ex) {
            fail("Only a NullPointer was expected.");
        }

        Constraints con = new Constraints();
        try {
            ConstraintBasicNode act = ConstraintBasicNodeFactory.createConstrainedBinaryType(con, null);
            fail("NullPointerException was expected but not thrown.");
        } catch (NullPointerException ex) {
        } catch (Exception ex) {
            fail("Only a NullPointer was expected.");
        }

        byte[] binaryVal = "Lorem ipsum".getBytes();
        ConstraintBasicNode act = ConstraintBasicNodeFactory.createConstrainedBinaryType(con, binaryVal);
        assertEquals(BasicType.BINARY, act.getType());
        assertEquals(true, act.isEditable);
        assertNotNull(act.toJson());
        String exp = Base64.getEncoder().encodeToString(binaryVal);
        assertEquals(exp, act.getValue());
        assertEquals("{\"value\":\"" + exp + "\"}", act.toJsonString());
        assertEquals(JEditorPane.class, act.getComponent().getClass());
        assertEquals("Lorem ipsum", ((JEditorPane) act.getComponent()).getText());

        final String unwantedStr = "\"äöü\\n\\tß\\\\0xf00bar:\n<></>\\\";€¶@æ²³\\b01101®testâ€™\\\\u0048{[]}Test\\u2212\";";
        binaryVal = unwantedStr.getBytes(StandardCharsets.UTF_8);
        act = ConstraintBasicNodeFactory.createConstrainedBinaryType(con, binaryVal);
        assertEquals(BasicType.BINARY, act.getType());
        assertEquals(true, act.isEditable);
        assertNotNull(act.toJson());
        exp = Base64.getEncoder().encodeToString(binaryVal);
        assertEquals(exp, act.getValue());
        assertEquals("{\"value\":\"" + exp + "\"}", act.toJsonString());
        assertEquals(JEditorPane.class, act.getComponent().getClass());
        assertEquals(unwantedStr, ((JEditorPane) act.getComponent()).getText());

        // Wrong charset encoding.
        binaryVal = new byte[]{(byte) 0xFF};
        act = ConstraintBasicNodeFactory.createConstrainedBinaryType(con, binaryVal);
        assertEquals(BasicType.BINARY, act.getType());
        assertEquals("", act.getValue());
        assertEquals("{\"value\":\"\"}", act.toJsonString());
        assertEquals(JLabel.class, act.getComponent().getClass());
        assertTrue(((JLabel) act.getComponent()).getText().startsWith("Error: Unsupported"));

        binaryVal = unwantedStr.getBytes(StandardCharsets.ISO_8859_1);
        act = ConstraintBasicNodeFactory.createConstrainedBinaryType(con, binaryVal);
        assertEquals(BasicType.BINARY, act.getType());
        assertNotNull(act.toJson());
        assertEquals("", act.getValue());
        assertEquals(JLabel.class, act.getComponent().getClass());
        assertTrue(((JLabel) act.getComponent()).getText().startsWith("Error: Unsupported"));

        con = new Constraints();
        con.setLength(BigInteger.valueOf(5));
        binaryVal = "12345".getBytes();
        act = ConstraintBasicNodeFactory.createConstrainedBinaryType(con, binaryVal);
        assertEquals(BasicType.BINARY, act.getType());
        assertNotEquals("", act.getValue());
        assertEquals(JEditorPane.class, act.getComponent().getClass());

        // Wrong length.
        binaryVal = "1234".getBytes();
        act = ConstraintBasicNodeFactory.createConstrainedBinaryType(con, binaryVal);
        assertEquals(BasicType.BINARY, act.getType());
        assertEquals("", act.getValue());
        assertEquals(JLabel.class, act.getComponent().getClass());
        assertTrue(((JLabel) act.getComponent()).getText().startsWith("Error: Wrong binary length (len != 5 bytes)."));

        binaryVal = "123456".getBytes();
        act = ConstraintBasicNodeFactory.createConstrainedBinaryType(con, binaryVal);
        assertEquals(BasicType.BINARY, act.getType());
        assertEquals("", act.getValue());
        assertEquals(JLabel.class, act.getComponent().getClass());
        assertTrue(((JLabel) act.getComponent()).getText().startsWith("Error: Wrong binary length (len != 5 bytes)."));

        con = new Constraints();
        con.setMinimalLength(BigInteger.valueOf(7));
        con.setMaximalLength(BigInteger.valueOf(9));
        binaryVal = "1234567".getBytes();
        act = ConstraintBasicNodeFactory.createConstrainedBinaryType(con, binaryVal);
        assertEquals(BasicType.BINARY, act.getType());
        assertNotEquals("", act.getValue());
        assertEquals(JEditorPane.class, act.getComponent().getClass());

        binaryVal = "12345678".getBytes();
        act = ConstraintBasicNodeFactory.createConstrainedBinaryType(con, binaryVal);
        assertEquals(BasicType.BINARY, act.getType());
        assertNotEquals("", act.getValue());
        assertEquals(JEditorPane.class, act.getComponent().getClass());

        // Wrong minimal length
        binaryVal = "123456".getBytes();
        act = ConstraintBasicNodeFactory.createConstrainedBinaryType(con, binaryVal);
        assertEquals(BasicType.BINARY, act.getType());
        assertEquals("", act.getValue());
        assertEquals(JLabel.class, act.getComponent().getClass());
        assertTrue(((JLabel) act.getComponent()).getText().startsWith("Error: Wrong binary length (len < 7 bytes)."));

        // Wrong maximal length
        binaryVal = "1234567890".getBytes();
        act = ConstraintBasicNodeFactory.createConstrainedBinaryType(con, binaryVal);
        assertEquals(BasicType.BINARY, act.getType());
        assertEquals("", act.getValue());
        assertEquals(JLabel.class, act.getComponent().getClass());
        assertTrue(((JLabel) act.getComponent()).getText().startsWith("Error: Wrong binary length (len > 9 bytes)."));

        Constraints.ContentType cType = new Constraints.ContentType();
        cType.setType("text");
        cType.setSubtype(null);
        con = new Constraints();
        con.setContentType(cType);
        binaryVal = "Lorem ipsum".getBytes();
        act = ConstraintBasicNodeFactory.createConstrainedBinaryType(con, binaryVal);
        assertEquals(BasicType.BINARY, act.getType());
        assertEquals(Base64.getEncoder().encodeToString(binaryVal), act.getValue());
        assertEquals(JEditorPane.class, act.getComponent().getClass());
        assertEquals("Lorem ipsum", ((JEditorPane) act.getComponent()).getText());

        cType.setType("text");
        cType.setSubtype("xml");
        con = new Constraints();
        con.setContentType(cType);
        final String xmlText = "<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\" standalone=\\\"yes\\\"?>\\n"
                + "<dataTypeType>\\n"
                + "    <Basic>String</Basic>\\n"
                + "</dataTypeType>\\n\"";
        binaryVal = xmlText.getBytes();
        act = ConstraintBasicNodeFactory.createConstrainedBinaryType(con, binaryVal);
        assertEquals(BasicType.BINARY, act.getType());
        assertNotEquals("", act.getValue());
        assertEquals(Box.class, act.getComponent().getClass());

        cType.setType("application");
        cType.setSubtype("animl");
        con = new Constraints();
        con.setContentType(cType);
        binaryVal = xmlText.getBytes();
        act = ConstraintBasicNodeFactory.createConstrainedBinaryType(con, binaryVal);
        assertEquals(BasicType.BINARY, act.getType());
        assertEquals(Base64.getEncoder().encodeToString(binaryVal), act.getValue());
        assertNotEquals("", act.getValue());
        assertEquals(Box.class, act.getComponent().getClass());

        cType.setType("image");
        cType.setSubtype("png");
        con = new Constraints();
        con.setContentType(cType);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedImage buffImg;
        final URL siloPng = ConstraintBasicNode.class.getResource("/icons/sila-orchestrator-16px.png");
        try {
            buffImg = ImageIO.read(siloPng);
            ImageIO.write(buffImg, "png", baos);
            baos.close();
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
        binaryVal = baos.toByteArray();
        assertNotEquals(0, binaryVal.length);
        act = ConstraintBasicNodeFactory.createConstrainedBinaryType(con, binaryVal);
        assertEquals(BasicType.BINARY, act.getType());
        assertNotEquals("", act.getValue());
        assertEquals(ImagePanel.class, act.getComponent().getClass());
    }

    @Test
    public void createXmlNodeFromBinary() {
        final String xmlText = "<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\" standalone=\\\"yes\\\"?>\\n"
                + "<dataTypeType>\\n"
                + "    <Basic>String</Basic>\\n"
                + "</dataTypeType>\\n\"";
        byte[] binaryVal = xmlText.getBytes();
        ConstraintBasicNode act = ConstraintBasicNodeFactory.createXmlNodeFromBinary(new Constraints(), binaryVal);
        assertEquals(BasicType.BINARY, act.getType());
        assertEquals(true, act.isEditable);
        assertEquals(Base64.getEncoder().encodeToString(binaryVal), act.getValue());
        assertEquals(Box.class, act.getComponent().getClass());
        assertEquals(JEditorPane.class, ((JViewport) ((JScrollPane) act.getComponent().getComponent(0)).getComponent(0)).getComponent(0).getClass());
        assertEquals(xmlText, ((JEditorPane) ((JViewport) ((JScrollPane) act.getComponent().getComponent(0)).getComponent(0)).getComponent(0)).getText());
    }

    @Test
    public void createImageNodeFromBinary() {
        Constraints.ContentType cType = new Constraints.ContentType();
        cType.setType("image");
        cType.setSubtype("png");
        Constraints con = new Constraints();
        con.setContentType(cType);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedImage buffImg;
        final URL siloPng = ConstraintBasicNode.class.getResource("/icons/sila-orchestrator-16px.png");
        try {
            buffImg = ImageIO.read(siloPng);
            ImageIO.write(buffImg, "png", baos);
            baos.close();
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
        final byte[] binaryVal = baos.toByteArray();
        ByteArrayOutputStream bla = new ByteArrayOutputStream();
        bla.writeBytes(binaryVal);
        assertNotEquals(0, binaryVal.length);
        ConstraintBasicNode act = ConstraintBasicNodeFactory.createImageNodeFromBinary(con, binaryVal);
        assertEquals(BasicType.BINARY, act.getType());
        assertTrue(Base64.getEncoder().encodeToString(binaryVal).startsWith(act.getValue()));
        assertEquals(ImagePanel.class, act.getComponent().getClass());
    }

    @Test
    public void getSupportedContentType() {
        try {
            ConstraintBasicNodeFactory.getSupportedContentType(null);
            fail("NullPointerException was expected but not thrown.");
        } catch (NullPointerException ex) {
        } catch (Exception ex) {
            fail("Only a NullPointerException was expected.");
        }

        Constraints.ContentType cType = new Constraints.ContentType();
        try {
            ConstraintBasicNodeFactory.getSupportedContentType(cType);
            fail("NullPointerException was expected but not thrown.");
        } catch (NullPointerException ex) {
        } catch (Exception ex) {
            fail("Only a NullPointerException was expected.");
        }

        cType.setType("Bla");
        assertEquals(InternalContentType.UNKNOWN, ConstraintBasicNodeFactory.getSupportedContentType(cType));
        cType.setType("teXt");
        cType.setSubtype("");
        assertEquals(InternalContentType.TEXT, ConstraintBasicNodeFactory.getSupportedContentType(cType));
        cType.setSubtype("XML");
        assertEquals(InternalContentType.TEXT_XML, ConstraintBasicNodeFactory.getSupportedContentType(cType));
        cType.setType("image");
        cType.setSubtype("");
        assertEquals(InternalContentType.UNSUPPORTED, ConstraintBasicNodeFactory.getSupportedContentType(cType));
        cType.setType("Image");
        cType.setSubtype("jpeg");
        assertEquals(InternalContentType.IMAGE, ConstraintBasicNodeFactory.getSupportedContentType(cType));
        cType.setSubtype("png");
        assertEquals(InternalContentType.IMAGE, ConstraintBasicNodeFactory.getSupportedContentType(cType));
        cType.setSubtype("bmp");
        assertEquals(InternalContentType.IMAGE, ConstraintBasicNodeFactory.getSupportedContentType(cType));
        cType.setSubtype("gif");
        assertEquals(InternalContentType.IMAGE, ConstraintBasicNodeFactory.getSupportedContentType(cType));
        cType.setSubtype("webp"); // Nobody likes WebP images
        assertEquals(InternalContentType.UNSUPPORTED, ConstraintBasicNodeFactory.getSupportedContentType(cType));
        cType.setType("application");
        cType.setSubtype("animl");
        assertEquals(InternalContentType.TEXT_XML, ConstraintBasicNodeFactory.getSupportedContentType(cType));
        cType.setType("application");
        cType.setSubtype("JavaScript");
        assertEquals(InternalContentType.UNKNOWN, ConstraintBasicNodeFactory.getSupportedContentType(cType));
    }

    @Test
    public void createConstrainedBoolean() {
        Constraints con = new Constraints();
        try {
            ConstraintBasicNodeFactory.create(null, BasicType.BOOLEAN, con, null);
            fail("IllegalArgumentException was expected but not thrown.");
        } catch (IllegalArgumentException ex) {
        } catch (Exception ex) {
            fail("Only a IllegalArgumentException was expected.");
        }
    }
}
