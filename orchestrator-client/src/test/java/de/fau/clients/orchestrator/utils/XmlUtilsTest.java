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
    public void parserXmlDataType() throws JsonProcessingException {
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

        xmlTypeStr = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<dataTypeType>"
                + "  <Constrained>"
                + "    <DataType>"
                + "      <Basic>Real</Basic>"
                + "    </DataType>"
                + "  </Constrained>"
                + "</dataTypeType>";
        act = XmlUtils.parseXmlDataType(xmlTypeStr);
        assertNull(act.getBasic());
        assertNotNull(act.getConstrained());
        assertNull(act.getList());
        assertNull(act.getStructure());
        assertEquals(BasicType.REAL, act.getConstrained().getDataType().getBasic());
    }
}
