package de.fau.clients.orchestrator.utils;

import java.time.OffsetTime;
import java.time.ZoneOffset;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

public class OffsetTimeSpinnerModelTest {

    @Test
    void otsmWithUtcInitNoBounds() {
        final OffsetTime initTime = OffsetTime.of(8, 0, 30, 0, ZoneOffset.UTC);
        final OffsetTimeSpinnerModel otsm = new OffsetTimeSpinnerModel(initTime, null, null, null);
        assertEquals(OffsetTime.of(8, 0, 30, 0, ZoneOffset.UTC), (OffsetTime) otsm.getValue());
        assertEquals(OffsetTime.of(8, 1, 30, 0, ZoneOffset.UTC), (OffsetTime) otsm.getNextValue());
        assertEquals(OffsetTime.of(7, 59, 30, 0, ZoneOffset.UTC), (OffsetTime) otsm.getPreviousValue());
    }

    @Test
    void otsmWithPosOffsetInitNoBounds() {
        final ZoneOffset zo = ZoneOffset.ofHours(8);
        final OffsetTime initTime = OffsetTime.of(22, 0, 30, 0, zo);
        final OffsetTimeSpinnerModel otsm = new OffsetTimeSpinnerModel(initTime, null, null, null);
        assertEquals(OffsetTime.of(22, 0, 30, 0, zo), (OffsetTime) otsm.getValue());
        assertEquals(OffsetTime.of(22, 1, 30, 0, zo), (OffsetTime) otsm.getNextValue());
        assertEquals(OffsetTime.of(21, 59, 30, 0, zo), (OffsetTime) otsm.getPreviousValue());
    }

    @Test
    void otsmWithPosOffsetPrevDayInitNoBounds() {
        final ZoneOffset zo = ZoneOffset.ofHours(8);
        final OffsetTime initTime = OffsetTime.of(6, 0, 30, 0, zo);
        final OffsetTimeSpinnerModel otsm = new OffsetTimeSpinnerModel(initTime, null, null, null);
        assertEquals(OffsetTime.of(6, 0, 30, 0, zo), (OffsetTime) otsm.getValue());
        assertEquals(OffsetTime.of(6, 1, 30, 0, zo), (OffsetTime) otsm.getNextValue());
        assertEquals(OffsetTime.of(5, 59, 30, 0, zo), (OffsetTime) otsm.getPreviousValue());
    }

    @Test
    void otsmWithNegOffsetInitNoBounds() {
        final ZoneOffset zo = ZoneOffset.ofHours(-8);
        final OffsetTime initTime = OffsetTime.of(6, 0, 30, 0, zo);
        final OffsetTimeSpinnerModel otsm = new OffsetTimeSpinnerModel(initTime, null, null, null);
        assertEquals(OffsetTime.of(6, 0, 30, 0, zo), (OffsetTime) otsm.getValue());
        assertEquals(OffsetTime.of(6, 1, 30, 0, zo), (OffsetTime) otsm.getNextValue());
        assertEquals(OffsetTime.of(5, 59, 30, 0, zo), (OffsetTime) otsm.getPreviousValue());
    }

    @Test
    void otsmWithNegOffsetNextDayInitNoBounds() {
        final ZoneOffset zo = ZoneOffset.ofHours(-8);
        final OffsetTime initTime = OffsetTime.of(22, 0, 30, 0, zo);
        final OffsetTimeSpinnerModel otsm = new OffsetTimeSpinnerModel(initTime, null, null, null);
        assertEquals(OffsetTime.of(22, 0, 30, 0, zo), (OffsetTime) otsm.getValue());
        assertEquals(OffsetTime.of(22, 1, 30, 0, zo), (OffsetTime) otsm.getNextValue());
        assertEquals(OffsetTime.of(21, 59, 30, 0, zo), (OffsetTime) otsm.getPreviousValue());
    }

