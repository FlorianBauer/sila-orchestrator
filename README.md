# sila-orchestrator

A SiLA 2 client for coordinating various SiLA 2 services to build one continuous workflow.


### Build the Project

To build this project, a moderately current JDK and Maven installation is required.
Before building the actual project, the parent SiLA 2 library has to be build to solve all 
dependencies.

```bash
cd sila-orchestrator/extern/java_sila
mvn clean install -DskipTests
```

After that, the service can be built as usual. The resulting `*.jar`-file is located in the 
`target`-directory as `sila-orchestrator-exec.jar`.


### Usage

Run the SiLA 2 service: `java -jar sila-orchestrator-exec.jar`.
List the available network interfaces: `java -jar sila-orchestrator-exec.jar -l yes`
Enable network discovery (e.g. on localhost): `java -jar sila-orchestrator-exec.jar -n lo`

A SiLA 2 conform browser to inspect the available service(s) can be found here:
https://gitlab.com/SiLA2/sila_base/-/wikis/SiLA-Browser-Quickstart#run-the-sila-2-browser
