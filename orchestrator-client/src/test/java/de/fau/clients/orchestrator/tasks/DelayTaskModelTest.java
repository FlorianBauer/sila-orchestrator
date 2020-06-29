package de.fau.clients.orchestrator.tasks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

public class DelayTaskModelTest {

    static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void getDelayInMillisec() {
        DelayTaskModel instance = new DelayTaskModel();
        assertEquals(1_000L, instance.getDelayInMillisec());
        instance = new DelayTaskModel(2_000);
        assertEquals(2_000L, instance.getDelayInMillisec());
        instance = new DelayTaskModel(0);
        assertEquals(0L, instance.getDelayInMillisec());

        try {
            instance = new DelayTaskModel(-1);
            fail("IllegalArgumentException was expected but not thrown.");
        } catch (IllegalArgumentException ex) {
        } catch (Exception ex) {
            fail("Only a IllegalArgumentException was expected.");
        }
    }

    @Test
    public void setDelayInMillisec() {
        DelayTaskModel instance = new DelayTaskModel();
        instance.setDelayInMillisec(20);
        assertEquals(20L, instance.getDelayInMillisec());

        try {
            instance.setDelayInMillisec(-1);
            fail("IllegalArgumentException was expected but not thrown.");
        } catch (IllegalArgumentException ex) {
        } catch (Exception ex) {
            fail("Only a IllegalArgumentException was expected.");
        }
    }

    @Test
    public void setDelayFromMinSecMilli() {
        int min = 0;
        int sec = 2;
        int milli = 0;
        DelayTaskModel instance = new DelayTaskModel();
        instance.setDelayFromMinSecMilli(min, sec, milli);
        assertEquals(2_000L, instance.getDelayInMillisec());
        min = 1;
        sec = 0;
        milli = 0;
        instance.setDelayFromMinSecMilli(min, sec, milli);
        assertEquals(60_000L, instance.getDelayInMillisec());
        min = 0;
        sec = 61;
        milli = 1000;
        instance.setDelayFromMinSecMilli(min, sec, milli);
        assertEquals(62_000L, instance.getDelayInMillisec());

        try {
            instance.setDelayFromMinSecMilli(-1, 0, 0);
            fail("IllegalArgumentException was expected but not thrown.");
        } catch (IllegalArgumentException ex) {
        } catch (Exception ex) {
            fail("Only a IllegalArgumentException was expected.");
        }
        try {
            instance.setDelayFromMinSecMilli(0, -1, 0);
            fail("IllegalArgumentException was expected but not thrown.");
        } catch (IllegalArgumentException ex) {
        } catch (Exception ex) {
            fail("Only a IllegalArgumentException was expected.");
        }
        try {
            instance.setDelayFromMinSecMilli(0, 0, -1);
            fail("IllegalArgumentException was expected but not thrown.");
        } catch (IllegalArgumentException ex) {
        } catch (Exception ex) {
            fail("Only a IllegalArgumentException was expected.");
        }
    }

    @Test
    public void getDelayAsMinSecMilli() {
        DelayTaskModel instance = new DelayTaskModel();
        int[] expResult = new int[]{0, 1, 0};
        assertArrayEquals(expResult, instance.getDelayAsMinSecMilli());
        instance.setDelayInMillisec(60_000L);
        expResult = new int[]{1, 0, 0};
        assertArrayEquals(expResult, instance.getDelayAsMinSecMilli());
        instance.setDelayInMillisec(1_000L);
        expResult = new int[]{0, 1, 0};
        assertArrayEquals(expResult, instance.getDelayAsMinSecMilli());
        instance.setDelayInMillisec(999L);
        expResult = new int[]{0, 0, 999};
        assertArrayEquals(expResult, instance.getDelayAsMinSecMilli());
        instance.setDelayInMillisec(61_001L);
        expResult = new int[]{1, 1, 1};
        assertArrayEquals(expResult, instance.getDelayAsMinSecMilli());
        instance.setDelayInMillisec(122_002L);
        expResult = new int[]{2, 2, 2};
        assertArrayEquals(expResult, instance.getDelayAsMinSecMilli());
        instance.setDelayInMillisec(62_000L);
        expResult = new int[]{1, 2, 0};
        assertArrayEquals(expResult, instance.getDelayAsMinSecMilli());
    }

    @Test
    public void serializeToJson() throws JsonProcessingException {
        DelayTaskModel instance = new DelayTaskModel();
        String actual = mapper.writeValueAsString(instance);
        assertEquals("{\"delay\":{\"delayInMillisec\":1000}}", actual);
        instance.setDelayInMillisec(4321);
        actual = mapper.writeValueAsString(instance);
        assertEquals("{\"delay\":{\"delayInMillisec\":4321}}", actual);
        instance.setDelayInMillisec(0);
        actual = mapper.writeValueAsString(instance);
        assertEquals("{\"delay\":{\"delayInMillisec\":0}}", actual);
    }

    @Test
    public void deserializeFromJson() throws JsonProcessingException {
        DelayTaskModel instance = mapper.readValue("{\"delay\":{\"delayInMillisec\":1000}}", DelayTaskModel.class);
        assertEquals(1_000L, instance.getDelayInMillisec());
        instance = mapper.readValue("{\"delay\":{\"delayInMillisec\":0}}", DelayTaskModel.class);
        assertEquals(0L, instance.getDelayInMillisec());
        instance = mapper.readValue("{\"delay\":{\"delayInMillisec\":42}}", DelayTaskModel.class);
        assertEquals(42L, instance.getDelayInMillisec());

        try {
            instance = mapper.readValue("{\"delay\":{\"delayInMillisec\":-1}}", DelayTaskModel.class);
            fail("JsonMappingException was expected but not thrown.");
        } catch (JsonMappingException ex) {
        } catch (Exception ex) {
            fail("Only a JsonMappingException was expected");
        }

        try {
            instance = mapper.readValue("{\"delay\":{\"delay\":42}}", DelayTaskModel.class);
            fail("UnrecognizedPropertyException was expected but not thrown.");
        } catch (UnrecognizedPropertyException ex) {
        } catch (Exception ex) {
            fail("Only a UnrecognizedPropertyException was expected");
        }
    }
}