    @Test
    void otsmWithUtcInitMinBounds() {
        final OffsetTime initTime = OffsetTime.of(8, 0, 30, 0, ZoneOffset.UTC);
        final OffsetTime startTime = OffsetTime.of(8, 0, 30, 0, ZoneOffset.UTC);
        final OffsetTimeSpinnerModel otsm = new OffsetTimeSpinnerModel(initTime, startTime, null, null);
        assertEquals(OffsetTime.of(8, 0, 30, 0, ZoneOffset.UTC), (OffsetTime) otsm.getValue());
        assertEquals(OffsetTime.of(8, 1, 30, 0, ZoneOffset.UTC), (OffsetTime) otsm.getNextValue());
        assertNull(otsm.getPreviousValue());

        otsm.setValue(OffsetTime.of(0, 0, 0, 0, ZoneOffset.UTC));
        assertNotNull(otsm.getNextValue());
        assertNull(otsm.getPreviousValue());

        otsm.setValue(OffsetTime.of(23, 59, 59, 999999999, ZoneOffset.UTC));
        assertNotNull(otsm.getNextValue());
        assertNotNull(otsm.getPreviousValue());
    }

    @Test
    void otsmWithUtcInitMaxBounds() {
        final OffsetTime initTime = OffsetTime.of(22, 0, 30, 0, ZoneOffset.UTC);
        final OffsetTime endTime = OffsetTime.of(22, 0, 30, 0, ZoneOffset.UTC);
        final OffsetTimeSpinnerModel otsm = new OffsetTimeSpinnerModel(initTime, null, endTime, null);
        assertEquals(OffsetTime.of(22, 0, 30, 0, ZoneOffset.UTC), (OffsetTime) otsm.getValue());
        assertNull(otsm.getNextValue());
        assertEquals(OffsetTime.of(21, 59, 30, 0, ZoneOffset.UTC), (OffsetTime) otsm.getPreviousValue());

        otsm.setValue(OffsetTime.of(0, 0, 0, 0, ZoneOffset.UTC));
        assertNotNull(otsm.getNextValue());
        assertNotNull(otsm.getPreviousValue());

        otsm.setValue(OffsetTime.of(23, 59, 59, 999999999, ZoneOffset.UTC));
        assertNotNull(otsm.getNextValue());
        assertNotNull(otsm.getPreviousValue());
    }

    @Test
    void otsmWithPosOffInitMinBounds() {
        final ZoneOffset zo = ZoneOffset.ofHours(8);
        final OffsetTime initTime = OffsetTime.of(6, 0, 30, 0, zo);
        final OffsetTime startTime = OffsetTime.of(6, 0, 30, 0, zo);
        final OffsetTimeSpinnerModel otsm = new OffsetTimeSpinnerModel(initTime, startTime, null, null);
        assertEquals(OffsetTime.of(6, 0, 30, 0, zo), (OffsetTime) otsm.getValue());
        assertEquals(OffsetTime.of(6, 1, 30, 0, zo), (OffsetTime) otsm.getNextValue());
        assertNull(otsm.getPreviousValue());

        otsm.setValue(OffsetTime.of(0, 0, 0, 0, zo));
        assertNotNull(otsm.getNextValue());
        assertNull(otsm.getPreviousValue());

        otsm.setValue(OffsetTime.of(23, 59, 59, 999999999, zo));
        assertNotNull(otsm.getNextValue());
        assertNotNull(otsm.getPreviousValue());
    }

    @Test
    void otsmWithPosOffInitMaxBounds() {
        final ZoneOffset zo = ZoneOffset.ofHours(8);
        final OffsetTime initTime = OffsetTime.of(22, 0, 30, 0, zo);
        final OffsetTime endTime = OffsetTime.of(22, 0, 30, 0, zo);
        final OffsetTimeSpinnerModel otsm = new OffsetTimeSpinnerModel(initTime, null, endTime, null);
        assertEquals(OffsetTime.of(22, 0, 30, 0, zo), (OffsetTime) otsm.getValue());
        assertNull(otsm.getNextValue());
        assertEquals(OffsetTime.of(21, 59, 30, 0, zo), (OffsetTime) otsm.getPreviousValue());

        otsm.setValue(OffsetTime.of(0, 0, 0, 0, zo));
        assertNotNull(otsm.getNextValue());
        assertNotNull(otsm.getPreviousValue());

        otsm.setValue(OffsetTime.of(23, 59, 59, 999999999, zo));
        assertNotNull(otsm.getNextValue());
        assertNotNull(otsm.getPreviousValue());
    }

