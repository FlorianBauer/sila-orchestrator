package de.fau.clients.orchestrator.cli;

import com.google.common.net.HostAndPort;
import static de.fau.clients.orchestrator.OrchestratorGui.COPYRIGHT_NOTICE;
import de.fau.clients.orchestrator.ctx.ConnectionManager;
import de.fau.clients.orchestrator.queue.TaskQueueData;
import de.fau.clients.orchestrator.queue.TaskQueueTable;
import de.fau.clients.orchestrator.tasks.ConnectionStatus;
import de.fau.clients.orchestrator.tasks.ExecPolicy;
import de.fau.clients.orchestrator.tasks.QueueTask;
import de.fau.clients.orchestrator.tasks.TaskState;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import lombok.NonNull;
import sila_java.library.manager.ServerAdditionException;
import sila_java.library.manager.models.Server;

/**
 * Class for handling the Command-Line Interface (CLI) controls. This class is responsible for
 * processing the parsed command-line arguments and invoking the associate actions.
 *
 * @see CommandlineArguments
 */
public final class CommandlineControls {

    private final Properties gitProps;
    private final ConnectionManager conManager;

    public CommandlineControls(final Properties gitProps, final ConnectionManager conManager) {
        this.gitProps = gitProps;
        this.conManager = conManager;
    }

    /**
     * Processes the parsed arguments stored in the given CommandlineArgument-object. The order of
     * operation is fixed and does not depend on the arrangement of the of the initial arguments.
     *
     * @param args The CommandlineArguments-object containing the CLI arguments.
     * @return 0 on success, anything else on error.
     *
     * @see CommandlineArguments
     */
    public int processArgs(@NonNull final CommandlineArguments args) {

        if (args.isHelpToPrint) {
            System.out.println(CommandlineArguments.USAGE_INFO_MSG);
        }

        if (args.isVersionToPrint) {
            System.out.println(gitProps.getProperty("git.build.version")
                    + "-" + gitProps.getProperty("git.commit.id.abbrev"));
        }

        if (args.isAboutInfoToPrint) {
            System.out.println("sila-orchestrator"
                    + "\n " + COPYRIGHT_NOTICE
                    + "\n Version: " + gitProps.getProperty("git.build.version")
                    + "-" + gitProps.getProperty("git.commit.id.abbrev")
                    + "\n Git Commit: " + gitProps.getProperty("git.commit.id")
                    + "\n Timestamp: " + gitProps.getProperty("git.commit.time")
                    + "\n Git Repository: " + gitProps.getProperty("git.remote.origin.url")
                    + "\n E-Mail: florian.bauer.dev@gmail.com"
                    + "\n License: Apache-2.0");
        }

        if (!args.serverToAdd.isEmpty()) {
            connectToServerList(args.serverToAdd);
        }

        if (args.isServerListToPrint) {
            scanNetworkAndListAvailableServer();
        }

        if (args.isSiloFileToCheck()) {
            int retVal = checkSiloFile(args.siloFilePathForCheck);
            if (retVal == 0) {
                System.out.println("Check passed. All tasks are ready for execution.");
            } else {
                System.err.println("Check failed! One or more tasks are currently not ready for exectuion.");
                return retVal;
            }
        }

        if (args.isSiloFileToExecute()) {
            int retVal = executeSiloFile(args.siloFilePathForExec);
            return retVal;
        }
        return 0;
    }

    private void connectToServerList(List<String> hostPortStrList) {
        for (final String hostPortStr : hostPortStrList) {
            final HostAndPort hp;
            try {
                hp = HostAndPort.fromString(hostPortStr);
                conManager.addServer(hp.getHost(), hp.getPort());
            } catch (ServerAdditionException ex) {
                System.err.println("Could not connect to '" + hostPortStr + "': " + ex.getMessage());
            } catch (IllegalArgumentException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }

    /**
     * Does a network scan and lists all currently available server.
     */
    private void scanNetworkAndListAvailableServer() {
        conManager.scanNetwork();
        final Map<UUID, Server> serverMap = conManager.getServerManager().getServers();
        if (serverMap.isEmpty()) {
            System.out.println("\nNo server available.");
            return;
        }
        serverMap.forEach((uuid, server) -> {
            System.out.println("\n" + server.getConfiguration().getName()
                    + "\n" + uuid
                    + "\n" + server.getHostAndPort().toString()
                    + "\nJoined: " + server.getJoined().toInstant().toString());
        });
    }

    /**
     * Checks if all tasks within an *.silo-file are online and ready for execution.
     *
     * @param siloFilePath The path to the *.silo-file to check.
     * @return 0 on success, -1 on error or the number of the offline task.
     */
    private int checkSiloFile(final String siloFilePath) {
        final TaskQueueData tcd;
        try {
            tcd = TaskQueueData.createFromFile(siloFilePath);
        } catch (final IOException | IllegalArgumentException ex) {
            System.err.println(ex.getMessage());
            return -1;
        }

        conManager.scanNetwork();
        TaskQueueTable tqt = new TaskQueueTable();
        tcd.importToTaskQueue(tqt);

        for (int i = 0; i < tqt.getRowCount(); i++) {
            final QueueTask task = tqt.getTaskFromRow(i);
            if (task.getConnectionStatus() == ConnectionStatus.OFFLINE) {
                System.out.println("Task #" + (i + 1) + " '" + task.toString()
                        + "' is offline or not ready.");
                return i + 1;
            }
        }
        return 0;
    }

    /**
     * Executes all queue entries inside the given *.silo-file.
     *
     * @param siloFilePath The path to the *.silo-file.
     * @return 0 on success, -1 on error or the number of the failed task.
     */
    private int executeSiloFile(final String siloFilePath) {
        final TaskQueueData tcd;
        try {
            tcd = TaskQueueData.createFromFile(siloFilePath);
        } catch (final IOException | IllegalArgumentException ex) {
            System.err.println(ex.getMessage());
            return -1;
        }

        conManager.scanNetwork();
        TaskQueueTable tqt = new TaskQueueTable();
        tcd.importToTaskQueue(tqt);

        for (int i = 0; i < tqt.getRowCount(); i++) {
            final QueueTask task = tqt.getTaskFromRow(i);
            task.run();
            if (task.getState() != TaskState.FINISHED_SUCCESS) {
                // apply execution policy
                if (tqt.getTaskPolicyFromRow(i) == ExecPolicy.HALT_AFTER_ERROR) {
                    System.out.println("Halted after task #" + (i + 1) + " '" + task.toString()
                            + "' with state " + task.getState().toString()
                            + " at " + task.getEndTimeStamp() + ".");
                    return i + 1;
                }
            } else {
                System.out.println("Finished task #" + (i + 1) + " '" + task.toString()
                        + "' with state " + task.getState().toString()
                        + " at " + task.getEndTimeStamp() + ".");
            }
        }
        return 0;
    }
}
