package de.fau.clients.orchestrator.feature_explorer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;

public class DateTimeUtilsTest {

    @Test
    public void parseIsoDate() {
        LocalDate exp = LocalDate.of(1999, 8, 7);
        assertEquals(exp, DateTimeUtils.parseIsoDate("1999-08-07"));
        assertEquals(exp, DateTimeUtils.parseIsoDate("19990807"));
        assertEquals(exp, DateTimeUtils.parseIsoDate("1999-08-07Z"));
        assertEquals(exp, DateTimeUtils.parseIsoDate("19990807Z"));
        assertEquals(exp, DateTimeUtils.parseIsoDate("1999-08-07+02"));
        assertEquals(exp, DateTimeUtils.parseIsoDate("1999-08-07-02"));
        assertEquals(exp, DateTimeUtils.parseIsoDate("1999-08-07+0215"));
        assertEquals(exp, DateTimeUtils.parseIsoDate("1999-08-07-0215"));
        assertEquals(exp, DateTimeUtils.parseIsoDate("1999-08-07+02:15"));
        assertEquals(exp, DateTimeUtils.parseIsoDate("1999-08-07-02:15"));
        assertEquals(exp, DateTimeUtils.parseIsoDate("1999-08-07+02:15:15"));
        assertEquals(exp, DateTimeUtils.parseIsoDate("1999-08-07-02:15:15"));
        assertEquals(exp, DateTimeUtils.parseIsoDate("19990807+02"));
        assertEquals(exp, DateTimeUtils.parseIsoDate("19990807+0215"));
        assertEquals(exp, DateTimeUtils.parseIsoDate("19990807+23"));
        assertEquals(exp, DateTimeUtils.parseIsoDate("19990807-23"));
        final String expStr = exp.toString();
        assertEquals(expStr, DateTimeUtils.parseIsoDate("1999-08-07").toString());
        assertEquals(expStr, DateTimeUtils.parseIsoDate("19990807").toString());
        assertEquals(expStr, DateTimeUtils.parseIsoDate("1999-08-07Z").toString());
        assertEquals(expStr, DateTimeUtils.parseIsoDate("19990807Z").toString());
        // nobody should use this software after this date anymore, but you never know...
        exp = LocalDate.of(90000, 8, 7);
        assertEquals(exp, DateTimeUtils.parseIsoDate("+90000-08-07"));
        // just in case somone is operating in the stone age
        exp = LocalDate.of(-90000, 8, 7);
        assertEquals(exp, DateTimeUtils.parseIsoDate("-90000-08-07"));

        // invalid dates
        try {
            DateTimeUtils.parseIsoDate(null);
            fail("NullPointerException was expected but not thrown.");
        } catch (NullPointerException ex) {
        } catch (Exception ex) {
            fail("Only a NullPointerException was expected.");
        }
        assertNull(DateTimeUtils.parseIsoDate(""));
        assertNull(DateTimeUtils.parseIsoDate("07.08.1999"));
        assertNull(DateTimeUtils.parseIsoDate("1999-8-07"));
        assertNull(DateTimeUtils.parseIsoDate("1999-08-7"));
        assertNull(DateTimeUtils.parseIsoDate("19990807+02:15"));
        assertNull(DateTimeUtils.parseIsoDate("19990807+02:15:15"));
    }

