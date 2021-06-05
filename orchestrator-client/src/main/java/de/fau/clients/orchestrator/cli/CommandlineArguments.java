package de.fau.clients.orchestrator.cli;

import java.util.ArrayList;

/**
 * Class for parsing, storing and handling the Command-line Interface (CLI)-arguments. The
 * corresponding actions associated with de flags are handled in the
 * <code>CommandlineControls</code>-class.
 *
 * @see CommandlineControls
 */
public class CommandlineArguments {

    protected static final String SHORT_FLAG_PREFIX = "-";
    protected static final String LONG_FLAG_PREFIX = "--";

    protected static final char HELP_SHORT_FLAG = 'h';
    protected static final String HELP_LONG_FLAG = "--help";
    protected static final char VERSION_SHORT_FLAG = 'v';
    protected static final String VERSION_LONG_FLAG = "--version";
    protected static final String ABOUT_LONG_FLAG = "--about";
    protected static final String INFO_LONG_FLAG = "--info";
    protected static final char ADD_SERVER_SHORT_FLAG = 'a';
    protected static final String ADD_SERVER_LONG_FLAG = "--add-server";
    protected static final char LIST_SERVER_SHORT_FLAG = 'l';
    protected static final String LIST_SERVER_LONG_FLAG = "--list-server";
    protected static final char CHECK_TASKS_SHORT_FLAG = 'c';
    protected static final String CHECK_TASKS_LONG_FLAG = "--check-tasks";
    protected static final char EXECUTE_SHORT_FLAG = 'x';
    protected static final String EXECUTE_LONG_FLAG = "--execute";

    protected static final String USAGE_INFO_MSG = "Usage: java -jar sila-orchestrator.jar [args]"
            + "\n -" + HELP_SHORT_FLAG + ", " + HELP_LONG_FLAG
            + "\n\t Print this help message."
            + "\n -" + VERSION_SHORT_FLAG + ", " + VERSION_LONG_FLAG
            + "\n\t Print the version number."
            + "\n " + ABOUT_LONG_FLAG + ", " + INFO_LONG_FLAG
            + "\n\t Print some general information about this software."
            + "\n -" + ADD_SERVER_SHORT_FLAG + " <[host]:[port]>, " + ADD_SERVER_LONG_FLAG + " <[host]:[port]>"
            + "\n\t Add a SiLA server by connecting via the given address."
            + "\n\t Examples: 'localhost:8080', '127.0.0.1:50052', '[2001:db8::8a2e:370:7334:1234]:50053'."
            + "\n -" + LIST_SERVER_SHORT_FLAG + ", " + LIST_SERVER_LONG_FLAG
            + "\n\t Lists all available servers which can be discoverd by an network scan."
            + "\n -" + CHECK_TASKS_SHORT_FLAG + " <silo-file>, " + CHECK_TASKS_LONG_FLAG + " <silo-file>"
            + "\n\t Checks all tasks in the given *.silo-file if they are ready for execution."
            + "\n -" + EXECUTE_SHORT_FLAG + " <silo-file>, " + EXECUTE_LONG_FLAG + " <silo-file>"
            + "\n\t Loads and executes the provided *.silo-file.";

    protected static final String ERROR_HOST_PORT_ARG_MSG = "Host address and port option is missing.";
    protected static final String ERROR_HOST_PORT_INVALID_MSG = "Host address and port string is invalid. "
            + "Option must be in the form [host]:[port] e.g. '127.0.0.1:50052'.";
    protected static final String ERROR_SILO_FILE_ARG_MSG = "Path to *.silo-file is missing.";
    protected static final String ERROR_UNKONWN_ARG_MSG = "Unknown argument";

    public boolean isHelpToPrint = false;
    public boolean isVersionToPrint = false;
    public boolean isAboutInfoToPrint = false;
    public boolean isServerListToPrint = false;
    public ArrayList<String> serverToAdd = new ArrayList<>();
    public String siloFilePathForExec = null;
    public String siloFilePathForCheck = null;

    public CommandlineArguments() {
    }

    /**
     * Parses the given argument list and creates an initialize `CommandlineArguments`-object.
     *
     * @param args The argument list to parse.
     * @return A initialized `CommandlineArguments`-object.
     * @throws IllegalArgumentException on error.
     */
    public static CommandlineArguments createFromArgs(final String[] args)
            throws IllegalArgumentException {
        CommandlineArguments cmdArgs = new CommandlineArguments();
        for (int i = 0; i < args.length;) {
            final String arg = args[i];
            if (arg.startsWith(LONG_FLAG_PREFIX)) {
                i = cmdArgs.parseLongArg(i, args);
            } else if (arg.startsWith(SHORT_FLAG_PREFIX)) {
                i = cmdArgs.parseShortArg(i, args);
            } else {
                throw new IllegalArgumentException(ERROR_UNKONWN_ARG_MSG + " '" + arg + "'.");
            }
        }
        return cmdArgs;
    }

    public boolean isSiloFileToCheck() {
        return (siloFilePathForCheck != null);
    }

    public boolean isSiloFileToExecute() {
        return (siloFilePathForExec != null);
    }

