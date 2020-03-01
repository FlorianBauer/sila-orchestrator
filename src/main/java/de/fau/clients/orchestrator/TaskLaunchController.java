package de.fau.clients.orchestrator;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.util.ArrayList;
import sila2.de.fau.utilities.tasklaunchcontroller.v1.TaskLaunchControllerGrpc;
import sila2.de.fau.utilities.tasklaunchcontroller.v1.TaskLaunchControllerOuterClass;
import sila_java.library.core.sila.types.SiLAErrors;

@Slf4j
class TaskLaunchController extends TaskLaunchControllerGrpc.TaskLaunchControllerImplBase {

    @Override
    public void startTask(
            TaskLaunchControllerOuterClass.StartTask_Parameters req,
            StreamObserver<TaskLaunchControllerOuterClass.StartTask_Responses> responseObserver
    ) {
        if (!req.hasExecutable()) {
            responseObserver.onError(SiLAErrors.generateValidationError(
                    "Executable",
                    "Executable parameter was not set. Specify a executable with at least one "
                    + "character."
            ));
            return;
        }

        // Custom ValidationError example
        String command = req.getExecutable().getValue();
        ArrayList<String> commandAndArgs = new ArrayList<>(req.getArgumentListCount() + 1);
        commandAndArgs.add(0, command);
        for (int i = 1; i < req.getArgumentListCount();) {
            String str = req.getArgumentList(i).getValue();
            if (str.isEmpty()) {
                continue;
            }
            log.info("arg" + i + ": '" + str + "'");
            commandAndArgs.add(i, str);
            i++;
        }

        ProcessBuilder pb = new ProcessBuilder(commandAndArgs);
        try {
            Process proc = pb.start();
            int exitValue = proc.waitFor();
            log.info("exec return: " + String.valueOf(exitValue));
        } catch (IOException | InterruptedException ex) {
            log.error("Execption: ", ex);
        }
        responseObserver.onCompleted();
    }
}
