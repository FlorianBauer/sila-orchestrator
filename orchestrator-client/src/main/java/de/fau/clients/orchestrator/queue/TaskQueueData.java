package de.fau.clients.orchestrator.queue;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fau.clients.orchestrator.tasks.CommandTask;
import de.fau.clients.orchestrator.tasks.CommandTaskModel;
import de.fau.clients.orchestrator.tasks.DelayTask;
import de.fau.clients.orchestrator.tasks.DelayTaskModel;
import de.fau.clients.orchestrator.tasks.LocalExecTask;
import de.fau.clients.orchestrator.tasks.LocalExecTaskModel;
import de.fau.clients.orchestrator.tasks.TaskEntry;
import de.fau.clients.orchestrator.tasks.TaskModel;
import de.fau.clients.orchestrator.utils.VersionNumber;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import sila_java.library.manager.models.Server;

/**
 * Class responsible for importing and exporting the task-queue from/to a JSON-file.
 *
 * @see TaskEntry
 */
@Slf4j
@JsonPropertyOrder({"siloFileVersion", "tasks"})
public class TaskQueueData {

    /**
     * Save-file version identifier to allow managing compatibility with potential older or future
     * releases.
     */
    public static final VersionNumber SILO_FILE_VERSION = new VersionNumber(1, 0, 1);
    private static final ObjectMapper mapper = new ObjectMapper();
    private VersionNumber loadedFile = null;
    private ArrayList<TaskEntry> tasks = null;

    /**
     * Creates a <code>TaskQueueData</code> object and populates it with the task-model data for
     * serialization in JSON.
     *
     * @param queue The task queue to extract the data from.
     * @return A populated <code>TaskQueueData</code> object for JSON serialization.
     */
    public static TaskQueueData createFromTaskQueue(final TaskQueueTable queue) {
        final int rows = queue.getRowCount();
        final TaskQueueData data = new TaskQueueData();
        data.tasks = new ArrayList<>(rows);
        for (int i = 0; i < rows; i++) {
            data.tasks.add(new TaskEntry(queue.getTaskIdFromRow(i),
                    queue.getTaskFromRow(i).getCurrentTaskModel(),
                    queue.getTaskPolicyFromRow(i)));
        }
        return data;
    }

    /**
     * Writes the given queue data into the given file.
     *
     * @param outFile The file to write out.
     * @param queueData The queue data to write.
     * @throws IOException
     */
    public static void writeToFile(final Path outFile, final TaskQueueData queueData)
            throws IOException {
        final BufferedWriter bw = Files.newBufferedWriter(outFile);
        mapper.writerWithDefaultPrettyPrinter().writeValue(bw, queueData);
    }

    /**
     * Loads the given *.silo file and returns its content as <code>TaskQueueData</code> object for
     * further processing.
     *
     * @param siloFile The path to the *.silo file.
     * @param outMsg The output for user messages (e.g. the appropriate error message on failure).
     * @return The initialized <code>TaskQueueData</code> object containing the data from the file
     * or <code>null</code> on error.
     */
    public static TaskQueueData createFromFile(final String siloFile, StringBuilder outMsg) {
        Path filePath = Paths.get(siloFile);
        if (Files.notExists(filePath)) {
            outMsg.append("Could not find file \"").append(filePath).append("\".");
            return null;
        }

        log.info("Opend file: " + filePath);
        final String loadedVersionStr;
        try {
            loadedVersionStr = mapper.readTree(Files.newInputStream(filePath))
                    .get("siloFileVersion")
                    .asText();
        } catch (IOException ex) {
            outMsg.append(ex.getMessage());
            return null;
        } catch (Exception ex) {
            outMsg.append("Could not query file version number: ").append(ex.getMessage());
            return null;
        }

        boolean isMinorHigher = false;
        final VersionNumber loadedFile = VersionNumber.parseVersionString(loadedVersionStr);
        log.info("Silo-file version: " + loadedFile.toString());
        if (loadedFile.getMajorNumber() > SILO_FILE_VERSION.getMajorNumber()) {
            final String errMsg = "The opened file with its format version "
                    + loadedFile.toString() + " is not compatible with this Sowftware."
                    + "\nOnly file formats up to version " + SILO_FILE_VERSION.toString()
                    + " are supported!";
            outMsg.append(errMsg);
            return null;
        } else if (loadedFile.getMinorNumber() > SILO_FILE_VERSION.getMinorNumber()) {
            // minor number is higher, import may fail
            isMinorHigher = true;
        }

        final TaskQueueData tqd;
        try {
            tqd = mapper.readValue(Files.newInputStream(filePath), TaskQueueData.class);
        } catch (IOException ex) {
            if (isMinorHigher) {
                final String errMsg = "The opened file with its format version "
                        + loadedFile.toString() + " is not compatible with this Sowftware."
                        + "\nOnly file formats up to version " + SILO_FILE_VERSION.toString()
                        + " are supported!";
                outMsg.append(errMsg);
            } else {
                outMsg.append(ex.getMessage());
            }
            return null;
        }
        return tqd;
    }

    /**
     * Imports the data (tasks) hold by this instance into the given task queue.
     *
     * @param queue The task queue to import the data.
     * @param serverMap A current map with available server.
     */
    public void importToTaskQueue(final TaskQueueTable queue, final Map<UUID, Server> serverMap) {
        for (final TaskEntry entry : this.tasks) {
            final TaskModel taskModel = entry.getTaskModel();
            if (taskModel instanceof CommandTaskModel) {
                final CommandTaskModel ctm = (CommandTaskModel) taskModel;
                ctm.importFromIdentifiers(serverMap);
                queue.addCommandTaskWithId(entry.taskId, new CommandTask(ctm), entry.taskPolicy);
            } else if (taskModel instanceof DelayTaskModel) {
                final DelayTaskModel dtm = (DelayTaskModel) taskModel;
                queue.addTaskWithId(entry.taskId, new DelayTask(dtm), entry.taskPolicy);
            } else if (taskModel instanceof LocalExecTaskModel) {
                final LocalExecTaskModel letm = (LocalExecTaskModel) taskModel;
                queue.addTaskWithId(entry.taskId, new LocalExecTask(letm), entry.taskPolicy);
            } else {
                log.warn("Unknow TaskModel instance found. Task import omitted.");
            }
        }
        queue.showColumn(TaskQueueTable.COLUMN_SERVER_UUID_IDX);
    }

    public ArrayList<TaskEntry> getTasks() {
        return tasks;
    }

    public void setTasks(ArrayList<TaskEntry> tasks) {
        this.tasks = tasks;
    }

    @JsonGetter("siloFileVersion")
    public String getSupportedSiloFileVersion() {
        return SILO_FILE_VERSION.toString();
    }

    @JsonSetter("siloFileVersion")
    public void setSiloFileVersion(final String version) {
        loadedFile = VersionNumber.parseVersionString(version);
    }

    @JsonIgnore
    public VersionNumber getLoadedSiloFileVersion() {
        return loadedFile;
    }
}
