# sila-orchestrator

A SiLA 2 client for coordinating various SiLA 2 services to build one continuous workflow.

### Build the Project

Enter the project directory and use the following command to build the project:

```bash
mvn clean install -DskipTests
```

After that, the resulting `*.jar`-file is located in the `orchestrator-client/target`-directory as 
`orchestrator-client-exec.jar`.

### Usage

Run the SiLA 2 service: `java -jar orchestrator-client-exec.jar`.
```
Usage: java -jar orchestrator-client-exec.jar [args]
-h, --help
    Print this help message.
-v, --version
    Print the version number.
--about, --info
    Print some general information about this software.
```

A SiLA 2 conform browser to inspect the available service(s) can be found here:
https://gitlab.com/SiLA2/sila_base/-/wikis/SiLA-Browser-Quickstart#run-the-sila-2-browser