    @Test
    public void parseIsoTime() {
        OffsetTime exp = OffsetTime.of(10, 20, 30, 0, DateTimeUtils.LOCAL_OFFSET);
        assertEquals(exp, DateTimeUtils.parseIsoTime("10:20:30"));
        assertEquals(exp, DateTimeUtils.parseIsoTime("102030"));
        assertEquals(exp.toString(), DateTimeUtils.parseIsoTime("10:20:30").toString());
        assertEquals(exp.toString(), DateTimeUtils.parseIsoTime("102030").toString());
        exp = OffsetTime.of(10, 20, 30, 100000000, DateTimeUtils.LOCAL_OFFSET);
        assertEquals(exp, DateTimeUtils.parseIsoTime("10:20:30.1"));
        assertEquals(exp, DateTimeUtils.parseIsoTime("102030.1"));
        assertEquals(exp.toString(), DateTimeUtils.parseIsoTime("10:20:30.1").toString());
        assertEquals(exp.toString(), DateTimeUtils.parseIsoTime("102030.1").toString());
        exp = OffsetTime.of(10, 20, 30, 120000000, DateTimeUtils.LOCAL_OFFSET);
        assertEquals(exp, DateTimeUtils.parseIsoTime("10:20:30.12"));
        assertEquals(exp, DateTimeUtils.parseIsoTime("102030.12"));
        exp = OffsetTime.of(10, 20, 30, 123000000, DateTimeUtils.LOCAL_OFFSET);
        assertEquals(exp, DateTimeUtils.parseIsoTime("10:20:30.123"));
        assertEquals(exp, DateTimeUtils.parseIsoTime("102030.123"));

        exp = OffsetTime.of(10, 20, 30, 0, ZoneOffset.UTC);
        exp = exp.withOffsetSameInstant(DateTimeUtils.LOCAL_OFFSET);
        assertEquals(exp, DateTimeUtils.parseIsoTime("10:20:30Z"));
        assertEquals(exp, DateTimeUtils.parseIsoTime("12:20:30+02"));
        assertEquals(exp, DateTimeUtils.parseIsoTime("08:20:30-02"));
        assertEquals(exp, DateTimeUtils.parseIsoTime("02:20:30+16"));
        assertEquals(exp, DateTimeUtils.parseIsoTime("18:20:30-16"));
        assertEquals(exp, DateTimeUtils.parseIsoTime("12:35:30+02:15"));
        assertEquals(exp, DateTimeUtils.parseIsoTime("08:05:30-02:15"));
        assertEquals(exp, DateTimeUtils.parseIsoTime("12:35:45+02:15:15"));
        assertEquals(exp, DateTimeUtils.parseIsoTime("08:05:15-02:15:15"));
        assertEquals(exp, DateTimeUtils.parseIsoTime("102030Z"));
        assertEquals(exp, DateTimeUtils.parseIsoTime("122030+02"));
        assertEquals(exp, DateTimeUtils.parseIsoTime("082030-02"));
        assertEquals(exp, DateTimeUtils.parseIsoTime("123530+0215"));
        assertEquals(exp, DateTimeUtils.parseIsoTime("080530-0215"));

        exp = OffsetTime.of(10, 20, 30, 123000000, ZoneOffset.UTC);
        exp = exp.withOffsetSameInstant(DateTimeUtils.LOCAL_OFFSET);
        assertEquals(exp, DateTimeUtils.parseIsoTime("10:20:30.123Z"));
        assertEquals(exp, DateTimeUtils.parseIsoTime("12:20:30.123+02"));
        assertEquals(exp, DateTimeUtils.parseIsoTime("18:20:30.123-16"));
        assertEquals(exp, DateTimeUtils.parseIsoTime("12:35:30.123+02:15"));
        assertEquals(exp, DateTimeUtils.parseIsoTime("08:05:15.123-02:15:15"));
        assertEquals(exp, DateTimeUtils.parseIsoTime("102030.123Z"));
        assertEquals(exp, DateTimeUtils.parseIsoTime("122030.123+02"));
        assertEquals(exp, DateTimeUtils.parseIsoTime("080530.123-0215"));

        // invalid times
        try {
            DateTimeUtils.parseIsoTime(null);
            fail("NullPointerException was expected but not thrown.");
        } catch (NullPointerException ex) {
        } catch (Exception ex) {
            fail("Only a NullPointerException was expected.");
        }
        assertNull(DateTimeUtils.parseIsoTime(""));
        assertNull(DateTimeUtils.parseIsoTime("1:02:30"));
        assertNull(DateTimeUtils.parseIsoTime("10:2:30"));
        assertNull(DateTimeUtils.parseIsoTime("10:02:3"));
        assertNull(DateTimeUtils.parseIsoTime("10:02:30.1234"));
        assertNull(DateTimeUtils.parseIsoTime("100230.1234"));
    }

