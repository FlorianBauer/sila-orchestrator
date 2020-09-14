package de.fau.clients.orchestrator.utils;

import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

/**
 * Just checks for missing icons.
 */
public class IconProviderTest {

    @Test
    public void checkOnMissingIcons() {
        try {
            IconProvider.values();
        } catch (ExceptionInInitializerError ex) {
            fail("One or more icons are missing. Please check the file paths in the "
                    + "'IconProvider'-class.");
        } catch (Exception ex) {
            fail(ex);
        }
    }
}
