package de.fau.clients.orchestrator.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static de.fau.clients.orchestrator.cli.CommandlineArguments.isHostAndPortStrValid;

public class CommandlineArgumentsTest {

    @Test
    public void testDefaultConstructor() {
        final CommandlineArguments result = new CommandlineArguments();
        assertFalse(result.isAboutInfoToPrint);
        assertFalse(result.isHelpToPrint);
        assertFalse(result.isServerListToPrint);
        assertFalse(result.isVersionToPrint);
        assertFalse(result.isSiloFileToCheck());
        assertFalse(result.isSiloFileToExecute());
        assertTrue(result.serverToAdd.isEmpty());
        assertNull(result.siloFilePathForCheck);
        assertNull(result.siloFilePathForExec);
    }

    @Test
    public void createFromArgs_nullPtrEx() {
        assertThrows(NullPointerException.class, () -> CommandlineArguments.createFromArgs(null));
    }

    @Test
    public void createFromArgs_noFlags() {
        final String[] args = {};
        final CommandlineArguments result = CommandlineArguments.createFromArgs(args);
        assertFalse(result.isAboutInfoToPrint);
        assertFalse(result.isHelpToPrint);
        assertFalse(result.isServerListToPrint);
        assertFalse(result.isVersionToPrint);
        assertFalse(result.isSiloFileToCheck());
        assertFalse(result.isSiloFileToExecute());
        assertTrue(result.serverToAdd.isEmpty());
        assertNull(result.siloFilePathForCheck);
        assertNull(result.siloFilePathForExec);
    }

    @Test
    public void createFromArgs_checkAllLongFlags() {
        final String[] args = {
            "--help",
            "--version",
            "--about",
            "--info",
            "--add-server", "127.0.0.1:50052",
            "--add-server", "127.0.0.1:50053",
            "--list-server",
            "--check-tasks", "fileA.silo",
            "--execute", "fileB.silo"
        };
        final CommandlineArguments result = CommandlineArguments.createFromArgs(args);
        assertTrue(result.isHelpToPrint);
        assertTrue(result.isVersionToPrint);
        assertTrue(result.isAboutInfoToPrint);
        assertEquals("127.0.0.1:50052", result.serverToAdd.get(0));
        assertEquals("127.0.0.1:50053", result.serverToAdd.get(1));
        assertTrue(result.isServerListToPrint);
        assertTrue(result.isSiloFileToCheck());
        assertEquals("fileA.silo", result.siloFilePathForCheck);
        assertTrue(result.isSiloFileToExecute());
        assertEquals("fileB.silo", result.siloFilePathForExec);
    }

    @Test
    public void createFromArgs_checkAllShortFlags() {
        final String[] args = {
            "-h",
            "-v",
            "-a", "127.0.0.1:50052",
            "-a", "127.0.0.1:50053",
            "-l",
            "-c", "fileA.silo",
            "-x", "fileB.silo"
        };
        final CommandlineArguments result = CommandlineArguments.createFromArgs(args);
        assertTrue(result.isHelpToPrint);
        assertTrue(result.isVersionToPrint);
        assertEquals("127.0.0.1:50052", result.serverToAdd.get(0));
        assertEquals("127.0.0.1:50053", result.serverToAdd.get(1));
        assertTrue(result.isServerListToPrint);
        assertTrue(result.isSiloFileToCheck());
        assertEquals("fileA.silo", result.siloFilePathForCheck);
        assertTrue(result.isSiloFileToExecute());
        assertEquals("fileB.silo", result.siloFilePathForExec);
    }

    @Test
    public void createFromArgs_checkChainedShortFlags() {
        CommandlineArguments result = CommandlineArguments.createFromArgs(new String[]{"-hvl"});
        assertTrue(result.isHelpToPrint);
        assertTrue(result.isVersionToPrint);
        assertTrue(result.isServerListToPrint);

        result = CommandlineArguments.createFromArgs(new String[]{"-LHV"});
        assertTrue(result.isHelpToPrint);
        assertTrue(result.isVersionToPrint);
        assertTrue(result.isServerListToPrint);
    }

