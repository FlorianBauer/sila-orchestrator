package de.fau.clients.orchestrator.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import sila_java.library.core.models.BasicType;
import sila_java.library.core.models.Constraints;
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

    @Test
    void parseXmlConstraints() throws JsonProcessingException {
        String xmlConStr = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<Constraints>\n"
                + "    <Set>\n"
                + "        <Value>pos1</Value>\n"
                + "        <Value>pos2</Value>\n"
                + "    </Set>\n"
                + "</Constraints>";
        Constraints act = XmlUtils.parseXmlConstraints(xmlConStr);
        assertNotNull(act.getSet());
        assertEquals(2, act.getSet().getValue().size());
        assertEquals("pos1", act.getSet().getValue().get(0));
        assertEquals("pos2", act.getSet().getValue().get(1));

        xmlConStr = "<Constraints>"
                + "  <MinimalExclusive>0</MinimalExclusive>"
                + "  <MaximalExclusive>10</MaximalExclusive>"
                + "</Constraints>";
        act = XmlUtils.parseXmlConstraints(xmlConStr);
        assertEquals("0", act.getMinimalExclusive());
        assertEquals("10", act.getMaximalExclusive());

        xmlConStr = "<Constraints>\n"
                + " <Unit>\n"
                + "     <Label>mL</Label>\n"
                + "     <Factor>0.000001</Factor>\n"
                + "     <Offset>0</Offset>\n"
                + "     <UnitComponent>\n"
                + "         <SIUnit>Meter</SIUnit>\n"
                + "         <Exponent>3</Exponent>\n"
                + "     </UnitComponent>\n"
                + " </Unit>\n"
                + "</Constraints>";
        act = XmlUtils.parseXmlConstraints(xmlConStr);
        assertNotNull(act.getUnit());
        assertEquals("mL", act.getUnit().getLabel());
        assertEquals(0.000001, act.getUnit().getFactor().doubleValue());
        assertEquals(0.0, act.getUnit().getOffset().doubleValue());
        assertEquals(1, act.getUnit().getUnitComponent().size());
        assertEquals("Meter", act.getUnit().getUnitComponent().get(0).getSIUnit());
        assertEquals(3, act.getUnit().getUnitComponent().get(0).getExponent().intValue());
    }
}
