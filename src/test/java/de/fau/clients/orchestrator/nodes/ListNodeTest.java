package de.fau.clients.orchestrator.nodes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigInteger;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import sila_java.library.core.models.BasicType;
import sila_java.library.core.models.Constraints;
import sila_java.library.core.models.DataTypeType;
import sila_java.library.core.models.ListType;

public class ListNodeTest {

    static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void create() {
        try {
            ListNode.create(null, null);
            fail("NullPointerException was expected but not thrown.");
        } catch (NullPointerException ex) {
        } catch (Exception ex) {
            fail("Only a NullPointer was expected.");
        }

        final DataTypeType dtt = new DataTypeType();
        dtt.setBasic(BasicType.INTEGER);
        final ListType listType = new ListType();
        listType.setDataType(dtt);

        ListNode act = ListNode.create(null, listType);
        assertTrue(act.isEditable());
        assertEquals(1, act.getListSize());
        assertEquals("[{\"value\":\"0\"}]", act.toJson().toString());

        assertEquals(JPanel.class, act.getComponent().getClass());
        // check "Add" and "Remove" buttons
        JComponent comp = (JComponent) act.getComponent().getComponent(1);
        assertEquals(Box.class, comp.getClass());
        assertEquals(JButton.class, comp.getComponent(0).getClass());
        assertTrue(comp.getComponent(0).isEnabled());
        assertEquals(JButton.class, comp.getComponent(1).getClass());
        assertTrue(comp.getComponent(1).isEnabled());
    }

    @Test
    public void createWithConstraint() throws JsonProcessingException {
        try {
            ListNode.createWithConstraint(null, null, null, null);
            fail("NullPointerException was expected but not thrown.");
        } catch (NullPointerException ex) {
        } catch (Exception ex) {
            fail("Only a NullPointer was expected.");
        }

        final DataTypeType dtt = new DataTypeType();
        dtt.setBasic(BasicType.INTEGER);
        final ListType listType = new ListType();
        listType.setDataType(dtt);

        try {
            ListNode.createWithConstraint(null, listType, null, null);
            fail("NullPointerException was expected but not thrown.");
        } catch (NullPointerException ex) {
        } catch (Exception ex) {
            fail("Only a NullPointer was expected.");
        }

        {
            ListNode act = ListNode.createWithConstraint(null, listType, new Constraints(), null);
            assertTrue(act.isEditable());
            assertEquals(0, act.getListSize());
        }

        {
            Constraints con = new Constraints();
            con.setElementCount(BigInteger.valueOf(3));
            ListNode act = ListNode.createWithConstraint(null, listType, con, null);
            assertFalse(act.isEditable());
            assertEquals(3, act.getListSize());
        }

        {
            Constraints con = new Constraints();
            con.setMinimalElementCount(BigInteger.valueOf(3));
            con.setMaximalElementCount(BigInteger.valueOf(5));
            ListNode act = ListNode.createWithConstraint(null, listType, con, null);
            assertTrue(act.isEditable());
            assertEquals(3, act.getListSize());
            // check "Add" and "Remove" buttons
            JComponent comp = (JComponent) act.getComponent().getComponent(3);
            assertEquals(Box.class, comp.getClass());
            // "Add" is enabled
            assertEquals(JButton.class, comp.getComponent(0).getClass());
            assertTrue(comp.getComponent(0).isEnabled());
            // "Remove" is disabled
            assertEquals(JButton.class, comp.getComponent(1).getClass());
            assertFalse(comp.getComponent(1).isEnabled());
        }

        {
            ListNode act = ListNode.createWithConstraint(null, listType, new Constraints(), null);
            assertTrue(act.isEditable());
            assertEquals(0, act.getListSize());
            // check "Add" and "Remove" buttons
            JComponent comp = (JComponent) act.getComponent().getComponent(0);
            assertEquals(Box.class, comp.getClass());
            // "Add" is enabled
            assertEquals(JButton.class, comp.getComponent(0).getClass());
            assertTrue(comp.getComponent(0).isEnabled());
            // "Remove" is disabled
            assertEquals(JButton.class, comp.getComponent(1).getClass());
            assertFalse(comp.getComponent(1).isEnabled());
        }

        {
            Constraints con = new Constraints();
            con.setMaximalElementCount(BigInteger.valueOf(2));
            String jsonStr = "[{\"value\":\"1\"},{\"value\":\"2\"}]";
            JsonNode jsonNode = mapper.readTree(jsonStr);
            ListNode act = ListNode.createWithConstraint(null, listType, con, jsonNode);
            assertTrue(act.isEditable());
            assertEquals(2, act.getListSize());
            // check "Add" and "Remove" buttons
            JComponent comp = (JComponent) act.getComponent().getComponent(2);
            assertEquals(Box.class, comp.getClass());
            // "Add" is disabled
            assertEquals(JButton.class, comp.getComponent(0).getClass());
            assertFalse(comp.getComponent(0).isEnabled());
            // "Remove" is enabled
            assertEquals(JButton.class, comp.getComponent(1).getClass());
            assertTrue(comp.getComponent(1).isEnabled());
        }

        {
            Constraints con = new Constraints();
            con.setMaximalElementCount(BigInteger.valueOf(3));
            String jsonStr = "[]";
            JsonNode jsonNode = mapper.readTree(jsonStr);
            ListNode act = ListNode.createWithConstraint(null, listType, con, jsonNode);
            assertTrue(act.isEditable());
            assertEquals(1, act.getListSize());
            // check "Add" and "Remove" buttons
            JComponent comp = (JComponent) act.getComponent().getComponent(1);
            assertEquals(Box.class, comp.getClass());
            // "Add" is enabled
            assertEquals(JButton.class, comp.getComponent(0).getClass());
            assertTrue(comp.getComponent(0).isEnabled());
            // "Remove" is enabled
            assertEquals(JButton.class, comp.getComponent(1).getClass());
            assertTrue(comp.getComponent(1).isEnabled());
        }
    }
}
