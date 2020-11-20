package de.fau.clients.orchestrator.nodes;

import de.fau.clients.orchestrator.utils.DateTimeParser;
import de.fau.clients.orchestrator.utils.ImagePanel;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JViewport;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import sila_java.library.core.models.BasicType;
import sila_java.library.core.models.Constraints;

public class ConstraintBasicNodeFactoryTest {

    private static final double DELTA = 0.000001;

    @Test
    void create() {
        try {
            ConstraintBasicNodeFactory.create(null, BasicType.BOOLEAN, null);
            fail("NullPointerException was expected but not thrown.");
        } catch (NullPointerException ex) {
        } catch (Exception ex) {
            fail("Only a NullPointer was expected.");
        }

        Constraints con = new Constraints();
        try {
            ConstraintBasicNodeFactory.create(null, null, con);
            fail("NullPointerException was expected but not thrown.");
        } catch (NullPointerException ex) {
        } catch (Exception ex) {
            fail("Only a NullPointer was expected.");
        }

        BasicNode act = ConstraintBasicNodeFactory.create(null, BasicType.BINARY, con);
        assertEquals(BasicType.BINARY, act.getType());
        assertArrayEquals("".getBytes(), (byte[]) act.getValue());
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

        byte[] binaryVal = "Lorem ipsum".getBytes(StandardCharsets.UTF_8);
        ConstraintBasicNode act = ConstraintBasicNodeFactory.createConstrainedBinaryType(con, binaryVal);
        assertEquals(BasicType.BINARY, act.getType());
        assertEquals(true, act.isEditable);
        assertNotNull(act.toJson());
        assertArrayEquals(binaryVal, (byte[]) act.getValue());
        String exp = Base64.getEncoder().encodeToString(binaryVal);
        assertEquals("{\"value\":\"" + exp + "\"}", act.toJsonString());
        assertEquals(JEditorPane.class, act.getComponent().getClass());
        assertEquals("Lorem ipsum", ((JEditorPane) act.getComponent()).getText());

        final String unwantedStr = "\"äöü\\n\\tß\\\\0xf00bar:\n<></>\\\";€¶@æ²³\\b01101®testâ€™\\\\u0048{[]}Test\\u2212\";";
        binaryVal = unwantedStr.getBytes(StandardCharsets.UTF_8);
        act = ConstraintBasicNodeFactory.createConstrainedBinaryType(con, binaryVal);
        assertEquals(BasicType.BINARY, act.getType());
        assertEquals(true, act.isEditable);
        assertNotNull(act.toJson());
        assertArrayEquals(binaryVal, (byte[]) act.getValue());
        exp = Base64.getEncoder().encodeToString(binaryVal);
        assertEquals("{\"value\":\"" + exp + "\"}", act.toJsonString());
        assertEquals(JEditorPane.class, act.getComponent().getClass());
        assertEquals(unwantedStr, ((JEditorPane) act.getComponent()).getText());

        // Wrong charset encoding.
        binaryVal = new byte[]{(byte) 0xFF};
        act = ConstraintBasicNodeFactory.createConstrainedBinaryType(con, binaryVal);
        assertEquals(BasicType.BINARY, act.getType());
        assertArrayEquals("".getBytes(), (byte[]) act.getValue());
        assertEquals("{\"value\":\"\"}", act.toJsonString());
        assertEquals(JLabel.class, act.getComponent().getClass());
        assertTrue(((JLabel) act.getComponent()).getText().startsWith("Error: Unsupported"));

        binaryVal = unwantedStr.getBytes(StandardCharsets.ISO_8859_1);
        act = ConstraintBasicNodeFactory.createConstrainedBinaryType(con, binaryVal);
        assertEquals(BasicType.BINARY, act.getType());
        assertNotNull(act.toJson());
        assertArrayEquals("".getBytes(), (byte[]) act.getValue());
        assertEquals(JLabel.class, act.getComponent().getClass());
        assertTrue(((JLabel) act.getComponent()).getText().startsWith("Error: Unsupported"));

        con = new Constraints();
        con.setLength(BigInteger.valueOf(5));
        binaryVal = "12345".getBytes();
        act = ConstraintBasicNodeFactory.createConstrainedBinaryType(con, binaryVal);
        assertEquals(BasicType.BINARY, act.getType());
        assertArrayEquals(binaryVal, (byte[]) act.getValue());
        assertEquals(JEditorPane.class, act.getComponent().getClass());

        // Wrong length.
        binaryVal = "1234".getBytes();
        act = ConstraintBasicNodeFactory.createConstrainedBinaryType(con, binaryVal);
        assertEquals(BasicType.BINARY, act.getType());
        assertArrayEquals("".getBytes(), (byte[]) act.getValue());
        assertEquals(JLabel.class, act.getComponent().getClass());
        assertTrue(((JLabel) act.getComponent()).getText().startsWith("Error: Wrong binary length (len != 5 bytes)."));

        binaryVal = "123456".getBytes();
        act = ConstraintBasicNodeFactory.createConstrainedBinaryType(con, binaryVal);
        assertEquals(BasicType.BINARY, act.getType());
        assertArrayEquals("".getBytes(), (byte[]) act.getValue());
        assertEquals(JLabel.class, act.getComponent().getClass());
        assertTrue(((JLabel) act.getComponent()).getText().startsWith("Error: Wrong binary length (len != 5 bytes)."));

        con = new Constraints();
        con.setMinimalLength(BigInteger.valueOf(7));
        con.setMaximalLength(BigInteger.valueOf(9));
        binaryVal = "1234567".getBytes();
        act = ConstraintBasicNodeFactory.createConstrainedBinaryType(con, binaryVal);
        assertEquals(BasicType.BINARY, act.getType());
        assertArrayEquals(binaryVal, (byte[]) act.getValue());
        assertEquals(JEditorPane.class, act.getComponent().getClass());

        binaryVal = "12345678".getBytes();
        act = ConstraintBasicNodeFactory.createConstrainedBinaryType(con, binaryVal);
        assertEquals(BasicType.BINARY, act.getType());
        assertArrayEquals(binaryVal, (byte[]) act.getValue());
        assertEquals(JEditorPane.class, act.getComponent().getClass());

        // Wrong minimal length
        binaryVal = "123456".getBytes();
        act = ConstraintBasicNodeFactory.createConstrainedBinaryType(con, binaryVal);
        assertEquals(BasicType.BINARY, act.getType());
        assertArrayEquals("".getBytes(), (byte[]) act.getValue());
        assertEquals(JLabel.class, act.getComponent().getClass());
        assertTrue(((JLabel) act.getComponent()).getText().startsWith("Error: Wrong binary length (len < 7 bytes)."));

        // Wrong maximal length
        binaryVal = "1234567890".getBytes();
        act = ConstraintBasicNodeFactory.createConstrainedBinaryType(con, binaryVal);
        assertEquals(BasicType.BINARY, act.getType());
        assertArrayEquals("".getBytes(), (byte[]) act.getValue());
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
        assertArrayEquals(binaryVal, (byte[]) act.getValue());
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
        assertArrayEquals(binaryVal, (byte[]) act.getValue());
        assertEquals(Box.class, act.getComponent().getClass());

        cType.setType("application");
        cType.setSubtype("animl");
        con = new Constraints();
        con.setContentType(cType);
        binaryVal = xmlText.getBytes();
        act = ConstraintBasicNodeFactory.createConstrainedBinaryType(con, binaryVal);
        assertEquals(BasicType.BINARY, act.getType());
        assertArrayEquals(binaryVal, (byte[]) act.getValue());
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
        assertNotEquals(0, ((byte[]) act.getValue()).length);
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
        assertArrayEquals(binaryVal, (byte[]) act.getValue());
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
//        assertArrayEquals(binaryVal, ((byte[]) act.getValue()));
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
            ConstraintBasicNodeFactory.create(null, BasicType.BOOLEAN, con);
            fail("IllegalArgumentException was expected but not thrown.");
        } catch (IllegalArgumentException ex) {
        } catch (Exception ex) {
            fail("Only a IllegalArgumentException was expected.");
        }
    }

    @Test
    public void createConstrainedDateType() {
        Constraints con = new Constraints();
        LocalDate dateValue = LocalDate.of(2020, 11, 9);

        try {
            ConstraintBasicNodeFactory.createConstrainedDateType(null, dateValue);
            fail("NullPointerException was expected but not thrown.");
        } catch (NullPointerException ex) {
        } catch (Exception ex) {
            fail("Only a NullPointerException was expected.");
        }

        try {
            ConstraintBasicNodeFactory.createConstrainedDateType(con, null);
            fail("NullPointerException was expected but not thrown.");
        } catch (NullPointerException ex) {
        } catch (Exception ex) {
            fail("Only a NullPointerException was expected.");
        }

        ConstraintBasicNode act = ConstraintBasicNodeFactory.createConstrainedDateType(con, dateValue);
        assertEquals(BasicType.DATE, act.getType());
        assertEquals(dateValue, act.getValue());
        assertNotNull(act.getConstaint());
        assertEquals(Box.class, act.getComponent().getClass());
        assertEquals(JSpinner.class, act.getComponent().getComponent(0).getClass());
        assertEquals("2020-11-09", ((JSpinner) act.getComponent().getComponent(0)).getValue().toString());
        assertEquals(JLabel.class, act.getComponent().getComponent(2).getClass());
        assertEquals("Invalid Constraint", ((JLabel) act.getComponent().getComponent(2)).getText());

        Constraints.Set conSet = new Constraints.Set();
        List<String> list = conSet.getValue();
        list.add("2020-12-01");
        list.add("2020-12-02");
        list.add("2020-12-03");
        list.add("2020-12-04");
        con.setSet(conSet);
        act = ConstraintBasicNodeFactory.createConstrainedDateType(con, dateValue);
        assertEquals(DateTimeParser.parseIsoDate("2020-12-01"), act.getValue());
        assertEquals(JComboBox.class, act.getComponent().getClass());
        assertEquals(4, ((JComboBox) act.getComponent()).getItemCount());
        assertEquals(0, ((JComboBox) act.getComponent()).getSelectedIndex());
        assertEquals("2020-12-01", ((JComboBox) act.getComponent()).getSelectedItem().toString());

        dateValue = LocalDate.of(2020, 12, 3);
        act = ConstraintBasicNodeFactory.createConstrainedDateType(con, dateValue);
        assertEquals(dateValue, act.getValue());
        assertEquals(4, ((JComboBox) act.getComponent()).getItemCount());
        assertEquals(2, ((JComboBox) act.getComponent()).getSelectedIndex());
        assertEquals("2020-12-03", ((JComboBox) act.getComponent()).getSelectedItem().toString());

        conSet = new Constraints.Set();
        list = conSet.getValue();
        list.add("2020-12-11");
        list.add("-= invlaid =-");
        list.add("2020-12-13");
        con.setSet(conSet);
        dateValue = LocalDate.of(2020, 12, 13);
        act = ConstraintBasicNodeFactory.createConstrainedDateType(con, dateValue);
        assertEquals(dateValue, act.getValue());
        assertEquals(2, ((JComboBox) act.getComponent()).getItemCount());
        assertEquals(1, ((JComboBox) act.getComponent()).getSelectedIndex());
        assertEquals("2020-12-13", ((JComboBox) act.getComponent()).getSelectedItem().toString());

        con = new Constraints();
        con.setMaximalExclusive("2020-12-24");
        act = ConstraintBasicNodeFactory.createConstrainedDateType(con, dateValue);
        assertEquals("< 2020-12-24", ((JLabel) act.getComponent().getComponent(2)).getText());

        con = new Constraints();
        con.setMaximalInclusive("2020-12-24");
        act = ConstraintBasicNodeFactory.createConstrainedDateType(con, dateValue);
        assertEquals("≤ 2020-12-24", ((JLabel) act.getComponent().getComponent(2)).getText());

        con = new Constraints();
        con.setMinimalExclusive("2018-01-01");
        act = ConstraintBasicNodeFactory.createConstrainedDateType(con, dateValue);
        assertEquals("> 2018-01-01", ((JLabel) act.getComponent().getComponent(2)).getText());

        con = new Constraints();
        con.setMinimalInclusive("2019-01-01");
        act = ConstraintBasicNodeFactory.createConstrainedDateType(con, dateValue);
        assertEquals("≥ 2019-01-01", ((JLabel) act.getComponent().getComponent(2)).getText());

        con.setMaximalInclusive("2020-12-24");
        act = ConstraintBasicNodeFactory.createConstrainedDateType(con, dateValue);
        assertEquals("≥ 2019-01-01 ∧ ≤ 2020-12-24", ((JLabel) act.getComponent().getComponent(2)).getText());
    }

    @Test
    public void createConstrainedIntegerType() {
        long intValue = 4711;
        try {
            ConstraintBasicNodeFactory.createConstrainedIntegerType(null, intValue);
            fail("NullPointerException was expected but not thrown.");
        } catch (NullPointerException ex) {
        } catch (Exception ex) {
            fail("Only a NullPointerException was expected.");
        }

        Constraints con = new Constraints();
        ConstraintBasicNode act = ConstraintBasicNodeFactory.createConstrainedIntegerType(con, intValue);
        assertEquals(BasicType.INTEGER, act.getType());
        assertEquals(4711l, act.getValue());
        assertNotNull(act.getConstaint());
        assertEquals(Box.class, act.getComponent().getClass());
        assertEquals(JSpinner.class, act.getComponent().getComponent(0).getClass());
        assertEquals("4711", ((JSpinner) act.getComponent().getComponent(0)).getValue().toString());
        assertEquals(JLabel.class, act.getComponent().getComponent(2).getClass());
        assertEquals("Invalid Constraint", ((JLabel) act.getComponent().getComponent(2)).getText());

        Constraints.Set conSet = new Constraints.Set();
        List<String> list = conSet.getValue();
        list.add("10");
        list.add("20");
        list.add("30");
        con.setSet(conSet);
        act = ConstraintBasicNodeFactory.createConstrainedIntegerType(con, intValue);
        assertEquals(10l, act.getValue());
        assertEquals(JComboBox.class, act.getComponent().getClass());
        assertEquals(3, ((JComboBox) act.getComponent()).getItemCount());
        assertEquals(0, ((JComboBox) act.getComponent()).getSelectedIndex());
        assertEquals("10", ((JComboBox) act.getComponent()).getSelectedItem().toString());

        intValue = 30;
        act = ConstraintBasicNodeFactory.createConstrainedIntegerType(con, intValue);
        assertEquals(30l, act.getValue());
        assertEquals(3, ((JComboBox) act.getComponent()).getItemCount());
        assertEquals(2, ((JComboBox) act.getComponent()).getSelectedIndex());
        assertEquals("30", ((JComboBox) act.getComponent()).getSelectedItem().toString());

        con = new Constraints();
        con.setMinimalExclusive("0");
        intValue = 0l;
        act = ConstraintBasicNodeFactory.createConstrainedIntegerType(con, intValue);
        assertEquals("1", ((JSpinner) act.getComponent().getComponent(0)).getValue().toString());
        assertEquals("2", ((JSpinner) act.getComponent().getComponent(0)).getNextValue().toString());

        con = new Constraints();
        con.setMaximalExclusive("5");
        intValue = 7l;
        act = ConstraintBasicNodeFactory.createConstrainedIntegerType(con, intValue);
        assertEquals("4", ((JSpinner) act.getComponent().getComponent(0)).getValue().toString());
        assertEquals("3", ((JSpinner) act.getComponent().getComponent(0)).getPreviousValue().toString());

        con = new Constraints();
        con.setMaximalExclusive("256");
        act = ConstraintBasicNodeFactory.createConstrainedIntegerType(con, intValue);
        assertEquals("< 256", ((JLabel) act.getComponent().getComponent(2)).getText());

        con = new Constraints();
        con.setMaximalInclusive("255");
        act = ConstraintBasicNodeFactory.createConstrainedIntegerType(con, intValue);
        assertEquals("≤ 255", ((JLabel) act.getComponent().getComponent(2)).getText());

        con = new Constraints();
        con.setMinimalExclusive("64");
        act = ConstraintBasicNodeFactory.createConstrainedIntegerType(con, intValue);
        assertEquals("> 64", ((JLabel) act.getComponent().getComponent(2)).getText());

        con = new Constraints();
        con.setMinimalInclusive("128");
        act = ConstraintBasicNodeFactory.createConstrainedIntegerType(con, intValue);
        assertEquals("≥ 128", ((JLabel) act.getComponent().getComponent(2)).getText());

        con.setMaximalInclusive("4096");
        act = ConstraintBasicNodeFactory.createConstrainedIntegerType(con, intValue);
        assertEquals("≥ 128 ∧ ≤ 4096", ((JLabel) act.getComponent().getComponent(2)).getText());

        con = new Constraints();
        con.setMinimalInclusive("256");
        con.setMaximalInclusive("64");
        try {
            ConstraintBasicNodeFactory.createConstrainedIntegerType(con, intValue);
            fail("IllegalArgumentException was expected but not thrown.");
        } catch (IllegalArgumentException ex) {
        } catch (Exception ex) {
            fail("Only a IllegalArgumentException was expected.");
        }
    }

    @Test
    public void createConstrainedRealType() {
        double realValue = 3.141592653589793;

        try {
            ConstraintBasicNodeFactory.createConstrainedRealType(null, realValue);
            fail("NullPointerException was expected but not thrown.");
        } catch (NullPointerException ex) {
        } catch (Exception ex) {
            fail("Only a NullPointerException was expected.");
        }

        Constraints con = new Constraints();
        ConstraintBasicNode act = ConstraintBasicNodeFactory.createConstrainedRealType(con, realValue);
        assertEquals(BasicType.REAL, act.getType());
        assertEquals(realValue, (double) act.getValue(), DELTA);
        assertNotNull(act.getConstaint());
        assertEquals(Box.class, act.getComponent().getClass());
        assertEquals(JSpinner.class, act.getComponent().getComponent(0).getClass());
        assertEquals("3.141592653589793", ((JSpinner) act.getComponent().getComponent(0)).getValue().toString());
        assertEquals(JLabel.class, act.getComponent().getComponent(2).getClass());
        assertEquals("Invalid Constraint", ((JLabel) act.getComponent().getComponent(2)).getText());

        Constraints.Set conSet = new Constraints.Set();
        List<String> list = conSet.getValue();
        list.add("0.1");
        list.add("0.2");
        list.add("0.3");
        con.setSet(conSet);
        act = ConstraintBasicNodeFactory.createConstrainedRealType(con, realValue);
        assertEquals(0.1, act.getValue());
        assertEquals(JComboBox.class, act.getComponent().getClass());
        assertEquals(3, ((JComboBox) act.getComponent()).getItemCount());
        assertEquals(0, ((JComboBox) act.getComponent()).getSelectedIndex());
        assertEquals("0.1", ((JComboBox) act.getComponent()).getSelectedItem().toString());

        realValue = 0.3;
        act = ConstraintBasicNodeFactory.createConstrainedRealType(con, realValue);
        assertEquals(0.3, act.getValue());
        assertEquals(3, ((JComboBox) act.getComponent()).getItemCount());
        assertEquals(2, ((JComboBox) act.getComponent()).getSelectedIndex());
        assertEquals("0.3", ((JComboBox) act.getComponent()).getSelectedItem().toString());

        con = new Constraints();
        con.setMinimalExclusive("0.0");
        realValue = 0.0;
        act = ConstraintBasicNodeFactory.createConstrainedRealType(con, realValue);
        assertEquals("> 0.0", ((JLabel) act.getComponent().getComponent(2)).getText());
        assertEquals(0.001, Double.parseDouble(((JSpinner) act.getComponent().getComponent(0)).getValue().toString()), DELTA);
        assertEquals(0.101, Double.parseDouble(((JSpinner) act.getComponent().getComponent(0)).getNextValue().toString()), DELTA);

        con = new Constraints();
        con.setMaximalExclusive("2.2");
        realValue = 2.3;
        act = ConstraintBasicNodeFactory.createConstrainedRealType(con, realValue);
        assertEquals("< 2.2", ((JLabel) act.getComponent().getComponent(2)).getText());
        assertEquals(2.199, Double.parseDouble(((JSpinner) act.getComponent().getComponent(0)).getValue().toString()), DELTA);
        assertEquals(2.099, Double.parseDouble(((JSpinner) act.getComponent().getComponent(0)).getPreviousValue().toString()), DELTA);

        con = new Constraints();
        con.setMaximalInclusive("255.2");
        act = ConstraintBasicNodeFactory.createConstrainedRealType(con, realValue);
        assertEquals("≤ 255.2", ((JLabel) act.getComponent().getComponent(2)).getText());

        con = new Constraints();
        con.setMinimalExclusive("64.3");
        act = ConstraintBasicNodeFactory.createConstrainedRealType(con, realValue);
        assertEquals("> 64.3", ((JLabel) act.getComponent().getComponent(2)).getText());

        con = new Constraints();
        con.setMinimalInclusive("128.4");
        act = ConstraintBasicNodeFactory.createConstrainedRealType(con, realValue);
        assertEquals("≥ 128.4", ((JLabel) act.getComponent().getComponent(2)).getText());

        con.setMaximalInclusive("4096.5");
        act = ConstraintBasicNodeFactory.createConstrainedRealType(con, realValue);
        assertEquals("≥ 128.4 ∧ ≤ 4096.5", ((JLabel) act.getComponent().getComponent(2)).getText());

        con = new Constraints();
        con.setMinimalInclusive("256.6");
        con.setMaximalInclusive("64.7");
        try {
            ConstraintBasicNodeFactory.createConstrainedRealType(con, realValue);
            fail("IllegalArgumentException was expected but not thrown.");
        } catch (IllegalArgumentException ex) {
        } catch (Exception ex) {
            fail("Only a IllegalArgumentException was expected.");
        }
    }

    @Test
    public void createConstrainedTimeType() {
        OffsetTime timeValue = OffsetTime.of(11, 55, 0, 0, ZoneOffset.UTC);

        try {
            ConstraintBasicNodeFactory.createConstrainedTimeType(null, timeValue);
            fail("NullPointerException was expected but not thrown.");
        } catch (NullPointerException ex) {
        } catch (Exception ex) {
            fail("Only a NullPointerException was expected.");
        }

        Constraints con = new Constraints();
        ConstraintBasicNode act = ConstraintBasicNodeFactory.createConstrainedTimeType(con, timeValue);
        assertEquals(BasicType.TIME, act.getType());
        assertEquals(timeValue, act.getValue());
        assertNotNull(act.getConstaint());
        assertEquals(Box.class, act.getComponent().getClass());
        assertEquals(JSpinner.class, act.getComponent().getComponent(0).getClass());
        OffsetTime exp = timeValue.withOffsetSameInstant(DateTimeParser.LOCAL_OFFSET);
        assertEquals(exp.toString(), ((JSpinner) act.getComponent().getComponent(0)).getValue().toString());
        assertEquals(JLabel.class, act.getComponent().getComponent(2).getClass());
        assertEquals("Invalid Constraint", ((JLabel) act.getComponent().getComponent(2)).getText());

        Constraints.Set conSet = new Constraints.Set();
        List<String> list = conSet.getValue();
        list.add("12:01:15Z");
        list.add("12:02:30Z");
        list.add("12:03:45Z");
        list.add("12:04:00Z");
        con.setSet(conSet);
        act = ConstraintBasicNodeFactory.createConstrainedTimeType(con, timeValue);
        assertEquals(DateTimeParser.parseIsoTime("12:01:15Z").withOffsetSameInstant(ZoneOffset.UTC), act.getValue());
        assertEquals(JComboBox.class, act.getComponent().getClass());
        assertEquals(4, ((JComboBox) act.getComponent()).getItemCount());
        assertEquals(0, ((JComboBox) act.getComponent()).getSelectedIndex());
        exp = OffsetTime.of(12, 01, 15, 0, ZoneOffset.UTC).withOffsetSameInstant(DateTimeParser.LOCAL_OFFSET);
        assertEquals(exp.toString(), ((JComboBox) act.getComponent()).getSelectedItem().toString());

        timeValue = OffsetTime.of(12, 03, 45, 0, ZoneOffset.UTC);
        act = ConstraintBasicNodeFactory.createConstrainedTimeType(con, timeValue);
        assertEquals(timeValue, act.getValue());
        assertEquals(2, ((JComboBox) act.getComponent()).getSelectedIndex());
        exp = timeValue.withOffsetSameInstant(DateTimeParser.LOCAL_OFFSET);
        assertEquals(exp.toString(), ((JComboBox) act.getComponent()).getSelectedItem().toString());

        conSet = new Constraints.Set();
        list = conSet.getValue();
        list.add("12:01:15Z");
        list.add("-= invlaid =-");
        list.add("12:03:45Z");
        con.setSet(conSet);
        act = ConstraintBasicNodeFactory.createConstrainedTimeType(con, timeValue);
        assertEquals(DateTimeParser.parseIsoTime("12:03:45Z").withOffsetSameInstant(ZoneOffset.UTC), act.getValue());
        assertEquals(JComboBox.class, act.getComponent().getClass());
        assertEquals(2, ((JComboBox) act.getComponent()).getItemCount());
        assertEquals(1, ((JComboBox) act.getComponent()).getSelectedIndex());

        con = new Constraints();
        con.setMinimalExclusive("08:00:00Z");
        timeValue = OffsetTime.of(05, 03, 45, 0, ZoneOffset.UTC);
        act = ConstraintBasicNodeFactory.createConstrainedTimeType(con, timeValue);
        exp = OffsetTime.of(8, 0, 0, 0, ZoneOffset.UTC).withOffsetSameInstant(DateTimeParser.LOCAL_OFFSET);
        assertEquals("> " + exp.toLocalTime().toString(), ((JLabel) act.getComponent().getComponent(2)).getText());
        exp = OffsetTime.of(8, 0, 1, 0, ZoneOffset.UTC).withOffsetSameInstant(DateTimeParser.LOCAL_OFFSET);
        assertEquals(exp, (OffsetTime) ((JSpinner) act.getComponent().getComponent(0)).getValue());
        exp = OffsetTime.of(8, 1, 1, 0, ZoneOffset.UTC).withOffsetSameInstant(DateTimeParser.LOCAL_OFFSET);
        assertEquals(exp, (OffsetTime) ((JSpinner) act.getComponent().getComponent(0)).getNextValue());

        con = new Constraints();
        con.setMaximalExclusive("20:00:00Z");
        timeValue = OffsetTime.of(22, 03, 45, 0, ZoneOffset.UTC);
        act = ConstraintBasicNodeFactory.createConstrainedTimeType(con, timeValue);
        exp = OffsetTime.of(20, 0, 0, 0, ZoneOffset.UTC).withOffsetSameInstant(DateTimeParser.LOCAL_OFFSET);
        assertEquals("< " + exp.toLocalTime().toString(), ((JLabel) act.getComponent().getComponent(2)).getText());
        exp = OffsetTime.of(19, 59, 59, 0, ZoneOffset.UTC).withOffsetSameInstant(DateTimeParser.LOCAL_OFFSET);
        assertEquals(exp, (OffsetTime) ((JSpinner) act.getComponent().getComponent(0)).getValue());
        exp = OffsetTime.of(19, 58, 59, 0, ZoneOffset.UTC).withOffsetSameInstant(DateTimeParser.LOCAL_OFFSET);
        assertEquals(exp, (OffsetTime) ((JSpinner) act.getComponent().getComponent(0)).getPreviousValue());

        con = new Constraints();
        con.setMaximalInclusive("22:30:00+02");
        timeValue = OffsetTime.of(20, 30, 0, 0, ZoneOffset.UTC);
        act = ConstraintBasicNodeFactory.createConstrainedTimeType(con, timeValue);
        exp = timeValue.withOffsetSameInstant(DateTimeParser.LOCAL_OFFSET);
        assertEquals("≤ " + exp.toLocalTime().toString(), ((JLabel) act.getComponent().getComponent(2)).getText());

        con = new Constraints();
        con.setMinimalInclusive("08:15:00-01");
        timeValue = OffsetTime.of(9, 15, 0, 0, ZoneOffset.UTC);
        act = ConstraintBasicNodeFactory.createConstrainedTimeType(con, timeValue);
        String exp1 = timeValue.withOffsetSameInstant(DateTimeParser.LOCAL_OFFSET).toLocalTime().toString();
        assertEquals("≥ " + exp1, ((JLabel) act.getComponent().getComponent(2)).getText());

        con.setMaximalInclusive("23:15:00+03");
        act = ConstraintBasicNodeFactory.createConstrainedTimeType(con, timeValue);
        String exp2 = OffsetTime.of(20, 15, 0, 0, ZoneOffset.UTC)
                .withOffsetSameInstant(DateTimeParser.LOCAL_OFFSET)
                .toLocalTime()
                .toString();
        assertEquals("≥ " + exp1 + " ∧ ≤ " + exp2, ((JLabel) act.getComponent().getComponent(2)).getText());
    }

    @Test
    public void createConstrainedTimestampType() {
        OffsetDateTime timestampValue = OffsetDateTime.of(2020, 12, 11, 10, 9, 8, 0, ZoneOffset.UTC);

        try {
            ConstraintBasicNodeFactory.createConstrainedTimestampType(null, timestampValue);
            fail("NullPointerException was expected but not thrown.");
        } catch (NullPointerException ex) {
        } catch (Exception ex) {
            fail("Only a NullPointerException was expected.");
        }

        Constraints con = new Constraints();
        ConstraintBasicNode act = ConstraintBasicNodeFactory.createConstrainedTimestampType(con, timestampValue);
        assertEquals(BasicType.TIMESTAMP, act.getType());
        assertEquals(timestampValue, act.getValue());
        assertNotNull(act.getConstaint());
        assertEquals(Box.class, act.getComponent().getClass());
        assertEquals(JSpinner.class, act.getComponent().getComponent(0).getClass());
        OffsetDateTime exp = timestampValue.withOffsetSameInstant(ZoneOffset.UTC);
        assertEquals(exp.toString(), ((JSpinner) act.getComponent().getComponent(0)).getValue().toString());
        assertEquals(JLabel.class, act.getComponent().getComponent(2).getClass());
        assertEquals("Invalid Constraint", ((JLabel) act.getComponent().getComponent(2)).getText());

        Constraints.Set conSet = new Constraints.Set();
        List<String> list = conSet.getValue();
        list.add("2020-12-11T10:09:00Z");
        list.add("2020-12-11T10:08:00Z");
        list.add("2020-12-11T10:07:32.123Z");
        list.add("2020-12-11T10:06:00Z");
        con.setSet(conSet);
        act = ConstraintBasicNodeFactory.createConstrainedTimestampType(con, timestampValue);
        exp = DateTimeParser.parseIsoDateTime("2020-12-11T10:09:00Z");
        assertEquals(exp, act.getValue());
        assertEquals(JComboBox.class, act.getComponent().getClass());
        assertEquals(4, ((JComboBox) act.getComponent()).getItemCount());
        assertEquals(0, ((JComboBox) act.getComponent()).getSelectedIndex());
        assertEquals(exp.toString(), ((JComboBox) act.getComponent()).getSelectedItem().toString());

        timestampValue = OffsetDateTime.of(2020, 12, 11, 10, 7, 32, 123456789, ZoneOffset.UTC);
        act = ConstraintBasicNodeFactory.createConstrainedTimestampType(con, timestampValue);
        exp = timestampValue.truncatedTo(ChronoUnit.MILLIS);
        assertEquals(exp, act.getValue());
        assertEquals(2, ((JComboBox) act.getComponent()).getSelectedIndex());
        assertEquals(exp.toString(), ((JComboBox) act.getComponent()).getSelectedItem().toString());

        conSet = new Constraints.Set();
        list = conSet.getValue();
        list.add("2020-12-11T10:08:00Z");
        list.add("-= invlaid =-");
        list.add("2020-12-11T10:06:00Z");
        con.setSet(conSet);
        timestampValue = DateTimeParser.parseIsoDateTime("2020-12-11T10:06:00Z");
        act = ConstraintBasicNodeFactory.createConstrainedTimestampType(con, timestampValue);
        assertEquals(timestampValue, act.getValue());
        assertEquals(JComboBox.class, act.getComponent().getClass());
        assertEquals(2, ((JComboBox) act.getComponent()).getItemCount());
        assertEquals(1, ((JComboBox) act.getComponent()).getSelectedIndex());

        con = new Constraints();
        con.setMinimalExclusive("2020-12-11T00:08:00Z");
        timestampValue = OffsetDateTime.of(2020, 12, 10, 05, 03, 45, 0, ZoneOffset.UTC);
        act = ConstraintBasicNodeFactory.createConstrainedTimestampType(con, timestampValue);
        exp = DateTimeParser.parseIsoDateTime("2020-12-11T00:08:00Z");
        assertEquals("> " + exp.toString(), ((JLabel) act.getComponent().getComponent(2)).getText());
        exp = DateTimeParser.parseIsoDateTime("2020-12-11T00:08:01Z");
        assertEquals(exp, (OffsetDateTime) ((JSpinner) act.getComponent().getComponent(0)).getValue());
        exp = DateTimeParser.parseIsoDateTime("2020-12-12T00:08:01Z");
        assertEquals(exp, (OffsetDateTime) ((JSpinner) act.getComponent().getComponent(0)).getNextValue());

        con = new Constraints();
        con.setMaximalExclusive("2020-12-22T20:00:00Z");
        timestampValue = OffsetDateTime.of(2022, 12, 22, 23, 03, 45, 0, ZoneOffset.UTC);
        act = ConstraintBasicNodeFactory.createConstrainedTimestampType(con, timestampValue);
        exp = OffsetDateTime.of(2020, 12, 22, 20, 0, 0, 0, ZoneOffset.UTC);
        assertEquals("< " + exp.toString(), ((JLabel) act.getComponent().getComponent(2)).getText());
        exp = OffsetDateTime.of(2020, 12, 22, 19, 59, 59, 0, ZoneOffset.UTC);
        assertEquals(exp, (OffsetDateTime) act.getValue());
        assertEquals(exp, (OffsetDateTime) ((JSpinner) act.getComponent().getComponent(0)).getValue());
        exp = OffsetDateTime.of(2020, 12, 21, 19, 59, 59, 0, ZoneOffset.UTC);
        assertEquals(exp, (OffsetDateTime) ((JSpinner) act.getComponent().getComponent(0)).getPreviousValue());

        con = new Constraints();
        con.setMaximalInclusive("2020-12-22T22:30:00+02");
        timestampValue = OffsetDateTime.of(2020, 12, 22, 20, 30, 0, 0, ZoneOffset.UTC);
        act = ConstraintBasicNodeFactory.createConstrainedTimestampType(con, timestampValue);
        assertEquals("≤ " + timestampValue.toString(), ((JLabel) act.getComponent().getComponent(2)).getText());

        con = new Constraints();
        con.setMinimalInclusive("2020-12-22T08:15:00-01");
        timestampValue = OffsetDateTime.of(2020, 12, 22, 9, 15, 0, 0, ZoneOffset.UTC);
        act = ConstraintBasicNodeFactory.createConstrainedTimestampType(con, timestampValue);
        String exp1 = timestampValue.toString();
        assertEquals("≥ " + exp1, ((JLabel) act.getComponent().getComponent(2)).getText());

        con.setMaximalInclusive("2020-12-22T23:15:00+03");
        act = ConstraintBasicNodeFactory.createConstrainedTimestampType(con, timestampValue);
        String exp2 = OffsetDateTime.of(2020, 12, 22, 20, 15, 0, 0, ZoneOffset.UTC).toString();
        assertEquals("≥ " + exp1 + " ∧ ≤ " + exp2, ((JLabel) act.getComponent().getComponent(2)).getText());
    }
}
