# Command-line Interface (CLI) Guide

This guide provides info on how to use the sila-orchestrator within a terminal, console or script. For info on how to use this software with the graphical user interface (GUI), take a look into the [README](../README.md) and the [User Guide](UserGuide.md).

The sila-orchestrator with its CLI-mode enables the usage within scripts and can therefore increases the degree of automation. Thereby a script can introduce flow control by creating a workflow with various tasks stored in different *.silo-files. Also "cron jobs" can be established to automatically start a queue-run at a predefined time.

To invoke the CLI-mode, the executable must be started with one or more arguments, which define the desired actions. The available options can be listed with the option `-h` (e.g. `java -jar sila-orchestrator.jar -h`).


## Usage overview:

```
Usage: java -jar sila-orchestrator.jar [args]
 -h, --help
	 Print this help message.
 -v, --version
	 Print the version number.
 --about, --info
	 Print some general information about this software.
 -a <[host]:[port]>, --add-server <[host]:[port]>
	 Add a SiLA server by connecting via the given address.
	 Examples: 'localhost:8080', '127.0.0.1:50052', '[2001:db8::8a2e:370:7334:1234]:50053'.
 -l, --list-server
	 Lists all available servers which can be discoverd by an network scan.
 -c <silo-file>, --check-tasks <silo-file>
	 Checks all tasks in the given *.silo-file if they are ready for execution.
 -x <silo-file>, --execute <silo-file>
	 Loads and executes the provided *.silo-file.
```

On `--check-tasks <silo-file>` and `--execute <silo-file>`, an automatic network scan is done beforehand. Therefore, a manual connection via `--add-server <[host]:[port]>` can be omitted when all tasks in the given *.silo-file rely on discoverable servers.

The provided arguments can be given in any arbitrary order, since the processing order of operation is predefined. E.g. a server-add `-a` which establishes a connection, is always handled before an check `-c` operation, which is also always processed before an *.silo-file execution `-x`. Therefore, if the `--check-tasks <silo-file>` is used in combination with the `--execute <silo-file>` option, any failed check will immediately exit the program without executing any tasks at all.


## Example Scripts

Bash:
```bash
#!/bin/bash

# The path to the sila-orchestrator executable.
SILA_ORCHESTRATOR=/path/to/sila-orchestrator.jar
# The path to the *.silo-file we want to execute.
SILO_FILE=/path/to/myQueueFile.silo

# Run the sila-orchestrator in CLI mode.
# Prints the help text, connects to a server, checks a *.silo-file, executes a *.silo-file.
java -jar $SILA_ORCHESTRATOR -h -a 127.0.0.1:50052 -c $SILO_FILE -x $SILO_FILE

# Retrieve the exit value.
RET_VAL=${?}

# Evaluate the exit value.
if [ $RET_VAL -eq 0 ]; then
  echo "Everything went fine. sila-orchestrator returned with 0."
elif [ $RET_VAL -lt 0 ]; then
  echo "Error. sila-orchestrator returned with the negativ value $RET_VAL." 
elif [ $RET_VAL -gt 0 ]; then
  echo "The check or execution of the *.silo-file failed because of the task entry nr. $RET_VAL."
fi
```

Python:
```python
#!/usr/bin/env python3

import subprocess

# The path to the sila-orchestrator executable.
SILA_ORCHESTRATOR: str = "/path/to/sila-orchestrator.jar"
# The path to the *.silo-file we want to execute.
SILO_FILE: str = "/path/to/myQueueFile.silo"

ret_val = subprocess.call(["java", "-jar", SILA_ORCHESTRATOR, "-h", "-c", SILO_FILE, "-x", SILO_FILE])

if ret_val == 0:
    print("Everything went fine. sila-orchestrator returned with 0.")
elif ret_val < 0:
    print(f"Error. sila-orchestrator returned with the negativ value {ret_val}.")
elif ret_val > 0:
    print(f"The check or execution of the *.silo-file failed because of the task entry nr. {ret_val}.")
```

PowerShell:
```powershell
# The path to the sila-orchestrator executable.
﻿$sila_orchestrator = "C:\path\to\sila-orchestrator.jar"
# The path to the *.silo-file we want to execute.
$silo_file = "C:\path\to\myQueueFile.silo"

# Runs the program in the same process as the console.
# Print help text, checks the entries in the *.silo-file, exectues the *.silo-file.
java -jar $sila_orchestrator -h -c $silo_file -x $silo_file

if ($LASTEXITCODE -eq 0) {
    echo "Everything went fine."
} elseif ($LASTEXITCODE -lt 0) {
    echo "Error." 
} elseif  ($LASTEXITCODE -gt 0) {
    echo "Error. Task nr. $LASTEXITCODE failed."
}

# Runs the program in an new, dedicated process
#Start-Process java -ArgumentList "-jar", "$sila_orchestrator", "-h", "-c $silo_file", "-x $silo_file"
```
