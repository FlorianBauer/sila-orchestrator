package de.fau.clients.orchestrator.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

public class VersionNumberTest {

    @Test
    public void parseVersionString() {
        VersionNumber exp = new VersionNumber(1, 2, 3);
        VersionNumber res = VersionNumber.parseVersionString("1.2.3");
        assertEquals(exp, res);
        res = VersionNumber.parseVersionString("v1.2.3");
        assertEquals(exp, res);
        res = VersionNumber.parseVersionString("1.2.3.4");
        assertEquals(exp, res);
        res = VersionNumber.parseVersionString("1.2.3-SNAPSHOT");
        assertEquals(exp, res);
        res = VersionNumber.parseVersionString("1.2.3-0x4f54e");
        assertEquals(exp, res);
        res = VersionNumber.parseVersionString("version 1.2.3");
        assertEquals(exp, res);
        exp = new VersionNumber(1, 2, 0);
        res = VersionNumber.parseVersionString("1.2-454e");
        assertEquals(exp, res);
        res = VersionNumber.parseVersionString("1.2-SNAPSHOT-45");
        assertEquals(exp, res);
        res = VersionNumber.parseVersionString("v1.2-SNAPSHOT-45");
        assertEquals(exp, res);
        exp = new VersionNumber(11, 22, 33);
        res = VersionNumber.parseVersionString("11.22.33");
        assertEquals(exp, res);
        res = VersionNumber.parseVersionString("v11.22.33");
        assertEquals(exp, res);
        res = VersionNumber.parseVersionString("11.22.33-48f5d47");
        assertEquals(exp, res);
        exp = new VersionNumber(1, 0, 0);
        res = VersionNumber.parseVersionString("1");
        assertEquals(exp, res);
        res = VersionNumber.parseVersionString("v1");
        assertEquals(exp, res);
        res = VersionNumber.parseVersionString("v1-4587f");
        assertEquals(exp, res);
        exp = new VersionNumber(1, 2, 0);
        res = VersionNumber.parseVersionString("1.2");
        assertEquals(exp, res);
        res = VersionNumber.parseVersionString("v1.2");
        assertEquals(exp, res);
        res = VersionNumber.parseVersionString("v1.2-4587f");
        assertEquals(exp, res);

        exp = new VersionNumber(0, 0, 0);
        res = VersionNumber.parseVersionString("foo bar");
        assertEquals(exp, res);
        res = VersionNumber.parseVersionString("");
        assertEquals(exp, res);
        res = VersionNumber.parseVersionString("................");
        assertEquals(exp, res);
        res = VersionNumber.parseVersionString("null");
        assertEquals(exp, res);

        try {
            VersionNumber.parseVersionString(null);
            fail("NullPointerException was expected but not thrown.");
        } catch (NullPointerException ex) {
        } catch (Exception ex) {
            fail("Only a NullPointerException was expected.");
        }
    }
}