    @Test
    public void createFromArgs_unknownArg() {
        Throwable exception = assertThrows(
                IllegalArgumentException.class,
                () -> CommandlineArguments.createFromArgs(new String[]{"--unknown"})
        );
        assertTrue(exception.getMessage().startsWith(CommandlineArguments.ERROR_UNKONWN_ARG_MSG));

        exception = assertThrows(
                IllegalArgumentException.class,
                () -> CommandlineArguments.createFromArgs(new String[]{"-ü"})
        );
        assertTrue(exception.getMessage().startsWith(CommandlineArguments.ERROR_UNKONWN_ARG_MSG));

        exception = assertThrows(
                IllegalArgumentException.class,
                () -> CommandlineArguments.createFromArgs(new String[]{"-hl v"})
        );
        assertTrue(exception.getMessage().startsWith(CommandlineArguments.ERROR_UNKONWN_ARG_MSG));
    }

    @Test
    public void createFromArgs_addServerMissingOption() {
        Throwable exception = assertThrows(
                IllegalArgumentException.class,
                () -> CommandlineArguments.createFromArgs(new String[]{"--add-server"})
        );
        assertEquals(CommandlineArguments.ERROR_HOST_PORT_ARG_MSG, exception.getMessage());

        exception = assertThrows(
                IllegalArgumentException.class,
                () -> CommandlineArguments.createFromArgs(new String[]{"-a"})
        );
        assertEquals(CommandlineArguments.ERROR_HOST_PORT_ARG_MSG, exception.getMessage());
    }

    @Test
    public void createFromArgs_addServerInvalidOption() {
        Throwable exception = assertThrows(
                IllegalArgumentException.class,
                () -> CommandlineArguments.createFromArgs(new String[]{"--add-server", "localHorst"})
        );
        assertEquals(CommandlineArguments.ERROR_HOST_PORT_INVALID_MSG, exception.getMessage());

        exception = assertThrows(
                IllegalArgumentException.class,
                () -> CommandlineArguments.createFromArgs(new String[]{"-a", "127.0.0.1:123456"})
        );
        assertEquals(CommandlineArguments.ERROR_HOST_PORT_INVALID_MSG, exception.getMessage());
    }

    @Test
    public void createFromArgs_checkTasksMissingOption() {
        Throwable exception = assertThrows(
                IllegalArgumentException.class,
                () -> CommandlineArguments.createFromArgs(new String[]{"--check-tasks"})
        );
        assertEquals(CommandlineArguments.ERROR_SILO_FILE_ARG_MSG, exception.getMessage());

        exception = assertThrows(
                IllegalArgumentException.class,
                () -> CommandlineArguments.createFromArgs(new String[]{"-c"})
        );
        assertEquals(CommandlineArguments.ERROR_SILO_FILE_ARG_MSG, exception.getMessage());
    }

    @Test
    public void createFromArgs_executeMissingOption() {
        Throwable exception = assertThrows(
                IllegalArgumentException.class,
                () -> CommandlineArguments.createFromArgs(new String[]{"--execute"})
        );
        assertEquals(CommandlineArguments.ERROR_SILO_FILE_ARG_MSG, exception.getMessage());

        exception = assertThrows(
                IllegalArgumentException.class,
                () -> CommandlineArguments.createFromArgs(new String[]{"-x"})
        );
        assertEquals(CommandlineArguments.ERROR_SILO_FILE_ARG_MSG, exception.getMessage());
    }

    @Test
    public void isHostAndPortStrValid_valid() {
        assertTrue(isHostAndPortStrValid("localhost:2048"));
        assertTrue(isHostAndPortStrValid("localhost:50052"));
        assertTrue(isHostAndPortStrValid("192.168.0.1:50052"));
        assertTrue(isHostAndPortStrValid("172.0.0.1:50053"));
        // IPv6
        assertTrue(isHostAndPortStrValid("[ff:ff:ff:ff:ff:ff]:8080"));
        assertTrue(isHostAndPortStrValid("[ff:ff:ff:ff:ff:ff]:80054"));
        assertTrue(isHostAndPortStrValid("[86:21:31:16:55:40]:8080"));
        assertTrue(isHostAndPortStrValid("[86:21:31:16:55:40]:80055"));
        assertTrue(isHostAndPortStrValid("[2001:db8::8a2e:370:7334:1234]:1234"));
        assertTrue(isHostAndPortStrValid("[2001:db8::8a2e:370:7334:1234]:12345"));
    }

    @Test
    public void isHostAndPortStrValid_invalid() {
        assertFalse(isHostAndPortStrValid("localhost:123"));
        assertFalse(isHostAndPortStrValid("localhost:123456"));
        assertFalse(isHostAndPortStrValid(" localhost:50053 "));
        assertFalse(isHostAndPortStrValid("localhost"));
        assertFalse(isHostAndPortStrValid("localhost : 50052"));
        assertFalse(isHostAndPortStrValid(":50052"));
    }

}
