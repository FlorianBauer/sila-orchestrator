package de.fau.clients.orchestrator.tasks;

import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

/**
 * Just check for missing icons.
 */
public class ConnectionStatusTest {

    @Test
    public void checkOnMissingIcons() {
        try {
            ConnectionStatus.values();
        } catch (final ExceptionInInitializerError ex) {
            fail("One or more icons are missing. Please check the file paths in the "
                    + "'ConnectionStatus'-class.");
        } catch (final Exception ex) {
            fail(ex);
        }
    }
}
