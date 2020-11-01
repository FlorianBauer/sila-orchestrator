package de.fau.clients.orchestrator.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import sila_java.library.core.models.BasicType;
import sila_java.library.core.models.DataTypeType;

public class XmlUtilsTest {

    @Test
    public void parserXmlDataTypeBasic() throws JsonProcessingException {
        String xmlTypeStr = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<DataType>\n"
                + "  <Basic>Integer</Basic>\n"
                + "</DataType>\n";
        DataTypeType act = XmlUtils.parseXmlDataType(xmlTypeStr);
        assertEquals(BasicType.INTEGER, act.getBasic());
        assertNull(act.getConstrained());
        assertNull(act.getList());
        assertNull(act.getStructure());

        xmlTypeStr = "<dataTypeType>"
                + "  <Basic>String</Basic>"
                + "</dataTypeType>";
        act = XmlUtils.parseXmlDataType(xmlTypeStr);
        assertEquals(BasicType.STRING, act.getBasic());
        assertNull(act.getConstrained());
        assertNull(act.getList());
        assertNull(act.getStructure());

        xmlTypeStr = "<RootNodeWrapperTagGetsIgnoredBlaBla>"
                + "  <Basic>Real</Basic>"
                + "</RootNodeWrapperTagGetsIgnoredBlaBla>";
        act = XmlUtils.parseXmlDataType(xmlTypeStr);
        assertEquals(BasicType.REAL, act.getBasic());
        assertNull(act.getConstrained());
        assertNull(act.getList());
        assertNull(act.getStructure());

        xmlTypeStr = "<DataType>"
                + "  <Basic>Date</Basic>"
                + "</DataType>";
        act = XmlUtils.parseXmlDataType(xmlTypeStr);
        assertEquals(BasicType.DATE, act.getBasic());
        assertNull(act.getConstrained());
        assertNull(act.getList());
        assertNull(act.getStructure());

    }

    @Test
    public void parserXmlDataTypeConstrained() throws JsonProcessingException {
        String xmlTypeStr = "<dataTypeType>"
                + "  <Constrained>"
                + "    <DataType>"
                + "      <Basic>Real</Basic>"
                + "    </DataType>"
                + "    <Constraints>"
                + "      <MaximalExclusive>3.141592</MaximalExclusive>"
                + "    </Constraints>"
                + "  </Constrained>"
                + "</dataTypeType>";
        DataTypeType act = XmlUtils.parseXmlDataType(xmlTypeStr);
        assertNull(act.getBasic());
        assertNotNull(act.getConstrained());
        assertNull(act.getList());
        assertNull(act.getStructure());
        assertEquals(BasicType.REAL, act.getConstrained().getDataType().getBasic());
        assertEquals("3.141592", act.getConstrained().getConstraints().getMaximalExclusive());

        xmlTypeStr = "<dataTypeType>"
                + "  <Constrained>"
                + "    <DataType>"
                + "      <Basic>String</Basic>"
                + "    </DataType>"
                + "    <Constraints>"
                + "     <Set>"
                + "       <Value>A</Value>"
                + "       <Value>B</Value>"
                + "       <Value>C</Value>"
                + "      </Set>"
                + "    </Constraints>"
                + "  </Constrained>"
                + "</dataTypeType>";
        act = XmlUtils.parseXmlDataType(xmlTypeStr);
        assertNull(act.getBasic());
        assertNotNull(act.getConstrained());
        assertNull(act.getList());
        assertNull(act.getStructure());
        assertEquals(BasicType.STRING, act.getConstrained().getDataType().getBasic());
        assertNotNull(act.getConstrained().getConstraints().getSet());
        assertEquals("A", act.getConstrained().getConstraints().getSet().getValue().get(0));
        assertEquals("B", act.getConstrained().getConstraints().getSet().getValue().get(1));
        assertEquals("C", act.getConstrained().getConstraints().getSet().getValue().get(2));
    }
}