    @Test
    void otsmWithNegOffInitMinBounds() {
        final ZoneOffset zo = ZoneOffset.ofHours(-8);
        final OffsetTime initTime = OffsetTime.of(6, 0, 30, 0, zo);
        final OffsetTime startTime = OffsetTime.of(6, 0, 30, 0, zo);
        final OffsetTimeSpinnerModel otsm = new OffsetTimeSpinnerModel(initTime, startTime, null, null);
        assertEquals(OffsetTime.of(6, 0, 30, 0, zo), (OffsetTime) otsm.getValue());
        assertEquals(OffsetTime.of(6, 1, 30, 0, zo), (OffsetTime) otsm.getNextValue());
        assertNull(otsm.getPreviousValue());

        otsm.setValue(OffsetTime.of(0, 0, 0, 0, zo));
        assertNotNull(otsm.getNextValue());
        assertNull(otsm.getPreviousValue());

        otsm.setValue(OffsetTime.of(23, 59, 59, 999999999, zo));
        assertNotNull(otsm.getNextValue());
        assertNotNull(otsm.getPreviousValue());
    }

    @Test
    void otsmWithNegOffInitMaxBounds() {
        final ZoneOffset zo = ZoneOffset.ofHours(-8);
        final OffsetTime initTime = OffsetTime.of(22, 0, 30, 0, zo);
        final OffsetTime endTime = OffsetTime.of(22, 0, 30, 0, zo);
        final OffsetTimeSpinnerModel otsm = new OffsetTimeSpinnerModel(initTime, null, endTime, null);
        assertEquals(OffsetTime.of(22, 0, 30, 0, zo), (OffsetTime) otsm.getValue());
        assertNull(otsm.getNextValue());
        assertEquals(OffsetTime.of(21, 59, 30, 0, zo), (OffsetTime) otsm.getPreviousValue());

        otsm.setValue(OffsetTime.of(0, 0, 0, 0, zo));
        assertNotNull(otsm.getNextValue());
        assertNotNull(otsm.getPreviousValue());

        otsm.setValue(OffsetTime.of(23, 59, 59, 999999999, zo));
        assertNotNull(otsm.getNextValue());
        assertNotNull(otsm.getPreviousValue());
    }

    @Test
    void otsmWithPosOffInitMinMaxBounds() {
        final ZoneOffset zo = ZoneOffset.ofHours(8);
        final OffsetTime initTime = OffsetTime.of(6, 0, 30, 0, zo);
        final OffsetTime startTime = OffsetTime.of(6, 0, 30, 0, zo);
        final OffsetTime endTime = OffsetTime.of(22, 0, 30, 0, zo);
        final OffsetTimeSpinnerModel otsm = new OffsetTimeSpinnerModel(initTime, startTime, endTime, null);
        assertEquals(OffsetTime.of(6, 0, 30, 0, zo), (OffsetTime) otsm.getValue());
        assertEquals(OffsetTime.of(6, 1, 30, 0, zo), (OffsetTime) otsm.getNextValue());
        assertNull(otsm.getPreviousValue());

        otsm.setValue(OffsetTime.of(22, 0, 30, 0, zo));
        assertEquals(OffsetTime.of(22, 0, 30, 0, zo), (OffsetTime) otsm.getValue());
        assertNull(otsm.getNextValue());
        assertEquals(OffsetTime.of(21, 59, 30, 0, zo), (OffsetTime) otsm.getPreviousValue());
    }

    @Test
    void otsmWithNegOffInitMinMaxBounds() {
        final ZoneOffset zo = ZoneOffset.ofHours(-8);
        final OffsetTime initTime = OffsetTime.of(6, 0, 30, 0, zo);
        final OffsetTime startTime = OffsetTime.of(6, 0, 30, 0, zo);
        final OffsetTime endTime = OffsetTime.of(22, 0, 30, 0, zo);
        final OffsetTimeSpinnerModel otsm = new OffsetTimeSpinnerModel(initTime, startTime, endTime, null);
        assertEquals(OffsetTime.of(6, 0, 30, 0, zo), (OffsetTime) otsm.getValue());
        assertEquals(OffsetTime.of(6, 1, 30, 0, zo), (OffsetTime) otsm.getNextValue());
        assertNull(otsm.getPreviousValue());

        otsm.setValue(OffsetTime.of(22, 0, 30, 0, zo));
        assertEquals(OffsetTime.of(22, 0, 30, 0, zo), (OffsetTime) otsm.getValue());
        assertNull(otsm.getNextValue());
        assertEquals(OffsetTime.of(21, 59, 30, 0, zo), (OffsetTime) otsm.getPreviousValue());
    }