    @Test
    public void parseIsoDateTime() {
        OffsetDateTime exp = OffsetDateTime.of(1999, 8, 17, 10, 20, 30, 0, ZoneOffset.UTC);
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T10:20:30Z"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("19990817T102030Z"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T11:20:30+01"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("19990817T122030+02"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-18T00:20:30+14"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T11:20:30+01:00"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("19990817T122030+0200"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T12:35:30+02:15"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("19990817T123530+0215"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T12:35:45+02:15:15"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T09:20:30-01"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("19990817T082030-02"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-16T22:20:30-12"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T09:20:30-01:00"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T08:20:30-02:00"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("19990817T082030-0200"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T08:05:30-02:15"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T08:05:30-0215"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T08:05:15-02:15:15"));

        // check output string
        final String expStr = exp.toString();
        assertEquals(expStr, DateTimeUtils.parseIsoDateTime("1999-08-17T10:20:30Z").toString());
        assertEquals(expStr, DateTimeUtils.parseIsoDateTime("19990817T102030Z").toString());
        assertEquals(expStr, DateTimeUtils.parseIsoDateTime("1999-08-17T11:20:30+01").toString());
        assertEquals(expStr, DateTimeUtils.parseIsoDateTime("19990817T122030+02").toString());
        assertEquals(expStr, DateTimeUtils.parseIsoDateTime("1999-08-18T00:20:30+14").toString());
        assertEquals(expStr, DateTimeUtils.parseIsoDateTime("1999-08-17T11:20:30+01:00").toString());
        assertEquals(expStr, DateTimeUtils.parseIsoDateTime("19990817T122030+0200").toString());
        assertEquals(expStr, DateTimeUtils.parseIsoDateTime("1999-08-17T12:35:30+02:15").toString());
        assertEquals(expStr, DateTimeUtils.parseIsoDateTime("19990817T123530+0215").toString());
        assertEquals(expStr, DateTimeUtils.parseIsoDateTime("1999-08-17T12:35:45+02:15:15").toString());
        assertEquals(expStr, DateTimeUtils.parseIsoDateTime("1999-08-17T09:20:30-01").toString());
        assertEquals(expStr, DateTimeUtils.parseIsoDateTime("19990817T082030-02").toString());
        assertEquals(expStr, DateTimeUtils.parseIsoDateTime("1999-08-16T22:20:30-12").toString());
        assertEquals(expStr, DateTimeUtils.parseIsoDateTime("1999-08-17T09:20:30-01:00").toString());
        assertEquals(expStr, DateTimeUtils.parseIsoDateTime("1999-08-17T08:20:30-02:00").toString());
        assertEquals(expStr, DateTimeUtils.parseIsoDateTime("19990817T082030-0200").toString());
        assertEquals(expStr, DateTimeUtils.parseIsoDateTime("1999-08-17T08:05:30-02:15").toString());
        assertEquals(expStr, DateTimeUtils.parseIsoDateTime("1999-08-17T08:05:30-0215").toString());
        assertEquals(expStr, DateTimeUtils.parseIsoDateTime("1999-08-17T08:05:15-02:15:15").toString());

        // without seconds
        exp = OffsetDateTime.of(1999, 8, 17, 10, 20, 00, 0, ZoneOffset.UTC);
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T10:20Z"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T11:20+01"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-18T00:20+14"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T11:20+01:00"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T12:20+02:00"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T12:35+02:15"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T09:20-01"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T08:20-02"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-16T22:20-12"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T09:20-01:00"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T08:20-02:00"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T08:05-02:15"));

        // with nanos
        exp = OffsetDateTime.of(1999, 8, 17, 10, 20, 30, 100000000, ZoneOffset.UTC);
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T10:20:30.1Z"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T11:20:30.1+01"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T12:35:30.1+02:15"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T12:35:45.1+02:15:15"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T08:05:30.1-02:15"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T08:05:15.1-02:15:15"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("19990817T102030.1Z"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("19990817T112030.1+01"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("19990817T080530.1-0215"));
        assertEquals(exp.toString(), DateTimeUtils.parseIsoDateTime("19990817T080530.1-0215").toString());
        exp = OffsetDateTime.of(1999, 8, 17, 10, 20, 30, 120000000, ZoneOffset.UTC);
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T10:20:30.12Z"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T11:20:30.12+01"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T12:35:30.12+02:15"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T08:05:30.12-02:15"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("19990817T102030.12Z"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("19990817T112030.12+01"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("19990817T080530.12-0215"));
        exp = OffsetDateTime.of(1999, 8, 17, 10, 20, 30, 123000000, ZoneOffset.UTC);
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T10:20:30.123Z"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("19990817T102030.123Z"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T11:20:30.123+01"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T12:35:30.123+02:15"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T08:05:30.123-02:15"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("19990817T102030.123Z"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("19990817T112030.123+01"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("19990817T080530.123-0215"));
        exp = OffsetDateTime.of(1999, 8, 17, 10, 20, 30, 123400000, ZoneOffset.UTC);
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T10:20:30.1234Z"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T11:20:30.1234+01"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T12:35:30.1234+02:15"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T08:05:30.1234-02:15"));

        // with local time as input
        exp = OffsetDateTime.of(1999, 8, 17, 10, 20, 30, 0, DateTimeUtils.LOCAL_OFFSET);
        exp = exp.atZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime();
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T10:20:30"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("19990817T102030"));
        exp = OffsetDateTime.of(1999, 8, 17, 10, 20, 30, 100000000, DateTimeUtils.LOCAL_OFFSET);
        exp = exp.atZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime();
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T10:20:30.1"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("19990817T102030.1"));
        exp = OffsetDateTime.of(1999, 8, 17, 10, 20, 30, 120000000, DateTimeUtils.LOCAL_OFFSET);
        exp = exp.atZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime();
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T10:20:30.12"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("19990817T102030.12"));
        exp = OffsetDateTime.of(1999, 8, 17, 10, 20, 30, 123000000, DateTimeUtils.LOCAL_OFFSET);
        exp = exp.atZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime();
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("1999-08-17T10:20:30.123"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("19990817T102030.123"));

        // invalid timestamps
        try {
            DateTimeUtils.parseIsoDateTime(null);
            fail("NullPointerException was expected but not thrown.");
        } catch (NullPointerException ex) {
        } catch (Exception ex) {
            fail("Only a NullPointerException was expected.");
        }
        assertNull(DateTimeUtils.parseIsoDateTime(""));
        assertNull(DateTimeUtils.parseIsoDateTime("0-08-17T10:20:30Z")); // invalid years
        assertNull(DateTimeUtils.parseIsoDateTime("00-08-17T10:20:30Z"));
        assertNull(DateTimeUtils.parseIsoDateTime("000-08-17T10:20:30Z"));
        assertNull(DateTimeUtils.parseIsoDateTime("1999-08-17T10:20")); // local time no seconds
        assertNull(DateTimeUtils.parseIsoDateTime("1999-8-17T10:20:30Z")); // single digit in month
        assertNull(DateTimeUtils.parseIsoDateTime("1999-08-7T10:20:30Z")); // single digit in day
        assertNull(DateTimeUtils.parseIsoDateTime("1999-08-17T10:20:30+2")); // single digit in offset
        assertNull(DateTimeUtils.parseIsoDateTime("1999-13-17T10:20:30Z")); // invalid month
        assertNull(DateTimeUtils.parseIsoDateTime("1999-00-17T10:20:30Z")); // invalid month
        assertNull(DateTimeUtils.parseIsoDateTime("1999-08-32T10:20:30Z")); // invalid day
        assertNull(DateTimeUtils.parseIsoDateTime("1999-08-00T10:20:30Z")); // invalid day
        assertNull(DateTimeUtils.parseIsoDateTime("1999-08-17T24:20:30Z")); // invalid hour
        assertNull(DateTimeUtils.parseIsoDateTime("1999-08-17T10:60:30Z")); // invalid minutes
        assertNull(DateTimeUtils.parseIsoDateTime("1999-08-17T10:20:60Z")); // invalid seconds
        // SiLA supports only up to three decimal places after seconds.
        assertNull(DateTimeUtils.parseIsoDateTime("1999-08-17T10:20:30.1234"));
        assertNull(DateTimeUtils.parseIsoDateTime("1999-08-17T12:35:30+021"));
        assertNull(DateTimeUtils.parseIsoDateTime("19990817T123545+021515"));
        assertNull(DateTimeUtils.parseIsoDateTime("19990817T1020Z"));
        assertNull(DateTimeUtils.parseIsoDateTime("19990817T1220+02"));
        assertNull(DateTimeUtils.parseIsoDateTime("1999-08-17T12:35+0215"));
        assertNull(DateTimeUtils.parseIsoDateTime("1999-08-17T08:05-0215"));

        // special cases
        exp = OffsetDateTime.of(2019, 2, 28, 10, 20, 30, 0, ZoneOffset.UTC);
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("2019-02-30T10:20:30Z")); // not a leap year
        exp = OffsetDateTime.of(2020, 2, 29, 10, 20, 30, 0, ZoneOffset.UTC);
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("2020-02-30T10:20:30Z")); // leap year
        assertNotNull(DateTimeUtils.parseIsoDateTime("1999-08-17T00:20:30Z"));
        assertNotNull(DateTimeUtils.parseIsoDateTime("1999-08-17T10:00:30Z"));
        assertNotNull(DateTimeUtils.parseIsoDateTime("1999-08-17T10:20:00Z"));
        assertNotNull(DateTimeUtils.parseIsoDateTime("1999-08-17T10:20:30.123"));
        exp = OffsetDateTime.of(0, 8, 17, 10, 20, 30, 0, ZoneOffset.UTC);
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("0000-08-17T10:20:30Z"));
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("0000-08-17T10:20:30Z"));
        exp = OffsetDateTime.of(9999, 8, 17, 10, 20, 30, 0, ZoneOffset.UTC);
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("9999-08-17T10:20:30Z"));
        exp = OffsetDateTime.of(90000, 8, 17, 10, 20, 30, 0, ZoneOffset.UTC);
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("+90000-08-17T10:20:30Z"));
        exp = OffsetDateTime.of(-90000, 8, 17, 10, 20, 30, 0, ZoneOffset.UTC);
        assertEquals(exp, DateTimeUtils.parseIsoDateTime("-90000-08-17T10:20:30Z"));
    }
}