    /**
     * Parses and evaluates one single long flag argument.
     *
     * @param pos The position of the argument int the argument list to evaluate.
     * @param args The argument list.
     * @return The position of the next argument.
     * @throws IllegalArgumentException on error.
     */
    private int parseLongArg(int pos, final String[] args) throws IllegalArgumentException {
        final String arg = args[pos];
        if (arg.equalsIgnoreCase(HELP_LONG_FLAG)) {
            isHelpToPrint = true;
        } else if (arg.equalsIgnoreCase(VERSION_LONG_FLAG)) {
            isVersionToPrint = true;
        } else if (arg.equalsIgnoreCase(ABOUT_LONG_FLAG) || arg.equalsIgnoreCase(INFO_LONG_FLAG)) {
            isAboutInfoToPrint = true;
        } else if (arg.equalsIgnoreCase(ADD_SERVER_LONG_FLAG)) {
            if (pos + 1 < args.length) {
                final String hostPortOption = args[pos + 1];
                if (!isHostAndPortStrValid(hostPortOption)) {
                    throw new IllegalArgumentException(ERROR_HOST_PORT_INVALID_MSG);
                }
                serverToAdd.add(hostPortOption);
                return pos + 2;
            } else {
                throw new IllegalArgumentException(ERROR_HOST_PORT_ARG_MSG);
            }
        } else if (arg.equalsIgnoreCase(LIST_SERVER_LONG_FLAG)) {
            isServerListToPrint = true;
        } else if (arg.equalsIgnoreCase(CHECK_TASKS_LONG_FLAG)) {
            if (pos + 1 < args.length) {
                final String siloFile = args[pos + 1];
                siloFilePathForCheck = siloFile;
                return pos + 2;
            } else {
                throw new IllegalArgumentException(ERROR_SILO_FILE_ARG_MSG);
            }
        } else if (arg.equalsIgnoreCase(EXECUTE_LONG_FLAG)) {
            if (pos + 1 < args.length) {
                final String siloFile = args[pos + 1];
                siloFilePathForExec = siloFile;
                return pos + 2;
            } else {
                throw new IllegalArgumentException(ERROR_SILO_FILE_ARG_MSG);
            }
        } else {
            throw new IllegalArgumentException(ERROR_UNKONWN_ARG_MSG + " '" + arg + "'.");
        }
        return pos + 1;
    }

    /**
     * Parses and evaluates one single short flag argument. On an argument which consist of chained
     * together short flags (e.g. `ls -rtl` instead of `ls -r -t -l`), all flags inside the argument
     * getting evaluated.
     *
     * @param pos The position of the argument int the argument list to evaluate.
     * @param args The argument list.
     * @return The position of the next argument.
     * @throws IllegalArgumentException on error.
     */
    private int parseShortArg(int pos, final String[] args) {
        char arg = Character.toLowerCase(args[pos].charAt(1));
        switch (arg) {
            case ADD_SERVER_SHORT_FLAG:
                if (pos + 1 < args.length) {
                    final String hostPortOption = args[pos + 1];
                    if (!isHostAndPortStrValid(hostPortOption)) {
                        throw new IllegalArgumentException(ERROR_HOST_PORT_INVALID_MSG);
                    }
                    serverToAdd.add(hostPortOption);
                    return pos + 2;
                } else {
                    throw new IllegalArgumentException(ERROR_HOST_PORT_ARG_MSG);
                }
            case CHECK_TASKS_SHORT_FLAG:
                if (pos + 1 < args.length) {
                    final String siloFile = args[pos + 1];
                    siloFilePathForCheck = siloFile;
                    return pos + 2;
                } else {
                    throw new IllegalArgumentException(ERROR_SILO_FILE_ARG_MSG);
                }
            case EXECUTE_SHORT_FLAG:
                if (pos + 1 < args.length) {
                    final String siloFile = args[pos + 1];
                    siloFilePathForExec = siloFile;
                    return pos + 2;
                } else {
                    throw new IllegalArgumentException(ERROR_SILO_FILE_ARG_MSG);
                }
            default:
                /**
                 * On *nix systems it is common to chain short flags without options together.
                 * Therefore, we parse every single character in the entire argument string.
                 */
                for (int i = 1; i < args[pos].length(); i++) {
                    arg = Character.toLowerCase(args[pos].charAt(i));
                    switch (arg) {
                        case HELP_SHORT_FLAG:
                            isHelpToPrint = true;
                            break;
                        case VERSION_SHORT_FLAG:
                            isVersionToPrint = true;
                            break;
                        case LIST_SERVER_SHORT_FLAG:
                            isServerListToPrint = true;
                            break;
                        default:
                            throw new IllegalArgumentException(ERROR_UNKONWN_ARG_MSG + " '" + arg + "'.");
                    }
                }
                return pos + 1;
        }
    }

    /**
     * Checks if the host and port string has a valid form like <code>[host]:[port]</code>. Some
     * examples of valid strings:<code>
     * 172.0.0.1:50052
     * localhost:8080
     * 172.0.0.1:5003
     * [2001:db8::8a2e:370:7334:1234]:8080
     * </code>
     *
     * @param hostPort The string containing the host address and the port in one string
     * @return true if valid, otherwise false.
     */
    static protected boolean isHostAndPortStrValid(final String hostPort) {
        return hostPort.matches("\\S+\\:\\d{4,5}");
    }
}
