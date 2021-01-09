# User Guide

This guide provides a short descriptions of the available functionalities of the sila-orchestrator and how to use them. Instructions on how to build and run the software can be found in the [README](../README.md).


## Function Overview

* Discover SiLA servers with a network scan.
* Function for manually adding SiLA servers.
* Save and load task-queues with all their parameters to/from a file.
* Vilidators for constrained types (validate on `[Tap]`)
	- Ranges (numbers, date, time, timestamp, string lengths)
	- Regex match expressions
	- List ranges
	- Sets
	- XML
	
* Export function to save the current task queue data to a *.csv sheet.
* Enabled editing for constrained SiLA Properties to validate after pressing `[Tap]` (It's not a bug, it's a feature!).
* Support for binary types.
* Support for Images (jpeg, png, gif).
* Built-in function for time delays.
* Built-in function to locally execute programs and scripts on the client host.
* Show Results in Raw- or Node-View.
* Headless mode for CLI.
* Platform independent. Runs on Windows, Linux and any other OS with a JavaSE/JDK 11 installation.


## Basic Functions

Click the *Scan* <img src="pictures/network-scan.svg" width="24" height="24"> button or press `[Alt]+[C]` to search in the local network for discoverable SiLA servers.

If a server can not be discovered (e.g. does not support the discover-function or is hidden in the network on purpose), a manual connection can be established by clicking the *Add* <img src="pictures/server-add.svg" width="24" height="24"> button or `[Alt]+[A]` to open the configuration window.

To save the current task queue to a *.silo-file, click <img src="pictures/document-save.svg" width="24" height="24"> or use the shortcut `[Ctrl]+[S]`.

To open a existing *.silo-file, click <img src="pictures/document-open.svg" width="24" height="24"> or press `[Ctrl]+[O]`.

To add a delay-task to the end of the queue, click *Add Delay* <img src="pictures/delay-add.svg" width="24" height="24"> or drag the icon from the toolbar directly into the desired position in the task queue.
When a delay-task was added to the queue and gets executed, the set timer gets triggered, and the execution of the next element in the queue gets delayed until the timer runs out.

To add a local execution tasks, use the *Add Exec* <img src="pictures/exec-add.svg" width="24" height="24"> button to insert, or drag the icon from the toolbar into the task queue.
If the inserted task in the queue gets then selected, a command and its arguments, as well as its expected return value, can be set.
The return value of a successful run depends on the corresponding command, but is usually 0.
Also, the concrete syntax of the command depends on the operating system of the client.

**Some examples on Linux:**  
Run a bash-command:
`/bin/bash -c "sleep 5"`, expected return value `0`.  
Run a bash-command inside a terminal* (depending on the Linux distro, the terminal application can also be `mate-terminal`, `xfce4-terminal`, `xterm`, etc.):
``gnome-terminal -- bash -c "echo This bash-command runs on `hostname`.; sleep 5"``, expected return value `255`.

**Some examples on Windows:**  
Open the calculator:
`calc`, expected return value `0`  
Execute a command in the Windows PowerShell:
`powershell "sleep 5"`, expected return value `0`.

*Clear Queue* <img src="pictures/queue-clear.svg" width="24" height="24"> purges all entries form the current queue.

The current data in the queue can be exported any time to a *.csv-file with *Export Queue* <img src="pictures/queue-export.svg" width="24" height="24">. The *.csv-file can then be imported and used with a common spreadsheet program (e.g. LibreOffice Calc or Microsoft Excel). Note that the exported data is only meant to be used for archiving or analysis purposes. A import back to the sila-orchestrator is therefore not possible.


## Server and Feature Entries

After successfully establishing a connection to at least one SiLA complaint server, the left panel shows various, hierarchically grouped  entries. By hovering over one of these entries, the corresponding SiLA description will appear.

![server tree](pictures/server-tree.png)


 Symbol                                                           | Description 
------------------------------------------------------------------|-------------
<img src="pictures/server-online.svg" width="24" height="24">     | A server node which is available and online.
<img src="pictures/server-offline.svg" width="24" height="24">    | A server node which has become offline.
<img src="pictures/sila-feature.svg" width="24" height="24">      | A regular SiLA Feature.
<img src="pictures/sila-feature-core.svg" width="24" height="24"> | A SiLA Core Feature.
<img src="pictures/property.svg" width="24" height="24">          | A Feature property.
<img src="pictures/command.svg" width="24" height="24">           | A Feature command.


## Task Queue

The task queue is one of the main components of the sila-orchestrator. All the SiLA commands and tasks within the queue can be chained together and executed one after another. This enables the User to build complex, automated workflows, run them, store them and re-run them over again without repeating all the configuration steps.

