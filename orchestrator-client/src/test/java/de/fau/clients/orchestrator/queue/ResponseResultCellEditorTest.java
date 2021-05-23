package de.fau.clients.orchestrator.queue;

import static de.fau.clients.orchestrator.queue.ResponseResultCellEditor.isResultValueEmpty;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class ResponseResultCellEditorTest {

    @Test
    void isResultValueEmptyTest_checkNullObj() {
        assertTrue(isResultValueEmpty(null));
        final String nullStr = null;
        assertTrue(isResultValueEmpty(nullStr));
    }

    @Test
    void isResultValueEmptyTest_checkEmtyStr() {
        assertTrue(isResultValueEmpty(""));
        assertTrue(isResultValueEmpty(" "));
        assertTrue(isResultValueEmpty("   "));
        assertTrue(isResultValueEmpty("\t"));
        assertTrue(isResultValueEmpty("\t\t\t"));
        assertTrue(isResultValueEmpty("\n\n\n"));
    }

    @Test
    void isResultValueEmptyTest_checkNonEmtyStr() {
        assertFalse(isResultValueEmpty("a"));
        assertFalse(isResultValueEmpty("  a  "));
        assertFalse(isResultValueEmpty(":"));
        assertFalse(isResultValueEmpty("()"));
        assertFalse(isResultValueEmpty("[]"));
    }

    @Test
    void isResultValueEmptyTest_checkEmptyJsonStr() {
        assertTrue(isResultValueEmpty("{}"));
        assertTrue(isResultValueEmpty(" {} "));
        assertTrue(isResultValueEmpty(" { } "));
        assertTrue(isResultValueEmpty("{   }"));
        assertTrue(isResultValueEmpty("\t{}\t"));
        assertTrue(isResultValueEmpty("\t{\t\t}\t"));
        assertTrue(isResultValueEmpty("\n{\n}\n"));
        assertTrue(isResultValueEmpty("\n{\n\n}\n"));
    }

    @Test
    void isResultValueEmptyTest_checkNonEmptyJsonStr() {
        assertFalse(isResultValueEmpty("{a}"));
        assertFalse(isResultValueEmpty(" { a } "));
        assertFalse(isResultValueEmpty("{ a }"));
        assertFalse(isResultValueEmpty("\t{a}\t"));
        assertFalse(isResultValueEmpty("\t{\ta\t}\t"));
        assertFalse(isResultValueEmpty("\n{\na\n}\n"));
        assertFalse(isResultValueEmpty("\n{\na}\n"));
        assertFalse(isResultValueEmpty("b{}"));
        assertFalse(isResultValueEmpty("{}b"));
        assertFalse(isResultValueEmpty("b { } b"));
    }
}