    @Test
    void otsmWithUtcInitDiffZonesBounds() {
        final OffsetTime initTime = OffsetTime.of(6, 0, 30, 0, ZoneOffset.UTC);
        final OffsetTime startTime = OffsetTime.of(7, 0, 30, 0, ZoneOffset.ofHours(1));
        final OffsetTime endTime = OffsetTime.of(21, 0, 30, 0, ZoneOffset.ofHours(-1));
        final OffsetTimeSpinnerModel otsm = new OffsetTimeSpinnerModel(initTime, startTime, endTime, null);
        assertEquals(OffsetTime.of(6, 0, 30, 0, ZoneOffset.UTC), (OffsetTime) otsm.getValue());
        assertEquals(OffsetTime.of(6, 1, 30, 0, ZoneOffset.UTC), (OffsetTime) otsm.getNextValue());
        assertNull(otsm.getPreviousValue());

        otsm.setValue(OffsetTime.of(22, 0, 29, 0, ZoneOffset.UTC));
        assertEquals(OffsetTime.of(22, 0, 29, 0, ZoneOffset.UTC), (OffsetTime) otsm.getValue());
        assertNull(otsm.getNextValue());
        assertEquals(OffsetTime.of(21, 59, 29, 0, ZoneOffset.UTC), (OffsetTime) otsm.getPreviousValue());
    }

    @Test
    void otsmWithPosZoneInitDiffZonesBounds() {
        final OffsetTime initTime = OffsetTime.of(7, 0, 30, 0, ZoneOffset.ofHours(1));
        final OffsetTime startTime = OffsetTime.of(6, 0, 30, 0, ZoneOffset.UTC);
        final OffsetTime endTime = OffsetTime.of(21, 0, 30, 0, ZoneOffset.ofHours(-1));
        final OffsetTimeSpinnerModel otsm = new OffsetTimeSpinnerModel(initTime, startTime, endTime, null);
        assertEquals(OffsetTime.of(7, 0, 30, 0, ZoneOffset.ofHours(1)), (OffsetTime) otsm.getValue());
        assertEquals(OffsetTime.of(7, 1, 30, 0, ZoneOffset.ofHours(1)), (OffsetTime) otsm.getNextValue());
        assertNull(otsm.getPreviousValue());

        otsm.setValue(OffsetTime.of(22, 0, 29, 0, ZoneOffset.UTC));
        assertEquals(OffsetTime.of(23, 0, 29, 0, ZoneOffset.ofHours(1)), (OffsetTime) otsm.getValue());
        assertNull(otsm.getNextValue());
        assertEquals(OffsetTime.of(22, 59, 29, 0, ZoneOffset.ofHours(1)), (OffsetTime) otsm.getPreviousValue());
    }

    @Test
    void otsmWithNegZoneInitDiffZonesBounds() {
        final OffsetTime initTime = OffsetTime.of(5, 0, 30, 0, ZoneOffset.ofHours(-1));
        final OffsetTime startTime = OffsetTime.of(6, 0, 30, 0, ZoneOffset.UTC);
        final OffsetTime endTime = OffsetTime.of(23, 0, 30, 0, ZoneOffset.ofHours(1));
        final OffsetTimeSpinnerModel otsm = new OffsetTimeSpinnerModel(initTime, startTime, endTime, null);
        assertEquals(OffsetTime.of(5, 0, 30, 0, ZoneOffset.ofHours(-1)), (OffsetTime) otsm.getValue());
        assertEquals(OffsetTime.of(5, 1, 30, 0, ZoneOffset.ofHours(-1)), (OffsetTime) otsm.getNextValue());
        assertNull(otsm.getPreviousValue());

        otsm.setValue(OffsetTime.of(22, 0, 29, 0, ZoneOffset.UTC));
        assertEquals(OffsetTime.of(21, 0, 29, 0, ZoneOffset.ofHours(-1)), (OffsetTime) otsm.getValue());
        assertNull(otsm.getNextValue());
        assertEquals(OffsetTime.of(20, 59, 29, 0, ZoneOffset.ofHours(-1)), (OffsetTime) otsm.getPreviousValue());
    }
}