Now, before the queue can be executed, it must be filled with tasks first. To do this, select a command and click the <img src="pictures/entry-add.svg" width="24" height="24"> button to append it to the end of the queue, or drag <img src="pictures/command.svg" width="24" height="24"> directly into the desired location within the queue.

The order of a task can be changed by moving its position with the <img src="pictures/move-up.svg" width="24" height="24"> and <img src="pictures/move-down.svg" width="24" height="24"> buttons.

To remove a task, use the <img src="pictures/task-remove.svg" width="24" height="24"> button.

Finally, to run the entire task queue, click the  <img src="pictures/queue-exec-start.svg" width="24" height="24"> button.

To abort the current run, click <img src="pictures/queue-exec-stop.svg" width="24" height="24">.


### Queue Columns

The task queue itself consists of the following columns:

* ID
* Connection
* Task
* Server UUID
* Policy
* State
* Start Time
* End Time
* Duration
* Result

Not all columns are on display by default but can selectively be shown or hidden by clicking the `...`-button at the upper-right corner or by right-clicking in the column header of the table.

The *ID* field holds a unique number to identify the task. The number can be edited by double-clicking into the cell.

The *Connection* column shows with an online <img src="pictures/task-online.svg" width="24" height="24">, or an offline <img src="pictures/task-offline.svg" width="24" height="24"> symbol if the task is ready for execution. A neutral <img src="pictures/task-neutral.svg" width="24" height="24"> symbol (e.g. on delay-tasks) indicates that no network connection is necessary.

*Task* holds the command identifier.

The *Server UUID* column holds the Universal Unique IDs of the server instances for each task. The instance of each task can be re-assigned by choosing a entry from the drop-down menu in the cell. This is especially useful when a *.silo-file from an older session is loaded but the UUID of the original server changed in the meantime. Therefore the UUID can be changed to the new instance to let the tasks become online and ready for execution once again.

The *Policy* column holds the entries for the error handling of each task. The contents can either be `HALT_AFTER_ERROR` or `PROCEED_AFTER_ERROR`. On `HALT_AFTER_ERROR`, the entire queue execution is stopped after the affected task finished with an error. If the value in the cell is set to `PROCEED_AFTER_ERROR`, a queue run is continued even if execution of the task was not successful.

The *State* column signals the current state of each task.
- <img src="pictures/state-neutral.svg" width="24" height="24"> Neutral: The task is or was not executed (yet).
- <img src="pictures/state-running.svg" width="24" height="24"> Running: The task is currently executed.
- <img src="pictures/state-finished-success.svg" width="24" height="24"> Finished with success: The task was run successfully.
- <img src="pictures/state-finished-error.svg" width="24" height="24"> Finished with error: The task failed or could not be completed.

The columns *Start Time* as well as *End Time* contain timestamp entries of the beginning and end of the task execution.

*Duration* shows the relative time a task took to finish.

The *Result* cells hold the outcome of each task execution. This can be an empty result `-`, an error message, or a SiLA element nested in the cell indicated by `[...]`. Note: Empty braces `{}` are also considered valid result values.

All columns in the table can be rearranged by dragging the corresponding column header into the desired position.


## Context Sensitive Views

According to the type of the current selected item, the panel in the bottom shows one of the following views.


### Server Detail View

The details of a server can be viewed when the corresponding <img src="pictures/server-online.svg" width="24" height="24"> entry gets selected.

The server view shows various info and offers a built-in function for renaming the server:  
![server details](pictures/server-details.png)


### Feature View

* Shows the information given in the Feature Definition in a clear, human-readable form.
* Shows `Fully Qualified Identifier` for every SiLA element.

![feature view](pictures/feature-view.png)


### Property View

* Selecting a Property node shows the current SiLA Property values.
* Requesting a value update can be done by clicking <img src="pictures/refresh.svg" width="24" height="24"> or by deselecting and selecting the node again.
* Not modifiable SiLA properties with a constraint get validated. The value can be edited to check against the validator by pressing `[Tab]`, but does not affect the actual value on the server.

Example:  
A not so SiLA complaint Command Parameter Identifier (just a example, no intention to blame the vendor of this server):  
![validation wrong](pictures/command-parameter-id-wrong.png)

The, according to the standard, correct Command Parameter Identifier the validator would accept:  
![validation correct](pictures/command-parameter-id-correct.png)


### Command View

To show and edit the parameter of a SiLA command, the command must be added to the task queue first. After that, the command can be selected in the task queue and the parameters can be set accordingly.

To execute a single command, without invoking any other entries in the task queue, click the <img src="pictures/execute.svg" width="24" height="24"> button.

Depending on the defined types of the various parameters, a SiLA constraint can limit available input options.

Examples of numeric constraints:  
![numeric constraints](pictures/numeric-constraints.png)

Examples of date and time constraints:  
![date time constraints](pictures/date-time-constraints.png)

Examples of string constraints:  
![string constraints](pictures/string-constraints.png)
